package thaumicenergistics.client.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/** Text that must stay inside a window whatever the language is. */
@OnlyIn(Dist.CLIENT)
final class ScreenText {
   static final int TITLE_COLOR = 4210752;

   private ScreenText() {
   }

   /** The title, cut with "…" when a long translation would not fit {@code width} pixels. */
   static String fitTitle(Font font, Component title, int width) {
      String text = title.getString();
      return font.width(text) <= width ? text : font.plainSubstrByWidth(text, width - font.width("…")) + "…";
   }
}
