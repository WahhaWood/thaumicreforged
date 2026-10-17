package thaumcraft.api.visnet;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import thaumcraft.api.LevelBlockPos;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.util.Utils;

public class VisNetHandler {
   public static HashMap<ResourceKey<Level>, HashMap<LevelBlockPos, WeakReference<TileVisNode>>> sources = new HashMap<>();
   static ArrayList<LevelBlockPos> cache = new ArrayList<>();
   private static final HashMap<LevelBlockPos, ArrayList<WeakReference<TileVisNode>>> nearbyNodes = new HashMap<>();

   public static int drainVis(Level level, BlockPos blockPos, Aspect aspect, int amount) {
      int drainedAmount = 0;
      LevelBlockPos drainer = new LevelBlockPos(blockPos, level.dimension());
      if (!nearbyNodes.containsKey(drainer)) {
         calculateNearbyNodes(level, blockPos);
      }

      ArrayList<WeakReference<TileVisNode>> nodes = nearbyNodes.get(drainer);
      if (nodes != null && nodes.size() > 0) {
         for (WeakReference<TileVisNode> noderef : nodes) {
            TileVisNode node = noderef.get();
            if (node != null) {
               int a = node.consumeVis(aspect, amount);
               drainedAmount += a;
               amount -= a;
               if (a > 0) {
                  int color = Aspect.getPrimalAspects().indexOf(aspect);
                  Utils.generateVisEffect(level, blockPos, node.getBlockPos(), color);
               }

               if (amount <= 0) {
                  break;
               }
            }
         }
      }

      return drainedAmount;
   }

   public static void addSource(Level level, TileVisNode vs) {
      sources.computeIfAbsent(level.dimension(), resourceKey -> new HashMap<>()).put(vs.getLocation(), new WeakReference<>(vs));
      nearbyNodes.clear();
   }

   public static boolean isNodeValid(WeakReference<TileVisNode> node) {
      return node != null && node.get() != null && !node.get().isRemoved();
   }

   public static WeakReference<TileVisNode> addNode(Level level, TileVisNode vn) {
      WeakReference<TileVisNode> ref = new WeakReference<>(vn);
      HashMap<LevelBlockPos, WeakReference<TileVisNode>> sourcelist = sources.get(level.dimension());
      if (sourcelist == null) {
         return null;
      } else {
         ArrayList<Object[]> nearby = new ArrayList<>();

         for (WeakReference<TileVisNode> root : sourcelist.values()) {
            if (isNodeValid(root)) {
               TileVisNode source = root.get();
               float r = inRange(vn.getLocation(), source.getLocation(), vn.getRange());
               if (r > 0.0F) {
                  nearby.add(new Object[]{source, r - vn.getRange() * 2});
               }

               nearby = findClosestNodes(vn, source, nearby);
               cache.clear();
            }
         }

         float dist = Float.MAX_VALUE;
         TileVisNode closest = null;
         if (nearby.size() > 0) {
            for (Object[] o : nearby) {
               if ((Float)o[1] < dist
                  && (vn.getAttunement() == -1 || ((TileVisNode)o[0]).getAttunement() == -1 || vn.getAttunement() == ((TileVisNode)o[0]).getAttunement())
                  && canNodeBeSeen(vn, (TileVisNode)o[0])) {
                  dist = (Float)o[1];
                  closest = (TileVisNode)o[0];
               }
            }
         }

         if (closest != null) {
            closest.getChildren().add(ref);
            nearbyNodes.clear();
            return new WeakReference<>(closest);
         } else {
            return null;
         }
      }
   }

   public static ArrayList<Object[]> findClosestNodes(TileVisNode target, TileVisNode parent, ArrayList<Object[]> in) {
      if (cache.size() <= 512 && !cache.contains(new LevelBlockPos(parent))) {
         cache.add(new LevelBlockPos(parent));

         for (WeakReference<TileVisNode> childWR : parent.getChildren()) {
            TileVisNode child = childWR.get();
            if (child != null && !child.equals(target) && !child.equals(parent)) {
               float r2 = inRange(child.getLocation(), target.getLocation(), target.getRange());
               if (r2 > 0.0F) {
                  in.add(new Object[]{child, r2});
               }

               in = findClosestNodes(target, child, in);
            }
         }

         return in;
      } else {
         return in;
      }
   }

   private static float inRange(LevelBlockPos cc1, LevelBlockPos cc2, int range) {
      float distance = (float)cc1.distSqr(cc2);
      return distance > range * range ? -1.0F : distance;
   }

   private static void calculateNearbyNodes(Level level, BlockPos blockPos) {
      HashMap<LevelBlockPos, WeakReference<TileVisNode>> sourcelist = sources.get(level.dimension());
      if (sourcelist != null) {
         ArrayList<WeakReference<TileVisNode>> cn = new ArrayList<>();
         LevelBlockPos drainer = new LevelBlockPos(blockPos, level.dimension());

         for (WeakReference<TileVisNode> root : sourcelist.values()) {
            if (isNodeValid(root)) {
               TileVisNode source = root.get();
               TileVisNode closest = null;
               float range = Float.MAX_VALUE;
               float r = inRange(drainer, source.getLocation(), source.getRange());
               if (r > 0.0F) {
                  range = r;
                  closest = source;
               }

               ArrayList<WeakReference<TileVisNode>> children = new ArrayList<>();

               for (WeakReference<TileVisNode> child : getAllChildren(source, children)) {
                  TileVisNode n = child.get();
                  if (n != null && !n.equals(source)) {
                     float r2 = inRange(n.getLocation(), drainer, n.getRange());
                     if (r2 > 0.0F && r2 < range) {
                        range = r2;
                        closest = n;
                     }
                  }
               }

               if (closest != null) {
                  cn.add(new WeakReference<>(closest));
               }
            }
         }

         nearbyNodes.put(drainer, cn);
      }
   }

   private static ArrayList<WeakReference<TileVisNode>> getAllChildren(TileVisNode source, ArrayList<WeakReference<TileVisNode>> list) {
      return getAllChildren(new HashSet<>(), source, list);
   }

   private static ArrayList<WeakReference<TileVisNode>> getAllChildren(Set<TileVisNode> tiles, TileVisNode source, ArrayList<WeakReference<TileVisNode>> list) {
      if (!tiles.add(source)) {
         return list;
      } else {
         for (WeakReference<TileVisNode> child : source.getChildren()) {
            TileVisNode n = child.get();
            if (n != null && n.hasLevel() && n.getLevel().isLoaded(n.getBlockPos())) {
               list.add(child);
               list = getAllChildren(tiles, n, list);
            }
         }

         return list;
      }
   }

   /** TC4: nothing solid between the two (blocks without a collision box, like the node itself, let the ray through). */
   public static boolean canNodeBeSeen(TileVisNode source, TileVisNode target) {
      Level level = source.getLevel();
      if (level == null) {
         return false;
      }

      BlockHitResult hit = ThaumcraftApiHelper.rayTraceIgnoringSource(
         level, source.getBlockPos().getCenter(), target.getBlockPos().getCenter(), false, true, false
      );
      return hit == null || hit.getBlockPos().equals(target.getBlockPos());
   }
}
