package thaumcraft.block.node_transducer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.api.ITickable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.blockentity.ThaumcraftBlockEntity;
import thaumcraft.api.nodes.NodeModifier;
import thaumcraft.api.nodes.NodeType;
import thaumcraft.block.airy.aura_node.AuraNodeBlockEntity;
import thaumcraft.block.airy.energized_aura.EnergizedAuraNodeBlock;
import thaumcraft.block.airy.energized_aura.NodeEnergizedBlockEntity;
import thaumcraft.block.node_stabilizer.NodeStabilizerBlock;
import thaumcraft.block.node_stabilizer.NodeStabilizerBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModSounds;

public class NodeTransducerBlockEntity extends ThaumcraftBlockEntity implements ITickable {
   public int count = -1;
   public int status = 0;
   private int skipTicks = 0;

   public NodeTransducerBlockEntity(BlockPos blockPos, BlockState blockState) {
      super((BlockEntityType<?>)ModBlockEntities.NODE_TRANSDUCER.get(), blockPos, blockState);
   }

   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      if (this.count == -1) {
         this.checkStatus(level, pos);
      }

      BlockPos nodePos = pos.below();

      if (this.status == 1 && this.count >= 1000) {
         BlockEntity tile = level.getBlockEntity(nodePos);
         if (tile instanceof AuraNodeBlockEntity node) {
            AspectList base = node.getAspectsBase().copy();
            if (base.size() == 0) {
               base = node.getAspects().copy();
            }
            NodeType type = node.getNodeType();
            NodeModifier mod = node.getNodeModifier();

            level.setBlock(nodePos, ((Block)ModBlocks.ENERGIZED_AURA_NODE.get()).defaultBlockState(), 3);
            BlockEntity tilenew = level.getBlockEntity(nodePos);
            if (tilenew instanceof NodeEnergizedBlockEntity energizedNode) {
               energizedNode.setNodeModifier(mod);
               energizedNode.setNodeType(type);
               energizedNode.setAspects(base.copy());
               energizedNode.setForcedPrimalVisAmount(node.getAwakenPrimalVisAmount());
               energizedNode.setupNode();
            }

            this.checkStatus(level, pos);
            level.blockEvent(pos, state.getBlock(), 10, 10);
            this.setChanged();
            this.markForUpdate();
         }
      }

      if (this.status == 2 && this.count <= 50) {
         BlockEntity tile = level.getBlockEntity(nodePos);
         if (tile instanceof NodeEnergizedBlockEntity energizedNode) {
            AspectList base = energizedNode.getAuraBase().copy();
            NodeType type = energizedNode.getNodeType();
            NodeModifier mod = energizedNode.getNodeModifier();
            int forced = energizedNode.getForcedPrimalVisAmount();

            level.setBlock(nodePos, ((Block)ModBlocks.AURA_NODE.get()).defaultBlockState(), 3);
            BlockEntity tilenew = level.getBlockEntity(nodePos);
            if (tilenew instanceof AuraNodeBlockEntity normalNode) {
               normalNode.setNodeModifier(mod);
               normalNode.setNodeType(type);
               normalNode.setAspects(new AspectList());
               normalNode.getAspectsBase().aspects.putAll(base.aspects);
               normalNode.setAwakenPrimalVisAmount(forced);
            }

            level.blockEvent(pos, state.getBlock(), 10, 10);
            this.setChanged();
            this.markForUpdate();
            this.status = 0;
         }
      }

      boolean powered = level.hasNeighborSignal(pos);
      if (this.status != 0 && powered) {
         if (this.count < 1000) {
            this.count++;
            BlockEntity tilenew = level.getBlockEntity(nodePos);
            if (tilenew instanceof AuraNodeBlockEntity nd) {
               AspectList al = nd.getAspects();
               if (al.size() > 0) {
                  Aspect[] aspects = al.getAspects();
                  nd.takeFromContainer(aspects[level.random.nextInt(aspects.length)], 1);
                  if (this.count % 5 == 0 || nd.getAspects().visSize() == 0) {
                     nd.setChanged();
                     nd.markForUpdate();
                  }
               }
            }
            if (this.count % 20 == 0) {
               this.setChanged();
               this.markForUpdate();
            }
         }
      } else if (this.count > 0) {
         this.count--;
         if (this.count % 20 == 0) {
            this.setChanged();
            this.markForUpdate();
         }
      }

