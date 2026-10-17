package thaumcraft.item.equipment;

import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.IArchitect;
import thaumcraft.api.IRepairable;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.item.equipment.ichor.IchorToolHelper;
import thaumcraft.util.InventoryUtils;
import thaumcraft.util.ItemStackData;

/**
 * Лопата землероя (Shovel of the Earthmover) — порт {@code ItemElementalShovel} из TC4 1.7.10.
 * Копает 3×3 (без Shift) и ставит до 9 блоков 3×3 (без Shift); Shift — обычная лопата.
 * Ориентация плоскости размещения переключается клавишей G (как {@code or} в оригинале).
 */
public class ItemElementalShovel extends ShovelItem implements IRepairable, IArchitect {
   private static final String TAG_ORIENTATION = "or";

   public ItemElementalShovel() {
      super(
         ThaumcraftApi.TOOL_MATERIAL_ELEMENTAL,
         new Properties().durability(1500).rarity(Rarity.RARE).attributes(DiggerItem.createAttributes(ThaumcraftApi.TOOL_MATERIAL_ELEMENTAL, 1.5F, -3.0F))
      );
   }

   // ------------------------------------------------------------------
   // Копание 3×3 (порт func_150894_a из ItemElementalShovel)
   // ------------------------------------------------------------------
   @Override
   public boolean mineBlock(@NotNull ItemStack stack, @NotNull Level level, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull LivingEntity miningEntity) {
      if (!level.isClientSide && miningEntity instanceof Player player && !player.isShiftKeyDown()) {
         Direction face = IchorToolHelper.rayTraceFace(player, pos);

         for (int aa = -1; aa <= 1; aa++) {
            for (int bb = -1; bb <= 1; bb++) {
               if (aa == 0 && bb == 0) {
                  continue;
               }

               BlockPos target = planePosition(pos, face, aa, bb);
               BlockState targetState = level.getBlockState(target);
               if (targetState.getDestroySpeed(level, target) < 0.0F) {
                  continue;
               }

               if (!isEffectiveAgainst(targetState) && !stack.isCorrectToolForDrops(targetState)) {
                  continue;
               }

               if (level.destroyBlock(target, true, player)) {
                  stack.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
               }
            }
         }
      }

      return super.mineBlock(stack, level, state, pos, miningEntity);
   }

   private static boolean isEffectiveAgainst(BlockState state) {
      return state.is(net.minecraft.tags.BlockTags.MINEABLE_WITH_SHOVEL);
   }

   /** Позиция в 3×3-плоскости копания, перпендикулярной грани удара (порт логики 1.7.10). */
   private static BlockPos planePosition(BlockPos clicked, Direction face, int aa, int bb) {
      Axis axis = face.getAxis();
      int xx = 0;
      int yy = 0;
      int zz = 0;
      if (axis == Axis.Y) {
         xx = aa;
         zz = bb;
      } else if (axis == Axis.Z) {
         xx = aa;
         yy = bb;
      } else {
         zz = aa;
         yy = bb;
      }

      return clicked.offset(xx, yy, zz);
   }

   // ------------------------------------------------------------------
   // Размещение 3×3 (порт func_77648_a из ItemElementalShovel)
   // ------------------------------------------------------------------
   @Override
   public InteractionResult useOn(@NotNull UseOnContext ctx) {
      Level level = ctx.getLevel();
      Player player = ctx.getPlayer();
      if (player == null || player.isShiftKeyDown()) {
         return super.useOn(ctx);
      }

      BlockPos clicked = ctx.getClickedPos();
      Direction face = ctx.getClickedFace();
      ItemStack tool = ctx.getItemInHand();
      BlockState sourceState = level.getBlockState(clicked);
      if (sourceState.isAir() || sourceState.is(Blocks.BEDROCK) || level.getBlockEntity(clicked) != null) {
         return InteractionResult.PASS;
      }

      ItemStack template = new ItemStack(sourceState.getBlock().asItem());
      if (!(template.getItem() instanceof BlockItem blockItem)) {
         return InteractionResult.PASS;
      }

      // Особый случай оригинала: клик по траве — размещаем землю.
      ItemStack material = template;
      BlockState placeState = sourceState;
      if (sourceState.is(Blocks.GRASS_BLOCK)) {
         material = new ItemStack(Blocks.DIRT);
         placeState = Blocks.DIRT.defaultBlockState();
      }

      int orientation = getOrientation(tool);
      boolean creative = player.getAbilities().instabuild;
      boolean placedAny = false;

      if (level.isClientSide) {
         // Клиентский предпросмотр уже нарисован; сервер подтвердит размещение.
         for (int aa = -1; aa <= 1; aa++) {
            for (int bb = -1; bb <= 1; bb++) {
               BlockPos p = placementPosition(clicked, face, aa, bb, orientation, player);
               if (p != null && canPlaceAt(level, p)) {
                  player.swing(ctx.getHand(), true);
                  return InteractionResult.SUCCESS;
               }
            }
         }
         return InteractionResult.PASS;
      }

      for (int aa = -1; aa <= 1; aa++) {
         for (int bb = -1; bb <= 1; bb++) {
            BlockPos p = placementPosition(clicked, face, aa, bb, orientation, player);
            if (p == null || !canPlaceAt(level, p)) {
               continue;
            }

            if (!creative && !InventoryUtils.consumeInventoryItem(player, material)) {
               continue;
            }

            level.playSound(null, p, placeState.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 0.6F, 0.9F + level.random.nextFloat() * 0.2F);
            level.setBlock(p, placeState, 3);
            tool.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
            thaumcraft.Thaumcraft.setup.blockSparkle(level, p.getX(), p.getY(), p.getZ(), 8401408, 4);
            placedAny = true;
         }
      }

      if (placedAny) {
         player.swing(ctx.getHand(), true);
         return InteractionResult.SUCCESS;
      }

      return InteractionResult.PASS;
   }

