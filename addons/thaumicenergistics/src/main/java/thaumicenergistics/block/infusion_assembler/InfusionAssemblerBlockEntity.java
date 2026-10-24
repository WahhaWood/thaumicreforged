package thaumicenergistics.block.infusion_assembler;

import appeng.api.crafting.IPatternDetails;
import appeng.api.inventories.InternalInventory;
import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.storage.MEStorage;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.AEItemKey;
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
import java.util.EnumSet;
import java.util.List;
import thaumicenergistics.ae2.recipe.AssemblerKind;
import thaumicenergistics.ae2.recipe.AssemblerPattern;
import thaumicenergistics.ae2.recipe.AssemblerRecipe;
import thaumicenergistics.ae2.recipe.AssemblerRecipes;
import thaumicenergistics.ae2.recipe.RecipeAssembler;
import thaumicenergistics.init.ModBlockEntities;
import thaumicenergistics.init.ModBlocks;
import thaumicenergistics.menu.InfusionAssemblerMenu;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.network.PacketHandler;
import thaumcraft.network.fx.PacketFXBlockSparkle;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

public class InfusionAssemblerBlockEntity extends AENetworkedInvBlockEntity implements ICraftingProvider, MenuProvider, RecipeAssembler {
   private static final String NBTKEY_UPGRADES = "upgradeCount";
   private static final String NBTKEY_UPGRADEINV = "upgrades";
   private static final String NBTKEY_CRAFTING = "isCrafting";
   private static final String NBTKEY_CRAFTING_PATTERN = "pattern";
   private static final String NBTKEY_ROTATION = "rotation";
   private static final int BASE_TICKS_PER_CRAFT = 20;
   private static final double ACTIVE_POWER = 8.0;
   public static final int MAX_SPEED_UPGRADES = 4;
   public final AppEngInternalInventory coreInventory = new AppEngInternalInventory(this, 1);
   public final AppEngInternalInventory craftingInventory = new AppEngInternalInventory(this, 1);
   private final IUpgradeInventory upgradeInventory;
   public boolean isCrafting = false;
   private AssemblerPattern currentPattern = null;
   private ResourceLocation currentRecipeId = null;
   private final AssemblerRecipes recipeBook = new AssemblerRecipes(AssemblerKind.INFUSION);
   public int craftTickCounter = 0;
   private int delayTickCounter = 0;
   private final MachineSource mySource;
   private boolean isActive = false;
   private int upgradeCount = 0;
   private float warpPowerMultiplier = 1.0F;
   private boolean delayedUpdate = false;
   private int rotation;
   private final AspectList clientCostAspects = new AspectList();
   private int clientFxTick = 0;

