package thaumcraft.api.research;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public class ResearchCategoryList {
   public final ResourceLocation icon;
   public final ResourceLocation background;
   public final int lineColor;
   public final Map<String, ResearchItem> research = new HashMap<>();
   public int minColumn;
   public int maxColumn;
   public int minRow;
   public int maxRow;

   public ResearchCategoryList(ResourceLocation icon, ResourceLocation background) {
      this(icon, background, 2763306);
   }

   public ResearchCategoryList(ResourceLocation icon, ResourceLocation background, int lineColor) {
      this.icon = icon;
      this.background = background;
      this.lineColor = lineColor;
      this.minColumn = Integer.MAX_VALUE;
      this.maxColumn = Integer.MIN_VALUE;
      this.minRow = Integer.MAX_VALUE;
      this.maxRow = Integer.MIN_VALUE;
   }
}
