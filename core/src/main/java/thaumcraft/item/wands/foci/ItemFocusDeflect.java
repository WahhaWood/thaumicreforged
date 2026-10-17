package thaumcraft.item.wands.foci;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
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

public class ItemFocusDeflect extends ItemFocusBasic {
   private static final AspectList visUsage = new AspectList().add(Aspect.ORDER, 8).add(Aspect.AIR, 4);
   public static final List<Class<?>> deflectBlacklist = new ArrayList<>();

   public ItemFocusDeflect() {
      super("deflect");
   }

   @Override
   public String getSortingHelper(ItemStack itemstack) {
      return "DEFLECT" + super.getSortingHelper(itemstack);
   }

   @Override
   public int getFocusColor(ItemStack stack) {
      return 16777215;
   }

   @Override
   public AspectList getVisCost(ItemStack stack) {
      // Возвращаем базовую цену без скидки frugal: её уже применяет
      // consumeAllVis через getConsumptionModifier. Ручной пересчёт здесь
      // давал бы скидку дважды.
      return visUsage.copy();
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
      // Цену читаем из стака фокуса: там лежат апгрейды, в стаке жезла их не видно.
      ItemStack focusStack = wand.getFocusItem(wandStack);
      if (wand.consumeAllVis(wandStack, player, this.getVisCost(focusStack), true, false)) {
         protectFromProjectiles(player);
      }
   }

   public static void protectFromProjectiles(Player player) {
      Level level = player.level();
      AABB aabb = new AABB(player.getX() - 4.0, player.getY() - 4.0, player.getZ() - 4.0, player.getX() + 3.0, player.getY() + 3.0, player.getZ() + 3.0);

      for (Entity e : level.getEntitiesOfClass(Entity.class, aabb, ex -> ex instanceof Projectile && !isBlacklisted(ex))) {
         if (!(e instanceof Projectile proj) || proj.getOwner() != player) {
            Vec3 diff = new Vec3(e.getX() - player.getX(), e.getY() - player.getY(), e.getZ() - player.getZ());
            Vec3 motion = e.getDeltaMovement().normalize().scale(diff.length() * 2.0);

            for (int i = 0; i < 6; i++) {
               Thaumcraft.setup.sparkle((float)e.getX(), (float)e.getY(), (float)e.getZ(), 6);
            }

            e.setPos(e.getX() + motion.x, e.getY() + motion.y, e.getZ() + motion.z);
         }
      }
   }

   private static boolean isBlacklisted(Entity entity) {
      Class<? extends Entity> clazz = (Class<? extends Entity>)entity.getClass();
      if (deflectBlacklist.contains(clazz)) {
         return true;
      } else {
         for (Class<?> testClass : deflectBlacklist) {
            if (testClass.isInterface() && testClass.isAssignableFrom(clazz)) {
               return true;
            }
         }

         return false;
      }
   }

   public static void setupBlackList() {
      deflectBlacklist.add(ThrownExperienceBottle.class);
   }

   @Override
   public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack itemStack, int rank) {
      return switch (rank) {
         case 1, 2, 3, 4, 5 -> new FocusUpgradeType[]{FocusUpgradeType.frugal};
         default -> null;
      };
   }
}
