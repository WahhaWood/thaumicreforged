package thaumcraft.dimension.outer_lands;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.DimensionSpecialEffects.SkyType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class OuterLandsEffects extends DimensionSpecialEffects {
   public OuterLandsEffects() {
      super(1.0F, false, SkyType.NONE, false, false);
   }

   @NotNull
   public Vec3 getBrightnessDependentFogColor(Vec3 biomeFogColor, float sunHeight) {
      return new Vec3(0.09411764705882353, 0.07529411764705882, 0.09411764705882353);
   }

   public boolean isFoggyAt(int x, int z) {
      return true;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean renderClouds(
      ClientLevel level, int ticks, float partialTick, PoseStack poseStack, double camX, double camY, double camZ, Matrix4f projectionMatrix
   ) {
      return true;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean renderSky(
      ClientLevel level, int ticks, float partialTick, PoseStack poseStack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog
   ) {
      return true;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean renderSnowAndRain(ClientLevel level, int ticks, float partialTick, LightTexture lightTexture, double camX, double camY, double camZ) {
      return true;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean tickRain(ClientLevel level, int ticks, Camera camera) {
      return true;
   }
}
