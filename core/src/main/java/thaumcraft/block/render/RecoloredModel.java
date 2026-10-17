package thaumcraft.block.render;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.render.ITIntColor;

@OnlyIn(Dist.CLIENT)
public class RecoloredModel implements BakedModel {
   private final BakedModel original;
   private final int[] colors;
   private final boolean stateColor;

   @OnlyIn(Dist.CLIENT)
   public RecoloredModel(BakedModel original, int... colors) {
      this.original = original;
      this.colors = colors;
      this.stateColor = false;
   }

   @OnlyIn(Dist.CLIENT)
   private RecoloredModel(BakedModel original, boolean stateColor) {
      this.original = original;
      this.colors = new int[0];
      this.stateColor = stateColor;
   }

   @OnlyIn(Dist.CLIENT)
   public static RecoloredModel fromBlockState(BakedModel original) {
      return new RecoloredModel(original, true);
   }

   @NotNull
   public List<BakedQuad> getQuads(BlockState state, Direction side, @NotNull RandomSource rand) {
      return this.recolorQuads(state, this.original.getQuads(state, side, rand));
   }

   @NotNull
   @OnlyIn(Dist.CLIENT)
   public List<BakedQuad> getQuads(
      @Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType
   ) {
      return this.recolorQuads(state, this.original.getQuads(state, side, rand, data, renderType));
   }

   private List<BakedQuad> recolorQuads(BlockState state, List<BakedQuad> baseQuads) {
      List<BakedQuad> quads = new ArrayList<>();

      for (int i = 0; i < baseQuads.size(); i++) {
         int color = this.getColor(state, i);
         quads.add(color == -1 ? baseQuads.get(i) : this.recolorQuad(baseQuads.get(i), color));
      }

      return quads;
   }

   private int getColor(BlockState state, int quadIndex) {
      if (this.stateColor) {
         return state != null && state.getBlock() instanceof ITIntColor color ? 0xFF000000 | color.getColor(0) : -1;
      } else {
         return this.colors[Mth.clamp(quadIndex, 0, this.colors.length - 1)];
      }
   }

   private BakedQuad recolorQuad(BakedQuad quad, int color) {
      int[] vertexData = (int[])quad.getVertices().clone();

      for (int i = 0; i < vertexData.length; i += 8) {
         vertexData[i + 3] = color;
      }

      return new BakedQuad(vertexData, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade());
   }

   public boolean useAmbientOcclusion() {
      return this.original.useAmbientOcclusion();
   }

   public boolean isGui3d() {
      return this.original.isGui3d();
   }

   public boolean usesBlockLight() {
      return this.original.usesBlockLight();
   }

   public boolean isCustomRenderer() {
      return this.original.isCustomRenderer();
   }

   @NotNull
   public TextureAtlasSprite getParticleIcon() {
      return this.original.getParticleIcon();
   }

   @NotNull
   public TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
      return this.original.getParticleIcon(data);
   }

   @NotNull
   @OnlyIn(Dist.CLIENT)
   public BakedModel applyTransform(@NotNull ItemDisplayContext transformType, @NotNull PoseStack poseStack, boolean applyLeftHandTransform) {
      BakedModel transformed = this.original.applyTransform(transformType, poseStack, applyLeftHandTransform);
      if (transformed == this.original) {
         return this;
      } else {
         return this.stateColor ? fromBlockState(transformed) : new RecoloredModel(transformed, this.colors);
      }
   }

   @NotNull
   public ItemOverrides getOverrides() {
      return this.original.getOverrides();
   }
}