   public InfusionAssemblerBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)ModBlockEntities.INFUSION_ASSEMBLER.get(), pos, state);
      this.coreInventory.setMaxStackSize(0, 1);
      // legacy: cores are no longer used, a core left from an older version is ejected in onReady
      this.coreInventory.setFilter(new IAEItemFilter() {
         public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            return false;
         }
      });
      this.upgradeInventory = UpgradeInventories.forMachine((ItemLike)ModBlocks.INFUSION_ASSEMBLER.get(), 4, this::onChangeUpgrades);
      this.mySource = new MachineSource(this);
      this.getMainNode()
         .setIdlePowerUsage(0.0)
         .setExposedOnSides(EnumSet.of(Direction.DOWN, Direction.SOUTH, Direction.NORTH, Direction.WEST, Direction.EAST))
         .addService(ICraftingProvider.class, this)
         .setFlags(new GridFlags[]{GridFlags.REQUIRE_CHANNEL});
   }

   public boolean isActive() {
      if (this.level != null && !this.level.isClientSide()) {
         this.isActive = this.getMainNode().isActive();
      }

      return this.isActive;
   }

   private void markForDelayedUpdate() {
      this.delayedUpdate = true;
   }

   public int ticksPerCraft() {
      return 100 - 4 * this.upgradeCount;
   }

   public float getPercentComplete() {
      return this.isCrafting ? Math.min((float)this.craftTickCounter / (float)this.ticksPerCraft(), 1.0F) : 0.0F;
   }

   public IUpgradeInventory getUpgradeInventory() {
      return this.upgradeInventory;
   }

   public int getUpgradeCount() {
      return this.getSpeedUpgradeCount();
   }

   private int getSpeedUpgradeCount() {
      return this.upgradeInventory.getInstalledUpgrades(AEItems.SPEED_CARD);
   }

   private void onChangeUpgrades() {
      this.upgradeCount = this.getSpeedUpgradeCount();
      if (this.level != null && !this.level.isClientSide()) {
         this.setChanged();
         this.markForUpdate();
      }
   }

   public InternalInventory getInternalInventory() {
      return this.coreInventory;
   }

   public void onChangeInventory(AppEngInternalInventory inv, int slot) {
      if (inv == this.upgradeInventory) {
         this.onChangeUpgrades();
      }

      if (this.level != null && !this.level.isClientSide()) {
         this.setChanged();
         this.markForUpdate();
      }
   }

   public AECableType getCableConnectionType(Direction dir) {
      return AECableType.SMART;
   }

   public List<IPatternDetails> getAvailablePatterns() {
      return this.recipeBook.patterns(this.level);
   }

   public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] keyCounters) {
      if (!this.isCrafting
         && patternDetails instanceof AssemblerPattern infusionPattern
         && infusionPattern.recipe().kind() == AssemblerKind.INFUSION
         && this.recipeBook.isEnabled(infusionPattern.recipeId())) {
         this.isCrafting = true;
         this.craftTickCounter = 0;
         this.currentPattern = infusionPattern;
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

   public static void serverTick(Level level, BlockPos pos, BlockState state, InfusionAssemblerBlockEntity be) {
      if (level.isClientSide()) {
         return;
      }

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
      be.currentPattern = null;
      be.craftingInventory.setItemDirect(0, ItemStack.EMPTY);
      if (level instanceof net.minecraft.server.level.ServerLevel) {
         PacketHandler.sendToAllAround(new PacketFXBlockSparkle(pos, 16777215), level, pos, 24.0);
      }

      be.setChanged();
      be.markForUpdate();
   }

   public static void clientTick(Level level, BlockPos pos, BlockState state, InfusionAssemblerBlockEntity be) {
      be.clientTick();
   }

   private void clientTick() {
      if (this.isCrafting && this.craftTickCounter < 20) {
         this.craftTickCounter++;
      }

      if (this.isCrafting && this.level != null) {
         double px = (double)this.worldPosition.getX();
         double py = (double)this.worldPosition.getY();
         double pz = (double)this.worldPosition.getZ();
         float r = 0.5F + this.level.random.nextFloat() * 0.2F;
         ThaumcraftApi.getSetup()
            .blockRunes(this.level, px, py + 1.0, pz, r, 0.1F, 0.7F + this.level.random.nextFloat() * 0.3F, 25, -0.03F);
         if (++this.clientFxTick >= 6) {
            this.clientFxTick = 0;
            Aspect[] cost = this.clientCostAspects.getAspects();
            if (cost.length > 0) {
               Aspect aspect = cost[(int)(this.level.random.nextFloat() * cost.length) % cost.length];
               int color = aspect.getColor();
               float red = (float)(color >> 16 & 0xFF) / 255.0F;
               float green = (float)(color >> 8 & 0xFF) / 255.0F;
               float blue = (float)(color & 0xFF) / 255.0F;
               ThaumcraftApi.getSetup().blockRunes(this.level, px, py + 1.0, pz, red, green, blue, 20, -0.02F);
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

      this.upgradeCount = data.readInt();
      int newRotation = data.readByte();
      if (newRotation != this.rotation) {
         this.rotation = newRotation;
         changed = true;
      }

      this.clientCostAspects.aspects.clear();
      if (data.readBoolean()) {
         int costCount = data.readInt();
         for (int i = 0; i < costCount; i++) {
            Aspect aspect = Aspect.getAspect(data.readUtf());
            int amount = data.readInt();
            if (aspect != null && amount > 0) {
               this.clientCostAspects.add(aspect, amount);
            }
         }
      }

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

      data.writeInt(this.upgradeCount);
      data.writeByte(this.rotation);
      AspectList cost = this.currentPattern != null ? this.currentPattern.getAspects() : null;
      if (cost != null && cost.size() > 0) {
         data.writeBoolean(true);
         data.writeInt(cost.size());
         for (Aspect aspect : cost.getAspects()) {
            data.writeUtf(aspect.getTag());
            data.writeInt(cost.getAmount(aspect));
         }
      } else {
         data.writeBoolean(false);
      }

      this.recipeBook.write(data);
   }

   public void loadTag(CompoundTag data, Provider registries) {
      super.loadTag(data, registries);
      if (data.contains("upgradeCount")) {
         this.upgradeCount = data.getInt("upgradeCount");
      }

      this.upgradeInventory.readFromNBT(data, "upgrades", registries);
      this.recipeBook.load(data);
      this.currentRecipeId = data.contains("recipe") ? ResourceLocation.tryParse(data.getString("recipe")) : null;

      if (data.contains("isCrafting")) {
         this.isCrafting = data.getBoolean("isCrafting");
      }

      this.rotation = data.getInt("rotation");
   }

   public void saveAdditional(CompoundTag data, Provider registries) {
      super.saveAdditional(data, registries);
      data.putInt("upgradeCount", this.upgradeCount);
      this.upgradeInventory.writeToNBT(data, "upgrades", registries);
      this.recipeBook.save(data);
      if (this.currentPattern != null) {
         data.putString("recipe", this.currentPattern.recipeId().toString());
      }

      data.putBoolean("isCrafting", this.isCrafting);
      data.putInt("rotation", this.rotation);
   }

   public Component getDisplayName() {
      return Component.translatable("block.thaumicenergistics.infusion_assembler");
   }

   @Nullable
   public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
      return new InfusionAssemblerMenu(containerId, inventory, this);
   }

   public void setOwner(Player player) {
      this.getMainNode().setOwningPlayer(player);
   }

   public void setRotation(int rotation) {
      this.rotation = rotation;
      if (this.level != null && !this.level.isClientSide()) {
         this.setChanged();
         this.markForUpdate();
      }
   }

   public int getRotation() {
      return this.rotation;
   }
}
