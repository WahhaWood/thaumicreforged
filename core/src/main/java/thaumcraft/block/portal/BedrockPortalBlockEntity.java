package thaumcraft.block.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.ITickable;
import thaumcraft.init.registry.ModBlockEntities;

public class BedrockPortalBlockEntity extends BlockEntity implements ITickable {
   public BedrockPortalBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)ModBlockEntities.BEDROCK_PORTAL.get(), pos, state);
   }
}
