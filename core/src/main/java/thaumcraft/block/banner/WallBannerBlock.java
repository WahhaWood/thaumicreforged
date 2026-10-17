package thaumcraft.block.banner;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WallBannerBlock extends net.minecraft.world.level.block.WallBannerBlock {
   public static final MapCodec<net.minecraft.world.level.block.WallBannerBlock> CODEC = simpleCodec(props -> new WallBannerBlock(null));

   public WallBannerBlock(@Nullable DyeColor color) {
      super(
         color,
         Properties.of()
            .mapColor(MapColor.WOOD)
            .forceSolidOn()
            .instrument(NoteBlockInstrument.BASS)
            .noCollission()
            .strength(1.0F)
            .sound(SoundType.WOOD)
            .ignitedByLava()
      );
   }

   public MapCodec<net.minecraft.world.level.block.WallBannerBlock> codec() {
      return CODEC;
   }

   @NotNull
   public ItemInteractionResult useItemOn(
      @NotNull ItemStack stack,
      @NotNull BlockState state,
      @NotNull Level level,
      @NotNull BlockPos pos,
      @NotNull Player player,
      @NotNull InteractionHand hand,
      @NotNull BlockHitResult hitResult
   ) {
      return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
   }

   @NotNull
   public List<ItemStack> getDrops(@NotNull BlockState state, @NotNull Builder builder) {
      return super.getDrops(state, builder);
   }

   @Nullable
   public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
      return new BannerBlockEntity(blockPos, blockState);
   }
}
