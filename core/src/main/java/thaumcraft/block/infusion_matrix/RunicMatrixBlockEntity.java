package thaumcraft.block.infusion_matrix;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.api.ITickable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.block.ThaumcraftEntityBlock;
import thaumcraft.api.blockentity.ThaumcraftBlockEntity;
import thaumcraft.api.wands.IWandable;
import thaumcraft.block.arcane_pedestal.ArcanePedestalBlockEntity;
import thaumcraft.block.infusion_matrix.handlers.InfusionInstabilityHandler;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.init.registry.ModItems;
import thaumcraft.init.registry.ModSounds;
import thaumcraft.util.ItemStackData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.event.EventHooks;
import thaumcraft.api.aspects.IAspectSource;
import thaumcraft.api.crafting.IInfusionModifierRecipe;
import thaumcraft.network.PacketHandler;
import thaumcraft.network.fx.PacketFXEssenceSource;
import thaumcraft.network.fx.PacketFXInfusionSource;
import thaumcraft.recipe.ThaumcraftCraftingManager;

public class RunicMatrixBlockEntity extends ThaumcraftBlockEntity implements IWandable, IAspectContainer, ITickable {
   private static final String NBT_CRAFTING = "crafting";
   private static final String NBT_INSTABILITY = "instability";
   private static final String NBT_RECIPE_TYPE = "recipeType";
   private static final String NBT_RECIPE_XP = "recipeXP";
   private static final String NBT_RECIPE_INSTABILITY = "recipeInstability";
   private static final String NBT_RECIPE_PLAYER = "recipePlayer";
   private static final String NBT_RECIPE_INPUT = "recipeInput";
   private static final String NBT_RECIPE_OUTPUT = "recipeOutput";
   private static final String NBT_RECIPE_INGREDIENTS = "recipeIngredients";
   private static final String NBT_RECIPE_ESSENTIA = "recipeEssentia";
   private static final String NBT_IS_INFO = "isInfo";
   private static final String NBT_META_UPDATE = "metaUpdate";
   public static final int UPGRADE_COUNT = 5;
   public static final byte[] MAX_UPGRADES = new byte[]{4, 4, 1, 4, 1};
   public static final int UPGRADE_SPEED = 0;
   public static final int UPGRADE_EFFICIENCY = 1;
   public static final int UPGRADE_CLEANER = 2;
   public static final int UPGRADE_LUCK = 3;
   public static final int UPGRADE_STABILITY = 4;
   /** metaUpdate of the "matrix upgrade" item that unlocks rune slots. */
   public static final int UPGRADE_UNLOCK = 5;
   /** Rune effects as the Thaumonomicon pages describe them. */
   private static final int SPEED_UNITS_PER_LEVEL = 4;
   private static final float EFFICIENCY_CHANCE_PER_LEVEL = 0.05F;
   private static final float LUCK_CHANCE_PER_LEVEL = 0.025F;
   private final InfusionInstabilityHandler instabilityHandler;
   private boolean crafting = false;
   private boolean checkSurroundings = true;
   private int tickCount = 0;
   private int craftTickCount = 0;
   public float startUp;
   private int instability = 0;
   private AspectList recipeEssentia = new AspectList();
   private List<Ingredient> recipeIngredients = new ArrayList<>();
   private ItemStack recipeOutput = ItemStack.EMPTY;
   private ItemStack recipeInput = ItemStack.EMPTY;
   private UUID recipePlayer = null;
   private int recipeType = 0;
   private int recipeXP = 0;
   private int recipeInstability = 0;
   /** Чары для ветки зачарования (тип 1); null для обычных рецептов. */
   @Nullable
   private Holder<Enchantment> recipeEnchantment = null;
   /** Ticks between craft cycles: 10, or 20 while experience is being drained (TC4 countDelay). */
   private int countDelay = 10;
   /** Recipe id of a catalyst-modifying recipe (recipeType 2); its output is recomputed from the catalyst at the end. */
   @Nullable
   private ResourceLocation recipeModifierId = null;
   /** Essentia sources in reach, refreshed together with the stability scan. */
   private List<BlockPos> essentiaSources = List.of();
   private int itemCount = 0;
   private BlockPos itemTargetPedestal = null;
   private int itemTargetIngredientIndex = -1;
   private boolean clientSyncQueued = false;
   private int clientSyncDelayTicks = 0;
   /** Клиентский счётчик вспышки завершения крафта (фиолетовый взрыв + гало). */
   private int finishFlashTicks = 0;
   public boolean isUpgraded = false;
   public byte[] upgrades = new byte[5];
   public HashMap<String, RunicMatrixBlockEntity.SourceFX> sourceFX = new HashMap<>();
   private AABB findArea;

   public RunicMatrixBlockEntity(BlockPos blockPos, BlockState blockState) {
      super((BlockEntityType<?>)ModBlockEntities.RUNIC_MATRIX.get(), blockPos, blockState);
      this.instabilityHandler = new InfusionInstabilityHandler(this);
   }

   private void queueClientSync(int delayTicks) {
      this.setChanged();
      if (this.level instanceof ServerLevel) {
         if (!this.clientSyncQueued) {
            this.clientSyncQueued = true;
            this.clientSyncDelayTicks = Math.max(0, delayTicks);
         } else {
            this.clientSyncDelayTicks = Math.min(this.clientSyncDelayTicks, Math.max(0, delayTicks));
         }
      }
   }

   private void flushClientSync() {
      if (this.level instanceof ServerLevel sl) {
         if (this.clientSyncQueued) {
            if (this.clientSyncDelayTicks > 0) {
               this.clientSyncDelayTicks--;
            } else {
               this.clientSyncQueued = false;
               BlockState state = sl.getBlockState(this.worldPosition);
               sl.sendBlockUpdated(this.worldPosition, state, state, 2);
            }
         }
      }
   }

   private CompoundTag writeEssentiaTag() {
      CompoundTag e = new CompoundTag();
      this.recipeEssentia.save(e);
      return e;
   }

