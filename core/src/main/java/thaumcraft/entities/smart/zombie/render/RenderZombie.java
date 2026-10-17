package thaumcraft.entities.smart.zombie.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.ZombieVillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import thaumcraft.entities.smart.zombie.EntityBrainyZombie;
import thaumcraft.entities.smart.zombie.EntityGiantBrainyZombie;
import thaumcraft.entities.smart.zombie.EntityInhabitedZombie;

@OnlyIn(Dist.CLIENT)
public class RenderZombie<T extends Zombie> extends HumanoidMobRenderer<T, HumanoidModel<T>> {
   public static final ResourceLocation TEX_INHABITED = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/czombie.png");
   public static final ResourceLocation TEX_BRAINY = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/bzombie.png");
   public static final ResourceLocation TEX_BRAINY_VILLAGER = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/bzombievil.png");
   private final HumanoidModel<T> normalModel = (HumanoidModel<T>)this.model;
   private final HumanoidModel<T> villagerModel;

   public RenderZombie(Context context) {
      super(context, new ZombieModel(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5F);
      this.villagerModel = new ZombieVillagerModel(context.bakeLayer(ModelLayers.ZOMBIE_VILLAGER));
      this.addLayer(
         new HumanoidArmorLayer(
            this,
            new ZombieModel(context.bakeLayer(ModelLayers.ZOMBIE_INNER_ARMOR)),
            new ZombieModel(context.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR)),
            context.getModelManager()
         )
      );
   }

   @OnlyIn(Dist.CLIENT)
   public void render(@NotNull T entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
      if (entity instanceof EntityBrainyZombie brainy && brainy.isVillager()) {
         this.model = this.villagerModel;
      } else {
         this.model = this.normalModel;
      }

      super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
   }

   @NotNull
   public ResourceLocation getTextureLocation(@NotNull T entity) {
      if (entity instanceof EntityInhabitedZombie) {
         return TEX_INHABITED;
      } else if (entity instanceof EntityBrainyZombie brainy) {
         return brainy.isVillager() ? TEX_BRAINY_VILLAGER : TEX_BRAINY;
      } else {
         return TEX_BRAINY;
      }
   }

   @OnlyIn(Dist.CLIENT)
   protected void scale(@NotNull T entity, @NotNull PoseStack poseStack, float partialTickTime) {
      super.scale(entity, poseStack, partialTickTime);
      if (entity instanceof EntityGiantBrainyZombie gz) {
         float scale = gz.getAnger();
         poseStack.scale(scale, scale, scale);
      }
   }
}
