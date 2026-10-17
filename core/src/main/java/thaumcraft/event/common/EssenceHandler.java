package thaumcraft.event.common;

import java.util.ArrayList;
import java.util.HashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class EssenceHandler {
   private static final HashMap<EssenceHandler.LocalPos, ArrayList<EssenceHandler.LocalPos>> sources = new HashMap<>();
   private static final HashMap<EssenceHandler.LocalPos, Long> sourcesDelay = new HashMap<>();
   public static HashMap<String, EssenceHandler.EssenceSourceFX> sourceFX = new HashMap<>();

   public static class EssenceSourceFX {
      public BlockPos start;
      public BlockPos end;
      public int ticks;
      public int color;

      public EssenceSourceFX(BlockPos start, BlockPos end, int ticks, int color) {
         this.start = start;
         this.end = end;
         this.ticks = ticks;
         this.color = color;
      }
   }

   private record LocalPos(ResourceKey<Level> dim, BlockPos pos) {
   }
}