   private void readEssentiaTag(CompoundTag parent) {
      AspectList list = new AspectList();
      if (parent.contains("recipeEssentia", 10)) {
         list.load(parent.getCompound("recipeEssentia"));
      } else {
         list.load(parent);
      }

      this.recipeEssentia = list;
   }

   private void writeClientTag(CompoundTag tag) {
      tag.putBoolean("crafting", this.crafting);
      tag.putInt("instability", this.instability);
      tag.putInt("recipeType", this.recipeType);
      tag.putInt("recipeXP", this.recipeXP);
      tag.putInt("recipeInstability", this.recipeInstability);
      tag.put("recipeEssentia", this.writeEssentiaTag());
      tag.putBoolean("isUpgraded", this.isUpgraded);
      tag.putByteArray("upgrades", this.upgrades);
   }

   private void readClientTag(CompoundTag tag) {
      this.crafting = tag.getBoolean("crafting");
      this.instability = tag.getInt("instability");
      this.recipeType = tag.getInt("recipeType");
      this.recipeXP = tag.getInt("recipeXP");
      this.recipeInstability = tag.getInt("recipeInstability");
      this.readEssentiaTag(tag);
      this.isUpgraded = tag.getBoolean("isUpgraded");
      if (tag.contains("upgrades")) {
         byte[] u = tag.getByteArray("upgrades");
         if (u.length == 5) {
            this.upgrades = u;
         }
      }

      if (!this.crafting && this.craftTickCount > 0) {
         this.craftTickCount = Math.max(0, this.craftTickCount - 2);
         if (this.craftTickCount > 50) {
            this.craftTickCount = 50;
         }
      }
   }

   @Override
   public void clientTick(Level level, BlockPos pos, BlockState state) {
      this.tickCount++;
      if (this.crafting) {
         this.craftTickCount++;
         Thaumcraft.setup
            .blockRunes(
               level, pos.getX(), pos.getY() - 2, pos.getZ(), 0.5F + level.random.nextFloat() * 0.2F, 0.1F, 0.7F + level.random.nextFloat() * 0.3F, 25, -0.03F
            );
         if (this.craftTickCount % 65 == 0) {
            level.playLocalSound(pos, (SoundEvent)ModSounds.INFUSER.get(), SoundSource.BLOCKS, 0.5F, 1.0F, false);
         }
      } else if (this.craftTickCount > 0) {
         this.craftTickCount = Math.max(0, this.craftTickCount - 2);
         if (this.craftTickCount > 50) {
            this.craftTickCount = 50;
         }
      }

      boolean active = this.isActive();
      if (active && this.startUp < 1.0F) {
         this.startUp = Math.min(1.0F, this.startUp + Math.max(this.startUp / 10.0F, 0.001F));
      } else if (!active && this.startUp > 0.0F) {
         this.startUp = Math.max(0.0F, this.startUp - this.startUp / 10.0F);
      }

      if (this.finishFlashTicks > 0) {
         this.finishFlashTicks--;
      }

      this.handleClientSourceFX();
   }

   public int getInstability() {
      return this.instability;
   }

   public void setInstability(int instability) {
      this.instability = instability;
   }

   public boolean hasRune(int type) {
      return this.runeLevel(type) > 0;
   }

   public int runeLevel(int type) {
      return this.isUpgraded ? this.upgrades[type] : 0;
   }

   public int getUpgradeTotal() {
      return this.upgrades[0] + this.upgrades[1] + this.upgrades[2] + this.upgrades[3] + this.upgrades[4] * 3;
   }

   public int getRecipeInstability() {
      return this.recipeInstability;
   }

   public int getRecipeType() {
      return this.recipeType;
   }

   public int getRecipeXP() {
      return this.recipeXP;
   }

   public int getRecipeIngredientsCount() {
      return this.recipeIngredients.size();
   }

   public UUID getRecipePlayer() {
      return this.recipePlayer;
   }

   public void queueSync(int delayTicks) {
      this.queueClientSync(delayTicks);
   }

   public InfusionInstabilityHandler getInstabilityHandler() {
      return this.instabilityHandler;
   }

   public int getCraftTickCount() {
      return this.craftTickCount;
   }

   public boolean isCrafting() {
      return this.crafting;
   }

   /** Клиентский счётчик вспышки завершения (рисуется в рендерере как расширяющееся гало). */
   public int getFinishFlashTicks() {
      return this.finishFlashTicks;
   }

   /** Запуск вспышки завершения (вызывается из {@link RunicMatrixBlock#triggerEvent}, case 12). */
   public void startFinishFlash() {
      this.finishFlashTicks = 20;
   }

   private boolean isCentralItemValid() {
      BlockPos centerPos = this.worldPosition.below(2);
      if (!(this.level.getBlockEntity(centerPos) instanceof ArcanePedestalBlockEntity ped)) {
         return false;
      } else {
         ItemStack stack = ped.getItem(0);
         if (!stack.isEmpty() && !this.recipeInput.isEmpty()) {
            return this.recipeInput.getDamageValue() != 32767
               ? ItemStackData.sameItemSameTags(stack, this.recipeInput)
               : ItemStack.isSameItem(stack, this.recipeInput) && ItemStackData.sameItemSameTags(stack, this.recipeInput);
         } else {
            return false;
         }
      }
   }

   private int getMatrixUpdateType(ItemStack stack) {
      if (!stack.isEmpty() && stack.getItem() == ModItems.RUNIC_MATRIX.get()) {
         CompoundTag tag = ItemStackData.getTag(stack);
         return tag != null && tag.getBoolean("isInfo") && tag.contains("metaUpdate", 99) ? tag.getByte("metaUpdate") : -1;
      } else {
         return -1;
      }
   }

   private boolean canApplyMatrixUpdate(ItemStack resultItem) {
      int type = this.getMatrixUpdateType(resultItem);
      if (type < 0) {
         return true;
      } else if (type == 5) {
         return !this.isUpgraded;
      } else if (!this.isUpgraded) {
         return false;
      } else if (this.upgrades[type] >= MAX_UPGRADES[type]) {
         return false;
      } else {
         return type == 4 ? this.getUpgradeTotal() >= 12 : this.getUpgradeTotal() < 12;
      }
   }

