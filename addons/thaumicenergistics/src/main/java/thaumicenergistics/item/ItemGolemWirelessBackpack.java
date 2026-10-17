package thaumicenergistics.item;

import appeng.api.features.IGridLinkableHandler;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thaumcraft.util.ItemStackData;

public class ItemGolemWirelessBackpack extends Item {
   private static final Logger LOG = LoggerFactory.getLogger(ItemGolemWirelessBackpack.class);
   private static final String TAG_ACCESS_POINT = "accessPoint";
   public static final IGridLinkableHandler LINKABLE_HANDLER = new IGridLinkableHandler() {
      public boolean canLink(ItemStack stack) {
         return stack.getItem() instanceof ItemGolemWirelessBackpack;
      }

      public void link(ItemStack itemStack, GlobalPos pos) {
         GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, pos).result().ifPresent(tag -> ItemStackData.addTagElement(itemStack, "accessPoint", tag));
      }

      public void unlink(ItemStack itemStack) {
         ItemStackData.removeTagKey(itemStack, "accessPoint");
      }
   };

   public ItemGolemWirelessBackpack() {
      super(new Properties().stacksTo(1));
   }

   @OnlyIn(Dist.CLIENT)
   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
      if (getLinkedPosition(stack) != null) {
         tooltip.add(Component.translatable("gui.ae2.Linked"));
      } else {
         tooltip.add(Component.translatable("gui.ae2.Unlinked"));
      }
   }

   @Nullable
   public static GlobalPos getLinkedPosition(ItemStack stack) {
      CompoundTag tag = ItemStackData.getTag(stack);
      return tag != null && tag.contains("accessPoint", 10)
         ? GlobalPos.CODEC
            .decode(NbtOps.INSTANCE, tag.get("accessPoint"))
            .resultOrPartial(Util.prefix("Linked position", LOG::error))
            .<GlobalPos>map(Pair::getFirst)
            .orElse(null)
         : null;
   }

   public static void setLinkedPosition(ItemStack stack, @Nullable GlobalPos pos) {
      if (pos != null) {
         LINKABLE_HANDLER.link(stack, pos);
      } else {
         LINKABLE_HANDLER.unlink(stack);
      }
   }

   public static boolean isLinked(ItemStack stack) {
      return getLinkedPosition(stack) != null;
   }

   public static enum BackpackSkins {
      Thaumium("Thaum"),
      Stone("Stone"),
      Straw("Straw"),
      GreatWood("Wood"),
      Flesh("Flesh"),
      Clay("Clay"),
      Iron("Iron"),
      Tallow("Tallow"),
      Gold("Gold"),
      Diamond("Diamond");

      public static final ItemGolemWirelessBackpack.BackpackSkins[] VALUES = values();
      private ResourceLocation texture = null;
      private final String texID;

      private BackpackSkins(String ID) {
         this.texID = ID;
      }

      public ResourceLocation getTextureLocation() {
         if (this.texture == null) {
            this.texture = ResourceLocation.fromNamespaceAndPath("thaumicenergistics", "textures/models/golem_backpack/" + this.texID.toLowerCase() + ".png");
         }

         return this.texture;
      }
   }
}
