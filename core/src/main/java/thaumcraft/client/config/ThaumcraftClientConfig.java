package thaumcraft.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.neoforged.fml.loading.FMLPaths;
import thaumcraft.Thaumcraft;

public final class ThaumcraftClientConfig {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final Path FILE = FMLPaths.CONFIGDIR.get().resolve("thaumcraft-client.json");
   private static ThaumcraftClientConfig.ClientOptions options = new ThaumcraftClientConfig.ClientOptions();
   private static boolean loaded;

   private ThaumcraftClientConfig() {
   }

   public static synchronized void load() {
      if (!loaded) {
         options = new ThaumcraftClientConfig.ClientOptions();
         boolean shouldSave = !Files.exists(FILE);
         if (!shouldSave) {
            try (Reader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
               ThaumcraftClientConfig.ClientOptions loadedOptions = (ThaumcraftClientConfig.ClientOptions)GSON.fromJson(
                  reader, ThaumcraftClientConfig.ClientOptions.class
               );
               if (loadedOptions != null) {
                  options = loadedOptions;
               } else {
                  shouldSave = true;
               }
            } catch (Exception var6) {
               shouldSave = true;
               Thaumcraft.LOGGER.warn("Failed to read client config {}", FILE, var6);
            }
         }

         options.thaumonomiconFontMode = getResolvedFontMode().name();
         loaded = true;
         if (shouldSave) {
            save();
         }
      }
   }

   public static synchronized BookFontMode getBookFontMode() {
      load();
      return getResolvedFontMode();
   }

   public static synchronized BookFontMode toggleBookFontMode() {
      BookFontMode next = getBookFontMode().next();
      setBookFontMode(next);
      return next;
   }

   public static synchronized void setBookFontMode(BookFontMode mode) {
      load();
      options.thaumonomiconFontMode = (mode == null ? BookFontMode.ORIGINAL : mode).name();
      save();
   }

   public static synchronized void save() {
      if (loaded) {
         try {
            Files.createDirectories(FILE.getParent());

            try (Writer writer = Files.newBufferedWriter(FILE, StandardCharsets.UTF_8)) {
               GSON.toJson(options, writer);
            }
         } catch (Exception var5) {
            Thaumcraft.LOGGER.warn("Failed to write client config {}", FILE, var5);
         }
      }
   }

   private static BookFontMode getResolvedFontMode() {
      return BookFontMode.byName(options.thaumonomiconFontMode);
   }

   private static final class ClientOptions {
      private String thaumonomiconFontMode = BookFontMode.ORIGINAL.name();
   }
}
