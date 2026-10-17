package thaumcraft.block.airy.warding_aura;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.init.registry.ModBlockEntities;

public class WardingStoneFenceBlockEntity extends BlockEntity {
   private static final TagKey<Block> SUPPORT = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("thaumcraft", "warding_stone_support"));
   private int count = 0;

   public WardingStoneFenceBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)ModBlockEntities.WARDING_AURA.get(), pos, state);
   }
}
