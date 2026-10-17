package thaumcraft.block.opaque.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class WardedGlassBakedModel implements IDynamicBakedModel {
   public static final ModelProperty<int[]> CTM = new ModelProperty();
   private final int[] connectedTextureRefByID = new int[]{
      0,
      0,
      6,
      6,
      0,
      0,
      6,
      6,
      3,
      3,
      19,
      15,
      3,
      3,
      19,
      15,
      1,
      1,
      18,
      18,
      1,
      1,
      13,
      13,
      2,
      2,
      23,
      31,
      2,
      2,
      27,
      14,
      0,
      0,
      6,
      6,
      0,
      0,
      6,
      6,
      3,
      3,
      19,
      15,
      3,
      3,
      19,
      15,
      1,
      1,
      18,
      18,
      1,
      1,
      13,
      13,
      2,
      2,
      23,
      31,
      2,
      2,
      27,
      14,
      4,
      4,
      5,
      5,
      4,
      4,
      5,
      5,
      17,
      17,
      22,
      26,
      17,
      17,
      22,
      26,
      16,
      16,
      20,
      20,
      16,
      16,
      28,
      28,
      21,
      21,
      46,
      42,
      21,
      21,
      43,
      38,
      4,
      4,
      5,
      5,
      4,
      4,
      5,
      5,
      9,
      9,
      30,
      12,
      9,
      9,
      30,
      12,
      16,
      16,
      20,
      20,
      16,
      16,
      28,
      28,
      25,
      25,
      45,
      37,
      25,
      25,
      40,
      32,
      0,
      0,
      6,
      6,
      0,
      0,
      6,
      6,
      3,
      3,
      19,
      15,
      3,
      3,
      19,
      15,
      1,
      1,
      18,
      18,
      1,
      1,
      13,
      13,
      2,
      2,
      23,
      31,
      2,
      2,
      27,
      14,
      0,
      0,
      6,
      6,
      0,
      0,
      6,
      6,
      3,
      3,
      19,
      15,
      3,
      3,
      19,
      15,
      1,
      1,
      18,
      18,
      1,
      1,
      13,
      13,
      2,
      2,
      23,
      31,
      2,
      2,
      27,
      14,
      4,
      4,
      5,
      5,
      4,
      4,
      5,
      5,
      17,
      17,
      22,
      26,
      17,
      17,
      22,
      26,
      7,
      7,
      24,
      24,
      7,
      7,
      10,
      10,
      29,
      29,
      44,
      41,
      29,
      29,
      39,
      33,
      4,
      4,
      5,
      5,
      4,
      4,
      5,
      5,
      9,
      9,
      30,
      12,
      9,
      9,
      30,
      12,
      7,
      7,
      24,
      24,
      7,
      7,
      10,
      10,
      8,
      8,
      36,
      35,
      8,
      8,
      34,
      11
   };
   private final TextureAtlasSprite[] sprites;
   private final TextureAtlasSprite particle;
   private final ItemOverrides overrides;
   private final ItemTransforms transforms;
   private final boolean ao;
   private final boolean gui3d;
   private final boolean blockLight;
   private final List<BakedQuad>[][] faceQuads;
   private final List<BakedQuad> itemQuads;

   public WardedGlassBakedModel(
      TextureAtlasSprite[] sprites,
      TextureAtlasSprite particle,
      ItemOverrides overrides,
      ItemTransforms transforms,
      ModelState modelState,
      ResourceLocation modelLocation,
      boolean ao,
      boolean gui3d,
      boolean blockLight
   ) {
      this.sprites = sprites;
      this.particle = particle;
      this.overrides = overrides;
      this.transforms = transforms;
      this.ao = ao;
      this.gui3d = gui3d;
      this.blockLight = blockLight;
      this.faceQuads = new List[6][47];
      FaceBakery bakery = new FaceBakery();
      BlockFaceUV uv = new BlockFaceUV(new float[]{0.0F, 0.0F, 16.0F, 16.0F}, 0);
      Vector3f from = new Vector3f(0.0F, 0.0F, 0.0F);
      Vector3f to = new Vector3f(16.0F, 16.0F, 16.0F);

      for (Direction dir : Direction.values()) {
         int di = dir.get3DDataValue();

         for (int si = 0; si < 47; si++) {
            BlockElementFace face = new BlockElementFace(dir, -1, "", uv);
            BakedQuad quad = bakery.bakeQuad(from, to, face, sprites[si], dir, modelState, null, true);
            this.faceQuads[di][si] = Collections.singletonList(quad);
         }
      }

      List<BakedQuad> iq = new ArrayList<>();

      for (Direction dir : Direction.values()) {
         iq.addAll(this.faceQuads[dir.get3DDataValue()][0]);
      }

      this.itemQuads = Collections.unmodifiableList(iq);
   }

   @NotNull
   @OnlyIn(Dist.CLIENT)
   public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable RenderType renderType) {
      if (state == null) {
         return this.itemQuads;
      } else if (side == null) {
         return Collections.emptyList();
      } else {
         int[] idx = (int[])data.get(CTM);
         int si = idx != null ? idx[side.get3DDataValue()] : 0;
         if (si < 0 || si >= 47) {
            si = 0;
         }

         return this.faceQuads[side.get3DDataValue()][si];
      }
   }

   @NotNull
   public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
      if (state == null) {
         return this.itemQuads;
      } else {
         return side == null ? Collections.emptyList() : this.faceQuads[side.get3DDataValue()][0];
      }
   }

   @NotNull
   public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
      int[] idx = new int[6];

      for (Direction dir : Direction.values()) {
         idx[dir.get3DDataValue()] = this.computeFaceIndex(level, pos, state, dir);
      }

      return modelData.derive().with(CTM, idx).build();
   }

   private int computeFaceIndex(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction dir) {
      boolean[] a = new boolean[8];
      switch (dir) {
         case DOWN:
         case UP:
            a[0] = this.isSame(level, pos.offset(-1, 0, -1), state);
            a[1] = this.isSame(level, pos.offset(0, 0, -1), state);
            a[2] = this.isSame(level, pos.offset(1, 0, -1), state);
            a[3] = this.isSame(level, pos.offset(-1, 0, 0), state);
            a[4] = this.isSame(level, pos.offset(1, 0, 0), state);
            a[5] = this.isSame(level, pos.offset(-1, 0, 1), state);
            a[6] = this.isSame(level, pos.offset(0, 0, 1), state);
            a[7] = this.isSame(level, pos.offset(1, 0, 1), state);
            break;
         case NORTH:
         case SOUTH:
            int x0 = dir == Direction.NORTH ? 1 : -1;
            int x2 = dir == Direction.NORTH ? -1 : 1;
            a[0] = this.isSame(level, pos.offset(x0, 1, 0), state);
            a[1] = this.isSame(level, pos.offset(0, 1, 0), state);
            a[2] = this.isSame(level, pos.offset(x2, 1, 0), state);
            a[3] = this.isSame(level, pos.offset(x0, 0, 0), state);
            a[4] = this.isSame(level, pos.offset(x2, 0, 0), state);
            a[5] = this.isSame(level, pos.offset(x0, -1, 0), state);
            a[6] = this.isSame(level, pos.offset(0, -1, 0), state);
            a[7] = this.isSame(level, pos.offset(x2, -1, 0), state);
            break;
         case WEST:
         case EAST:
            int z0 = dir == Direction.EAST ? 1 : -1;
            int z2 = dir == Direction.EAST ? -1 : 1;
            a[0] = this.isSame(level, pos.offset(0, 1, z0), state);
            a[1] = this.isSame(level, pos.offset(0, 1, 0), state);
            a[2] = this.isSame(level, pos.offset(0, 1, z2), state);
            a[3] = this.isSame(level, pos.offset(0, 0, z0), state);
            a[4] = this.isSame(level, pos.offset(0, 0, z2), state);
            a[5] = this.isSame(level, pos.offset(0, -1, z0), state);
            a[6] = this.isSame(level, pos.offset(0, -1, 0), state);
            a[7] = this.isSame(level, pos.offset(0, -1, z2), state);
      }

      if (dir == Direction.DOWN) {
         boolean t0 = a[0];
         boolean t1 = a[1];
         boolean t2 = a[2];
         a[0] = a[5];
         a[1] = a[6];
         a[2] = a[7];
         a[5] = t0;
         a[6] = t1;
         a[7] = t2;
      }

      if (!a[1] || !a[3]) {
         a[0] = false;
      }

      if (!a[1] || !a[4]) {
         a[2] = false;
      }

      if (!a[6] || !a[3]) {
         a[5] = false;
      }

      if (!a[6] || !a[4]) {
         a[7] = false;
      }

      int mask = 0;

      for (int i = 0; i < 8; i++) {
         if (a[i]) {
            mask |= 1 << i;
         }
      }

      return mask >= 0 && mask < this.connectedTextureRefByID.length ? this.connectedTextureRefByID[mask] : 0;
   }

   private boolean isSame(BlockAndTintGetter level, BlockPos pos, BlockState self) {
      return level.getBlockState(pos).getBlock() == self.getBlock();
   }

   public boolean useAmbientOcclusion() {
      return this.ao;
   }

   public boolean isGui3d() {
      return this.gui3d;
   }

   public boolean usesBlockLight() {
      return this.blockLight;
   }

   public boolean isCustomRenderer() {
      return false;
   }

   @NotNull
   public TextureAtlasSprite getParticleIcon() {
      return this.particle;
   }

   @NotNull
   public TextureAtlasSprite getParticleIcon(ModelData data) {
      return this.particle;
   }

   @NotNull
   public ItemOverrides getOverrides() {
      return this.overrides;
   }

   @NotNull
   public ItemTransforms getTransforms() {
      return this.transforms;
   }

   @NotNull
   @OnlyIn(Dist.CLIENT)
   public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
      return ChunkRenderTypeSet.of(new RenderType[]{RenderType.translucent()});
   }
}
