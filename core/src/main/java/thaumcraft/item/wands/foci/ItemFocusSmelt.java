package thaumcraft.item.wands.foci;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.item.wands.ItemWandCasting;

public class ItemFocusSmelt extends ItemFocusBasic {
   private static final AspectList visUsage = new AspectList().add(Aspect.FIRE, 45).add(Aspect.ENTROPY, 12);
   public static final Map<UUID, ItemFocusSmelt.SmeltData> playerData = new HashMap<>();

   public ItemFocusSmelt() {
      super("smelt");
   }

   @Override
   public String getSortingHelper(ItemStack itemstack) {
      return "SMELT" + super.getSortingHelper(itemstack);
   }

   @Override
   public int getFocusColor(ItemStack stack) {
      return 16711680;
   }

   @Override
   public AspectList getVisCost(ItemStack stack) {
      return visUsage;
   }

   @Override
   public boolean isVisCostPerTick(ItemStack stack) {
      return false;
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
   public void onUsingFocusTick(ItemStack wandStack, Player player, int time) {
      ItemWandCasting wand = (ItemWandCasting)wandStack.getItem();
      Level level = player.level();
      UUID playerId = player.getUUID();
      // Тик засчитан за активную плавку — в конце метода по этому флагу
      // чистим протухшие записи, чтобы карта не текла
      boolean handled = false;
      if (wand.consumeAllVis(wandStack, player, visUsage, false, false)) {
         HitResult hit = player.pick(10.0, 1.0F, false);
         if (hit.getType() == Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult)hit;
            BlockPos blockPos = blockHit.getBlockPos();
            if (level.mayInteract(player, blockPos)) {
               BlockState state = level.getBlockState(blockPos);
               ItemStack blockStack = new ItemStack(state.getBlock());
               if (!blockStack.isEmpty()) {
                  Optional<SmeltingRecipe> recipeOpt = level.getRecipeManager()
                     .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(blockStack), level)
                     .map(RecipeHolder::value);
                  if (!recipeOpt.isEmpty()) {
                     ItemStack result = recipeOpt.get().getResultItem(level.registryAccess());
                     if (!result.isEmpty()) {
                        boolean decremented = false;
                        if (playerData.containsKey(playerId)) {
                           ItemFocusSmelt.SmeltData data = playerData.get(playerId);
                           if (data.equalPos(blockPos)) {
                              data.progress--;
                              decremented = true;
                              handled = true;
                              if (data.progress <= 0) {
                                 if (!level.isClientSide) {
                                    wand.consumeAllVis(wandStack, player, visUsage, true, false);
                                    playerData.remove(playerId);
                                    decremented = false;
                                    ItemStack out = result.copy();
                                    if (out.getItem() instanceof BlockItem) {
                                       Block resultBlock = Block.byItem(out.getItem());
                                       level.setBlock(blockPos, resultBlock.defaultBlockState(), 3);
                                    } else {
                                       // Обычные предметы не теряем: исходный блок убираем без дропа,
                                       // а результат плавки выбрасываем рядом
                                       level.destroyBlock(blockPos, false);
                                       ItemEntity drop = new ItemEntity(
                                          level, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, out
                                       );
                                       drop.setDefaultPickUpDelay();
                                       level.addFreshEntity(drop);
                                    }
                                 }

                                 level.playSound(
                                    null, player.getX(), player.getY(), player.getZ(), SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS, 0.6F, 1.0F
                                 );
                                 level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 1.0F, 1.0F);
                                 if (level.isClientSide) {
                                    for (int i = 0; i < 25; i++) {
                                       double x = blockPos.getX() + Math.random();
                                       double y = blockPos.getY() + Math.random();
                                       double z = blockPos.getZ() + Math.random();
                                       Thaumcraft.setup.wispFX2(level, x, y, z, (float)Math.random() / 2.0F, 4, true, true, (float)(-Math.random()) / 10.0F);
                                    }
                                 }
                              }
                           }
                        }

                        if (!decremented) {
                           int potency = this.getUpgradeLevel(wand.getFocusItem(wandStack), FocusUpgradeType.potency);
                           playerData.put(playerId, new ItemFocusSmelt.SmeltData(blockPos, 20 - Math.min(3, potency) * 5));
                           handled = true;
                        } else {
                           if (time % 3 == 0) {
                              level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 0.2F, 1.0F);
                           }

                           if (level.isClientSide) {
                              for (int i = 0; i < 2; i++) {
                                 double x = blockPos.getX() + Math.random();
                                 double y = blockPos.getY() + Math.random();
                                 double z = blockPos.getZ() + Math.random();
                                 Thaumcraft.setup.wispFX2(level, x, y, z, (float)Math.random() / 2.0F, 4, true, true, (float)(-Math.random()) / 10.0F);
                              }
                           }
                        }

                        if (level.isClientSide) {
                           Thaumcraft.setup
                              .beamCont(level, player, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, 2, 16711680, true, 0.0F, null, 1);
                        }
                     }
                  }
               }
            }
         }
      }

      if (!handled) {
         // Целимся не в переплавляемый блок (или нет виса) — старую запись не копим
         playerData.remove(playerId);
      }
   }

   @Override
   public void onPlayerStoppedUsingFocus(ItemStack wandStack, Level level, Player player, int count) {
      playerData.remove(player.getUUID());
   }

   @Override
   public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack itemStack, int rank) {
      return switch (rank) {
         case 1, 2 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency};
         case 3, 4, 5 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, FocusUpgradeType.treasure};
         default -> null;
      };
   }

   public static class SmeltData {
      public BlockPos pos;
      public int progress;

      public SmeltData(BlockPos pos, int progress) {
         this.pos = pos;
         this.progress = progress;
      }

      public boolean equalPos(BlockPos other) {
         return this.pos.equals(other);
      }
   }
}
