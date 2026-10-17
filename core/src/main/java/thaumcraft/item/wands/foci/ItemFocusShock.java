package thaumcraft.item.wands.foci;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.particles.fx.bolt.FXLightningBolt;
import thaumcraft.util.BlockUtils;
import thaumcraft.util.EntityUtils;

public class ItemFocusShock extends ItemFocusBasic {
   private static final AspectList costBase = new AspectList().add(Aspect.AIR, 25);
   private static final AspectList costChain = new AspectList().add(Aspect.AIR, 40).add(Aspect.WATER, 10);
   private static final AspectList costGround = new AspectList().add(Aspect.AIR, 75).add(Aspect.EARTH, 25);
   public static FocusUpgradeType chainlightning = new FocusUpgradeType(
      17,
      Thaumcraft.location("textures/foci/chainlightning.png"),
      "focus.upgrade.chainlightning.name",
      "focus.upgrade.chainlightning.text",
      new AspectList().add(Aspect.WEATHER, 1)
   );
   public static FocusUpgradeType earthshock = new FocusUpgradeType(
      18,
      Thaumcraft.location("textures/foci/earthshock.png"),
      "focus.upgrade.earthshock.name",
      "focus.upgrade.earthshock.text",
      new AspectList().add(Aspect.WEATHER, 1)
   );

   public ItemFocusShock() {
      super("shock");
   }

   @Override
   public String getSortingHelper(ItemStack itemstack) {
      return "BL" + super.getSortingHelper(itemstack);
   }

   @Override
   public int getFocusColor(ItemStack itemstack) {
      return 10466239;
   }

   @Override
   public AspectList getVisCost(ItemStack itemstack) {
      return this.isUpgradedWith(itemstack, chainlightning) ? costChain : (this.isUpgradedWith(itemstack, earthshock) ? costGround : costBase);
   }

   @Override
   public int getActivationCooldown(ItemStack focusstack) {
      return this.isUpgradedWith(focusstack, chainlightning) ? 500 : (this.isUpgradedWith(focusstack, earthshock) ? 1000 : 250);
   }

   @Override
   public ItemFocusBasic.WandFocusAnimation getAnimation(ItemStack itemstack) {
      return this.isUpgradedWith(itemstack, earthshock) ? ItemFocusBasic.WandFocusAnimation.WAVE : ItemFocusBasic.WandFocusAnimation.CHARGE;
   }

   @OnlyIn(Dist.CLIENT)
   public static void shootLightning(Level level, LivingEntity caster, double xx, double yy, double zz, boolean offset) {
      double px = caster.getX();
      double py = caster.getEyeY() - 0.3;
      double pz = caster.getZ();
      float yaw = caster.getYRot();
      px += -Mth.cos(yaw * (float) Math.PI / 180.0F) * 0.06F;
      pz += -Mth.sin(yaw * (float) Math.PI / 180.0F) * 0.06F;
      py -= 0.06;
      Vec3 look = caster.getLookAngle();
      px += look.x * 0.3;
      py += look.y * 0.3;
      pz += look.z * 0.3;
      FXLightningBolt bolt = new FXLightningBolt((ClientLevel)level, px, py, pz, xx, yy, zz, level.random.nextLong(), 6, 0.5F, 8);
      bolt.defaultFractal();
      bolt.setType(2);
      bolt.setWidth(0.125F);
      bolt.finalizeBolt();
   }

   @Override
   public ItemStack onFocusRightClick(ItemStack wandStack, Level level, Player player, InteractionHand hitResult) {
      ItemWandCasting wand = (ItemWandCasting)wandStack.getItem();
      InteractionHand hand = player.getMainHandItem() == wandStack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
      if (this.isUpgradedWith(wand.getFocusItem(wandStack), earthshock)) {
         // Цену читаем из стака фокуса: там лежат апгрейды, в стаке жезла их не видно.
         ItemStack focusStack = wand.getFocusItem(wandStack);
         if (wand.consumeAllVis(wandStack, player, this.getVisCost(focusStack), !level.isClientSide, false)) {
            // Порт 1.7.10: сфера землетрясения с учётом апгрейдов enlarge/potency.
            if (!level.isClientSide) {
               thaumcraft.entities.brainless.projectile.shock_orb.EntityShockOrb orb =
                  new thaumcraft.entities.brainless.projectile.shock_orb.EntityShockOrb(level, player);
               orb.area = orb.area
                  + this.getUpgradeLevel(wand.getFocusItem(wandStack), FocusUpgradeType.enlarge) * 2;
               orb.damage = (int)(orb.damage + (double)wand.getFocusPotency(wandStack) * 1.33);
               if (level.addFreshEntity(orb)) {
                  level.playSound(
                     null, orb.getX(), orb.getY(), orb.getZ(),
                     thaumcraft.init.registry.ModSounds.ZAP.get(), net.minecraft.sounds.SoundSource.PLAYERS,
                     1.0F, 1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F
                  );
               }
            }

            player.swing(hand);
         }
      } else {
         player.startUsingItem(hand);
      }

      return wandStack;
   }

   @Override
   public void onUsingFocusTick(ItemStack stack, Player player, int count) {
      this.doLightningBolt(stack, player, count);
   }

