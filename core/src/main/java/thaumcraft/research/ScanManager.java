package thaumcraft.research;

import java.util.HashSet;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.api.LevelBlockPos;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.nodes.INode;
import thaumcraft.api.research.IScanEventHandler;
import thaumcraft.api.research.ScanResult;
import thaumcraft.block.airy.aura_node.AuraNodeBlockEntity;
import thaumcraft.block.airy.energized_aura.NodeEnergizedBlockEntity;
import thaumcraft.block.jar.node.NodeJarBlockEntity;
import thaumcraft.init.registry.Config;
import thaumcraft.init.registry.ModFluids;
import thaumcraft.network.PacketHandler;
import thaumcraft.network.playerdata.PacketAspectDiscovery;
import thaumcraft.network.playerdata.PacketAspectPool;
import thaumcraft.recipe.ThaumcraftCraftingManager;
import thaumcraft.util.PlayerNotifications;

public class ScanManager implements IScanEventHandler {
   private static final double PHENOMENA_SCAN_RANGE = 5.0;
   private static final double NODE_SCAN_RADIUS_SQR = 0.5625;

   @Override
   public ScanResult scanPhenomena(ItemStack stack, Level level, Player player) {
      HitResult hitResult = player.pick(5.0, 0.0F, true);
      if (hitResult instanceof BlockHitResult blockHitResult && hitResult.getType() == Type.BLOCK) {
         String fluidPhenomena = getFluidPhenomena(level.getFluidState(blockHitResult.getBlockPos()));
         if (fluidPhenomena != null) {
            return ScanResult.ofPhenomena(fluidPhenomena);
         }
      }

      String nodeId = findPointedNodeId(level, player, 10.0);
      return nodeId != null ? ScanResult.ofPhenomena("NODE" + nodeId) : null;
   }

   public static int generateItemHash(ItemStack stack) {
      return ScanIdentity.getItemHash(stack);
   }

   public static int generateBlockHash(Block block) {
      return ScanIdentity.getBlockHash(block);
   }

   public static int getItemScanHash(ItemStack stack) {
      return generateItemHash(stack);
   }

   public static int getEntityScanHash(Entity entity) {
      return ScanIdentity.getEntityHash(entity);
   }

   public static AspectList generateEntityAspects(Entity entity) {
      return EntityAspectResolver.resolve(entity);
   }

   public static INode getNode(Level world, String locationId) {
      if (locationId == null || locationId.isEmpty()) {
         return null;
      }
      LevelBlockPos blockPos = AuraNodeBlockEntity.locations.get(locationId);
      if (blockPos != null && world.dimension().equals(blockPos.dim)) {
         BlockEntity blockEntity = world.getBlockEntity(blockPos.asBlockPos());
         if (blockEntity instanceof INode node && !(blockEntity instanceof NodeJarBlockEntity)) {
            return node;
         }
      }

      // Coordinate-parsing fallback from dim_namespace:dim_path:x:y:z:... locationId format
      try {
         String[] parts = locationId.split(":");
         if (parts.length >= 5) {
            ResourceLocation dimLoc = ResourceLocation.fromNamespaceAndPath(parts[0], parts[1]);
            int x = Integer.parseInt(parts[2]);
            int y = Integer.parseInt(parts[3]);
            int z = Integer.parseInt(parts[4]);
            if (world.dimension().location().equals(dimLoc)) {
               BlockPos pos = new BlockPos(x, y, z);
               BlockEntity be = world.getBlockEntity(pos);
               if (be instanceof INode node && !(be instanceof NodeJarBlockEntity)) {
                  AuraNodeBlockEntity.locations.put(locationId, new LevelBlockPos(pos, world.dimension()));
                  return node;
               }
            }
         }
      } catch (Exception ignored) {
      }

      return null;
   }

   @NotNull
   private static AspectList generateNodeAspects(Level world, String locationId) {
      INode node = getNode(world, locationId);
      return node != null ? nodeAspects(node.getAspects(), node.getNodeType()) : new AspectList();
   }

