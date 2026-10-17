package thaumcraft.block.opaque.render;

import com.google.gson.JsonObject;
import java.util.function.Function;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import thaumcraft.block.warded.render.WardedBlockBakedModel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class WardedBlockUnbakedGeometry implements IUnbakedGeometry<WardedBlockUnbakedGeometry> {
   private final ResourceLocation particle;

   public WardedBlockUnbakedGeometry(JsonObject json) {
      String particle = GsonHelper.getAsString(json, "particle", "minecraft:block/stone");
      this.particle = ResourceLocation.tryParse(particle);
   }

   @OnlyIn(Dist.CLIENT)
   public BakedModel bake(
      IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides
   ) {
      TextureAtlasSprite particleSprite = spriteGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, this.particle));
      return new WardedBlockBakedModel(particleSprite);
   }
}
