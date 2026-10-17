package thaumcraft.particles.fx;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ParticleUtils {
   public static final DeferredRegister<ParticleType<?>> REGISTER = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, "thaumcraft");
   public static final ResourceLocation PARTICLE_TEXTURE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/particles.png");
   public static final ResourceLocation PARTICLE_TEXTURE_2 = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/particles2.png");
   public static final ParticleRenderType TC_PARTICLE = new ParticleRenderType() {
      public BufferBuilder begin(Tesselator tesselator, TextureManager textureManager) {
         RenderSystem.setShader(GameRenderer::getParticleShader);
         RenderSystem.setShaderTexture(0, ParticleUtils.PARTICLE_TEXTURE);
         RenderSystem.disableCull();
         RenderSystem.enableBlend();
         RenderSystem.depthMask(false);
         RenderSystem.blendFunc(770, 771);
         return tesselator.begin(Mode.QUADS, DefaultVertexFormat.PARTICLE);
      }

      @Override
      public String toString() {
         return "THAUMCRAFT_PARTICLE";
      }
   };

   public static void addEffect(Particle particle) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft != null && minecraft.particleEngine != null) {
         if (minecraft.isSameThread()) {
            minecraft.particleEngine.add(particle);
         } else {
            minecraft.execute(() -> minecraft.particleEngine.add(particle));
         }
      }
   }

   public static boolean checkParticleDistance(Particle particle, int distance) {
      Minecraft mc = Minecraft.getInstance();
      GraphicsStatus gs = (GraphicsStatus)mc.options.graphicsMode().get();
      int d = gs == GraphicsStatus.FAST ? distance / 2 : distance;
      Entity camera = mc.getCameraEntity();
      if (camera == null) {
         return true;
      } else if (camera.distanceToSqr(particle.getPos()) > (double)d * d) {
         particle.remove();
         return false;
      } else {
         return true;
      }
   }

   public static void init(RegisterParticleProvidersEvent event) {
   }
}
