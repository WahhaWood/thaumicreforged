package thaumicenergistics.block.arcane_assembler;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.IPatternDetails.IInput;
import appeng.api.inventories.InternalInventory;
import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.storage.MEStorage;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkedInvBlockEntity;
import appeng.core.definitions.AEItems;
import appeng.me.helpers.MachineSource;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;
import java.lang.ref.WeakReference;
import java.util.Hashtable;
import java.util.List;
import thaumicenergistics.ae2.recipe.AssemblerKind;
import thaumicenergistics.ae2.recipe.AssemblerPattern;
import thaumicenergistics.ae2.recipe.AssemblerRecipe;
import thaumicenergistics.ae2.recipe.AssemblerRecipes;
import thaumicenergistics.ae2.recipe.RecipeAssembler;
import thaumicenergistics.init.ModBlockEntities;
import thaumicenergistics.init.ModBlocks;
import thaumicenergistics.init.ModItems;
import thaumicenergistics.integration.tc.DigiVisSourceData;
import thaumicenergistics.menu.ArcaneAssemblerMenu;
import thaumicenergistics.util.VisCraftingHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.network.PacketHandler;
import thaumcraft.network.fx.PacketFXBlockSparkle;
import thaumcraft.api.visnet.TileVisNode;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

public class ArcaneAssemblerBlockEntity extends AENetworkedInvBlockEntity implements ICraftingProvider, MenuProvider, RecipeAssembler {
   private static final String NBTKEY_VIS_INTERFACE = "vis_interface";
   private static final String NBTKEY_UPGRADEINV = "upgrades";
   private static final String NBTKEY_CRAFTING = "isCrafting";
   private static final String NBTKEY_CRAFTING_PATTERN = "pattern";
   private static final String NBTKEY_VISUPGRADE = "visUpgrade";
   private static final String NBTKEY_ARMORS = "armors";
   public static final String NBTKEY_STORED_VIS = "stored_vis";
   public static final int CVIS_MULTIPLER = 10;
   private static final int BASE_TICKS_PER_CRAFT = 20;
   public static final Aspect[] PRIMALS = new Aspect[]{Aspect.AIR, Aspect.WATER, Aspect.FIRE, Aspect.ORDER, Aspect.ENTROPY, Aspect.EARTH};
   public static final int MAX_STORED_CVIS = 1500;
   public static final double IDLE_POWER = 0.0;
   public static final double ACTIVE_POWER = 1.5;
   public static final double WARP_POWER_PERCENT = 0.15;
   public static final int MAX_SPEED_UPGRADES = 4;
   public final AppEngInternalInventory coreInventory = new AppEngInternalInventory(this, 1, 1);
   public final AppEngInternalInventory armorInventory = new AppEngInternalInventory(this, 4);
   public final AppEngInternalInventory craftingInventory = new AppEngInternalInventory(this, 1);
   private final IUpgradeInventory upgradeInventory;
   public boolean isCrafting = false;
   private AssemblerPattern currentPattern = null;
   private ResourceLocation currentRecipeId = null;
   private final AssemblerRecipes recipeBook = new AssemblerRecipes(AssemblerKind.ARCANE);
   private final DigiVisSourceData visSourceInfo = new DigiVisSourceData();
   private final AspectList storedVis = new AspectList();
   private int visTickCounter = 0;
   public int craftTickCounter = 0;
   private int visSyncTick = 0;
   private int clientFxTick = 0;
   private WeakReference<TileVisNode> cachedVisRelay = new WeakReference<>(null);
   private int delayTickCounter = 0;
   private final MachineSource mySource;
   private boolean isActive = false;
   private boolean visUpgrade = false;
   private final Hashtable<Aspect, Float> visDiscount = new Hashtable<>();
   private float warpPowerMultiplier = 1.0F;
   private boolean delayedUpdate = false;
   boolean flag_RecalcVis = false;

