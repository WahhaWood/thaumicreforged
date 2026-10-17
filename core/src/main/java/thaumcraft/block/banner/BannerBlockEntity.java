package thaumcraft.block.banner;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.blockentity.ThaumcraftBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;

public class BannerBlockEntity extends ThaumcraftBlockEntity {
   private Aspect aspect = null;
   private DyeColor baseColor = ((AbstractBannerBlock)this.getBlockState().getBlock()).getColor();

   public BannerBlockEntity(BlockPos blockPos, BlockState blockState) {
      super((BlockEntityType<?>)ModBlockEntities.BANNER.get(), blockPos, blockState);
   }

   @OnlyIn(Dist.CLIENT)
   public AABB getRenderBoundingBox() {
      return new AABB(
         this.worldPosition.getX(),
         this.worldPosition.getY(),
         this.worldPosition.getZ(),
         this.worldPosition.getX() + 1.0,
         this.worldPosition.getY() + 2.0,
         this.worldPosition.getZ() + 1.0
      );
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound) {
      String as = compound.getString("aspect");
      if (as != null && as.length() > 0) {
         this.setAspect(Aspect.getAspect(as));
      } else {
         this.aspect = null;
      }
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound) {
      compound.putString("aspect", this.getAspect() == null ? "" : this.getAspect().getTag());
   }

   public Aspect getAspect() {
      return this.aspect;
   }

   public void setAspect(Aspect aspect) {
      this.aspect = aspect;
   }

   public DyeColor getColor() {
      return this.baseColor;
   }

   @OnlyIn(Dist.CLIENT)
   public void fromColor(DyeColor color) {
      this.baseColor = color;
   }
}