   /** Our addition (E18): an energized node is scanned like the node it came from, by its base aspects. */
   @NotNull
   private static AspectList generateEnergizedNodeAspects(Level world, String scanId) {
      BlockPos pos = NodeEnergizedBlockEntity.parseScanId(world, scanId);
      return pos != null && world.getBlockEntity(pos) instanceof NodeEnergizedBlockEntity node
         ? nodeAspects(node.getAuraBase(), node.getNodeType())
         : new AspectList();
   }

   @NotNull
   private static AspectList nodeAspects(AspectList ta, thaumcraft.api.nodes.NodeType type) {
      AspectList tags = new AspectList();
      for (Aspect a : ta.getAspectsSorted()) {
         tags.merge(a, Math.max(4, ta.getAmount(a) / 10));
      }

      // NB: if/else вместо switch по enum NodeType — см. комментарий в hasBeenScanned.
      if (type == thaumcraft.api.nodes.NodeType.UNSTABLE) {
         tags.merge(Aspect.ENTROPY, 4);
      } else if (type == thaumcraft.api.nodes.NodeType.HUNGRY) {
         tags.merge(Aspect.HUNGER, 4);
      } else if (type == thaumcraft.api.nodes.NodeType.TAINTED) {
         tags.merge(Aspect.TAINT, 4);
      } else if (type == thaumcraft.api.nodes.NodeType.PURE) {
         tags.merge(Aspect.HEAL, 2);
         tags.add(Aspect.ORDER, 2);
      } else if (type == thaumcraft.api.nodes.NodeType.DARK) {
         tags.merge(Aspect.DEATH, 2);
         tags.add(Aspect.DARKNESS, 2);
      }

      return tags;
   }

   private static String getFluidPhenomena(FluidState fluidState) {
      if (fluidState.isEmpty()) {
         return null;
      } else if (fluidState.is(FluidTags.WATER)) {
         return "WATER";
      } else if (fluidState.is(FluidTags.LAVA)) {
         return "LAVA";
      } else if (fluidState.is((Fluid)ModFluids.FLUX_GOO.getSource().get()) || fluidState.is((Fluid)ModFluids.FLUX_GOO.getFlowing().get())) {
         return "FLUX_GOO";
      } else if (fluidState.is((Fluid)ModFluids.FLUX_GAS.getSource().get()) || fluidState.is((Fluid)ModFluids.FLUX_GAS.getFlowing().get())) {
         return "FLUX_GAS";
      } else if (fluidState.is((Fluid)ModFluids.FLUID_PURE.getSource().get()) || fluidState.is((Fluid)ModFluids.FLUID_PURE.getFlowing().get())) {
         return "PURE";
      } else {
         return !fluidState.is((Fluid)ModFluids.FLUID_DEATH.getSource().get()) && !fluidState.is((Fluid)ModFluids.FLUID_DEATH.getFlowing().get())
            ? null
            : "DEATH";
      }
   }