   public ArcaneAssemblerBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)ModBlockEntities.ARCANE_ASSEMBLER.get(), pos, state);
      // legacy: knowledge cores are no longer used, a core left from an older version is ejected in onReady
      this.coreInventory.setFilter(new IAEItemFilter() {
         public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            return false;
         }
      });
      this.upgradeInventory = UpgradeInventories.forMachine((ItemLike)ModBlocks.ARCANE_ASSEMBLER.get(), 4, this::onChangeUpgrades);
      this.mySource = new MachineSource(this);

      for (Aspect primal : PRIMALS) {
         this.visDiscount.put(primal, this.getBaseVisCostModifier(primal));
      }

      this.storedVis.setMaxAmount(this.getMaxStoredCvis());
      this.getMainNode().setIdlePowerUsage(0.0).addService(ICraftingProvider.class, this).setFlags(new GridFlags[]{GridFlags.REQUIRE_CHANNEL});
   }

   private void calculateVisDiscounts() {
      this.flag_RecalcVis = false;

      for (Aspect primal : PRIMALS) {
         this.visDiscount.put(primal, this.getCurrentVisDiscount(primal));
      }

      this.warpPowerMultiplier = 1.0F;
      this.warpPowerMultiplier = (float)(
         (double)this.warpPowerMultiplier + (double)VisCraftingHelper.INSTANCE.calculateArmorWarp(this.armorInventory, 0, 4) * 0.15
      );
   }

   public float getDiscount(Aspect aspect) {
      return this.getCurrentVisDiscount(aspect);
   }

   public float getVisDiscountForAspect(Aspect aspect) {
      return this.visUpgrade ? 0.0F : this.getCurrentVisDiscount(aspect);
   }

   protected float getBaseVisCostModifier(Aspect aspect) {
      return VisCraftingHelper.INSTANCE.getScepterVisModifier(aspect);
   }

   protected boolean isVisUpgrade() {
      return this.visUpgrade;
   }

   private float getCurrentVisDiscount(Aspect aspect) {
      float discount = this.getBaseVisCostModifier(aspect);
      return discount - VisCraftingHelper.INSTANCE.calculateArmorDiscount(this.armorInventory, 0, 4, aspect);
   }

   public int getStoredCvis(Aspect aspect) {
      return this.storedVis.getAmount(aspect);
   }

   public void setStoredCvis(Aspect aspect, int value) {
      this.storedVis.aspects.put(aspect, Integer.valueOf(value));
   }

   public int getMaxStoredCvis() {
      return 1500;
   }

   public AspectList getStoredVis() {
      return this.storedVis;
   }

   public static CompoundTag getCraftTag() {
      CompoundTag data = new CompoundTag();
      AspectList vis = new AspectList(1500);

      for (Aspect primal : PRIMALS) {
         vis.add(primal, 1500);
      }

      vis.save(data, "stored_vis");
      return data;
   }

   private int getRequiredAmountForAspect(Aspect aspect) {
      return this.getRequiredAmountForAspect(aspect, this.currentPattern.getAspects().getAmount(aspect));
   }

   protected int getRequiredAmountForAspect(Aspect aspect, int amount) {
      return (int)Math.ceil((double)(this.getDiscount(aspect) * (float)(amount * 10)));
   }

   /** Returns empty containers (buckets, bottles...) of the consumed inputs to the ME network, or drops them. */
   private void returnContainerRemainders() {
      if (this.currentPattern == null || this.level == null) {
         return;
      }

      IGrid grid = this.getMainNode().getGrid();
      for (IInput input : this.currentPattern.getInputs()) {
         GenericStack[] possibleInputs = input.getPossibleInputs();
         if (possibleInputs.length == 0 || !(possibleInputs[0].what() instanceof AEItemKey itemKey)) {
            continue;
         }

         ItemStack consumed = itemKey.toStack(1);
         if (!consumed.getItem().hasCraftingRemainingItem(consumed)) {
            continue;
         }

         ItemStack remainder = consumed.getCraftingRemainingItem();
         AEItemKey remainderKey = AEItemKey.of(remainder);
         if (remainderKey == null) {
            continue;
         }

         long left = (long)remainder.getCount() * input.getMultiplier();
         if (grid != null) {
            left -= grid.getStorageService().getInventory().insert(remainderKey, left, Actionable.MODULATE, this.mySource);
         }

         while (left > 0L) {
            int n = (int)Math.min(left, (long)remainder.getMaxStackSize());
            net.minecraft.world.level.block.Block.popResource(this.level, this.worldPosition, remainderKey.toStack(n));
            left -= n;
         }
      }
   }

   public int ticksPerCraft() {
      return 20 - 4 * this.upgradeInventory.getInstalledUpgrades(AEItems.SPEED_CARD);
   }

   public float getPercentComplete() {
      return this.isCrafting ? Math.min((float)this.craftTickCounter / (float)this.ticksPerCraft(), 1.0F) : 0.0F;
   }

   public boolean isActive() {
      return this.isActive;
   }

   private void markForDelayedUpdate() {
      this.delayedUpdate = true;
   }

   public IUpgradeInventory getUpgradeInventory() {
      return this.upgradeInventory;
   }

   public InternalInventory getInternalInventory() {
      return this.coreInventory;
   }

   public void onChangeInventory(AppEngInternalInventory inv, int slot) {
      if (inv == this.armorInventory) {
         this.calculateVisDiscounts();
      }

      if (this.level != null && !this.level.isClientSide()) {
         this.setChanged();
         this.markForUpdate();
      }
   }

   public void onChangeUpgrades() {
      this.visUpgrade = this.upgradeInventory.getInstalledUpgrades((ItemLike)ModItems.VIS_UPGRADE.get()) > 0;
   }

   public AECableType getCableConnectionType(Direction dir) {
      return AECableType.SMART;
   }

   public List<IPatternDetails> getAvailablePatterns() {
      return this.recipeBook.patterns(this.level);
   }

   public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] keyCounters) {
      if (!this.isCrafting
         && patternDetails instanceof AssemblerPattern pattern
         && pattern.recipe().kind() == AssemblerKind.ARCANE
         && this.recipeBook.isEnabled(pattern.recipeId())) {
         AspectList aspectList = pattern.getAspects();
         if (aspectList != null) {
            for (Aspect asp : Aspect.getPrimalAspects()) {
               if (aspectList.getAmount(asp) > this.getMaxStoredCvis()) {
                  return false;
               }
            }
         }

         this.isCrafting = true;
         this.craftTickCounter = 0;
         this.currentPattern = pattern;
         GenericStack output = this.currentPattern.getPrimaryOutput();
         this.craftingInventory.setItemDirect(0, ((AEItemKey)output.what()).toStack((int)output.amount()));
         this.markForUpdate();
         return true;
      } else {
         return false;
      }
   }

   public boolean isBusy() {
      return this.isCrafting;
   }

   private TileVisNode findVisRelay() {
      if (this.level == null) {
         return null;
      }

      TileVisNode cached = this.cachedVisRelay.get();
      if (cached != null && !cached.isRemoved() && this.level.getBlockEntity(cached.getBlockPos()) == cached) {
         return cached;
      }

      for (Direction dir : Direction.values()) {
         BlockEntity neighbor = this.level.getBlockEntity(this.worldPosition.relative(dir));
         if (neighbor instanceof TileVisNode node) {
            this.cachedVisRelay = new WeakReference<>(node);
            return node;
         }
      }

      this.cachedVisRelay = new WeakReference<>(null);
      return null;
   }

   private void trickleVis() {
      if (this.level == null || this.level.isClientSide()) {
         return;
      }

      TileVisNode relay = this.findVisRelay();
      if (relay == null) {
         return;
      }

      boolean changed = false;
      int max = this.getMaxStoredCvis();
      for (Aspect primal : PRIMALS) {
         int have = this.storedVis.getAmount(primal);
         if (have < max) {
            int got = 0;
            try {
               got = relay.consumeVis(primal, Math.min(4, max - have));
            } catch (Exception ignored) {
            }

            if (got > 0) {
               this.storedVis.add(primal, got);
               changed = true;
            }
         }
      }

      if (changed) {
         this.setChanged();
         if (++this.visSyncTick >= 40) {
            this.visSyncTick = 0;
            this.markForUpdate();
         }
      }
   }

   private boolean hasRequiredVis() {
      if (this.currentPattern == null) {
         return false;
      }

      AspectList cost = this.currentPattern.getAspects();
      if (cost == null || cost.size() == 0) {
         return true;
      }

      for (Aspect aspect : cost.getAspects()) {
         int need = this.getRequiredAmountForAspect(aspect, cost.getAmount(aspect));
         if (this.storedVis.getAmount(aspect) < need) {
            return false;
         }
      }

      return true;
   }

   private void consumeRequiredVis() {
      if (this.currentPattern == null) {
         return;
      }

      AspectList cost = this.currentPattern.getAspects();
      if (cost == null) {
         return;
      }

      for (Aspect aspect : cost.getAspects()) {
         int need = this.getRequiredAmountForAspect(aspect, cost.getAmount(aspect));
         if (need > 0) {
            this.storedVis.reduce(aspect, need);
         }
      }
   }

   public static void serverTick(Level level, BlockPos pos, BlockState state, ArcaneAssemblerBlockEntity be) {
      if (level.isClientSide()) {
         return;
      }

      be.trickleVis();
      if (!be.isCrafting || be.currentPattern == null) {
         return;
      }

      if (be.getMainNode() == null || !be.getMainNode().isActive()) {
         return;
      }

      be.craftTickCounter++;
      int needed = be.ticksPerCraft();
      if (be.craftTickCounter < needed) {
         be.setChanged();
         return;
      }

      if (!be.hasRequiredVis()) {
         be.craftTickCounter = needed;
         return;
      }

      be.consumeRequiredVis();
      GenericStack output = be.currentPattern.getPrimaryOutput();
      boolean completed = false;
      try {
         IGrid grid = be.getMainNode().getGrid();
         if (grid != null) {
            MEStorage storage = grid.getStorageService().getInventory();
            long simInserted = storage.insert(output.what(), output.amount(), Actionable.SIMULATE, be.mySource);
            if (simInserted >= output.amount()) {
               long modInserted = storage.insert(output.what(), output.amount(), Actionable.MODULATE, be.mySource);
               completed = modInserted >= output.amount();
            }
         }
      } catch (Exception ignored) {
         // keep the result and retry on the next tick instead of losing it
      }

      if (!completed) {
         be.craftTickCounter = needed;
         return;
      }

      be.isCrafting = false;
      be.craftTickCounter = 0;
      be.returnContainerRemainders();
      be.currentPattern = null;
      be.craftingInventory.setItemDirect(0, ItemStack.EMPTY);
      if (level instanceof net.minecraft.server.level.ServerLevel) {
         PacketHandler.sendToAllAround(new PacketFXBlockSparkle(pos, 16777215), level, pos, 24.0);
      }

      be.setChanged();
      be.markForUpdate();
   }

   public static void clientTick(Level level, BlockPos pos, BlockState state, ArcaneAssemblerBlockEntity be) {
      be.clientTick();
   }

   private void clientTick() {
      if (this.flag_RecalcVis) {
         this.calculateVisDiscounts();
      }

      if (this.isCrafting && this.craftTickCounter < 20) {
         this.craftTickCounter++;
      }

      if (this.isCrafting && this.level != null) {
         double px = (double)this.worldPosition.getX();
         double py = (double)this.worldPosition.getY();
         double pz = (double)this.worldPosition.getZ();
         if ((this.craftTickCounter & 1) == 0) {
            ThaumcraftApi.getSetup().blockRunes(this.level, px, py + 1.0, pz, 0.7F, 0.9F, 1.0F, 20, -0.02F);
         }

         if (++this.clientFxTick >= 6) {
            this.clientFxTick = 0;
            Aspect dominant = null;
            int most = 0;
            for (Aspect primal : PRIMALS) {
               int have = this.storedVis.getAmount(primal);
               if (have > most) {
                  most = have;
                  dominant = primal;
               }
            }

            if (dominant != null) {
               int color = dominant.getColor();
               ThaumcraftApi.getSetup()
                  .blockRunes(
                     this.level,
                     px,
                     py + 1.0,
                     pz,
                     (float)(color >> 16 & 0xFF) / 255.0F,
                     (float)(color >> 8 & 0xFF) / 255.0F,
                     (float)(color & 0xFF) / 255.0F,
                     20,
                     -0.02F
                  );
            }
         }
      }
   }

   public void onReady() {
      super.onReady();
      if (this.currentRecipeId != null) {
         AssemblerRecipe recipe = this.recipeBook.recipes(this.level).get(this.currentRecipeId);
         if (recipe != null) {
            this.currentPattern = new AssemblerPattern(recipe);
         } else {
            this.isCrafting = false;
         }

         this.currentRecipeId = null;
      }

      if (this.level != null && !this.level.isClientSide()) {
         AssemblerRecipes.track(this);
         ItemStack legacyCore = this.coreInventory.getStackInSlot(0);
         if (!legacyCore.isEmpty()) {
            Block.popResource(this.level, this.worldPosition, legacyCore.copy());
            this.coreInventory.setItemDirect(0, ItemStack.EMPTY);
         }
      }

      this.calculateVisDiscounts();
   }

   @Override
   public void setRemoved() {
      super.setRemoved();
      AssemblerRecipes.untrack(this);
   }

   @Override
   public void onChunkUnloaded() {
      super.onChunkUnloaded();
      AssemblerRecipes.untrack(this);
   }

   @Override
   public AssemblerRecipes recipeBook() {
      return this.recipeBook;
   }

   public void onBreak() {
      this.isCrafting = false;
   }

   protected boolean readFromStream(RegistryFriendlyByteBuf data) {
      boolean changed = super.readFromStream(data);
      boolean newActive = data.readBoolean();
      if (newActive != this.isActive) {
         this.isActive = newActive;
         changed = true;
      }

      boolean newCrafting = data.readBoolean();
      if (newCrafting != this.isCrafting) {
         this.isCrafting = newCrafting;
         changed = true;
      }

      if (this.isCrafting) {
         this.craftTickCounter = data.readInt();
         if (this.level != null) {
            for (int i = 0; i < 2; i++) {
               ThaumcraftApi.getSetup()
                  .blockRunes(
                     this.level,
                     (double)this.worldPosition.getX(),
                     (double)this.worldPosition.getY(),
                     (double)this.worldPosition.getZ(),
                     0.5F,
                     0.0F,
                     0.5F,
                     10,
                     -0.1F
                  );
            }
         }
      }

      this.storedVis.aspects.clear();

      for (Aspect primal : PRIMALS) {
         this.storedVis.add(primal, data.readInt());
      }

      this.visUpgrade = data.readBoolean();
      this.recipeBook.read(data);
      return changed;
   }

   protected void writeToStream(RegistryFriendlyByteBuf data) {
      super.writeToStream(data);
      data.writeBoolean(this.isActive());
      data.writeBoolean(this.isCrafting);
      if (this.isCrafting) {
         data.writeInt(this.craftTickCounter);
      }

      for (Aspect primal : PRIMALS) {
         data.writeInt(this.storedVis.getAmount(primal));
      }

      data.writeBoolean(this.visUpgrade);
      this.recipeBook.write(data);
   }

   public void loadTag(CompoundTag data, Provider registries) {
      super.loadTag(data, registries);
      this.armorInventory.readFromNBT(data, "armors", registries);
      if (data.contains("vis_interface")) {
         this.visSourceInfo.readFromNBT(data, "vis_interface");
      }

      this.readVisLevelsFromNBT(data);
      if (data.contains("visUpgrade")) {
         this.visUpgrade = data.getBoolean("visUpgrade");
      }

      this.upgradeInventory.readFromNBT(data, "upgrades", registries);
      if (data.contains("isCrafting")) {
         this.isCrafting = data.getBoolean("isCrafting");
      }

      this.recipeBook.load(data);
      this.currentRecipeId = data.contains("recipe") ? ResourceLocation.tryParse(data.getString("recipe")) : null;
   }

   public void saveAdditional(CompoundTag data, Provider registries) {
      super.saveAdditional(data, registries);
      this.armorInventory.writeToNBT(data, "armors", registries);
      this.visSourceInfo.writeToNBT(data, "vis_interface");
      this.writeVisLevelsToNBT(data);
      data.putBoolean("visUpgrade", this.visUpgrade);
      this.upgradeInventory.writeToNBT(data, "upgrades", registries);
      data.putBoolean("isCrafting", this.isCrafting);
      this.recipeBook.save(data);
      if (this.currentPattern != null) {
         data.putString("recipe", this.currentPattern.recipeId().toString());
      }
   }

   public void readVisLevelsFromNBT(CompoundTag data) {
      this.storedVis.setMaxAmount(this.getMaxStoredCvis());
      if (data.contains("stored_vis")) {
         this.storedVis.load(data, "stored_vis");
      }
   }

   public void writeVisLevelsToNBT(CompoundTag data) {
      if (this.storedVis.size() > 0) {
         this.storedVis.save(data, "stored_vis");
      }
   }

   public void setOwner(Player player) {
      this.getMainNode().setOwningPlayer(player);
   }

   public Component getDisplayName() {
      return Component.translatable("block.thaumicenergistics.arcane_assembler");
   }

   @Nullable
   public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
      return new ArcaneAssemblerMenu(containerId, inventory, this);
   }
}