   private static BlockPos placementPosition(BlockPos clicked, Direction face, int aa, int bb, int orientation, Player player) {
      int xm = face.getStepX();
      int ym = face.getStepY();
      int zm = face.getStepZ();
      int xx = 0;
      int yy = 0;
      int zz = 0;
      Axis axis = face.getAxis();
      int yawQuadrant = Mth.floor(player.getYRot() * 4.0F / 360.0F + 0.5F) & 3;
      boolean alongZ = yawQuadrant != 0 && yawQuadrant != 2;

      if (orientation == 1) {
         yy = bb;
         if (axis == Axis.Y) {
            if (alongZ) {
               zz = aa;
            } else {
               xx = aa;
            }
         } else if (axis == Axis.Z) {
            zz = aa;
         } else {
            xx = aa;
         }
      } else if (orientation == 2) {
         if (axis == Axis.Y) {
            yy = bb;
            if (alongZ) {
               zz = aa;
            } else {
               xx = aa;
            }
         } else {
            zz = bb;
            xx = aa;
         }
      } else if (axis == Axis.Y) {
         xx = aa;
         zz = bb;
      } else if (axis == Axis.Z) {
         xx = aa;
         yy = bb;
      } else {
         zz = aa;
         yy = bb;
      }

      return clicked.offset(xx + xm, yy + ym, zz + zm);
   }

   private static boolean canPlaceAt(Level level, BlockPos p) {
      BlockState at = level.getBlockState(p);
      return at.isAir() || at.canBeReplaced();
   }

   // ------------------------------------------------------------------
   // IArchitect: предпросмотр размещения
   // ------------------------------------------------------------------
   @Override
   public ArrayList<BlockPos> getArchitectBlocks(ItemStack stack, Level level, BlockPos blockPos, Direction direction, Player player) {
      ArrayList<BlockPos> out = new ArrayList<>();
      if (player == null || player.isShiftKeyDown() || direction == null) {
         return out;
      }

      int orientation = getOrientation(stack);
      for (int aa = -1; aa <= 1; aa++) {
         for (int bb = -1; bb <= 1; bb++) {
            BlockPos p = placementPosition(blockPos, direction, aa, bb, orientation, player);
            if (p != null && canPlaceAt(level, p)) {
               out.add(p);
            }
         }
      }

      return out;
   }

   @Override
   public boolean showAxis(ItemStack stack, Level level, Player player, Direction direction, IArchitect.EnumAxis axis) {
      return false;
   }

   // ------------------------------------------------------------------
   // Ориентация (клавиша G), порт getOrientation/setOrientation из 1.7.10
   // ------------------------------------------------------------------
   public static int getOrientation(ItemStack stack) {
      CompoundTag tag = ItemStackData.getTag(stack);
      if (tag != null && tag.contains(TAG_ORIENTATION)) {
         return Math.floorMod(tag.getByte(TAG_ORIENTATION), 3);
      }

      return 0;
   }

   public static void setOrientation(ItemStack stack, int orientation) {
      ItemStackData.getOrCreateTag(stack).putByte(TAG_ORIENTATION, (byte)Math.floorMod(orientation, 3));
   }
}
