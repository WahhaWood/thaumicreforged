package thaumcraft.block.airy.aura_node;

import java.awt.Color;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.api.ITickable;
import thaumcraft.api.LevelBlockPos;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.blockentity.ThaumcraftBlockEntity;
import thaumcraft.api.nodes.INode;
import thaumcraft.api.nodes.NodeModifier;
import thaumcraft.api.nodes.NodeType;
import thaumcraft.api.research.ScanResult;
import thaumcraft.api.wands.IWandable;
import thaumcraft.block.node_stabilizer.NodeStabilizerBlock;
import thaumcraft.entities.brainless.aspect_orb.AspectOrbEntity;
import thaumcraft.entities.smart.zombie.EntityGiantBrainyZombie;
import thaumcraft.init.registry.ModBiomes;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.item.compass_stone.ItemCompassStone;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.network.PacketHandler;
import thaumcraft.network.fx.PacketFXBlockZap;
import thaumcraft.research.ResearchManager;
import thaumcraft.research.ScanManager;
import thaumcraft.util.Utils;

public class AuraNodeBlockEntity extends ThaumcraftBlockEntity implements INode, IWandable, ITickable {
   long lastActive = 0L;
   AspectList aspects = new AspectList(256);
   AspectList aspectsBase = new AspectList(256);
   private int awakenPrimalVisAmount = 0;
   public static HashMap<String, LevelBlockPos> locations = new HashMap<>();
   private NodeType nodeType = NodeType.NORMAL;
   private NodeModifier nodeModifier = null;
   int count = 0;
   int regeneration = -1;
   int wait = 0;
   String id = null;
   byte nodeLock = 0;
   boolean catchUp = false;
   public Entity drainEntity = null;
   public HitResult drainCollision = null;
   public int drainColor = 16777215;
   public Color targetColor = new Color(16777215);
   public Color color = new Color(16777215);
   private WeakReference<FakePlayer> fakePlayer;

   public AuraNodeBlockEntity(BlockPos blockPos, BlockState blockState) {
      this((BlockEntityType<?>)ModBlockEntities.AURA_NODE.get(), blockPos, blockState);
   }

   public AuraNodeBlockEntity(BlockEntityType<?> type, BlockPos blockPos, BlockState blockState) {
      super(type, blockPos, blockState);
   }

   @Override
   public void onLoad() {
      super.onLoad();
      this.ensureServerId();
      this.registerLocation();
   }

   @Override
   public void clearRemoved() {
      super.clearRemoved();
      this.ensureServerId();
      this.registerLocation();
   }

   @Override
   public String getId() {
      if ((this.id == null || this.id.isEmpty()) && this.level != null) {
         if (!this.level.isClientSide) {
            this.generateId();
         } else {
            this.id = this.level.dimension().location()
               + ":" + this.worldPosition.getX()
               + ":" + this.worldPosition.getY()
               + ":" + this.worldPosition.getZ()
               + ":client";
         }
      }
      this.registerLocation();
      return this.id == null ? "" : this.id;
   }

   public void setId(String id) {
      String previousId = this.id;
      this.id = id;
      this.updateLocationRegistration(previousId);
   }

   public String generateId() {
      if (this.level != null) {
         // Детерминированный id по позиции: стабилен между перезаходами, не требует setChanged()
         // (случайный суффикс убран — он провоцировал setChanged во время конструирования чанка
         // и вешал серверный поток насмерть через getChunk->managedBlock).
         this.setId(
            this.level.dimension().location()
               + ":"
               + this.worldPosition.getX()
               + ":"
               + this.worldPosition.getY()
               + ":"
               + this.worldPosition.getZ()
         );
      }

      return this.getId();
   }

   private void ensureServerId() {
      if ((this.id == null || this.id.isEmpty()) && this.level != null && !this.level.isClientSide) {
         this.generateId();
      }
   }

   private void updateLocationRegistration(String previousId) {
      if (locations != null) {
         if (previousId != null && !previousId.isEmpty() && !previousId.equals(this.id)) {
            locations.remove(previousId);
         }

         this.registerLocation();
      }
   }

   private void registerLocation() {
      if (this.id != null && !this.id.isEmpty() && this.level != null && locations != null) {
         locations.put(this.id, new LevelBlockPos(this));
      }
   }

   public void onChunkUnloaded() {
      if (locations != null && this.id != null) {
         locations.remove(this.id);
      }

      super.onChunkUnloaded();
   }

   public void setRemoved() {
      if (locations != null && this.id != null) {
         locations.remove(this.id);
      }

      super.setRemoved();
   }