   private ItemStack getCentralItem() {
      BlockPos pos = this.worldPosition.below(2);
      if (this.level.getBlockEntity(pos) instanceof ArcanePedestalBlockEntity ped) {
         ItemStack s = ped.getItem(0);
         if (!s.isEmpty()) {
            return s.copy();
         }
      }

      return ItemStack.EMPTY;
   }

   public AABB getFindArea() {
      if (this.findArea == null) {
         this.findArea = new AABB(this.worldPosition).inflate(10.0, 10.0, 10.0);
      }

      return this.findArea;
   }

   public void setActive(boolean value) {
      if (this.level != null && !this.level.isClientSide) {
         this.level.setBlockAndUpdate(this.worldPosition, (BlockState)this.getBlockState().setValue(ThaumcraftEntityBlock.ACTIVE, value));
         this.queueClientSync(0);
      }
   }

   public boolean isActive() {
      return (Boolean)this.getBlockState().getValue(ThaumcraftEntityBlock.ACTIVE);
   }

   @Override
   public void loadAdditional(@NotNull CompoundTag tag, @NotNull Provider provider) {
      super.loadAdditional(tag, provider);
      this.crafting = tag.getBoolean("crafting");
      this.instability = tag.getInt("instability");
      this.recipeType = tag.getInt("recipeType");
      this.recipeXP = tag.getInt("recipeXP");
      this.recipeInstability = tag.getInt("recipeInstability");
      this.recipeEnchantment = null;
      if (tag.contains("recipeEnchantment", 8)) {
         try {
            net.minecraft.resources.ResourceLocation id = net.minecraft.resources.ResourceLocation.parse(tag.getString("recipeEnchantment"));
            net.minecraft.resources.ResourceKey<net.minecraft.world.item.enchantment.Enchantment> key =
               net.minecraft.resources.ResourceKey.create(Registries.ENCHANTMENT, id);
            this.recipeEnchantment = provider.lookupOrThrow(Registries.ENCHANTMENT).get(key).orElse(null);
         } catch (Exception e) {
            this.recipeEnchantment = null;
         }
      }
      this.recipeModifierId = tag.contains("recipeModifier", 8) ? ResourceLocation.tryParse(tag.getString("recipeModifier")) : null;
      if (tag.contains("recipePlayer")) {
         this.recipePlayer = tag.getUUID("recipePlayer");
      } else {
         this.recipePlayer = null;
      }

      this.readEssentiaTag(tag);
      this.recipeInput = ItemStack.EMPTY;
      this.recipeOutput = ItemStack.EMPTY;
      if (tag.contains("recipeInput", 10)) {
         this.recipeInput = ItemStackData.loadStack(tag.getCompound("recipeInput"), provider);
      }

      if (tag.contains("recipeOutput", 10)) {
         this.recipeOutput = ItemStackData.loadStack(tag.getCompound("recipeOutput"), provider);
      }

      this.isUpgraded = tag.getBoolean("isUpgraded");
      if (tag.contains("upgrades")) {
         byte[] u = tag.getByteArray("upgrades");
         if (u.length == 5) {
            this.upgrades = u;
         }
      }

      this.recipeIngredients.clear();
      ListTag ingList = tag.getList("recipeIngredients", 10);

      for (int i = 0; i < ingList.size(); i++) {
         CompoundTag c = ingList.getCompound(i);
         if (c.contains("json", 8)) {
            try {
               JsonElement el = JsonParser.parseString(c.getString("json"));
               Ingredient ing = (Ingredient)Ingredient.CODEC.parse(JsonOps.INSTANCE, el).result().orElse(null);
               if (ing != null && !ing.isEmpty()) {
                  this.recipeIngredients.add(ing);
               }
            } catch (Exception var8) {
            }
         } else {
            ItemStack stack = ItemStackData.loadStack(c, provider);
            if (!stack.isEmpty()) {
               this.recipeIngredients.add(Ingredient.of(new ItemStack[]{stack}));
            }
         }
      }
   }

   @Override
   protected void saveAdditional(@NotNull CompoundTag tag, @NotNull Provider provider) {
      super.saveAdditional(tag, provider);
      tag.putBoolean("crafting", this.crafting);
      tag.putInt("instability", this.instability);
      tag.putInt("recipeType", this.recipeType);
      tag.putInt("recipeXP", this.recipeXP);
      tag.putInt("recipeInstability", this.recipeInstability);
      if (this.recipeEnchantment != null && this.recipeEnchantment.getKey() != null) {
         tag.putString("recipeEnchantment", this.recipeEnchantment.getKey().location().toString());
      }
      if (this.recipeModifierId != null) {
         tag.putString("recipeModifier", this.recipeModifierId.toString());
      }
      if (this.recipePlayer != null) {
         tag.putUUID("recipePlayer", this.recipePlayer);
      }

      tag.put("recipeEssentia", this.writeEssentiaTag());
      tag.putBoolean("isUpgraded", this.isUpgraded);
      tag.putByteArray("upgrades", this.upgrades);
      if (!this.recipeInput.isEmpty()) {
         tag.put("recipeInput", ItemStackData.saveStack(this.recipeInput, provider));
      }

      if (!this.recipeOutput.isEmpty()) {
         tag.put("recipeOutput", ItemStackData.saveStack(this.recipeOutput, provider));
      }

      ListTag ingList = new ListTag();

      for (Ingredient ing : this.recipeIngredients) {
         CompoundTag c = new CompoundTag();
         c.putString("json", Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, ing).result().<String>map(JsonElement::toString).orElse("[]"));
         ingList.add(c);
      }

