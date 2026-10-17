package thaumcraft.block.lamp;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.blockentity.ThaumcraftBlockEntity;
import thaumcraft.block.bore.render.BoreBaseRenderer;
import thaumcraft.init.registry.ModBlocks;

@OnlyIn(Dist.CLIENT)
public class ArcaneLampRenderer implements BlockEntityRenderer<ThaumcraftBlockEntity> {
   public ArcaneLampRenderer(Context context) {
   }

   public void render(
      @NotNull ThaumcraftBlockEntity entity, float pt, @NotNull PoseStack pose, @NotNull MultiBufferSource source, int brightnessOverride, int overlayTexture
   ) {
      Level level = entity.getLevel();
      if (level != null) {
         BlockPos blockPos = entity.getBlockPos();
         Direction face = entity.getFacing();

         for (Direction value : Direction.values()) {
            if (value != face && !value.getAxis().isVertical()) {
               BlockState state = level.getBlockState(blockPos.relative(value));
               if (state.is((Block)ModBlocks.ARCANE_BORE_BASE.get())) {
                  BoreBaseRenderer.renderConnect(
                     (BlockState)entity.getBlockState().setValue(BlockStateProperties.FACING, value), pose, source, brightnessOverride, overlayTexture
                  );
               }
            }
         }

         BoreBaseRenderer.renderConnect(entity.getBlockState(), pose, source, brightnessOverride, overlayTexture);
      }
   }
}
