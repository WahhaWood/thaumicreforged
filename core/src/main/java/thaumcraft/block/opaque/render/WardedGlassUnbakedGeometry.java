package thaumcraft.block.opaque.render;

import java.util.function.Function;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class WardedGlassUnbakedGeometry implements IUnbakedGeometry<WardedGlassUnbakedGeometry> {
   private final ResourceLocation texturePrefix;
   private final ResourceLocation particle;

   public WardedGlassUnbakedGeometry(ResourceLocation texturePrefix, ResourceLocation particle) {
      this.texturePrefix = texturePrefix;
      this.particle = particle;
   }

   @OnlyIn(Dist.CLIENT)
   public BakedModel bake(
      IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides
   ) {
      TextureAtlasSprite[] sprites = new TextureAtlasSprite[47];

      for (int i = 0; i < 47; i++) {
         ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(this.texturePrefix.getNamespace(), this.texturePrefix.getPath() + (i + 1));
         sprites[i] = spriteGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, rl));
      }

      TextureAtlasSprite particleSprite = spriteGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, this.particle));
      ItemTransforms transforms = context.getTransforms();
      return new WardedGlassBakedModel(
         sprites,
         particleSprite,
         overrides,
         transforms,
         modelState,
         ResourceLocation.fromNamespaceAndPath(this.texturePrefix.getNamespace(), this.texturePrefix.getPath()),
         context.useAmbientOcclusion(),
         context.isGui3d(),
         context.useBlockLight()
      );
   }
}