   private static String findPointedNodeId(Level level, Player player, double range) {
      Vec3 eyePos = player.getEyePosition();
      Vec3 look = player.getViewVector(1.0F).normalize();
      String bestId = null;
      double bestDistance = range + 1.0;

      for (Entry<String, LevelBlockPos> entry : AuraNodeBlockEntity.locations.entrySet()) {
         LevelBlockPos nodePos = entry.getValue();
         if (nodePos != null && level.dimension().equals(nodePos.dim) && level.isLoaded(nodePos.asBlockPos())) {
            BlockEntity blockEntity = level.getBlockEntity(nodePos.asBlockPos());
            if (blockEntity instanceof INode node && !(blockEntity instanceof NodeJarBlockEntity)) {
               Vec3 nodeCenter = Vec3.atCenterOf(nodePos);
               Vec3 offset = nodeCenter.subtract(eyePos);
               double distanceOnRay = offset.dot(look);
               if (!(distanceOnRay < 0.0) && !(distanceOnRay > range) && !(distanceOnRay >= bestDistance)) {
                  double distanceFromRaySqr = offset.lengthSqr() - distanceOnRay * distanceOnRay;
                  if (distanceFromRaySqr <= 0.75) {
                     HitResult obstruction = level.clip(
                        new net.minecraft.world.level.ClipContext(
                           eyePos, nodeCenter, net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, player
                        )
                     );
                     if (obstruction.getType() == Type.MISS || (obstruction instanceof BlockHitResult blockHit && blockHit.getBlockPos().equals(nodePos.asBlockPos()))) {
                        bestDistance = distanceOnRay;
                        bestId = node.getId();
                     }
                  }
               }
            }
         }
      }

      // Direct raycast fallback to detect nodes even if not yet in locations map
      if (bestId == null) {
         HitResult directHit = level.clip(
            new net.minecraft.world.level.ClipContext(
               eyePos, eyePos.add(look.scale(range)), net.minecraft.world.level.ClipContext.Block.OUTLINE, net.minecraft.world.level.ClipContext.Fluid.NONE, player
            )
         );
         if (directHit instanceof BlockHitResult bhr && directHit.getType() == Type.BLOCK) {
            BlockEntity be = level.getBlockEntity(bhr.getBlockPos());
            if (be instanceof INode node && !(be instanceof NodeJarBlockEntity)) {
               bestId = node.getId();
            }
         }
      }

      return bestId;
   }

   @NotNull
   private static AspectList generatePhenomenaAspects(String phenomena) {
      return switch (phenomena) {
         case "WATER" -> new AspectList().add(Aspect.WATER, 4);
         case "LAVA" -> new AspectList().add(Aspect.FIRE, 4).add(Aspect.EARTH, 4);
         case "PURE" -> new AspectList().add(Aspect.ORDER, 4).add(Aspect.HEAL, 4);
         case "FLUX_GOO", "FLUX_GAS" -> new AspectList().add(Aspect.TAINT, 4).add(Aspect.ENTROPY, 2);
         case "DEATH" -> new AspectList().add(Aspect.DEATH, 4).add(Aspect.WATER, 2);
         default -> new AspectList();
      };
   }

   @NotNull
   private static AspectList getItemScanAspects(ItemStack stack) {
      AspectList objectTags = ThaumcraftCraftingManager.getObjectTags(stack);
      return ThaumcraftCraftingManager.getBonusTags(stack, objectTags);
   }

   @NotNull
   private static AspectList getEntityScanAspects(Entity entity) {
      return entity instanceof ItemEntity itemEntity ? getItemScanAspects(itemEntity.getItem()) : generateEntityAspects(entity);
   }

   @NotNull
   private static AspectList getPhenomenaScanAspects(ScanResult scan, Level world) {
      if (scan.phenomena.startsWith(NodeEnergizedBlockEntity.SCAN_PREFIX)) {
         return generateEnergizedNodeAspects(world, scan.phenomena.substring(NodeEnergizedBlockEntity.SCAN_PREFIX.length()));
      }

      return scan.phenomena.startsWith("NODE") ? generateNodeAspects(world, scan.phenomena.replace("NODE", "")) : generatePhenomenaAspects(scan.phenomena);
   }

   public static boolean isValidScanTarget(Player player, ScanResult scan, String prefix) {
      if (scan == null) {
         return false;
      } else if (prefix.equals("@") && !isValidScanTarget(player, scan, "#")) {
         return false;
      } else {
         PlayerKnowledge knowledge = PlayerKnowledge.of(player);

         // NB: if/else вместо switch по enum — см. комментарий в hasBeenScanned.
         if (scan.type == ScanResult.Type.ITEM) {
            return !knowledge.getObjectsScanned().contains(prefix + generateItemHash(scan.stack));
         } else if (scan.type == ScanResult.Type.BLOCK) {
            return !knowledge.getObjectsScanned().contains(prefix + generateBlockHash(scan.block));
         } else if (scan.type == ScanResult.Type.ENTITY) {
            return scan.entity instanceof ItemEntity item
               ? !knowledge.getObjectsScanned().contains(prefix + generateItemHash(item.getItem()))
               : !knowledge.getEntitiesScanned().contains(prefix + ScanIdentity.getEntityHash(scan.entity));
         } else {
            return !knowledge.getPhenomenaScanned().contains(prefix + scan.phenomena);
         }
      }
   }