      tag.put("recipeIngredients", ingList);
   }

   private CompoundTag createClientUpdateTag() {
      CompoundTag tag = new CompoundTag();
      this.writeClientTag(tag);
      return tag;
   }

   @NotNull
   public CompoundTag getUpdateTag(@NotNull Provider provider) {
      return this.createClientUpdateTag();
   }

   @Override
   public void handleUpdateTag(CompoundTag tag, Provider provider) {
      this.readClientTag(tag);
   }

   @Nullable
   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   @Override
   public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, Provider provider) {
      CompoundTag tag = pkt.getTag();
      if (tag != null && this.level != null && this.level.isClientSide) {
         this.handleUpdateTag(tag, provider);
      }
   }

   private void handleClientSourceFX() {
      // Подсказка о нестабильности на клиенте. В старой системе поле нестабильности
      // жило в диапазоне 0..25, и nextInt(200) давало вспышку примерно в 12% кадров.
      // Теперь это дефицит в очках (до ~120 при разобранном алтаре), поэтому окно
      // ролла расширено вчетверо: полностью нестабильная матрица мигает около трёх
      // раз в секунду вместо двенадцати — заметно, но не засоряет экран.
      if (this.crafting && this.instability > 0
         && this.level.random.nextInt(InfusionInstabilityHandler.EVENT_ROLL * 4) <= this.instability) {
         float range = 2.0F;
         Thaumcraft.setup
            .nodeBolt(
               this.level,
               this.worldPosition.getX() + 0.5F,
               this.worldPosition.getY() + 0.5F,
               this.worldPosition.getZ() + 0.5F,
               this.worldPosition.getX() + 0.5F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * range,
               this.worldPosition.getY() + 0.5F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * range,
               this.worldPosition.getZ() + 0.5F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * range
            );
      }

      if (!this.sourceFX.isEmpty()) {
         Iterator<Entry<String, RunicMatrixBlockEntity.SourceFX>> it = this.sourceFX.entrySet().iterator();

         while (it.hasNext()) {
            Entry<String, RunicMatrixBlockEntity.SourceFX> entry = it.next();
            RunicMatrixBlockEntity.SourceFX fx = entry.getValue();
            if (fx.ticks <= 0) {
               it.remove();
            } else {
               if (fx.loc.equals(this.worldPosition)) {
                  Entity entity = this.level.getEntity(fx.color);
                  if (entity != null) {
                     Thaumcraft.setup
                        .drawInfusionParticles4(
                           this.level,
                           entity.getX() + (this.level.random.nextFloat() - this.level.random.nextFloat()) * entity.getBbWidth(),
                           entity.getBoundingBox().minY + this.level.random.nextFloat() * entity.getBbHeight(),
                           entity.getZ() + (this.level.random.nextFloat() - this.level.random.nextFloat()) * entity.getBbWidth(),
                           this.worldPosition.getX(),
                           this.worldPosition.getY(),
                           this.worldPosition.getZ()
                        );
                  }
               } else {
                  assert this.level != null;

                  if (this.level.getBlockEntity(fx.loc) instanceof ArcanePedestalBlockEntity pedestal) {
                     ItemStack stack = pedestal.getItem(0);
                     if (!stack.isEmpty()) {
                        // Ручей фиолетовых искр от пьедестала к матрице. Летающие
                        // спрайты самого предмета (drawInfusionParticles1/2) убраны
                        // по фидбеку — выглядит как «предмет летит в матрицу».
                        Thaumcraft.setup
                           .drawInfusionParticles3(
                              this.level,
                              fx.loc.getX() + this.level.random.nextFloat(),
                              fx.loc.getY() + this.level.random.nextFloat() + 1.0F,
                              fx.loc.getZ() + this.level.random.nextFloat(),
                              this.worldPosition.getX(),
                              this.worldPosition.getY(),
                              this.worldPosition.getZ()
                           );
                     }
                  } else {
                     fx.ticks = 0;
                  }
               }

               fx.ticks--;
            }
         }
      }
   }

   /**
    * ПКМ жезлом по матрице (порт {@code TileInfusionMatrix#onWandRightClick} из 1.7.10):
    * активная матрица с валидной структурой запускает инфузию; неактивная — «раскрывается»,
    * если структура собрана. Возврат: 1 — обработано, -1 — нет.
    */
   @Override
   public int onWandRightClick(
      @NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockPos blockPos, @NotNull Direction direction
   ) {
      if (level.isClientSide()) {
         return -1;
      }

      if (this.isActive() && !this.crafting) {
         if (!this.validLocation()) {
            this.setActive(false);
            return -1;
         }

         ItemStack central = this.getCentralItem();
         if (central.isEmpty()) {
            player.displayClientMessage(
               Component.translatable("message.thaumcraft.matrix.needs_catalyst"), true
            );
            return -1;
         }

         List<ItemStack> items = this.collectPedestalItems();
         thaumcraft.api.crafting.InfusionRecipe recipe = thaumcraft.recipe.ThaumcraftCraftingManager
            .findInfusionRecipe(items, central, player);
         if (recipe == null) {
            int started = this.tryStartModifier(level, player, central, items);
            return started > 0 ? started : this.tryStartEnchanting(level, player, central, items);
         }

         if (!this.canApplyMatrixUpdate(recipe.getResultItem())) {
            player.displayClientMessage(Component.translatable("message.thaumcraft.matrix.upgrade_refused"), true);
            return -1;
         }

         this.recipeInput = central.copy();
         this.recipeOutput = recipe.getResultItem().copy();
         this.recipeIngredients = new java.util.ArrayList<>(recipe.components);
         this.recipeEssentia = recipe.aspects.copy();
         this.recipeEnchantment = null;
         this.recipeModifierId = null;
         this.recipeType = 0;
         this.recipeXP = 0;
         this.recipeInstability = recipe.getInstability();
         this.beginCraft(level, player);
         return 1;
      } else if (!this.isActive()) {
         if (this.validLocation()) {
            // Структура собрана, матрица закрыта — раскрываем (как в оригинале).
            this.setActive(true);
            level.blockEvent(this.worldPosition, level.getBlockState(this.worldPosition).getBlock(), 1, 0);
            level.playSound(
               null, this.worldPosition, net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME, net.minecraft.sounds.SoundSource.BLOCKS, 0.8F, 1.3F
            );
            return 1;
         }

         // Клик не дал результата — объясняем, чего не хватает (после этого
         // WandTriggerRegistry достроит алтарь из камня, если тот расставлен).
         player.displayClientMessage(
            Component.translatable("message.thaumcraft.matrix.incomplete"), true
         );
      }

      return -1;
   }

   /**
    * Старт зачарования на алтаре — вторая ветка {@code TileInfusionMatrix#craftingStart}:
    * предмет уже зачарован, рецепт дотягивает чары на уровень выше за опыт и эссенцию.
    */
   private int tryStartEnchanting(Level level, Player player, ItemStack central, List<ItemStack> items) {
      thaumcraft.api.crafting.InfusionEnchantmentRecipe enchantRecipe = thaumcraft.recipe.ThaumcraftCraftingManager
         .findInfusionEnchantmentRecipe(items, central, player);
      if (enchantRecipe == null) {
         return -1;
      }

      this.recipeInput = central.copy();
      this.recipeOutput = central.copy();
      this.recipeIngredients = new java.util.ArrayList<>(enchantRecipe.components);
      thaumcraft.api.aspects.AspectList essentia = enchantRecipe.aspects.copy();
      float mod = enchantRecipe.getEssenceMod(central);
      for (thaumcraft.api.aspects.Aspect aspect : essentia.getAspects()) {
         essentia.add(aspect, (int)(essentia.getAmount(aspect) * mod));
      }

      this.recipeEssentia = essentia;
      this.recipeEnchantment = enchantRecipe.enchantment;
      this.recipeModifierId = null;
      this.recipeType = 1;
      this.recipeXP = enchantRecipe.calcXP(central);
      this.recipeInstability = enchantRecipe.calcInstability(central);
      this.beginCraft(level, player);
      return 1;
   }

   /**
    * Start of a catalyst-modifying recipe (runic augmentation, fortress helm upgrades). In TC4 these were
    * infusion recipes with an NBT output applied to the catalyst; here the output is recomputed from the
    * catalyst when the craft ends.
    */
   private int tryStartModifier(Level level, Player player, ItemStack central, List<ItemStack> items) {
      RecipeHolder<? extends IInfusionModifierRecipe> holder = ThaumcraftCraftingManager.findInfusionModifierRecipe(items, central, player);
      if (holder == null) {
         return -1;
      }

      IInfusionModifierRecipe recipe = holder.value();
      this.recipeInput = central.copy();
      this.recipeOutput = recipe.modifierItem(central);
      this.recipeIngredients = new ArrayList<>(recipe.getIngredientList(central));
      this.recipeEssentia = recipe.getAspects(central).copy();
      this.recipeEnchantment = null;
      this.recipeModifierId = holder.id();
      this.recipeType = 2;
      this.recipeXP = recipe.getExperienceCost(central);
      this.recipeInstability = recipe.getInstability(central);
      this.beginCraft(level, player);
      return 1;
   }

   private void beginCraft(Level level, Player player) {
      this.recipePlayer = player.getUUID();
      this.refreshInstability();
      this.craftTickCount = 0;
      this.tickCount = 0;
      this.countDelay = 10;
      this.resetItemTarget();
      this.crafting = true;
      this.queueClientSync(0);
      level.playSound(null, this.worldPosition, ModSounds.CRAFT_START.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
   }

   /**
    * Каноническая проверка структуры (порт {@code TileInfusionMatrix#validLocation}):
    * центральный пьедестал внизу и 4 пилона по углам нижнего яруса.
    */
   public boolean validLocation() {
      if (this.level == null) {
         return false;
      }

      BlockPos center = this.worldPosition.below(2);
      if (!(this.level.getBlockEntity(center) instanceof ArcanePedestalBlockEntity)) {
         return false;
      }

      return this.level.getBlockEntity(center.east().south()) instanceof thaumcraft.block.infusion_pillar.InfusionPillarBlockEntity
         && this.level.getBlockEntity(center.east().north()) instanceof thaumcraft.block.infusion_pillar.InfusionPillarBlockEntity
         && this.level.getBlockEntity(center.west().north()) instanceof thaumcraft.block.infusion_pillar.InfusionPillarBlockEntity
         && this.level.getBlockEntity(center.west().south()) instanceof thaumcraft.block.infusion_pillar.InfusionPillarBlockEntity;
   }

   /** Items on this altar's pedestals (central excluded), by the same rule as the draw and the stability score. */
   private List<ItemStack> collectPedestalItems() {
      List<ItemStack> items = new ArrayList<>();
      if (this.level == null) {
         return items;
      }

      for (BlockPos pos : this.instabilityHandler.findPedestals()) {
         if (this.level.getBlockEntity(pos) instanceof ArcanePedestalBlockEntity pedestal && !pedestal.getItem(0).isEmpty()) {
            items.add(pedestal.getItem(0).copy());
         }
      }

      return items;
   }

   /**
    * Server loop. Craft steps follow TC4 craftCycle: one step every {@link #countDelay} ticks, essentia first,
    * then the pedestal items, then the result. Stability and events run on their own period.
    */
   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      this.tickCount++;

      // Port of TileInfusionMatrix#func_145845_h: structure check every 20 ticks while crafting, else every 100.
      if (this.tickCount % (this.crafting ? 20 : 100) == 0 && !this.validLocation() && this.isActive()) {
         this.setActive(false);
      }

      if (this.hasRune(UPGRADE_CLEANER) && this.isActive() && this.tickCount % InfusionInstabilityHandler.EVENT_PERIOD_TICKS == 0) {
         this.instabilityHandler.cleanFlux(level);
      }

      if (!this.crafting) {
         this.flushClientSync();
         return;
      }

      this.craftTickCount++;

      // The structure scan also refreshes the pedestal list the craft step and the events use.
      if (this.tickCount % InfusionInstabilityHandler.EVENT_PERIOD_TICKS == 0) {
         this.refreshInstability();
      }

      if (this.instability > 0) {
         this.instabilityHandler.handleEssentiaLoss(level);
      }

      if (this.instability > 0 && this.tickCount % InfusionInstabilityHandler.EVENT_PERIOD_TICKS == 0
         && level.random.nextInt(InfusionInstabilityHandler.EVENT_ROLL) <= this.instability) {
         this.runInstabilityEvent(level);
      }

      if (this.crafting && this.tickCount % this.countDelay == 0) {
         this.craftCycle(level);
      }

      this.flushClientSync();
   }

   /** One craft step (port of TileInfusionMatrix#craftCycle). */
   private void craftCycle(Level level) {
      // A missing or swapped catalyst always triggers an event before the craft is cancelled, as in TC4.
      if (!this.isCentralItemValid()) {
         this.runInstabilityEvent(level);
         this.cancelCraft(level);
         return;
      }

      if (this.recipeType == 1 && this.recipeXP > 0) {
         if (this.drainXpTick(level)) {
            this.countDelay = 20;
            return;
         }

         // No player nearby: the essentia demand grows instead, as in the original.
         Aspect[] ingEss = this.recipeEssentia.getAspects();
         if (ingEss.length > 0 && level.random.nextInt(3) == 0) {
            this.recipeEssentia.add(ingEss[level.random.nextInt(ingEss.length)], 1);
            this.queueClientSync(0);
         }

         return;
      }

      this.countDelay = 10;
      if (this.recipeEssentia.visSize() > 0) {
         this.drainEssentia(level);
      } else if (this.recipeIngredients.isEmpty()) {
         this.finishCraft(level);
      } else {
         this.drawIngredient(level);
      }
   }

   /** Отмена инфузии (порт ветки «invalid» из {@code TileInfusionMatrix#craftCycle}). */
   private void cancelCraft(Level level) {
      this.instability = 0;
      this.crafting = false;
      this.recipeEssentia = new AspectList();
      this.recipeInput = ItemStack.EMPTY;
      this.recipeOutput = ItemStack.EMPTY;
      this.recipeIngredients.clear();
      this.resetItemTarget();
      this.recipeType = 0;
      this.recipeXP = 0;
      this.recipeEnchantment = null;
      this.recipeModifierId = null;
      this.recipeInstability = 0;
      this.recipePlayer = null;
      this.craftTickCount = 0;
      this.countDelay = 10;
      this.queueClientSync(0);
      level.playSound(
         null,
         this.worldPosition,
         thaumcraft.init.registry.ModSounds.CRAFT_FAIL.get(),
         net.minecraft.sounds.SoundSource.BLOCKS,
         1.0F,
         0.6F
      );
   }

   /**
    * Забрать один компонент с ближайшего подходящего пьедестала.
    *
    * <p>Порт ветки ингредиентов {@code TileInfusionMatrix#craftCycle}: первый цикл
    * только «прицеливается» — матрица начинает тянуть предмет (FX-пакет на клиент,
    * ручей частиц от пьедестала к матрице), и лишь через 5 циклов
    * (countDelay, как в оригинале) предмет реально уходит. До тех пор он лежит
    * на пьедестале и может быть выбит событием нестабильности.
    */
   private void drawIngredient(Level level) {
      if (this.recipeIngredients.isEmpty()) {
         this.resetItemTarget();
         return;
      }

      // Continue pulling the current target while it is still valid.
      if (this.itemTargetPedestal != null) {
         Ingredient ing = this.itemTargetIngredientIndex >= 0 && this.itemTargetIngredientIndex < this.recipeIngredients.size()
            ? this.recipeIngredients.get(this.itemTargetIngredientIndex)
            : null;
         if (level.getBlockEntity(this.itemTargetPedestal) instanceof ArcanePedestalBlockEntity ped
            && ing != null
            && !ped.getItem(0).isEmpty()
            && ing.test(ped.getItem(0))) {
            this.itemCount--;
            if (this.itemCount <= 0) {
               // Luck rune: 2.5% per level that the item stays on its pedestal although it counts as used.
               if (level.random.nextFloat() >= LUCK_CHANCE_PER_LEVEL * this.runeLevel(UPGRADE_LUCK)) {
                  ItemStack stack = ped.getItem(0);
                  ItemStack container = stack.getCraftingRemainingItem();
                  ped.setItem(0, container.isEmpty() ? ItemStack.EMPTY : container.copy());
               }

               this.recipeIngredients.remove(this.itemTargetIngredientIndex);
               // Flash on the source pedestal (blockEvent 11 of craftCycle).
               level.blockEvent(this.itemTargetPedestal, level.getBlockState(this.itemTargetPedestal).getBlock(), 11, 0);
               this.resetItemTarget();
               this.queueClientSync(0);
            }

            return;
         }

         // The item was knocked off or the pedestal emptied: aim again.
         this.resetItemTarget();
      }

      for (BlockPos pos : this.instabilityHandler.getPedestals()) {
         if (!(level.getBlockEntity(pos) instanceof ArcanePedestalBlockEntity pedestal) || pedestal.getItem(0).isEmpty()) {
            continue;
         }

         ItemStack stack = pedestal.getItem(0);
         for (int i = 0; i < this.recipeIngredients.size(); i++) {
            if (this.recipeIngredients.get(i).test(stack)) {
               this.itemTargetPedestal = pos.immutable();
               this.itemTargetIngredientIndex = i;
               this.itemCount = 5;
               // Particle stream from the pedestal to the matrix (PacketFXInfusionSource).
               PacketHandler.sendToAllAround(
                  new PacketFXInfusionSource(
                     this.worldPosition,
                     (byte)(this.worldPosition.getX() - pos.getX()),
                     (byte)(this.worldPosition.getY() - pos.getY()),
                     (byte)(this.worldPosition.getZ() - pos.getZ()),
                     0
                  ),
                  level,
                  this.worldPosition,
                  32.0
               );
               return;
            }
         }
      }
   }

   private void resetItemTarget() {
      this.itemTargetPedestal = null;
      this.itemTargetIngredientIndex = -1;
      this.itemCount = 0;
   }

   /** Забрать уровень опыта у ближайшего игрока (порт XP-ветки craftCycle). */
   private boolean drainXpTick(Level level) {
      for (Player target : level.getEntitiesOfClass(
         Player.class, new net.minecraft.world.phys.AABB(this.worldPosition).inflate(10.0)
      )) {
         if (target.experienceLevel > 0) {
            target.giveExperienceLevels(-1);
            this.recipeXP--;
            target.hurt(level.damageSources().magic(), level.random.nextInt(2));
            thaumcraft.network.PacketHandler.sendToAllAround(
               new thaumcraft.network.fx.PacketFXInfusionSource(this.worldPosition, (byte)0, (byte)0, (byte)0, target.getId()),
               level,
               this.worldPosition,
               32.0
            );
            level.playSound(
               null, target.blockPosition(), net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 2.0F + level.random.nextFloat() * 0.4F
            );
            this.queueClientSync(0);
            return true;
         }
      }

      return false;
   }

   /**
    * Drains essentia from nearby sources (jars and the like). TC4 takes one unit per craft step; the speed rune
    * raises that to 4 per level. The efficiency rune gives 5% per level per unit to leave the source untouched.
    */
   private void drainEssentia(Level level) {
      int initialBudget = Math.max(1, SPEED_UNITS_PER_LEVEL * this.runeLevel(UPGRADE_SPEED));
      int budget = initialBudget;
      float keepChance = EFFICIENCY_CHANCE_PER_LEVEL * this.runeLevel(UPGRADE_EFFICIENCY);
      for (BlockPos pos : this.essentiaSources) {
         if (budget <= 0 || this.recipeEssentia.size() == 0) {
            break;
         }

         if (!(level.getBlockEntity(pos) instanceof IAspectSource source) || source.getAspects() == null) {
            continue;
         }

         for (Aspect aspect : this.recipeEssentia.getAspects()) {
            while (budget > 0 && this.recipeEssentia.getAmount(aspect) > 0 && source.getAspects().getAmount(aspect) > 0) {
               if (level.random.nextFloat() >= keepChance && !source.takeFromContainer(aspect, 1)) {
                  break;
               }

               budget--;
               this.recipeEssentia.remove(aspect, 1);
               if (this.recipeEssentia.getAmount(aspect) <= 0) {
                  this.recipeEssentia.remove(aspect);
               }

               // Aspect stream from the source to the matrix (PacketFXEssentiaSource), coloured by the aspect.
               PacketHandler.sendToAllAround(
                  new PacketFXEssenceSource(
                     this.worldPosition,
                     (byte)(this.worldPosition.getX() - pos.getX()),
                     (byte)(this.worldPosition.getY() - pos.getY()),
                     (byte)(this.worldPosition.getZ() - pos.getZ()),
                     aspect.getColor()
                  ),
                  level,
                  this.worldPosition,
                  32.0
               );
            }
         }
      }

      if (budget < initialBudget) {
         this.queueClientSync(0);
      }
   }

   /** Finish: the result replaces the catalyst on the central pedestal (as in 1.7.10). */
   private void finishCraft(Level level) {
      BlockPos centerPos = this.worldPosition.below(2);
      ArcanePedestalBlockEntity center = level.getBlockEntity(centerPos) instanceof ArcanePedestalBlockEntity ped ? ped : null;
      ItemStack result = ItemStack.EMPTY;
      if (this.getMatrixUpdateType(this.recipeOutput) >= 0) {
         // A rune or the matrix upgrade is built into this matrix instead of being handed out as an item.
         this.applyMatrixUpdate(this.getMatrixUpdateType(this.recipeOutput));
         if (center != null) {
            center.setItem(0, ItemStack.EMPTY);
         }
      } else if (center != null && !center.getItem(0).isEmpty() && (this.recipeType == 1 || this.recipeType == 2)) {
         result = this.applyToCatalyst(level, center.getItem(0).copy());
         center.setInfusionOutput(result);
      } else {
         result = this.recipeOutput.copy();
         if (center != null) {
            center.setInfusionOutput(result);
         } else {
            level.addFreshEntity(
               new ItemEntity(level, this.worldPosition.getX() + 0.5, this.worldPosition.getY() - 1.0, this.worldPosition.getZ() + 0.5, result)
            );
         }
      }

      // TC4 fires the crafting event for the player who started the infusion.
      Player player = this.recipePlayer == null ? null : level.getPlayerByUUID(this.recipePlayer);
      if (player != null && !result.isEmpty()) {
         EventHooks.firePlayerCraftingEvent(player, result, new SimpleContainer(this.recipeInput.copy()));
      }

      // Final flash on the central pedestal (blockEvent 12 of craftCycle) and our purple burst on the matrix.
      level.blockEvent(centerPos, level.getBlockState(centerPos).getBlock(), 12, 0);
      level.blockEvent(this.worldPosition, level.getBlockState(this.worldPosition).getBlock(), 12, 0);

      // The original clears the instability on a successful finish (craftCycle).
      this.instability = 0;
      this.crafting = false;
      this.recipeInput = ItemStack.EMPTY;
      this.recipeOutput = ItemStack.EMPTY;
      this.recipeIngredients = new ArrayList<>();
      this.resetItemTarget();
      this.recipeEssentia = new AspectList();
      this.recipeInstability = 0;
      this.recipePlayer = null;
      this.recipeType = 0;
      this.recipeXP = 0;
      this.recipeEnchantment = null;
      this.recipeModifierId = null;
      this.craftTickCount = 0;
      this.tickCount = 0;
      this.queueClientSync(0);
   }

   /** Enchantment (type 1) or modifier (type 2) result built from the catalyst as it is now. */
   private ItemStack applyToCatalyst(Level level, ItemStack catalyst) {
      if (this.recipeType == 1 && this.recipeEnchantment != null) {
         catalyst.enchant(this.recipeEnchantment, EnchantmentHelper.getTagEnchantmentLevel(this.recipeEnchantment, catalyst) + 1);
         return catalyst;
      }

      if (this.recipeType == 2 && this.recipeModifierId != null
         && level.getRecipeManager().byKey(this.recipeModifierId).orElse(null) instanceof RecipeHolder<?> holder
         && holder.value() instanceof IInfusionModifierRecipe modifier) {
         return modifier.modifierItem(catalyst);
      }

      return this.recipeOutput.copy();
   }

   /** Builds a rune into the matrix; the stability rune wipes the other runes, as its page warns. */
   private void applyMatrixUpdate(int type) {
      if (type == UPGRADE_UNLOCK) {
         this.isUpgraded = true;
      } else if (type == UPGRADE_STABILITY) {
         this.upgrades = new byte[UPGRADE_COUNT];
         this.upgrades[UPGRADE_STABILITY] = 1;
      } else {
         this.upgrades[type]++;
      }

      this.setChanged();
   }

   @NotNull
   @Override
   public ItemStack onWandRightClick(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
      return ItemStack.EMPTY;
   }

   @Override
   public void onUsingWandTick(@NotNull ItemStack stack, @NotNull Player player, int count) {
   }

   @Override
   public void onWandStoppedUsing(@NotNull ItemStack stack, @NotNull Level level, @NotNull Player player, int count) {
   }

   @Override
   public AspectList getAspects() {
      return this.recipeEssentia;
   }

   @Override
   public void setAspects(AspectList aspectList) {
   }

   @Override
   public int addToContainer(Aspect aspect, int amount) {
      return 0;
   }

   @Override
   public boolean takeFromContainer(Aspect aspect, int amount) {
      return false;
   }

   @Override
   public boolean takeFromContainer(AspectList aspectList) {
      return false;
   }

   @Override
   public boolean doesContainerContainAmount(Aspect aspect, int amount) {
      return false;
   }

   @Override
   public boolean doesContainerContain(AspectList aspectList) {
      return false;
   }

   @Override
   public int containerContains(Aspect aspect) {
      return 0;
   }

   @Override
   public boolean doesContainerAccept(Aspect aspect) {
      return true;
   }

   public static class SourceFX {
      public BlockPos loc;
      public int ticks;
      public int color;
      public int entity;

      public SourceFX(BlockPos loc, int ticks, int color) {
         this.loc = loc;
         this.ticks = ticks;
         this.color = color;
      }
   }

   // ------------------------------------------------------------------
   // структура и нестабильность инфузии (порт идей 1.7.10 TileInfusionMatrix)
   // ------------------------------------------------------------------

   /**
    * Стабильность структуры алтаря в очках (включая базу матрицы).
    */
   private int scanStructureStability() {
      return this.instabilityHandler.scanStability();
   }

   /**
    * Пересчёт дефицита: сколько очков стабильности не хватает текущему рецепту.
    * Ноль — крафт идёт молча; положительное число — величина нестабильности,
    * по которой матрица бросает события (чем больше, тем чаще).
    */
   private void refreshInstability() {
      int required = InfusionInstabilityHandler.requiredStability(this.recipeInstability);
      int deficit = Math.max(0, required - this.scanStructureStability());
      // The stability rune fully stabilizes the matrix; the scan still runs to refresh the pedestal list.
      this.instability = this.hasRune(UPGRADE_STABILITY) ? 0 : deficit;
      this.essentiaSources = this.findEssentiaSources();
   }

   private List<BlockPos> findEssentiaSources() {
      List<BlockPos> found = new ArrayList<>();
      AABB area = this.getFindArea();
      for (BlockPos pos : BlockPos.betweenClosed(
         BlockPos.containing(area.minX, area.minY, area.minZ), BlockPos.containing(area.maxX, area.maxY, area.maxZ)
      )) {
         if (this.level.getBlockEntity(pos) instanceof IAspectSource) {
            found.add(pos.immutable());
         }
      }

      return found;
   }

   /** Случайное событие нестабильности (21 классический исход из TC4). */
   private void runInstabilityEvent(net.minecraft.world.level.Level level) {
      this.instabilityHandler.runInstabilityEvent(level);
   }
   /**
    * Возвращает алтарь инфузии к исходному виду — обычные блоки вместо
    * магических колонн, матрица выключена.
    *
    * <p>Чертёж взят из {@code WandManager.fitInfusionAltar} (1.7.10): относительно
    * позиции матрицы на уровень ниже идут четыре угловых блока магического камня,
    * ещё на уровень ниже — четыре блока магического кирпича. Именно эту постройку
    * игрок возводил руками до того, как жезл превратил её в алтарь, поэтому
    * «дефолтное состояние» — это оно.
    *
    * <p>В самом TC4 такого автозакрытия нет: `replaceInfusionAltar` лишь
    * СОЗДАЁТ алтарь, а при сломанной колонне крафт просто перестаёт идти, и
    * блоки остаются как есть. Возврат алтаря — запрошенная механика, а не
    * паритет, поэтому он помечен так.
    *
    * @param matrixPos позиция рунической матрицы (верхний центральный блок)
    */
   private static boolean reverting = false;

   /** Защита от повторного входа: возврат сам убирает колонны, а это снова зовёт onRemove. */
   public static boolean isReverting() {
      return reverting;
   }

   public static void revertAltar(Level level, BlockPos matrixPos) {
      reverting = true;
      try {
         revertAltarInner(level, matrixPos);
      } finally {
         reverting = false;
      }
   }

   private static void revertAltarInner(Level level, BlockPos matrixPos) {
      for (int dx : new int[]{-1, 1}) {
         for (int dz : new int[]{-1, 1}) {
            // Убираем колонны и возвращаем блоки постройки игрока.
            level.setBlockAndUpdate(
               matrixPos.offset(dx, -1, dz), ModBlocks.ARCANE_STONE.get().defaultBlockState()
            );
            level.setBlockAndUpdate(
               matrixPos.offset(dx, -2, dz), ModBlocks.ARCANE_BRICKS.get().defaultBlockState()
            );
         }
      }

      if (level.getBlockEntity(matrixPos) instanceof RunicMatrixBlockEntity matrix) {
         matrix.setActive(false);
      }
   }

   /** Ищет руническую матрицу в кубе 3x3x3 над точкой (для разрушения колонны). */
   public static BlockPos findMatrixNear(Level level, BlockPos pos) {
      for (int dy = 0; dy <= 3; dy++) {
         for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
               BlockPos check = pos.offset(dx, dy, dz);
               if (level.getBlockState(check).is(
                  (net.minecraft.world.level.block.Block)ModBlocks.RUNIC_MATRIX.get()
               )) {
                  return check;
               }
            }
         }
      }

      return null;
   }

}