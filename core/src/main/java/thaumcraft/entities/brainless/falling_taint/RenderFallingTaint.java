package thaumcraft.entities.brainless.falling_taint;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class RenderFallingTaint extends EntityRenderer<EntityFallingTaint> {
   private final BlockRenderDispatcher blockRenderer;

   public RenderFallingTaint(Context ctx) {
      super(ctx);
      this.blockRenderer = ctx.getBlockRenderDispatcher();
      this.shadowRadius = 0.5F;
   }

   @OnlyIn(Dist.CLIENT)
   public void render(EntityFallingTaint entity, float yaw, float partialTicks, PoseStack pose, MultiBufferSource buffer, int packedLight) {
      BlockState state = entity.getMimicState();
      if (state != null && !state.isAir()) {
         Level level = entity.level();
         BlockPos pos = entity.blockPosition();
         if (level.getBlockState(pos).getBlock() != state.getBlock()) {
            pose.pushPose();
            pose.translate(-0.5, 0.0, -0.5);
            this.blockRenderer.renderSingleBlock(state, pose, buffer, packedLight, OverlayTexture.NO_OVERLAY);
            pose.popPose();
            super.render(entity, yaw, partialTicks, pose, buffer, packedLight);
         }
      }
   }

   public ResourceLocation getTextureLocation(EntityFallingTaint entity) {
      return TextureAtlas.LOCATION_BLOCKS;
   }
}
