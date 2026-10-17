package thaumcraft.entities.smart.golem;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public final class GolemMarkerQueries {
   private GolemMarkerQueries() {
   }

   public static ArrayList<Container> getMarkedContainers(Level level, EntityGolemBase golem) {
      ArrayList<Container> results = new ArrayList<>();

      for (Marker marker : golem.getMarkers()) {
         BlockEntity be = level.getBlockEntity(marker.getPos());
         addIfContainer(results, be);
      }

      return results;
   }

   public static ArrayList<Container> getMarkedContainersAdjacentToGolem(Level level, EntityGolemBase golem) {
      ArrayList<Container> results = new ArrayList<>();

      for (Marker marker : golem.getMarkers()) {
         BlockPos pos = marker.getPos();
         if (golem.distanceToSqr(Vec3.atCenterOf(pos)) < 16.0) {
            BlockEntity be = level.getBlockEntity(pos);
            addIfContainer(results, be);
         }
      }

      return results;
   }

   public static ArrayList<BlockPos> getMarkedBlocksAdjacentToGolem(Level level, EntityGolemBase golem, byte color) {
      ArrayList<BlockPos> results = new ArrayList<>();

      for (Marker marker : golem.getMarkers()) {
         if (marker.getColor() == color || color == -1) {
            BlockPos pos = marker.getPos();
            if (!(level.getBlockEntity(pos) instanceof Container) && golem.distanceToSqr(Vec3.atCenterOf(pos)) < 16.0) {
               results.add(pos);
            }
         }
      }

      return results;
   }

   public static ArrayList<Container> getContainersWithRoom(Level level, EntityGolemBase golem, byte color) {
      return getContainersWithRoom(level, golem, color, ItemStack.EMPTY);
   }

   public static ArrayList<Container> getContainersWithRoom(Level level, EntityGolemBase golem, byte color, ItemStack requested) {
      ArrayList<Container> results = new ArrayList<>();
      ItemStack stack = requested.isEmpty() ? golem.getCarried() : requested;
      if (stack.isEmpty()) {
         return results;
      }

      for (Marker marker : golem.getMarkers()) {
         if (color != -1 && marker.getColor() != color) {
            continue;
         }

         BlockEntity be = level.getBlockEntity(marker.getPos());
         if (be instanceof Container container && canInsertStack(container, stack, marker.getSide())) {
            results.add(container);
         }
      }

      return results;
   }

   public static List<Direction> getMarkedSides(EntityGolemBase golem, BlockEntity tile, byte color) {
      BlockPos pos = tile.getBlockPos();
      return getMarkedSides(golem, pos.getX(), pos.getY(), pos.getZ(), color);
   }

   public static List<Direction> getMarkedSides(EntityGolemBase golem, int x, int y, int z, byte color) {
      List<Direction> out = new ArrayList<>();

      for (Marker mark : golem.getMarkers()) {
         BlockPos pos = mark.getPos();
         if (pos.getX() == x && pos.getY() == y && pos.getZ() == z && (color == -1 || mark.getColor() == color)) {
            out.add(mark.getSide());
         }
      }

      return out;
   }

   public static ArrayList<Container> getContainersWithGoods(Level level, EntityGolemBase golem, ItemStack goods, byte color) {
      ArrayList<Container> results = new ArrayList<>();

      for (Marker marker : golem.getMarkers()) {
         if (color != -1 && marker.getColor() != color) {
            continue;
         }

         BlockEntity be = level.getBlockEntity(marker.getPos());
         if (be instanceof Container container && canExtract(container, goods, marker.getSide(), golem)) {
            results.add(container);
         }
      }

      return results;
   }

   private static void addIfContainer(List<Container> list, BlockEntity be) {
      if (be instanceof Container container) {
         list.add(container);
      }
   }

   private static boolean canInsertStack(Container inventory, ItemStack stack, Direction side) {
      return false;
   }

   private static boolean canExtract(Container container, ItemStack goods, Direction side, EntityGolemBase golem) {
      return goods.isEmpty() ? false : false;
   }
}