   public static boolean hasBeenScanned(Player player, ScanResult scan) {
      PlayerKnowledge knowledge = PlayerKnowledge.of(player);

      // NB: без switch по enum — компилятор генерирует синтетический класс ScanManager$1,
      // который DevLaunch-класслоадер не находит в runClient (NoClassDefFoundError в тултипах).
      if (scan.type == ScanResult.Type.ITEM) {
         int hash = generateItemHash(scan.stack);
         HashSet<String> list = knowledge.getObjectsScanned();
         return list.contains("@" + hash) || list.contains("#" + hash);
      } else if (scan.type == ScanResult.Type.BLOCK) {
         int hash = generateBlockHash(scan.block);
         HashSet<String> list = knowledge.getObjectsScanned();
         return list.contains("@" + hash) || list.contains("#" + hash);
      } else if (scan.type == ScanResult.Type.ENTITY) {
         if (scan.entity instanceof ItemEntity item) {
            HashSet<String> list = knowledge.getObjectsScanned();
            int hash = generateItemHash(item.getItem());
            return list.contains("@" + hash) || list.contains("#" + hash);
         } else {
            HashSet<String> list = knowledge.getEntitiesScanned();
            int hash = ScanIdentity.getEntityHash(scan.entity);
            return list.contains("@" + hash) || list.contains("#" + hash);
         }
      } else {
         HashSet<String> list = knowledge.getPhenomenaScanned();
         return list.contains("@" + scan.phenomena) || list.contains("#" + scan.phenomena);
      }
   }

   public static void completeScan(Player player, ScanResult scan, String prefix) {
      ResearchManager researchManager = Thaumcraft.setup.getResearchManager();
      AspectList aspects = null;
      PlayerKnowledge knowledge = PlayerKnowledge.of(player);
      boolean ret = false;
      boolean scannedByThaumometer = prefix.equals("#") && !isValidScanTarget(player, scan, "@");
      Object clue = null;
      // NB: if/else вместо switch по enum — см. комментарий в hasBeenScanned.
      if (scan.type == ScanResult.Type.ITEM) {
         aspects = getItemScanAspects(scan.stack);
         if (validScan(aspects, player)) {
            clue = scan.stack;
            researchManager.completeScannedObject(player, prefix + generateItemHash(scan.stack));
            ret = true;
         }
      } else if (scan.type == ScanResult.Type.BLOCK) {
         aspects = ThaumcraftApi.getBlockTags(scan.block.defaultBlockState());
         if (validScan(aspects, player)) {
            ResourceLocation key = BuiltInRegistries.BLOCK.getKey(scan.block);
            clue = scan.stack != null && !scan.stack.isEmpty() ? scan.stack : (key == null ? scan.block.toString() : key.toString());
            researchManager.completeScannedObject(player, prefix + generateBlockHash(scan.block));
            ret = true;
         }
      } else if (scan.type == ScanResult.Type.ENTITY) {
         if (scan.entity instanceof ItemEntity entity) {
            ItemStack stack = entity.getItem().copy();
            stack.setCount(1);
            aspects = getItemScanAspects(stack);
            if (validScan(aspects, player)) {
               clue = entity.getItem();
               researchManager.completeScannedObject(player, prefix + generateItemHash(entity.getItem()));
               ret = true;
            }
         } else {
            aspects = getEntityScanAspects(scan.entity);
            if (validScan(aspects, player)) {
               clue = BuiltInRegistries.ENTITY_TYPE.getKey(scan.entity.getType()).toString();
               researchManager.completeScannedEntity(player, prefix + ScanIdentity.getEntityHash(scan.entity));
               ret = true;
            }
         }
      } else {
         aspects = getPhenomenaScanAspects(scan, player.level());
         boolean validPhenomenaScan = scan.phenomena.startsWith("NODE") || scan.phenomena.startsWith(NodeEnergizedBlockEntity.SCAN_PREFIX)
            ? aspects.size() > 0
            : validScan(aspects, player);
         if (validPhenomenaScan) {
            researchManager.completeScannedPhenomena(player, prefix + scan.phenomena);
            ret = true;
         }
      }

      if (player instanceof ServerPlayer serverPlayer && ret && aspects != null) {
         AspectList aspectsFinal = new AspectList();

         for (Aspect aspect : aspects.getAspects()) {
            if (knowledge.hasDiscoveredParentAspects(aspect)) {
               int amt = aspects.getAmount(aspect);
               if (scannedByThaumometer) {
                  amt = 0;
               }

               if (prefix.equals("#")) {
                  amt++;
               }

               int a = checkAndSyncAspectKnowledge(serverPlayer, aspect, amt);
               if (a > 0) {
                  aspectsFinal.merge(aspect, a);
               }
            }
         }

         if (clue != null) {
            ResearchManager.createClue(player, clue, aspectsFinal);
         }
      }
   }

