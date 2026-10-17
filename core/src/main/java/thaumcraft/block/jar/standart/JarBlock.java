package thaumcraft.block.jar.standart;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.util.DeferredSoundType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.sounds.SoundSource;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssenceContainerItem;
import thaumcraft.api.block.TickableEntityBlock;
import thaumcraft.init.registry.ModItems;
import thaumcraft.init.registry.ModSounds;
import thaumcraft.item.jar_label.JarLabelItem;
import thaumcraft.item.phial_essence.ItemPhialEssence;
import thaumcraft.util.ItemStackData;

public class JarBlock extends TickableEntityBlock {
   public static final DeferredSoundType SOUND_TYPE = new DeferredSoundType(
      1.0F, 1.0F, () -> SoundEvents.GLASS_BREAK, ModSounds.JAR, () -> SoundEvents.GLASS_PLACE, () -> SoundEvents.GLASS_HIT, () -> SoundEvents.GLASS_FALL
   );
   public static final VoxelShape SHAPE = Shapes.box(0.1875, 0.0, 0.1875, 0.8125, 0.75, 0.8125);

   public JarBlock() {
      this(Properties.of().strength(0.3F).sound(SOUND_TYPE).lightLevel(s -> 10).noOcclusion());
   }

   public JarBlock(Properties properties) {
      super(properties);
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
   }

   @NotNull
   @Override
   public RenderShape getRenderShape(@NotNull BlockState state) {
      return RenderShape.MODEL;
   }

   protected void createBlockStateDefinition(@NotNull Builder<Block, BlockState> builder) {
      builder.add(new Property[]{BlockStateProperties.HORIZONTAL_FACING});
   }

