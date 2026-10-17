package thaumcraft.util;

import com.mojang.logging.LogUtils;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import thaumcraft.client.ClientRegistryAccess;

/**
 * The 1.7.10 NBT interface on top of the {@code CUSTOM_DATA} component.
 *
 * <p>Components are immutable and shared between copies: {@code ItemStack.copy()} keeps the same
 * {@link CustomData} instance, and so does the slot snapshot the server compares against to decide
 * whether to sync a slot. A tag is therefore always edited as a copy and stored back as a new
 * instance; editing in place leaks into other copies and into the snapshot, and the client never
 * receives the change.
 */
public final class ItemStackData {
   private static final Logger LOGGER = LogUtils.getLogger();

   private ItemStackData() {
   }

   public static boolean hasTag(ItemStack stack) {
      return stack.has(DataComponents.CUSTOM_DATA);
   }

   /** A copy of the tag; editing it does not touch the stack. */
   public static CompoundTag getTag(ItemStack stack) {
      CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
      return customData == null ? null : customData.copyTag();
   }

   /**
    * A mutable tag owned by this stack only: the component is replaced with a fresh copy and its
    * backing tag is returned. Edits show up on this stack at once but never on its earlier copies.
    */
   public static CompoundTag getOrCreateTag(ItemStack stack) {
      CustomData data = stack.get(DataComponents.CUSTOM_DATA);
      stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data == null ? new CompoundTag() : data.getUnsafe()));
      return stack.get(DataComponents.CUSTOM_DATA).getUnsafe();
   }

   public static void setTag(ItemStack stack, CompoundTag tag) {
      if (tag != null && !tag.isEmpty()) {
         stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
      } else {
         stack.remove(DataComponents.CUSTOM_DATA);
      }
   }

   public static void removeTagKey(ItemStack stack, String key) {
      CompoundTag tag = getTag(stack);
      if (tag != null) {
         tag.remove(key);
         setTag(stack, tag);
      }
   }

   public static void addTagElement(ItemStack stack, String key, Tag value) {
      updateTag(stack, tag -> tag.put(key, value));
   }

   public static void updateTag(ItemStack stack, Consumer<CompoundTag> updater) {
      CustomData.update(DataComponents.CUSTOM_DATA, stack, updater);
   }

   public static boolean getBoolean(ItemStack stack, String key) {
      CompoundTag tag = getTag(stack);
      return tag != null && tag.getBoolean(key);
   }

   public static byte getByte(ItemStack stack, String key) {
      CompoundTag tag = getTag(stack);
      return tag == null ? 0 : tag.getByte(key);
   }

   public static int getInt(ItemStack stack, String key) {
      CompoundTag tag = getTag(stack);
      return tag == null ? 0 : tag.getInt(key);
   }

   public static String getString(ItemStack stack, String key) {
      CompoundTag tag = getTag(stack);
      return tag == null ? "" : tag.getString(key);
   }

   public static void putBoolean(ItemStack stack, String key, boolean value) {
      updateTag(stack, tag -> tag.putBoolean(key, value));
   }

   public static void putByte(ItemStack stack, String key, byte value) {
      updateTag(stack, tag -> tag.putByte(key, value));
   }

   public static void putInt(ItemStack stack, String key, int value) {
      updateTag(stack, tag -> tag.putInt(key, value));
   }

   /** The whole stack with all components (vanilla {@code ItemStack.save} format). */
   public static CompoundTag saveStack(ItemStack stack) {
      return saveStack(stack, registries());
   }

   public static CompoundTag saveStack(ItemStack stack, HolderLookup.Provider registries) {
      return stack.isEmpty() ? new CompoundTag() : (CompoundTag)stack.save(registries);
   }

   /** Reads both the vanilla format and the legacy {@code id + Count + tag} one (worlds before 1.0.7). */
   public static ItemStack loadStack(CompoundTag tag) {
      return loadStack(tag, registries());
   }

   public static ItemStack loadStack(CompoundTag tag, HolderLookup.Provider registries) {
      if (tag == null || !tag.contains("id", Tag.TAG_STRING)) {
         return ItemStack.EMPTY;
      }

      if (tag.contains("Count")) {
         return loadLegacyStack(tag);
      }

      return ItemStack.CODEC
         .parse(registries.createSerializationContext(NbtOps.INSTANCE), tag)
         .resultOrPartial(error -> LOGGER.warn("Failed to read stored item {}: {}", tag.getString("id"), error))
         .orElseGet(() -> loadBareStack(tag));
   }

   /** The item of a stored stack without decoding its components, for frequent type checks. */
   @Nullable
   public static Item peekItem(CompoundTag tag) {
      if (tag == null || !tag.contains("id", Tag.TAG_STRING)) {
         return null;
      }

      ResourceLocation id = ResourceLocation.tryParse(tag.getString("id"));
      Item item = id == null ? Items.AIR : BuiltInRegistries.ITEM.get(id);
      return item == Items.AIR ? null : item;
   }

   private static ItemStack loadLegacyStack(CompoundTag tag) {
      Item item = peekItem(tag);
      if (item == null) {
         return ItemStack.EMPTY;
      }

      ItemStack stack = new ItemStack(item, Math.max(1, tag.getByte("Count")));
      if (tag.contains("tag", Tag.TAG_COMPOUND)) {
         setTag(stack, tag.getCompound("tag"));
      }

      return stack;
   }

   /** Fallback when components fail to decode (no registries): item, count and CUSTOM_DATA only. */
   private static ItemStack loadBareStack(CompoundTag tag) {
      Item item = peekItem(tag);
      if (item == null) {
         return ItemStack.EMPTY;
      }

      ItemStack stack = new ItemStack(item, Math.max(1, tag.getInt("count")));
      CompoundTag components = tag.getCompound("components");
      if (components.contains("minecraft:custom_data", Tag.TAG_COMPOUND)) {
         setTag(stack, components.getCompound("minecraft:custom_data"));
      }

      return stack;
   }

   /** Registries for the stack codec: the server, else the client world, else static registries only. */
   private static HolderLookup.Provider registries() {
      MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
      if (server != null) {
         return server.registryAccess();
      }

      if (FMLEnvironment.dist.isClient()) {
         HolderLookup.Provider client = ClientRegistryAccess.get();
         if (client != null) {
            return client;
         }
      }

      return RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
   }

   public static boolean sameItemSameTags(ItemStack left, ItemStack right) {
      return ItemStack.isSameItem(left, right) && Objects.equals(left.get(DataComponents.CUSTOM_DATA), right.get(DataComponents.CUSTOM_DATA));
   }
}
