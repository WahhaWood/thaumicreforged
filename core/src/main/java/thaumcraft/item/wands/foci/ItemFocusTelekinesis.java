package thaumcraft.item.wands.foci;

import java.util.List;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.item.wands.ItemWandCasting;

public class ItemFocusTelekinesis extends ItemFocusBasic {
   private static final AspectList visUsage = new AspectList().add(Aspect.AIR, 5).add(Aspect.ENTROPY, 5);

   public ItemFocusTelekinesis() {
      super("telekinesis");
   }

   @Override
   public String getSortingHelper(ItemStack itemstack) {
      return "TELEKINESIS" + super.getSortingHelper(itemstack);
   }

   @Override
   public int getFocusColor(ItemStack stack) {
      return 10223806;
   }

   @Override
   public AspectList getVisCost(ItemStack stack) {
      return visUsage;
   }

   @Override
   public boolean isVisCostPerTick(ItemStack stack) {
      return true;
   }

   @Override
   public ItemFocusBasic.WandFocusAnimation getAnimation(ItemStack stack) {
      return ItemFocusBasic.WandFocusAnimation.CHARGE;
   }

   @Override
   public ItemStack onFocusRightClick(ItemStack wandStack, Level level, Player player, InteractionHand hand) {
      player.startUsingItem(hand);
      return wandStack;
   }

   @Override
   public void onUsingFocusTick(ItemStack wandStack, Player player, int ticks) {
      ItemWandCasting wand = (ItemWandCasting)wandStack.getItem();
      Level level = player.level();
      if (!wand.consumeAllVis(wandStack, player, this.getVisCost(wandStack), false, false)) {
         player.stopUsingItem();
      } else {
         Vec3 eyePos = player.getEyePosition();
         Vec3 lookVec = player.getViewVector(1.0F);
         int range = 6;
         double distance = 5.0;
         Vec3 target;
         if (!player.isShiftKeyDown()) {
            target = eyePos.add(lookVec.scale(5.0));
         } else {
            target = player.position().add(0.0, 0.5, 0.0);
         }

         AABB aabb = new AABB(target.x - 6.0, target.y - 6.0, target.z - 6.0, target.x + 6.0, target.y + 6.0, target.z + 6.0);
         List<ItemEntity> entities = level.getEntitiesOfClass(ItemEntity.class, aabb);
         if (!entities.isEmpty() && wand.consumeAllVis(wandStack, player, this.getVisCost(wandStack), true, false)) {
            for (ItemEntity item : entities) {
               Vec3 direction = target.subtract(item.position()).normalize().scale(0.3333);
               item.setDeltaMovement(direction);
               if (level.isClientSide) {
                  Thaumcraft.setup.sparkle((float)item.getX(), (float)item.getY(), (float)item.getZ(), 0);
               }
            }
         }
      }
   }

   @Override
   public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack itemStack, int rank) {
      return switch (rank) {
         case 1, 2, 3, 4, 5 -> new FocusUpgradeType[]{FocusUpgradeType.frugal};
         default -> null;
      };
   }
}