      if (this.count > 1000) {
         this.count = 1000;
      }
   }

   public void checkStatus(Level level, BlockPos pos) {
      if (this.count == -1) {
         this.count = 0;
      }

      BlockPos nodePos = pos.below();
      BlockState nodeState = level.getBlockState(nodePos);
      boolean hasStabilizer = this.hasStabilizer(level, pos);
      boolean powered = level.hasNeighborSignal(pos);

      if (this.status != 2 || this.count <= 50 || (hasStabilizer && nodeState.is((Block)ModBlocks.ENERGIZED_AURA_NODE.get()))) {
         if (powered && nodeState.is((Block)ModBlocks.AURA_NODE.get()) && hasStabilizer) {
            this.status = 1;
            this.setChanged();
            this.markForUpdate();
         } else if (nodeState.is((Block)ModBlocks.ENERGIZED_AURA_NODE.get())) {
            this.status = 2;
            this.count = 1000;
            this.setChanged();
            this.markForUpdate();
         } else {
            this.status = 0;
         }
      } else {
         EnergizedAuraNodeBlock.explodify(level, nodePos);
         this.status = 0;
         this.count = 50;
         this.setChanged();
         this.markForUpdate();
      }
   }

   @Override
   public void clientTick(Level level, BlockPos pos, BlockState state) {
      switch (this.status) {
         case 0:
            if (this.count > 0) {
               this.count--;
            }
            break;
         case 1:
            if (this.count < 1000) {
               this.count++;
            }
            break;
         case 2:
            this.count = 1000;
      }

      if (this.status == 1 && this.count > 50 && this.count < 1000) {
         boolean hasStabilizer = this.hasStabilizer(level, pos);
         if (level.random.nextBoolean()) {
            Thaumcraft.setup
               .nodeBolt(
                  level,
                  this.getX() + 0.25F + level.random.nextFloat() * 0.5F,
                  this.getY() + 0.5F,
                  this.getZ() + 0.25F + level.random.nextFloat() * 0.5F,
                  this.getX() + 0.5F,
                  this.getY() - 0.5F,
                  this.getZ() + 0.5F
               );
         }

         if (level.random.nextBoolean() && hasStabilizer) {
            Thaumcraft.setup
               .nodeBolt(
                  level,
                  this.getX() + 0.25F + level.random.nextFloat() * 0.5F,
                  this.getY() - 1.5F,
                  this.getZ() + 0.25F + level.random.nextFloat() * 0.5F,
                  this.getX() + 0.5F,
                  this.getY() - 0.5F,
                  this.getZ() + 0.5F
               );
         }
      }
   }

   /** TC4 checks only for the stabilizer tile two blocks down; with E12 it must also face the node. */
   private boolean hasStabilizerBlock(Level lvl, BlockPos p) {
      BlockState stabilizer = lvl.getBlockState(p.below(2));
      return lvl.getBlockEntity(p.below(2)) instanceof NodeStabilizerBlockEntity && stabilizer.getValue(NodeStabilizerBlock.FACING) == Direction.UP;
   }

   private boolean hasStabilizer(Level lvl, BlockPos p) {
      return this.hasStabilizerBlock(lvl, p) && !lvl.hasNeighborSignal(p.below(2));
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound) {
      super.loadCustom(compound);
      this.status = compound.getInt("status");
      this.count = compound.getInt("count");
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound) {
      super.saveCustom(compound);
      compound.putInt("status", this.status);
      compound.putInt("count", this.count);
   }

   @Nullable
   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   /** Block events must return true on the server as well: only then does the server send them to clients. */
   @Override
   public boolean triggerEvent(int i, int j) {
      if (i == 10 && j == 10) {
         if (this.level.isClientSide) {
            Thaumcraft.setup.burst(this.level, this.getX() + 0.5, this.getY() - 0.5, this.getZ() + 0.5, 1.0F);
            this.level.playLocalSound(this.worldPosition, (SoundEvent)ModSounds.CRAFT_FAIL.get(), SoundSource.BLOCKS, 0.5F, 1.0F, false);
         }

         return true;
      } else {
         return super.triggerEvent(i, j);
      }
   }
}
