package thaumcraft.entities.brainless.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class SpecialItemEntityRender extends ItemEntityRenderer {
   private final RandomSource random = RandomSource.create();

   public SpecialItemEntityRender(Context context) {
      super(context);
   }

   @OnlyIn(Dist.CLIENT)
   public void render(@NotNull ItemEntity entity, float p_115037_, float pt, @NotNull PoseStack pose, @NotNull MultiBufferSource source, int brightnessOverride) {
      RandomSource random = this.random;
      random.setSeed(245L);
      float offY = Mth.sin((entity.getAge() + pt) / 10.0F + entity.bobOffs) * 0.1F + 0.1F;
      int lines = Minecraft.getInstance().options.graphicsMode().get() == GraphicsStatus.FANCY ? 10 : 5;
      float rotationTick = entity.getAge() / 500.0F;
      pose.pushPose();
      pose.translate(0.0F, offY + 0.25F, 0.0F);
      pose.pushPose();

      for (int i = 0; i < lines; i++) {
         pose.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0F));
         pose.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0F));
         pose.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0F));
         pose.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0F));
         pose.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0F));
         pose.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0F + rotationTick * 360.0F));
         float n10 = random.nextFloat() * 20.0F + 5.0F;
         float n11 = random.nextFloat() * 2.0F + 1.0F;
         float y = n10 / (30.0F / (Math.min(entity.getAge(), 10) / 10.0F));
         float xz = n11 / (30.0F / (Math.min(entity.getAge(), 10) / 10.0F));
         VertexConsumer consumer = source.getBuffer(RenderType.lineStrip());
         Matrix4f matrix4f = pose.last().pose();
         consumer.addVertex(matrix4f, 0.0F, 0.0F, 0.0F).setColor(255, 255, 255, 255).setNormal(0.0F, 0.0F, -1.0F);
         consumer.addVertex(matrix4f, -0.866F * xz, y, -0.5F * xz).setColor(255, 0, 255, 0).setNormal(0.0F, 0.0F, -1.0F);
         consumer.addVertex(matrix4f, 0.866F * xz, y, -0.5F * xz).setColor(255, 0, 255, 0).setNormal(0.0F, 0.0F, -1.0F);
         consumer.addVertex(matrix4f, 0.0F, y, xz).setColor(255, 0, 255, 0).setNormal(0.0F, 0.0F, -1.0F);
         consumer.addVertex(matrix4f, -0.866F * xz, y, -0.5F * xz).setColor(255, 0, 255, 0).setNormal(0.0F, 0.0F, -1.0F);
      }

      pose.popPose();
      pose.popPose();
      super.render(entity, p_115037_, pt, pose, source, brightnessOverride);
   }
}
