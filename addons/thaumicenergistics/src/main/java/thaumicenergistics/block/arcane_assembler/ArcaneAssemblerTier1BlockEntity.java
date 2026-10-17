package thaumicenergistics.block.arcane_assembler;

import appeng.core.definitions.AEItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.aspects.Aspect;

public class ArcaneAssemblerTier1BlockEntity extends ArcaneAssemblerBlockEntity {
   private static final int BASE_TICKS_PER_CRAFT = 200;
   private static final int MAX_STORED_VIS = 1500;
   private static final float BASE_VIS_COST_MODIFIER = 0.8F;

   public ArcaneAssemblerTier1BlockEntity(BlockPos pos, BlockState state) {
      super(pos, state);
   }

   @Override
   public int getMaxStoredCvis() {
      return 15000;
   }

   @Override
   public int ticksPerCraft() {
      int upgrades = this.getUpgradeInventory().getInstalledUpgrades(AEItems.SPEED_CARD);
      return Math.max(1, 200 / (1 + upgrades));
   }

   @Override
   protected float getBaseVisCostModifier(Aspect aspect) {
      return 0.8F;
   }

   @Override
   public Component getDisplayName() {
      return Component.translatable("block.thaumicenergistics.arcane_assembler_1");
   }
}
