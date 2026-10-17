package thaumcraft.item.relics;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.function.Consumer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.nodes.INode;
import thaumcraft.api.research.IScanEventHandler;
import thaumcraft.api.research.ScanResult;
import thaumcraft.block.airy.energized_aura.NodeEnergizedBlockEntity;
import thaumcraft.block.table.ResearchRecipeScreen;
import thaumcraft.init.registry.ModSounds;
import thaumcraft.research.ScanManager;
import thaumcraft.util.EntityUtils;

public class ItemThaumometer extends Item {
   public ItemThaumometer() {
      super(new Properties().stacksTo(1).setNoRepair().rarity(Rarity.UNCOMMON));
   }


   public int getUseDuration(@NotNull ItemStack stack, LivingEntity entity) {
      return 25;
   }

   @NotNull
   public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
      if (hand == InteractionHand.MAIN_HAND && player.getOffhandItem().isEmpty()) {
         player.startUsingItem(hand);
      }

      return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
   }

   public void onUseTick(@NotNull Level level, @NotNull LivingEntity entity, @NotNull ItemStack stack, int count) {
      if (entity instanceof Player player) {
         // onUseTick тикает на клиенте для ВСЕХ видимых сущностей, включая чужих
         // игроков. Чужой скан ниже вызывал бы completeScan/validScan с knowledge
         // соседа на НАШЕМ клиенте: отсюда чужие попапы «не получается изучить»
         // и порча локального knowledge. Сервер и так обсчитывает каждого сам.
         if (level.isClientSide && !(player instanceof LocalPlayer)) {
            return;
         }

         if (count <= 5) {
            entity.stopUsingItem();
            ScanResult scan = this.doScan(stack, player, false);
            if (scan != null) {
               ScanManager.completeScan(player, scan, "@");
            }
         } else {
            if (level.isClientSide && player.tickCount % 2 == 0 && this.shouldPlayScanningTick(stack, player)) {
               level.playLocalSound(
                  player.getX(),
                  player.getY(),
                  player.getZ(),
                  (SoundEvent)ModSounds.CAMERA_TICKS.get(),
                  SoundSource.PLAYERS,
                  0.2F,
                  0.45F + level.random.nextFloat() * 0.1F,
                  false
               );
            }
         }
      }
   }

   private boolean shouldPlayScanningTick(ItemStack stack, Player player) {
      ScanResult scan = this.doScan(stack, player, true);
      return scan != null && ScanManager.isValidScanTarget(player, scan, "@");
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void initializeClient(Consumer<IClientItemExtensions> consumer) {
      consumer.accept(
         new IClientItemExtensions() {
            public boolean applyForgeHandTransform(
               PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess
            ) {
               boolean rightHand = arm == HumanoidArm.RIGHT;
               poseStack.translate((rightHand ? 1.0F : -1.0F) * 0.56F, -0.52F, -0.72F);
               if (rightHand && !player.getOffhandItem().isEmpty()) {
                  poseStack.mulPose(Axis.YN.rotationDegrees(90.0F));
                  poseStack.scale(0.2F, 0.2F, 0.2F);
               }

               return true;
            }

            /**
             * Корпус прибора плюс его экран: в 1.7.10 это отдельный
             * {@code ItemThaumometerRenderer}, подключавшийся через
             * {@code IItemRenderer}. Эффект тот же, цепляется через
             * {@code getCustomRenderer}.
             */
            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
               return thaumcraft.item.render.ThaumometerItemRenderer.getInstance();
            }
         }
      );
   }

   @OnlyIn(Dist.CLIENT)
   public ScanResult doScan(ItemStack stack, Player player) {
      return this.doScan(stack, player, true);
   }

   public ScanResult doScan(ItemStack stack, Player player, boolean clientSide) {
      Level level = player.level();
      Entity pointedEntity = EntityUtils.getPointedEntity(player, 0.5, 10.0, 0.0F, true);
      if (pointedEntity != null) {
         ScanResult sr = ScanResult.ofEntity(pointedEntity);
         if (clientSide) {
            cacheScanResult(sr);
            return sr;
         } else if (ScanManager.isValidScanTarget(player, sr, "@")) {
            if (level.isClientSide) {
               Thaumcraft.setup
                  .blockRunes(
                     level,
                     pointedEntity.getX() - 0.5,
                     pointedEntity.getY() + pointedEntity.getEyeHeight() / 2.0F,
                     pointedEntity.getZ() - 0.5,
                     0.3F + level.random.nextFloat() * 0.7F,
                     0.0F,
                     0.3F + level.random.nextFloat() * 0.7F,
                     (int)(pointedEntity.getBbHeight() * 15.0F),
                     0.03F
                  );
            }

            return sr;
         } else {
            return null;
         }
      } else {
         for (IScanEventHandler seh : ThaumcraftApi.scanEventhandlers) {
            ScanResult scan = seh.scanPhenomena(stack, level, player);
            if (scan != null) {
               if (clientSide) {
                  cacheScanResult(scan);
                  return scan;
               }

               if (ScanManager.isValidScanTarget(player, scan, "@")) {
                  return scan;
               }

               return null;
            }
         }

         if (player.pick(10.0, 0.0F, true) instanceof BlockHitResult result && result.getType() == Type.BLOCK) {
            BlockPos blockPos = result.getBlockPos();
            BlockEntity pointedTile = level.getBlockEntity(blockPos);
            String nodePhenomena = pointedTile instanceof INode node
               ? "NODE" + node.getId()
               : pointedTile instanceof NodeEnergizedBlockEntity energized ? NodeEnergizedBlockEntity.SCAN_PREFIX + energized.getScanId() : null;
            if (nodePhenomena != null) {
               ScanResult sr = ScanResult.ofPhenomena(nodePhenomena);
               if (clientSide) {
                  cacheScanResult(sr);
                  return sr;
               }

               if (ScanManager.isValidScanTarget(player, sr, "@")) {
                  if (level.isClientSide) {
                     Thaumcraft.setup
                        .blockRunes(
                           level,
                           blockPos.getX(),
                           blockPos.getY() + 0.25,
                           blockPos.getZ(),
                           0.3F + level.random.nextFloat() * 0.7F,
                           0.0F,
                           0.3F + level.random.nextFloat() * 0.7F,
                           15,
                           0.03F
                        );
                  }

                  return sr;
               }

               return null;
            }

            BlockState blockState = level.getBlockState(blockPos);
            if (!blockState.isAir()) {
               ScanResult srx = BlockScanResolver.resolveScanResult(level, blockPos, blockState, result, player);
               if (srx == null) {
                  return null;
               }

               if (clientSide) {
                  cacheScanResult(srx);
                  return srx;
               }

               if (ScanManager.isValidScanTarget(player, srx, "@")) {
                  if (level.isClientSide) {
                     Thaumcraft.setup
                        .blockRunes(
                           level,
                           blockPos.getX(),
                           blockPos.getY() + 0.25,
                           blockPos.getZ(),
                           0.3F + level.random.nextFloat() * 0.7F,
                           0.0F,
                           0.3F + level.random.nextFloat() * 0.7F,
                           15,
                           0.03F
                        );
                  }

                  return srx;
               }

               return null;
            }
         }

         return null;
      }
   }

   @OnlyIn(Dist.CLIENT)
   private static void cacheScanResult(ScanResult result) {
      if (result != null) {
         if ((result.type == ScanResult.Type.ITEM || result.type == ScanResult.Type.BLOCK) && result.stack != null && !result.stack.isEmpty()) {
            ResearchRecipeScreen.cacheScannedStack(result.stack);
         } else if (result.type == ScanResult.Type.ENTITY && result.entity instanceof ItemEntity itemEntity) {
            ResearchRecipeScreen.cacheScannedStack(itemEntity.getItem());
         }
      }
   }
}