   @Nullable
   public BlockState getStateForPlacement(@NotNull BlockPlaceContext ctx) {
      return (BlockState)this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, ctx.getHorizontalDirection().getOpposite());
   }

   @NotNull
   @Override
   public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext ctx) {
      return SHAPE;
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
      return new JarBlockEntity(pos, state);
   }

   @NotNull
   public List<ItemStack> getDrops(@NotNull BlockState state, @NotNull net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
      List<ItemStack> drops = new ArrayList<>();
      BlockEntity be = (BlockEntity)builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
      if (be instanceof JarBlockEntity jar) {
         ItemStack stack = new ItemStack(this);
         if (jar.amount > 0 && jar.aspect != null && stack.getItem() instanceof IEssenceContainerItem container) {
            container.setAspects(stack, new AspectList().add(jar.aspect, jar.amount));
         }

         if (jar.aspectFilter != null) {
            ItemStackData.getOrCreateTag(stack).putString("AspectFilter", jar.aspectFilter.getTag());
         }

         drops.add(stack);
         return drops;
      } else {
         return super.getDrops(state, builder);
      }
   }

   @NotNull
   @Override
   public InteractionResult useWithoutItem(
      @NotNull BlockState state,
      @NotNull Level level,
      @NotNull BlockPos pos,
      @NotNull Player player,
      @NotNull BlockHitResult hit
   ) {
      if (!(level.getBlockEntity(pos) instanceof JarBlockEntity jar)) {
         return InteractionResult.PASS;
      }

      Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

      // 1. Shift + empty hand: remove label or empty jar
      if (player.isShiftKeyDown()) {
         if (jar.aspectFilter != null && hit.getDirection() == facing) {
            // Remove label from the labeled face
            Aspect removed = jar.aspectFilter;
            if (!level.isClientSide) {
               jar.aspectFilter = null;
               jar.markForUpdate();
               jar.setChanged();

               ItemStack labelStack = new ItemStack((Item)ModItems.JAR_LABEL.get());
               ((JarLabelItem)ModItems.JAR_LABEL.get()).setAspects(labelStack, new AspectList().add(removed, 1));
               if (!player.getInventory().add(labelStack)) {
                  player.drop(labelStack, false);
               }
            }
            level.playSound(player, pos, ModSounds.PAGE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            return InteractionResult.sidedSuccess(level.isClientSide);
         } else if (jar.amount > 0) {
            // Empty the jar on any other face (or if no filter)
            if (!level.isClientSide) {
               jar.amount = 0;
               if (jar.aspectFilter == null) {
                  jar.aspect = null;
               }
               jar.markForUpdate();
               jar.setChanged();
            }
            level.playSound(player, pos, ModSounds.JAR.get(), SoundSource.BLOCKS, 0.4F, 1.0F);
            level.playSound(player, pos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 0.5F, 1.0F);
            return InteractionResult.sidedSuccess(level.isClientSide);
         }
      }

      return InteractionResult.PASS;
   }

   @NotNull
   @Override
   public ItemInteractionResult useItemOn(
      @NotNull ItemStack handStack,
      @NotNull BlockState state,
      @NotNull Level level,
      @NotNull BlockPos pos,
      @NotNull Player player,
      @NotNull InteractionHand hand,
      @NotNull BlockHitResult hit
   ) {
      if (!(level.getBlockEntity(pos) instanceof JarBlockEntity jar)) {
         return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      }

      Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

      // 1. Shift + click on label face: remove label even if holding an item
      if (player.isShiftKeyDown() && jar.aspectFilter != null && hit.getDirection() == facing) {
         Aspect removed = jar.aspectFilter;
         if (!level.isClientSide) {
            jar.aspectFilter = null;
            jar.markForUpdate();
            jar.setChanged();

            ItemStack labelStack = new ItemStack((Item)ModItems.JAR_LABEL.get());
            ((JarLabelItem)ModItems.JAR_LABEL.get()).setAspects(labelStack, new AspectList().add(removed, 1));
            if (!player.getInventory().add(labelStack)) {
               player.drop(labelStack, false);
            }
         }
         level.playSound(player, pos, ModSounds.PAGE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
         return ItemInteractionResult.sidedSuccess(level.isClientSide);
      }

      // 2. Attach Jar Label
      if (!handStack.isEmpty() && jar.aspectFilter == null && handStack.is((Item)ModItems.JAR_LABEL.get())) {
         AspectList labelAspects = ((JarLabelItem)handStack.getItem()).getAspects(handStack);
         Aspect labelAspect = (labelAspects != null && labelAspects.size() > 0) ? labelAspects.getAspects()[0] : null;

         if (jar.amount == 0 && labelAspect == null) {
            // Blank label cannot be placed on an empty jar
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
         }

         if (jar.amount > 0 && labelAspect != null && labelAspect != jar.aspect) {
            // Labeled label with different aspect cannot be placed
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
         }

         if (!level.isClientSide) {
            if (jar.amount == 0 && labelAspect != null) {
               jar.aspect = labelAspect;
               jar.aspectFilter = labelAspect;
            } else if (jar.amount > 0) {
               jar.aspectFilter = jar.aspect;
            }

            // Set jar horizontal facing to clicked face (or player's facing opposite)
            Direction clickDir = hit.getDirection();
            Direction newFacing = clickDir.getAxis().isHorizontal() ? clickDir : player.getDirection().getOpposite();
            level.setBlock(pos, state.setValue(BlockStateProperties.HORIZONTAL_FACING, newFacing), 3);

            if (!player.getAbilities().instabuild) {
               handStack.shrink(1);
            }
            jar.markForUpdate();
            jar.setChanged();
         }
         level.playSound(player, pos, ModSounds.JAR.get(), SoundSource.BLOCKS, 0.4F, 1.0F);
         return ItemInteractionResult.sidedSuccess(level.isClientSide);
      }

      // 3. Phial of Essentia (fill jar or extract from jar)
      if (!handStack.isEmpty() && handStack.is((Item)ModItems.PHIAL_ESSENCE.get())) {
         ItemPhialEssence phial = (ItemPhialEssence) handStack.getItem();
         AspectList phialAspects = phial.getAspects(handStack);

         // Case 3a: Empty phial -> extract 8 essentia from jar
         if (phialAspects == null || phialAspects.size() == 0) {
            if (jar.amount >= 8 && jar.aspect != null) {
               if (!level.isClientSide) {
                  Aspect asp = jar.aspect;
                  jar.takeFromContainer(asp, 8);
                  jar.markForUpdate();
                  jar.setChanged();

                  ItemStack filled = new ItemStack((Item)ModItems.PHIAL_ESSENCE.get());
                  phial.setAspects(filled, new AspectList().add(asp, 8));

                  if (!player.getAbilities().instabuild) {
                     handStack.shrink(1);
                     if (handStack.isEmpty()) {
                        player.setItemInHand(hand, filled);
                     } else if (!player.getInventory().add(filled)) {
                        player.drop(filled, false);
                     }
                  } else if (!player.getInventory().contains(filled)) {
                     player.getInventory().add(filled);
                  }
               }
               level.playSound(player, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.5F, 1.0F);
               return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
         } else {
            // Case 3b: Filled phial -> deposit 8 essentia into jar
            Aspect asp = phialAspects.getAspects()[0];
            if (jar.amount <= jar.maxAmount - 8 && jar.doesContainerAccept(asp) && (jar.amount == 0 || jar.aspect == asp)) {
               if (!level.isClientSide) {
                  jar.addToContainer(asp, 8);
                  jar.markForUpdate();
                  jar.setChanged();

                  if (!player.getAbilities().instabuild) {
                     handStack.shrink(1);
                     ItemStack emptyPhial = new ItemStack((Item)ModItems.PHIAL_ESSENCE.get());
                     if (handStack.isEmpty()) {
                        player.setItemInHand(hand, emptyPhial);
                     } else if (!player.getInventory().add(emptyPhial)) {
                        player.drop(emptyPhial, false);
                     }
                  }
               }
               level.playSound(player, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 0.5F, 1.0F);
               return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
         }
      }

      return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
   }

   public boolean hasAnalogOutputSignal(@NotNull BlockState state) {
      return true;
   }

   public int getAnalogOutputSignal(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos) {
      return level.getBlockEntity(pos) instanceof JarBlockEntity jar ? Mth.floor((float)jar.amount / jar.maxAmount * 14.0F) + (jar.amount > 0 ? 1 : 0) : 0;
   }
}
