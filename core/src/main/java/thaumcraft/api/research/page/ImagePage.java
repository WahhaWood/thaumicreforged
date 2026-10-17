package thaumcraft.api.research.page;

import com.google.gson.JsonObject;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import thaumcraft.block.table.ResearchRecipeScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ImagePage extends TextPage {
   public ResourceLocation image;
   public int width;
   public int height;

   public ImagePage(ResourceLocation image, int width, int height, String text) {
      super(text);
      this.image = image;
      this.width = width;
      this.height = height;
   }

   public ImagePage(JsonObject object) {
      super(object);
      this.image = ResourceLocation.parse(GsonHelper.getAsString(object, "image"));
      this.width = GsonHelper.getAsInt(object, "width", 150);
      this.height = GsonHelper.getAsInt(object, "height", 40);
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void render(ResearchRecipeScreen screen, GuiGraphics graphics, Font font, int page, int x, int y, int mx, int my) {
      graphics.blit(this.image, x, y, 0.0F, 0.0F, this.width, this.height, this.width, this.height);
      super.render(screen, graphics, font, page, x + this.height, y, mx, my);
   }
}
