package thaumcraft.block.platform.render;

import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;
import thaumcraft.block.platform.PlatformBlock;
import thaumcraft.block.platform.PlatformBlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class PlatformBlockBakedModel implements IDynamicBakedModel {
   private static final ModelProperty<BlockState> MIMIC = new ModelProperty();
   private final TextureAtlasSprite particle;
   private final BakedModel fallbackModel;

   @OnlyIn(Dist.CLIENT)
   public PlatformBlockBakedModel(TextureAtlasSprite particle, BakedModel fallbackModel) {
      this.particle = particle;
      this.fallbackModel = fallbackModel;
   }

   public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
      if (level.getBlockEntity(pos) instanceof PlatformBlockEntity platform) {
         BlockState mimic = platform.getMimic();
         if (mimic != null && !mimic.isAir() && !(mimic.getBlock() instanceof PlatformBlock)) {
            return modelData.derive().with(MIMIC, mimic).build();
         }
      }

      return modelData.derive().with(MIMIC, Blocks.AIR.defaultBlockState()).build();
   }

   @OnlyIn(Dist.CLIENT)
   private List<BakedQuad> quads(@Nullable Direction side, RandomSource rand, ModelData data, @Nullable RenderType renderType) {
      BlockState mimic = (BlockState)data.get(MIMIC);
      if (mimic != null && !mimic.isAir() && !(mimic.getBlock() instanceof PlatformBlock)) {
         BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(mimic);
         return model == this ? Collections.emptyList() : model.getQuads(mimic, side, rand, ModelData.EMPTY, renderType);
      } else {
         return this.fallbackModel != null && this.fallbackModel != this
            ? this.fallbackModel.getQuads(null, side, rand, ModelData.EMPTY, renderType)
            : Collections.emptyList();
      }
   }

   public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
      return this.quads(side, rand, ModelData.EMPTY, null);
   }

   @OnlyIn(Dist.CLIENT)
   public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable RenderType renderType) {
      return this.quads(side, rand, data, renderType);
   }

   public TextureAtlasSprite getParticleIcon() {
      return this.particle;
   }

   public TextureAtlasSprite getParticleIcon(ModelData data) {
      return this.particle;
   }

   public boolean useAmbientOcclusion() {
      return true;
   }

   public boolean isGui3d() {
      return true;
   }

   public boolean usesBlockLight() {
      return true;
   }

   public boolean isCustomRenderer() {
      return false;
   }

   public ItemOverrides getOverrides() {
      return ItemOverrides.EMPTY;
   }

   @OnlyIn(Dist.CLIENT)
   public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
      return ChunkRenderTypeSet.of(new RenderType[]{RenderType.solid(), RenderType.cutout(), RenderType.cutoutMipped(), RenderType.translucent()});
   }
}
