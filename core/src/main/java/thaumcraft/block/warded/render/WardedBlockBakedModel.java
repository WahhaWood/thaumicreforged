package thaumcraft.block.warded.render;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
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
import thaumcraft.block.warded.WardedBlockEntity;
import thaumcraft.init.registry.ModBlocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class WardedBlockBakedModel implements IDynamicBakedModel {
   private static final ModelProperty<BlockState> MIMIC = new ModelProperty();
   private final TextureAtlasSprite particle;

   @OnlyIn(Dist.CLIENT)
   public WardedBlockBakedModel(TextureAtlasSprite particle) {
      this.particle = particle;
   }

   public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
      if (level.getBlockEntity(pos) instanceof WardedBlockEntity tw) {
         BlockState mimic = tw.getMimic();
         if (mimic != null && mimic.getBlock() != ModBlocks.WARDED.get()) {
            return modelData.derive().with(MIMIC, mimic).build();
         }
      }

      return modelData.derive().with(MIMIC, Blocks.STONE.defaultBlockState()).build();
   }

   @OnlyIn(Dist.CLIENT)
   private List<BakedQuad> quads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData, @Nullable RenderType renderType) {
      BlockState mimic = (BlockState)extraData.get(MIMIC);
      if (mimic == null) {
         mimic = Blocks.STONE.defaultBlockState();
      }

      if (mimic.getBlock() == ModBlocks.WARDED.get()) {
         return Collections.emptyList();
      } else {
         BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(mimic);
         return model.getQuads(mimic, side, rand, ModelData.EMPTY, renderType);
      }
   }

   public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
      return this.quads(state, side, rand, ModelData.EMPTY, null);
   }

   @OnlyIn(Dist.CLIENT)
   public List<BakedQuad> getQuads(
      @Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData, @Nullable RenderType renderType
   ) {
      return this.quads(state, side, rand, extraData, renderType);
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