   public static int checkAndSyncAspectKnowledge(ServerPlayer player, Aspect aspect, int amount) {
      PlayerKnowledge knowledge = PlayerKnowledge.of(player);
      int save = 0;
      if (!knowledge.hasDiscoveredAspect(aspect)) {
         PacketHandler.sendTo(new PacketAspectDiscovery(aspect.getTag()), player);
         amount += 2;
         save = amount;
      }

      if (knowledge.getAspectPoolFor(aspect) >= Config.aspectTotalCap) {
         amount = (int)Math.sqrt(amount);
      }

      if (amount > 1 && knowledge.getAspectPoolFor(aspect) >= Config.aspectTotalCap * 1.25F) {
         amount = 1;
      }

      if (knowledge.addAspectPool(aspect, (short)amount)) {
         PacketHandler.sendTo(new PacketAspectPool(aspect.getTag(), (short)amount, knowledge.getAspectPoolFor(aspect)), player);
         save = amount;
      }

      if (save > 0) {
         Thaumcraft.setup.getResearchManager().completeAspect(player, aspect, knowledge.getAspectPoolFor(aspect));
      }

      return save;
   }

   public static boolean validScan(AspectList aspects, Player player) {
      PlayerKnowledge knowledge = PlayerKnowledge.of(player);
      if (aspects != null && aspects.size() > 0) {
         for (Aspect aspect : aspects.getAspects()) {
            if (aspect != null && !aspect.isPrimal() && !knowledge.hasDiscoveredParentAspects(aspect)) {
               if (player.level().isClientSide) {
                  for (Aspect parent : aspect.getComponents()) {
                     if (!knowledge.hasDiscoveredAspect(parent)) {
                        PlayerNotifications.addNotification(
                           Component.translatable("tc.discoveryerror", new Object[]{Component.translatable("tc.aspect.help." + parent.getTag())})
                        );
                        break;
                     }
                  }
               }

               return false;
            }
         }

         return true;
      } else {
         if (player.level().isClientSide) {
            PlayerNotifications.addNotification(Component.translatable("tc.unknownobject"));
         }

         return false;
      }
   }

   @NotNull
   public static AspectList getScanAspects(ScanResult scan, Level world) {
      // NB: if/else вместо switch по enum — см. комментарий в hasBeenScanned.
      if (scan.type == ScanResult.Type.ITEM) {
         return getItemScanAspects(scan.stack);
      } else if (scan.type == ScanResult.Type.BLOCK) {
         return ThaumcraftApi.getBlockTags(scan.block.defaultBlockState());
      } else if (scan.type == ScanResult.Type.ENTITY) {
         return getEntityScanAspects(scan.entity);
      } else {
         return getPhenomenaScanAspects(scan, world);
      }
   }
}
