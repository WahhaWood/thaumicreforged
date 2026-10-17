package thaumcraft.block.cosmetic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.ITickable;
import thaumcraft.init.registry.ModBlockEntities;

public class WardingStoneBlockEntity extends BlockEntity implements ITickable {
   private static final TagKey<Block> GLIMMER = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("thaumcraft", "glimmer_of_light"));
   private int count = 0;

   public WardingStoneBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)ModBlockEntities.WARDING_STONE.get(), pos, state);
   }

   public boolean gettingPower() {
      return this.level == null ? false : this.level.hasNeighborSignal(this.worldPosition);
   }
}
