package thaumcraft.item.equipment;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.api.IRepairable;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.entities.brainless.following.FollowingItemEntity;
import thaumcraft.init.registry.Config;
import thaumcraft.item.equipment.ichor.IchorToolHelper;

/**
 * Топор потока (Axe of the Stream) — порт {@code ItemElementalAxe} из TC4 1.7.10.
 * Срубает всё дерево (от дальних брёвен) и притягивает дроп; ПКМ (удержание) — вихрь, собирающий предметы.
 */
public class ItemElementalAxe extends AxeItem implements IRepairable {
   public ItemElementalAxe() {
      super(
         ThaumcraftApi.TOOL_MATERIAL_ELEMENTAL,
         new Properties().durability(1500).rarity(Rarity.RARE).attributes(DiggerItem.createAttributes(ThaumcraftApi.TOOL_MATERIAL_ELEMENTAL, 5.0F, -3.0F))
      );
   }

   @Override
   public boolean mineBlock(@NotNull ItemStack stack, @NotNull Level level, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull LivingEntity miningEntity) {
      // Порт onBlockStartBreak: без Shift срубаем всё дерево целиком.
      if (!level.isClientSide && miningEntity instanceof Player player && !player.isShiftKeyDown() && state.is(net.minecraft.tags.BlockTags.LOGS)) {
         IchorToolHelper.fellTree(level, player, stack, pos, state);
         level.playSound(null, pos, thaumcraft.init.registry.ModSounds.BUBBLE.get(), SoundSource.BLOCKS, 0.15F, 1.0F);
         stack.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
      }

      return super.mineBlock(stack, level, state, pos, miningEntity);
   }

   public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
      return 72000;
   }

   @NotNull
   public UseAnim getUseAnimation(@NotNull ItemStack stack) {
      return UseAnim.BOW;
   }

   @NotNull
   public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      player.startUsingItem(hand);
      return InteractionResultHolder.consume(stack);
   }

   public void onUseTick(@NotNull Level level, @NotNull LivingEntity entity, @NotNull ItemStack stack, int count) {
      if (!(entity instanceof Player player)) {
         return;
      }

      double radius = Config.elementalAxeFindDropRadius;
      AABB box = player.getBoundingBox().inflate(radius);
      List<ItemEntity> items = level.getEntitiesOfClass(
         ItemEntity.class, box, e -> e.isAlive() && !(e instanceof FollowingItemEntity fe && fe.target != null)
      );

      for (ItemEntity item : items) {
         if (!level.isClientSide) {
            // Порт ItemElementalAxe.onUsingTick: притягиваем дроп к игроку (вихрь потока).
            Vec3 dir = new Vec3(
               player.getX() - item.getX(),
               player.getY() + player.getBbHeight() / 2.0 - item.getY(),
               player.getZ() - item.getZ()
            );
            double distance = dir.length();
            if (distance > 0.001) {
               dir = dir.scale(1.0 / distance);
               Vec3 push = item.getDeltaMovement().subtract(dir.scale(0.3));
               item.setDeltaMovement(
                  Math.max(-0.35, Math.min(0.35, push.x)),
                  Math.max(-0.35, Math.min(0.35, push.y)),
                  Math.max(-0.35, Math.min(0.35, push.z))
               );
            }
         } else {
            Vec3 pos = item.position();
            Thaumcraft.setup
               .crucibleBubble(
                  level,
                  (float)(pos.x + (level.random.nextFloat() - level.random.nextFloat()) * 0.125F),
                  (float)(pos.y + (level.random.nextFloat() - level.random.nextFloat()) * 0.125F),
                  (float)(pos.z + (level.random.nextFloat() - level.random.nextFloat()) * 0.125F),
                  0.33F,
                  0.33F,
                  1.0F
               );
         }
      }
   }
}