   /** Серверная часть узла: id, регистрация в вис-сети, замок стабилизатора, регенерация, голодный узел (порт TileNode). */
   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      this.ensureServerId();
      boolean change = false;
      change = this.handleHungryNodeFirst(change);
      this.count++;
      this.checkLock();
      change = this.handleDischarge(change);
      change = this.handleRecharge(change);
      change = this.handleTaintNode(change);
      change = this.handleNodeStability(change);
      change = this.handleDarkNode(change);
      change = this.handlePureNode(change);
      change = this.handleHungryNodeSecond(change);
      if (this.count <= 1 || this.count % 50 == 0) {
         this.registerLocation();
      }
      if (change) {
         this.setChanged();
         this.markForUpdate();
      }
   }

   private boolean handleHungryNodeFirst(boolean change) {
      if (this.getNodeType() == NodeType.HUNGRY && this.level != null) {
         AABB aabb = new AABB(this.worldPosition).inflate(15.0);
         List<Entity> ents = this.level.getEntitiesOfClass(Entity.class, aabb);
         if (ents != null && !ents.isEmpty()) {
            for (Entity eo : ents) {
               if (!(eo instanceof Player player && player.isCreative())) {
                  if (eo.isAlive() && !eo.isInvulnerable()) {
                     double d = this.worldPosition.getCenter().distanceTo(eo.position());
                     if (d < 2.0) {
                        eo.hurt(this.level.damageSources().magic(), 1.0F);
                        if (!eo.isAlive()) {
                           ScanResult scan = ScanResult.ofEntity(eo);
                           AspectList al = ScanManager.getScanAspects(scan, this.level);
                           if (al != null && al.size() > 0) {
                              al = ResearchManager.reduceToPrimals(al.copy());
                              if (al != null && al.size() > 0) {
                                 Aspect a = al.getAspects()[this.level.random.nextInt(al.size())];
                                 if (this.getAspects().getAmount(a) < this.getNodeVisBase(a)) {
                                    this.addToContainer(a, 1);
                                    change = true;
                                 } else if (this.level.random.nextInt(1 + this.getNodeVisBase(a) * 2) < al.getAmount(a)) {
                                    this.aspectsBase.add(a, 1);
                                    change = true;
                                 }
                              }
                           }
                        }
                     }
                  }

                  double var3 = (this.worldPosition.getX() + 0.5 - eo.getX()) / 15.0;
                  double var5 = (this.worldPosition.getY() + 0.5 - eo.getY()) / 15.0;
                  double var7 = (this.worldPosition.getZ() + 0.5 - eo.getZ()) / 15.0;
                  double var9 = Math.sqrt(var3 * var3 + var5 * var5 + var7 * var7);
                  double var11 = 1.0 - var9;
                  if (var11 > 0.0) {
                     var11 *= var11;
                     eo.setDeltaMovement(eo.getDeltaMovement().add(
                        var3 / var9 * var11 * 0.15,
                        var5 / var9 * var11 * 0.25,
                        var7 / var9 * var11 * 0.15
                     ));
                  }
               }
            }
         }
      }
      return change;
   }

   private boolean handleHungryNodeSecond(boolean change) {
      if (this.getNodeType() == NodeType.HUNGRY && this.count % 50 == 0 && this.level != null) {
         if (!NodeConfig.disableNodeHungryBreakBlocks) {
            RandomSource rand = this.level.random;
            int tx = this.worldPosition.getX() + rand.nextInt(16) - rand.nextInt(16);
            int ty = this.worldPosition.getY() + rand.nextInt(16) - rand.nextInt(16);
            int tz = this.worldPosition.getZ() + rand.nextInt(16) - rand.nextInt(16);
            ty = Math.min(ty, this.level.getHeight(Heightmap.Types.WORLD_SURFACE, tx, tz));
            BlockPos targetPos = new BlockPos(tx, ty, tz);
            Vec3 v1 = this.worldPosition.getCenter();
            Vec3 v2 = targetPos.getCenter();
            if (ThaumcraftApiHelper.rayTraceIgnoringSource(this.level, v1, v2, true, false, false) instanceof BlockHitResult mop
               && mop.getBlockPos().distSqr(this.worldPosition) < 256.0) {
               BlockPos bp = mop.getBlockPos();
               BlockState bi = this.level.getBlockState(bp);
               if (!bi.isAir()) {
                  float h = bi.getDestroySpeed(this.level, bp);
                  if (h >= 0.0F && h < 5.0F && !bi.is((Block)ModBlocks.AURA_NODE.get()) && !bi.is((Block)ModBlocks.ENERGIZED_AURA_NODE.get())) {
                     this.level.destroyBlock(bp, true);
                  }
               }
            }
         }
      }
      return change;
   }

   private boolean handleRecharge(boolean change) {
      if (this.regeneration < 0) {
         this.regeneration = 600;
         if (this.getNodeModifier() != null) {
            switch (this.getNodeModifier()) {
               case BRIGHT -> this.regeneration = 400;
               case PALE -> this.regeneration = 900;
               case FADING -> this.regeneration = 0;
            }
         }

         if (this.getLock() == 1) {
            this.regeneration *= 2;
         }

         if (this.getLock() == 2) {
            this.regeneration *= 20;
         }
      }

      if (this.catchUp && this.level != null) {
         this.catchUp = false;
         long ct = System.currentTimeMillis();
         int inc = this.regeneration * 75;
         int amt = inc > 0 ? (int)((ct - this.lastActive) / (long)inc) : 0;
         if (amt > 0) {
            for (int a = 0; a < Math.min(amt, this.aspectsBase.visSize()); a++) {
               AspectList al = new AspectList();
               for (Aspect aspect : this.getAspects().getAspects()) {
                  if (this.getAspects().getAmount(aspect) < this.getNodeVisBase(aspect)) {
                     al.add(aspect, 1);
                  }
               }

               if (al.size() > 0) {
                  this.addToContainer(al.getAspects()[this.level.random.nextInt(al.size())], 1);
                  change = true;
               }
            }
         }
      }

      if (this.count % 1200 == 0 && this.level != null) {
         for (Aspect aspectx : this.getAspects().getAspects()) {
            if (this.getAspects().getAmount(aspectx) <= 0) {
               this.setNodeVisBase(aspectx, (short)(this.getNodeVisBase(aspectx) - 1));
               if (this.level.random.nextInt(20) == 0 || this.getNodeVisBase(aspectx) <= 0) {
                  this.getAspects().remove(aspectx);
                  if (this.level.random.nextInt(5) == 0) {
                     if (this.getNodeModifier() == NodeModifier.BRIGHT) {
                        this.setNodeModifier(null);
                     } else if (this.getNodeModifier() == null) {
                        this.setNodeModifier(NodeModifier.PALE);
                     }
                  }

                  if (this.getNodeModifier() == NodeModifier.PALE && this.level.random.nextInt(5) == 0) {
                     this.setNodeModifier(NodeModifier.FADING);
                  }

                  this.nodeChange();
                  break;
               }

               this.nodeChange();
            }
         }

         if (this.getAspects().size() <= 0) {
            BlockState self = this.level.getBlockState(this.worldPosition);
            if (self.is(thaumcraft.init.registry.ModBlocks.SILVERWOOD_LOG_NODE.get())) {
               // Узел умер — осталось обычное бревно серебряного дерева.
               this.level.setBlock(this.worldPosition,
                  thaumcraft.init.registry.ModBlocks.SILVERWOOD_LOG.get().defaultBlockState(), 3);
            } else if (self.is(ModBlocks.CHARGED_OBSIDIAN_TOTEM.get())) {
               // TC4 left the totem standing once its node was gone.
               this.level.setBlock(this.worldPosition, ModBlocks.OBSIDIAN_TOTEM.get().withPropertiesOf(self), 3);
            } else {
               this.level.removeBlock(this.worldPosition, false);
            }

            this.setRemoved();
            return true;
         }
      }

      if (this.wait > 0) {
         this.wait--;
      }

      if (this.regeneration > 0 && this.wait == 0 && this.count % this.regeneration == 0 && this.level != null) {
         this.lastActive = System.currentTimeMillis();
         AspectList al = new AspectList();

         for (Aspect aspectxx : this.getAspects().getAspects()) {
            if (this.getAspects().getAmount(aspectxx) < this.getNodeVisBase(aspectxx)) {
               al.add(aspectxx, 1);
            }
         }

         if (al.size() > 0) {
            this.addToContainer(al.getAspects()[this.level.random.nextInt(al.size())], 1);
            change = true;
         }
      }

      return change;
   }

   private boolean handleDischarge(boolean change) {
      if (this.level == null || !this.getBlockState().is((Block)ModBlocks.AURA_NODE.get()) || this.getLock() == 1) {
         return change;
      } else if (this.getNodeModifier() == NodeModifier.FADING) {
         return change;
      } else {
         boolean shiny = this.getNodeType() == NodeType.HUNGRY || this.getNodeModifier() == NodeModifier.BRIGHT;
         int inc = this.getNodeModifier() == null ? 2 : (shiny ? 1 : (this.getNodeModifier() == NodeModifier.PALE ? 3 : 2));
         if (this.count % inc != 0) {
            return change;
         } else {
            RandomSource rand = this.level.random;
            int x = rand.nextInt(5) - rand.nextInt(5);
            int y = rand.nextInt(5) - rand.nextInt(5);
            int z = rand.nextInt(5) - rand.nextInt(5);
            if (this.getNodeModifier() == NodeModifier.PALE && rand.nextBoolean()) {
               return change;
            } else {
               if (x != 0 || y != 0 || z != 0) {
                  BlockPos otherPos = this.worldPosition.offset(x, y, z);
                  if (this.level.getBlockEntity(otherPos) instanceof INode nd
                     && this.level.getBlockState(otherPos).is((Block)ModBlocks.AURA_NODE.get())) {
                     if (nd instanceof AuraNodeBlockEntity otherNode && otherNode.getLock() > 0) {
                        return change;
                     }

                     int ndavg = (nd.getAspects().visSize() + nd.getAspectsBase().visSize()) / 2;
                     int thisavg = (this.getAspects().visSize() + this.getAspectsBase().visSize()) / 2;
                     if (ndavg < thisavg && nd.getAspects().size() > 0) {
                        Aspect a = nd.getAspects().getAspects()[rand.nextInt(nd.getAspects().size())];
                        boolean u = false;
                        if (this.getAspects().getAmount(a) < this.getNodeVisBase(a) && nd.takeFromContainer(a, 1)) {
                           this.addToContainer(a, 1);
                           u = true;
                        } else if (nd.takeFromContainer(a, 1)) {
                           if (rand.nextInt(1 + (int)((double)this.getNodeVisBase(a) / (shiny ? 1.5 : 1.0))) == 0) {
                              this.aspectsBase.add(a, 1);
                              if (this.getNodeModifier() == NodeModifier.PALE && rand.nextInt(100) == 0) {
                                 this.setNodeModifier(null);
                                 this.regeneration = -1;
                              }

                              if (rand.nextInt(3) == 0) {
                                 nd.setNodeVisBase(a, (short)(nd.getNodeVisBase(a) - 1));
                              }
                           }

                           u = true;
                        }

                        if (u) {
                           if (nd instanceof AuraNodeBlockEntity otherNode) {
                              otherNode.wait = otherNode.regeneration / 2;
                              otherNode.setChanged();
                              otherNode.markForUpdate();
                           }
                           change = true;
                           PacketHandler.sendToAllAround(
                              new PacketFXBlockZap(
                                 (float)otherPos.getX() + 0.5F, (float)otherPos.getY() + 0.5F, (float)otherPos.getZ() + 0.5F,
                                 (float)this.worldPosition.getX() + 0.5F, (float)this.worldPosition.getY() + 0.5F, (float)this.worldPosition.getZ() + 0.5F
                              ),
                              this.level,
                              this.worldPosition,
                              32.0
                           );
                        }
                     }
                  }
               }

               return change;
            }
         }
      }
   }

   private boolean handleTaintNode(boolean change) {
      if (this.level == null) return change;
      if (this.getNodeType() == NodeType.TAINTED && this.count % 50 == 0) {
         if (!NodeConfig.disableNodeTaintChunks) {
            RandomSource rand = this.level.random;
            int x = this.worldPosition.getX() + rand.nextInt(8) - rand.nextInt(8);
            int z = this.worldPosition.getZ() + rand.nextInt(8) - rand.nextInt(8);
            BlockPos targetPos = new BlockPos(x, this.worldPosition.getY(), z);
            if (!this.level.getBiome(targetPos).is(ModBiomes.TAINT)) {
               Utils.setBiomeAt(this.level, targetPos, ModBiomes.TAINT);
            }
         }

         if (!NodeConfig.disableNodeTaintFibers && this.level.random.nextBoolean()) {
            RandomSource rand = this.level.random;
            int x = this.worldPosition.getX() + rand.nextInt(5) - rand.nextInt(5);
            int y = this.worldPosition.getY() + rand.nextInt(5) - rand.nextInt(5);
            int z = this.worldPosition.getZ() + rand.nextInt(5) - rand.nextInt(5);
            BlockPos targetPos = new BlockPos(x, y, z);
            BlockState st = this.level.getBlockState(targetPos);
            if (st.isAir() || st.canBeReplaced()) {
               for (Direction d : Direction.values()) {
                  BlockPos adj = targetPos.relative(d);
                  if (this.level.getBlockState(adj).isFaceSturdy(this.level, adj, d.getOpposite())) {
                     this.level.setBlock(targetPos, ModBlocks.FIBROUS_TAINT.get().defaultBlockState(), 3);
                     break;
                  }
               }
            }
         }
      } else if (this.getNodeType() != NodeType.PURE && this.getNodeType() != NodeType.TAINTED && this.count % 100 == 0) {
         if (this.level.getBiome(this.worldPosition).is(ModBiomes.TAINT) && this.level.random.nextInt(500) == 0) {
            this.setNodeType(NodeType.TAINTED);
            this.nodeChange();
            change = true;
         }
      }
      return change;
   }

   private boolean handleNodeStability(boolean change) {
      if (this.level == null) return change;
      if (this.count % 100 == 0) {
         if (this.getNodeType() == NodeType.UNSTABLE && this.level.random.nextBoolean()) {
            if (this.getLock() == 0) {
               Aspect aspect = this.takeRandomPrimalFromSource();
               if (aspect != null) {
                  AspectOrbEntity orb = new AspectOrbEntity(
                     this.level, (double)this.worldPosition.getX() + 0.5, (double)this.worldPosition.getY() + 0.5, (double)this.worldPosition.getZ() + 0.5, aspect, 1
                  );
                  this.level.addFreshEntity(orb);
                  change = true;
               }
            } else if (this.level.random.nextInt(10000 / Math.max(1, (int)this.getLock())) == 42) {
               this.setNodeType(NodeType.NORMAL);
               change = true;
            }
         }

         if (this.getNodeModifier() == NodeModifier.FADING && this.getLock() > 0 && this.level.random.nextInt(12500 / Math.max(1, (int)this.getLock())) == 69) {
            this.setNodeModifier(NodeModifier.PALE);
            change = true;
         }
      }
      return change;
   }

   private boolean handleDarkNode(boolean change) {
      if (this.level == null) return change;
      if (this.getNodeType() == NodeType.DARK && this.count % 50 == 0) {
         if (!NodeConfig.disableNodeDarkChunks) {
            RandomSource rand = this.level.random;
            int x = this.worldPosition.getX() + rand.nextInt(12) - rand.nextInt(12);
            int z = this.worldPosition.getZ() + rand.nextInt(12) - rand.nextInt(12);
            BlockPos targetPos = new BlockPos(x, this.worldPosition.getY(), z);
            if (!this.level.getBiome(targetPos).is(ModBiomes.EERIE)) {
               Utils.setBiomeAt(this.level, targetPos, ModBiomes.EERIE);
            }

            if (!NodeConfig.disableNodeSummonBrainyZombie
               && rand.nextBoolean()
               && this.level.getNearestPlayer((double)this.worldPosition.getX() + 0.5, (double)this.worldPosition.getY() + 0.5, (double)this.worldPosition.getZ() + 0.5, 24.0, false) != null) {
               AABB aabb = new AABB(this.worldPosition).inflate(10.0, 6.0, 10.0);
               List<EntityGiantBrainyZombie> zombies = this.level.getEntitiesOfClass(EntityGiantBrainyZombie.class, aabb);
               if (zombies.size() <= 3) {
                  double d0 = (double)this.worldPosition.getX() + (rand.nextDouble() - rand.nextDouble()) * 5.0;
                  double d3 = (double)(this.worldPosition.getY() + rand.nextInt(3) - 1);
                  double d4 = (double)this.worldPosition.getZ() + (rand.nextDouble() - rand.nextDouble()) * 5.0;
                  EntityGiantBrainyZombie entity = new EntityGiantBrainyZombie(this.level);
                  entity.moveTo(d0, d3, d4, rand.nextFloat() * 360.0F, 0.0F);
                  this.level.addFreshEntity(entity);
                  this.level.levelEvent(2004, this.worldPosition, 0);
                  entity.spawnAnim();
               }
            }
         }
      }
      return change;
   }

   private boolean handlePureNode(boolean change) {
      if (this.level == null) return change;
      if (this.getNodeType() == NodeType.PURE && this.count % 50 == 0) {
         if (!NodeConfig.disableNodePureChunks) {
            RandomSource rand = this.level.random;
            int x = this.worldPosition.getX() + rand.nextInt(8) - rand.nextInt(8);
            int z = this.worldPosition.getZ() + rand.nextInt(8) - rand.nextInt(8);
            BlockPos targetPos = new BlockPos(x, this.worldPosition.getY(), z);
            if (this.level.getBiome(targetPos).is(ModBiomes.TAINT) || this.level.getBiome(targetPos).is(ModBiomes.EERIE)) {
               Utils.setBiomeAt(this.level, targetPos, ModBiomes.MAGICAL_FOREST);
            }
         }
      }
      return change;
   }

   @Override
   public void clientTick(Level level, BlockPos pos, BlockState state) {
      this.registerLocation();
      this.clientHungryNodeTick();
      this.count++;
      this.checkLock();
      if (this.getNodeType() == NodeType.DARK && this.count % 50 == 0) {
         ItemCompassStone.sinisterNodes.put(new LevelBlockPos(this), System.currentTimeMillis());
      }
   }

   public void nodeChange() {
      this.regeneration = -1;
      this.setChanged();
      this.markForUpdate();
   }

   private void clientHungryNodeTick() {
      if (this.getNodeType() == NodeType.HUNGRY) {
         RandomSource rand = this.level.random;

         for (int a = 0; a < Thaumcraft.setup.particleCount(1); a++) {
            BlockPos bp = this.worldPosition
               .offset(rand.nextInt(16) - rand.nextInt(16), rand.nextInt(16) - rand.nextInt(16), rand.nextInt(16) - rand.nextInt(16));
            bp = bp.atY(Math.min(bp.getY(), this.level.getHeight(Heightmap.Types.WORLD_SURFACE, bp.getX(), bp.getZ())));
            Vec3 v1 = this.worldPosition.getCenter();
            Vec3 v2 = bp.getCenter();
            if (ThaumcraftApiHelper.rayTraceIgnoringSource(this.level, v1, v2, true, false, false) instanceof BlockHitResult result
               && result.getBlockPos().distSqr(this.worldPosition) < 256.0) {
               bp = result.getBlockPos();
               BlockState bi = this.level.getBlockState(bp);
               if (!bi.isAir()) {
                  Thaumcraft.setup
                     .hungryNodeFX(
                        this.level,
                        bp.getX(),
                        bp.getY(),
                        bp.getZ(),
                        this.worldPosition.getX(),
                        this.worldPosition.getY(),
                        this.worldPosition.getZ(),
                        bi.getBlock(),
                        0
                     );
               }
            }
         }
      }
   }

   public byte getLock() {
      return this.nodeLock;
   }

   public void checkLock() {
      if ((this.count <= 1 || this.count % 50 == 0) && this.getBlockState().is((Block)ModBlocks.AURA_NODE.get())) {
         byte oldLock = this.nodeLock;
         this.nodeLock = (byte)Math.max(this.stabilizerLock(Direction.DOWN), this.stabilizerLock(Direction.UP));
         if (oldLock != this.nodeLock) {
            this.regeneration = -1;
         }
      }
   }

   /** Lock of an unpowered stabilizer on the given side that faces this node: 1 normal, 2 advanced, else 0. TC4 checks only below. */
   private int stabilizerLock(Direction side) {
      BlockPos pos = this.worldPosition.relative(side);
      BlockState state = this.level.getBlockState(pos);
      boolean normal = state.is((Block)ModBlocks.NODE_STABILIZER.get());
      if (!normal && !state.is((Block)ModBlocks.ADVANCED_NODE_STABILIZER.get())
         || state.getValue(NodeStabilizerBlock.FACING) != side.getOpposite()
         || this.level.hasNeighborSignal(pos)) {
         return 0;
      }

      return normal ? 1 : 2;
   }

   @Override
   public int onWandRightClick(
      @NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockPos blockPos, @NotNull Direction direction
   ) {
      return -1;
   }

   @NotNull
   @Override
   public ItemStack onWandRightClick(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
      ItemStack wandstack = player.getItemInHand(hand);
      player.startUsingItem(hand);
      ItemWandCasting wand = (ItemWandCasting)wandstack.getItem();
      wand.setObjectInUse(wandstack, this.worldPosition);
      return wandstack;
   }

   @Override
   public void onUsingWandTick(@NotNull ItemStack stack, @NotNull Player player, int count) {
      ItemWandCasting wand = (ItemWandCasting)stack.getItem();
      if (!this.isPlayerAimingAtNode(player)) {
         player.stopUsingItem();
      } else {
         if (count % 5 == 0) {
            int tap = 1;
            if (ResearchManager.isResearchComplete(player, "nodetapper1")) {
               tap++;
            }

            if (ResearchManager.isResearchComplete(player, "nodetapper2")) {
               tap++;
            }

            boolean preserve = !player.isShiftKeyDown()
               && ResearchManager.isResearchComplete(player, "nodepreserve")
               && !wand.getRod(stack).getTag().equals("wood")
               && !wand.getCap(stack).getTag().equals("iron");
            AspectList room = wand.getAspectsWithRoom(stack).copy();
            boolean success = false;
            Aspect aspect = this.chooseRandomFilteredFromSource(room, preserve);
            if (aspect != null) {
               int nodeAmt = this.getAspects().getAmount(aspect);
               int canPut = Math.max((wand.getMaxVis(stack) - wand.getVis(stack, aspect)) / 100, 0);
               int realTap = Math.min(tap, nodeAmt);
               realTap = Math.min(realTap, canPut);
               if (preserve && realTap == nodeAmt) {
                  realTap = Math.max(0, realTap - 1);
               }

               if (realTap > 0) {
                  if (!this.level.isClientSide) {
                     this.takeFromContainer(aspect, realTap);
                     int rem = wand.addVis(stack, aspect, realTap, true);
                     if (rem > 0) {
                        this.addToContainer(aspect, rem);
                     }

                     player.getInventory().setChanged();
                     this.drainColor = aspect.getColor();
                     this.setChanged();
                     this.markForUpdate();
                  }

                  success = true;
               }
            }

            if (success) {
               this.drainEntity = player;
               this.drainCollision = new BlockHitResult(Vec3.atCenterOf(this.worldPosition), Direction.UP, this.worldPosition, false);
               this.targetColor = new Color(this.drainColor);
            } else {
               this.drainEntity = null;
               this.drainCollision = null;
            }
         }

         if (player.level().isClientSide) {
            int r = this.targetColor.getRed();
            int g = this.targetColor.getGreen();
            int b = this.targetColor.getBlue();
            int r2 = this.color.getRed() * 4;
            int g2 = this.color.getGreen() * 4;
            int b2 = this.color.getBlue() * 4;
            this.color = new Color((r + r2) / 5, (g + g2) / 5, (b + b2) / 5);
         }
      }
   }

   private boolean isPlayerAimingAtNode(@NotNull Player player) {
      double traceRange = 5.0;
      double aimRadius = 1.0;
      Vec3 eyePos = player.getEyePosition();
      Vec3 viewVec = player.getViewVector(1.0F).normalize();
      Vec3 nodeCenter = Vec3.atCenterOf(this.worldPosition);
      Vec3 toNode = nodeCenter.subtract(eyePos);
      double distanceToNode = toNode.length();
      if (distanceToNode > 5.0) {
         return false;
      } else {
         double forwardDistance = toNode.dot(viewVec);
         if (forwardDistance <= 0.0) {
            return false;
         } else {
            double offAxisSqr = toNode.lengthSqr() - forwardDistance * forwardDistance;
            if (offAxisSqr > 1.0) {
               return false;
            } else {
               HitResult obstruction = this.level
                  .clip(new ClipContext(eyePos, nodeCenter, net.minecraft.world.level.ClipContext.Block.COLLIDER, Fluid.NONE, player));
               return obstruction.getType() == Type.MISS
                  ? true
                  : obstruction instanceof BlockHitResult blockHit && blockHit.getBlockPos().equals(this.worldPosition);
            }
         }
      }
   }

   @Override
   public void onWandStoppedUsing(@NotNull ItemStack stack, @NotNull Level level, @NotNull Player player, int count) {
      this.drainEntity = null;
      this.drainCollision = null;
      if (!level.isClientSide) {
         this.setChanged();
         this.markForUpdate();
      }
   }

   private void loadPersistentNodeData(@NotNull CompoundTag compound) {
      this.lastActive = compound.getLong("lastActive");
      AspectList al = new AspectList();
      ListTag listTag = compound.getList("AspectsBase", 10);

      for (int j = 0; j < listTag.size(); j++) {
         CompoundTag rs = listTag.getCompound(j);
         if (rs.contains("key")) {
            al.add(Aspect.getAspect(rs.getString("key")), rs.getInt("amount"));
         }
      }

      short oldBase = compound.getShort("nodeVisBase");
      this.aspectsBase = new AspectList();
      if (oldBase > 0 && al.size() == 0) {
         for (Aspect a : this.aspects.getAspects()) {
            this.aspectsBase.merge(a, oldBase);
         }
      } else {
         this.aspectsBase = al.copy();
      }

      int regen = 600;
      if (this.getNodeModifier() != null) {
         regen = switch (this.getNodeModifier()) {
            case BRIGHT -> 400;
            case PALE -> 900;
            case FADING -> 0;
         };
      }

      long ct = System.currentTimeMillis();
      int inc = regen * 75;
      if (regen > 0 && this.lastActive > 0L && ct > this.lastActive + inc) {
         this.catchUp = true;
      }
   }

   private void savePersistentNodeData(@NotNull CompoundTag compound) {
      compound.putLong("lastActive", this.lastActive);
      ListTag tlist = new ListTag();
      compound.put("AspectsBase", tlist);

      for (Aspect aspect : this.aspectsBase.getAspects()) {
         if (aspect != null) {
            CompoundTag f = new CompoundTag();
            f.putString("key", aspect.getTag());
            f.putInt("amount", this.aspectsBase.getAmount(aspect));
            tlist.add(f);
         }
      }
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound) {
      if (compound.contains("nodeId")) {
         String storedId = compound.getString("nodeId");
         if (!storedId.isEmpty()) {
            this.setId(storedId);
         }
      }

      this.setNodeType(NodeType.values()[compound.getByte("type")]);
      byte mod = compound.getByte("modifier");
      if (mod >= 0) {
         this.setNodeModifier(NodeModifier.values()[mod]);
      } else {
         this.setNodeModifier(null);
      }

      this.aspects.load(compound);
      if (this.level != null && compound.hasUUID("drainer")) {
         UUID de = compound.getUUID("drainer");
         this.drainEntity = this.level.getPlayerByUUID(de);
         if (this.drainEntity != null) {
            this.drainCollision = new BlockHitResult(
               new Vec3(this.drainEntity.getX(), this.drainEntity.getY(), this.drainEntity.getZ()), Direction.DOWN, this.worldPosition, false
            );
         }
      }

      this.drainColor = compound.getInt("draincolor");
      this.awakenPrimalVisAmount = compound.getInt("awakenPrimalVisAmount");
      this.loadPersistentNodeData(compound);
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound) {
      this.ensureServerId();
      if (this.id != null && !this.id.isEmpty()) {
         compound.putString("nodeId", this.id);
      }

      compound.putByte("type", (byte)this.getNodeType().ordinal());
      compound.putByte("modifier", this.getNodeModifier() == null ? -1 : (byte)this.getNodeModifier().ordinal());
      this.aspects.save(compound);
      if (this.drainEntity instanceof Player p) {
         compound.putUUID("drainer", p.getUUID());
      }

      compound.putInt("draincolor", this.drainColor);
      compound.putInt("awakenPrimalVisAmount", this.awakenPrimalVisAmount);
      this.savePersistentNodeData(compound);
   }

   @Override
   public AspectList getAspects() {
      return this.aspects;
   }

   @Override
   public AspectList getAspectsBase() {
      return this.aspectsBase;
   }

   @Override
   public void setAspects(AspectList aspectList) {
      aspectList.setMaxAmount(256);
      this.aspects = aspectList;
      this.aspectsBase = aspectList.copy();
   }

   @Override
   public int addToContainer(Aspect aspect, int amount) {
      int left = amount + this.aspects.getAmount(aspect) - this.aspectsBase.getAmount(aspect);
      left = left > 0 ? left : 0;
      this.aspects.add(aspect, amount - left);
      return left;
   }

   @Override
   public boolean takeFromContainer(Aspect aspect, int amount) {
      return this.aspects.reduce(aspect, amount);
   }

   public Aspect takeRandomPrimalFromSource() {
      Aspect[] primals = this.aspects.getPrimalAspects();
      Aspect asp = primals.length > 0 ? primals[this.level.random.nextInt(primals.length)] : null;
      return asp != null && this.aspects.reduce(asp, 1) ? asp : null;
   }

   public Aspect chooseRandomFilteredFromSource(AspectList filter, boolean preserve) {
      int min = preserve ? 1 : 0;
      ArrayList<Aspect> validaspects = new ArrayList<>();

      for (Aspect prim : this.aspects.getAspects()) {
         if (filter.getAmount(prim) > 0 && this.aspects.getAmount(prim) > min) {
            validaspects.add(prim);
         }
      }

      if (validaspects.size() == 0) {
         return null;
      } else {
         Aspect asp = validaspects.get(this.level.random.nextInt(validaspects.size()));
         return asp != null && this.aspects.getAmount(asp) > min ? asp : null;
      }
   }

   @Override
   public NodeType getNodeType() {
      return this.nodeType;
   }

   @Override
   public void setNodeType(NodeType nodeType) {
      this.nodeType = nodeType;
   }

   @Override
   public void setNodeModifier(NodeModifier nodeModifier) {
      this.nodeModifier = nodeModifier;
   }

   @Override
   public NodeModifier getNodeModifier() {
      return this.nodeModifier;
   }

   @Override
   public int getNodeVisBase(Aspect aspect) {
      return this.aspectsBase.getAmount(aspect);
   }

   @Override
   public void setNodeVisBase(Aspect aspect, short nodeVisBase) {
      if (this.aspectsBase.getAmount(aspect) < nodeVisBase) {
         this.aspectsBase.merge(aspect, nodeVisBase);
      } else {
         this.aspectsBase.reduce(aspect, this.aspectsBase.getAmount(aspect) - nodeVisBase);
      }
   }

   public int getAwakenPrimalVisAmount() {
      return this.awakenPrimalVisAmount;
   }

   public void setAwakenPrimalVisAmount(int awakenPrimalVisAmount) {
      this.awakenPrimalVisAmount = Math.max(0, awakenPrimalVisAmount);
   }

   @Override
   public boolean takeFromContainer(AspectList aspectList) {
      return false;
   }

   @Override
   public boolean doesContainerContainAmount(Aspect aspect, int amount) {
      return false;
   }

   @Override
   public boolean doesContainerContain(AspectList aspectList) {
      return false;
   }

   @Override
   public int containerContains(Aspect aspect) {
      return 0;
   }

   @Override
   public boolean doesContainerAccept(Aspect aspect) {
      return true;
   }

   public static class NodeConfig {
      public static boolean disableNodeHungryBreakBlocks = false;
      public static boolean disableNodeTaintChunks = false;
      public static boolean disableNodeTaintFibers = false;
      public static boolean disableNodePureChunks = false;
      public static boolean disableNodeDarkChunks = false;
      public static boolean disableNodeSummonBrainyZombie = false;
      public static int hungryNodeRadius = 2;
      public static boolean checkProtection = true;
   }
}