   public void doLightningBolt(ItemStack stack, Player player, int count) {
      ItemWandCasting wand = (ItemWandCasting)stack.getItem();
      Level level = player.level();
      // Цену читаем из стака фокуса: там лежат апгрейды, в стаке жезла их не видно.
      ItemStack focusStack = wand.getFocusItem(stack);
      if (!wand.consumeAllVis(stack, player, this.getVisCost(focusStack), !level.isClientSide, false)) {
         player.stopUsingItem();
      } else {
         int potency = wand.getFocusPotency(stack);
         Entity pointedEntity = EntityUtils.getPointedEntity(player, 0.0, 20.0, 1.1F);
         if (level.isClientSide) {
            BlockHitResult unused = BlockUtils.getTargetBlock(level, player, 32.0);
            Vec3 _x = player.getLookAngle();
            double _xx = player.getX() + _x.x * 10.0;
            double _xxx = player.getY() + _x.y * 10.0;
            double _xxxx = player.getZ() + _x.z * 10.0;
            if (unused != null && unused.getType() != Type.MISS) {
               Vec3 _xxxxx = unused.getLocation();
               _xx = _xxxxx.x;
               _xxx = _xxxxx.y;
               _xxxx = _xxxxx.z;

               for (int _xxxxxx = 0; _xxxxxx < 5; _xxxxxx++) {
                  Thaumcraft.setup
                     .sparkle(
                        (float)_xx + (level.random.nextFloat() - level.random.nextFloat()) * 0.3F,
                        (float)_xxx + (level.random.nextFloat() - level.random.nextFloat()) * 0.3F,
                        (float)_xxxx + (level.random.nextFloat() - level.random.nextFloat()) * 0.3F,
                        2.0F + level.random.nextFloat(),
                        2,
                        0.05F + level.random.nextFloat() * 0.05F
                     );
               }
            }

            if (pointedEntity != null) {
               _xx = pointedEntity.getX();
               _xxx = pointedEntity.getBoundingBox().minY + pointedEntity.getBbHeight() / 2.0F;
               _xxxx = pointedEntity.getZ();

               for (int var20 = 0; var20 < 5; var20++) {
                  Thaumcraft.setup
                     .sparkle(
                        (float)_xx + (level.random.nextFloat() - level.random.nextFloat()) * 0.6F,
                        (float)_xxx + (level.random.nextFloat() - level.random.nextFloat()) * 0.6F,
                        (float)_xxxx + (level.random.nextFloat() - level.random.nextFloat()) * 0.6F,
                        2.0F + level.random.nextFloat(),
                        2,
                        0.05F + level.random.nextFloat() * 0.05F
                     );
               }
            }

            shootLightning(level, player, _xx, _xxx, _xxxx, true);
         }

         SoundEvent shock = (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("thaumcraft", "shock"));
         if (shock != null) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), shock, SoundSource.PLAYERS, 0.25F, 1.0F);
         }

         // Серверная ветка оригинала: урон по указанной сущности и цепная молния.
         // Раньше её не было — на сервере фокус только играл звук и не бил.
         if (!level.isClientSide && pointedEntity instanceof LivingEntity living) {
            boolean pvpAllowed = level.getServer() == null || level.getServer().isPvpAllowed();
            boolean isPlayerTarget = living instanceof net.minecraft.world.entity.player.Player;
            if (!isPlayerTarget || pvpAllowed) {
               int cl = this.getUpgradeLevel(wand.getFocusItem(stack), chainlightning) * 2;
               living.hurt(
                  level.damageSources().playerAttack((net.minecraft.world.entity.player.Player)player),
                  (cl > 0 ? 6 : 4) + potency
               );
               // chainlightning: перескок на cl целей в радиусе 8, по 4 + potency каждая
               while (cl > 0) {
                  cl--;
                  cl += this.getUpgradeLevel(wand.getFocusItem(stack), FocusUpgradeType.enlarge) * 2;
                  LivingEntity center = living;
                  LivingEntity closest = null;
                  double best = Double.MAX_VALUE;
                  for (LivingEntity e : level.getEntitiesOfClass(
                     LivingEntity.class,
                     new AABB(center.getX() - 8.0, center.getY() - 8.0, center.getZ() - 8.0,
                        center.getX() + 8.0, center.getY() + 8.0, center.getZ() + 8.0),
                     ex -> ex != center && ex != player && !(ex instanceof net.minecraft.world.entity.player.Player) && ex.isAlive()
                  )) {
                     double dd = e.distanceToSqr(center);
                     if (dd < best) {
                        closest = e;
                        best = dd;
                     }
                  }

                  if (closest == null) {
                     break;
                  }

                  thaumcraft.network.PacketHandler.sendToAllAround(
                     new thaumcraft.network.fx.PacketFXZap(center.getId(), closest.getId()),
                     level, closest.blockPosition(), 64.0
                  );
                  closest.hurt(
                     level.damageSources().playerAttack((net.minecraft.world.entity.player.Player)player), 4 + potency
                  );
                  living = closest;
               }
            }
         }
      }
   }

   @Override
   public boolean canApplyUpgrade(ItemStack focusstack, Player player, FocusUpgradeType type, int rank) {
      return !type.equals(FocusUpgradeType.enlarge) || this.isUpgradedWith(focusstack, chainlightning) || this.isUpgradedWith(focusstack, earthshock);
   }

   @Override
   public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack itemstack, int rank) {
      return switch (rank) {
         case 1, 2 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency};
         case 3 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, chainlightning, earthshock};
         case 4, 5 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, FocusUpgradeType.enlarge};
         default -> null;
      };
   }
}
