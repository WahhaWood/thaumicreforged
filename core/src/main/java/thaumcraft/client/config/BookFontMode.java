package thaumcraft.client.config;

public enum BookFontMode {
   ORIGINAL("gui.thaumcraft.thaumonomicon_font.original", "gui.thaumcraft.thaumonomicon_font.original.short"),
   REALISTIC("gui.thaumcraft.thaumonomicon_font.realistic", "gui.thaumcraft.thaumonomicon_font.realistic.short");

   private final String translationKey;
   private final String shortTranslationKey;

   private BookFontMode(String translationKey, String shortTranslationKey) {
      this.translationKey = translationKey;
      this.shortTranslationKey = shortTranslationKey;
   }

   public String getTranslationKey() {
      return this.translationKey;
   }

   public String getShortTranslationKey() {
      return this.shortTranslationKey;
   }

   public BookFontMode next() {
      return this == ORIGINAL ? REALISTIC : ORIGINAL;
   }

   public static BookFontMode byName(String name) {
      if (name != null) {
         for (BookFontMode value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
               return value;
            }
         }
      }

      return ORIGINAL;
   }
}
