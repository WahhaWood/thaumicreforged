package thaumcraft.block.platform.render;

import com.google.gson.JsonObject;
import java.util.function.Function;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class PlatformBlockUnbakedGeometry implements IUnbakedGeometry<PlatformBlockUnbakedGeometry> {
   private final ResourceLocation particle;
   private final ResourceLocation fallbackModel;

   public PlatformBlockUnbakedGeometry(JsonObject json) {
      this.particle = ResourceLocation.tryParse(GsonHelper.getAsString(json, "particle", "thaumcraft:block/platform"));
      this.fallbackModel = ResourceLocation.tryParse(GsonHelper.getAsString(json, "fallback", "thaumcraft:block/platform_base"));
   }

   @OnlyIn(Dist.CLIENT)
   public BakedModel bake(
      IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides
   ) {
      TextureAtlasSprite particleSprite = spriteGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, this.particle));
      BakedModel bakedFallback = baker.bake(this.fallbackModel, modelState, spriteGetter);
      return new PlatformBlockBakedModel(particleSprite, bakedFallback);
   }

   public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
      modelGetter.apply(this.fallbackModel).resolveParents(modelGetter);
   }
}
