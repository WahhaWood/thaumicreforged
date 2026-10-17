package thaumcraft.block.vis_charged_relay;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.block.arcane_workbench.MagicWorkbenchBlockEntity;
import thaumcraft.block.vis_relay.VisRelayBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.item.wands.ItemWandCasting;

/** TC4 TileMagicWorkbenchCharger: a relay on an arcane workbench tops up the wand in it from the vis network. */
public class VisChargeRelayBlockEntity extends VisRelayBlockEntity {
   /** The workbench's wand slot (TC4 slot 10, ours 9). */
   private static final int WAND_SLOT = 9;

   public VisChargeRelayBlockEntity(BlockPos blockPos, BlockState blockState) {
      super((BlockEntityType<?>)ModBlockEntities.VIS_CHARGE_RELAY.get(), blockPos, blockState);
   }

   @Override
   public boolean isSource() {
      return false;
   }

   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      super.serverTick(level, pos, state);
      if (!(level.getBlockEntity(pos.below()) instanceof MagicWorkbenchBlockEntity workbench)) {
         return;
      }

      ItemStack wand = workbench.getItem(WAND_SLOT);
      if (!(wand.getItem() instanceof ItemWandCasting wandItem)) {
         return;
      }

      boolean charged = false;
      for (Aspect aspect : wandItem.getAspectsWithRoom(wand).getAspects()) {
         int drain = Math.min(5, wandItem.getMaxVis(wand) - wandItem.getVis(wand, aspect));
         if (drain > 0) {
            int got = this.consumeVis(aspect, drain);
            if (got > 0) {
               wandItem.addRealVis(wand, aspect, got, true);
               charged = true;
            }
         }
      }

      if (charged) {
         // The arcane recipe output depends on the wand's vis.
         workbench.setChanged();
      }
   }
}
