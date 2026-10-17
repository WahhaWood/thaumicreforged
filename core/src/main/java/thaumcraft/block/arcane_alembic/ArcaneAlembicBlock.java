package thaumcraft.block.arcane_alembic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssenceContainerItem;
import thaumcraft.api.block.ThaumcraftEntityBlock;
import thaumcraft.block.jar.standart.JarBlockItem;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModItems;
import thaumcraft.init.registry.ModSounds;
import thaumcraft.item.jar_label.JarLabelItem;
import thaumcraft.item.phial_essence.ItemPhialEssence;

public class ArcaneAlembicBlock extends ThaumcraftEntityBlock {
   private static final EnumProperty<ArcaneAlembicBlock.TypeStructure> TYPE = EnumProperty.create("type", ArcaneAlembicBlock.TypeStructure.class);

   public ArcaneAlembicBlock() {
      super(Properties.of().forceSolidOn().noOcclusion().sound(SoundType.METAL).strength(3.0F, 17.0F));
      this.registerDefaultState(
         (BlockState)((BlockState)this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH))
            .setValue(TYPE, ArcaneAlembicBlock.TypeStructure.NONE)
      );
   }

   protected void createBlockStateDefinition(@NotNull Builder<Block, BlockState> builder) {
      builder.add(new Property[]{BlockStateProperties.HORIZONTAL_FACING, TYPE});
   }

   @Nullable
   public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
      BlockState blockState = context.getLevel().getBlockState(context.getClickedPos().below());
      return (BlockState)((BlockState)this.defaultBlockState()
            .setValue(
               TYPE,
               blockState.is((Block)ModBlocks.ALCHEMY_FURNACE.get())
                  ? ArcaneAlembicBlock.TypeStructure.FIRST
                  : (blockState.is((Block)ModBlocks.ARCANE_ALEMBIC.get()) ? ArcaneAlembicBlock.TypeStructure.SECOND : ArcaneAlembicBlock.TypeStructure.NONE)
            ))
         .setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection());
   }

   @NotNull
   public ItemInteractionResult useItemOn(
      @NotNull ItemStack handStack,
      @NotNull BlockState blockState,
      @NotNull Level level,
      @NotNull BlockPos blockPos,
      @NotNull Player player,
      @NotNull InteractionHand hand,
      @NotNull BlockHitResult hitResult
   ) {
      if (!(level.getBlockEntity(blockPos) instanceof ArcaneAlembicBlockEntity alembic)) {
         return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      }

      // 1. Shift + empty hand: remove label or empty alembic
      if (player.isShiftKeyDown() && handStack.isEmpty()) {
         if (alembic.aspectFilter != null) {
            // Remove filter from alembic
            Aspect removed = alembic.aspectFilter;
            if (!level.isClientSide) {
               alembic.aspectFilter = null;
               alembic.markForUpdate();
               alembic.setChanged();

               ItemStack labelStack = new ItemStack((Item)ModItems.JAR_LABEL.get());
               ((JarLabelItem)ModItems.JAR_LABEL.get()).setAspects(labelStack, new AspectList().add(removed, 1));
               if (!player.getInventory().add(labelStack)) {
                  player.drop(labelStack, false);
               }
            }
            level.playSound(player, blockPos, ModSounds.PAGE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
         } else if (alembic.amount > 0) {
            // Empty alembic contents
            if (!level.isClientSide) {
               alembic.amount = 0;
               alembic.aspect = null;
               alembic.markForUpdate();
               alembic.setChanged();
            }
            level.playSound(player, blockPos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 0.4F, 1.0F);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
         }
         return ItemInteractionResult.sidedSuccess(level.isClientSide);
      }

      if (!handStack.isEmpty()) {
         // 2. Attach Jar Label to Alembic
         if (alembic.aspectFilter == null && handStack.is((Item)ModItems.JAR_LABEL.get())) {
            AspectList labelAspects = ((JarLabelItem)handStack.getItem()).getAspects(handStack);
            Aspect labelAspect = (labelAspects != null && labelAspects.size() > 0) ? labelAspects.getAspects()[0] : null;

            if (alembic.amount == 0 && labelAspect == null) {
               return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
            if (alembic.amount > 0 && labelAspect != null && labelAspect != alembic.aspect) {
               return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }

            if (!level.isClientSide) {
               if (alembic.amount == 0 && labelAspect != null) {
                  alembic.aspect = labelAspect;
                  alembic.aspectFilter = labelAspect;
               } else if (alembic.amount > 0) {
                  alembic.aspectFilter = alembic.aspect;
               }
               alembic.markForUpdate();
               alembic.setChanged();
               handStack.shrink(1);
            }
            level.playSound(player, blockPos, ModSounds.JAR.get(), SoundSource.BLOCKS, 0.4F, 1.0F);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
         }

         // 3. Phial of Essentia: extract 8 essentia from Alembic into empty phial
         if (handStack.is((Item)ModItems.PHIAL_ESSENCE.get())) {
            ItemPhialEssence phial = (ItemPhialEssence) handStack.getItem();
            AspectList phialAspects = phial.getAspects(handStack);

            if (phialAspects == null || phialAspects.size() == 0) {
               if (alembic.amount >= 8 && alembic.aspect != null) {
                  if (!level.isClientSide) {
                     Aspect asp = alembic.aspect;
                     alembic.takeFromContainer(asp, 8);
                     alembic.markForUpdate();
                     alembic.setChanged();

                     handStack.shrink(1);
                     ItemStack filled = new ItemStack((Item)ModItems.PHIAL_ESSENCE.get());
                     phial.setAspects(filled, new AspectList().add(asp, 8));
                     if (!player.getInventory().add(filled)) {
                        player.drop(filled, false);
                     }
                  }
                  level.playSound(player, blockPos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.5F, 1.0F);
                  return ItemInteractionResult.sidedSuccess(level.isClientSide);
               }
            }
         }

         // 4. Fill Jar (or Void Jar) from Alembic
         if (alembic.amount > 0 && alembic.aspect != null && handStack.getItem() instanceof JarBlockItem jarItem) {
            Aspect filter = jarItem.getFilter(handStack);
            if (filter == null || filter == alembic.aspect) {
               AspectList jarAspects = jarItem.getAspects(handStack);
               boolean isVoid = handStack.is((Item)ModItems.VOID_JAR.get());

               if (jarAspects != null && jarAspects.size() > 0) {
                  Aspect jarAspect = jarAspects.getAspects()[0];
                  if (jarAspect == alembic.aspect) {
                     int current = jarAspects.getAmount(jarAspect);
                     int space = isVoid ? alembic.amount : (64 - current);
                     int transfer = Math.min(space, alembic.amount);
                     if (transfer > 0) {
                        if (!level.isClientSide) {
                           // NB: аспект сохраняем ДО забора — takeFromContainer
                           // обнуляет alembic.aspect при полном осушении.
                           Aspect asp = alembic.aspect;
                           alembic.takeFromContainer(asp, transfer);
                           alembic.markForUpdate();
                           alembic.setChanged();

                           jarAspects.add(asp, transfer);
                           if (jarAspects.getAmount(asp) > 64 && !isVoid) {
                              jarAspects.reduce(asp, jarAspects.getAmount(asp) - 64);
                           }
                           jarItem.setAspects(handStack, jarAspects);
                        }
                        level.playSound(player, blockPos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 0.4F, 1.0F);
                        return ItemInteractionResult.sidedSuccess(level.isClientSide);
                     }
                  }
               } else {
                  // Jar is completely empty
                  int transfer = Math.min(64, alembic.amount);
                  if (transfer > 0) {
                     if (!level.isClientSide) {
                        Aspect asp = alembic.aspect;
                        alembic.takeFromContainer(asp, transfer);
                        alembic.markForUpdate();
                        alembic.setChanged();

                        if (handStack.getCount() == 1) {
                           jarItem.setAspects(handStack, new AspectList().add(asp, transfer));
                        } else {
                           handStack.shrink(1);
                           ItemStack filledJar = handStack.copyWithCount(1);
                           jarItem.setAspects(filledJar, new AspectList().add(asp, transfer));
                           if (!player.getInventory().add(filledJar)) {
                              player.drop(filledJar, false);
                           }
                        }
                     }
                     level.playSound(player, blockPos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 0.4F, 1.0F);
                     return ItemInteractionResult.sidedSuccess(level.isClientSide);
                  }
               }
            }
         }
      }

      return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
   }

   @Override
   public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull BlockState blockState, boolean p_60519_) {
      if (!state.is(blockState.getBlock())
         && level.getBlockEntity(blockPos) instanceof ArcaneAlembicBlockEntity arcaneAlembic
         && arcaneAlembic.aspectFilter != null) {
         Aspect filter = arcaneAlembic.aspectFilter;
      }

      super.onRemove(state, level, blockPos, blockState, p_60519_);
   }

   @Nullable
   public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
      return new ArcaneAlembicBlockEntity(blockPos, blockState);
   }

   public boolean hasAnalogOutputSignal(@NotNull BlockState blockState) {
      return true;
   }

   public int getAnalogOutputSignal(@NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos) {
      if (level.getBlockEntity(blockPos) instanceof ArcaneAlembicBlockEntity alembic) {
         float r = (float)alembic.amount / alembic.maxAmount;
         return Mth.floor(r * 14.0F) + (alembic.amount > 0 ? 1 : 0);
      } else {
         return super.getAnalogOutputSignal(blockState, level, blockPos);
      }
   }

   public void neighborChanged(
      @NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull Block block, @NotNull BlockPos blockPos1, boolean p_60514_
   ) {
      if (!level.isClientSide && level.getBlockEntity(blockPos) instanceof ArcaneAlembicBlockEntity alembic) {
         ;
      }
   }

   public static enum TypeStructure implements StringRepresentable {
      NONE,
      FIRST,
      SECOND;

      @NotNull
      public String getSerializedName() {
         return this.name().toLowerCase();
      }
   }
}
