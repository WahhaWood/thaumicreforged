package thaumcraft.debug.bridge;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Client half of the dev bridge.  Only referenced when a screenshot is asked
 * for, so dedicated servers never touch it.
 */
public final class DevBridgeClient {
   private DevBridgeClient() {
   }

   /** Saves a screenshot into run/screenshots and returns its absolute path. */
   @OnlyIn(Dist.CLIENT)
   public static String screenshot(String name) throws Exception {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft == null) {
         throw new IllegalStateException("no client");
      }

      String fileName = name.endsWith(".png") ? name : name + ".png";
      CountDownLatch latch = new CountDownLatch(1);
      String[] message = new String[1];

      minecraft.execute(() -> {
         try {
            Screenshot.grab(minecraft.gameDirectory, fileName, minecraft.getMainRenderTarget(), component -> {
               message[0] = component.getString();
               latch.countDown();
            });
         } catch (Throwable t) {
            message[0] = String.valueOf(t);
            latch.countDown();
         }
      });

      if (!latch.await(30L, TimeUnit.SECONDS)) {
         throw new IllegalStateException("screenshot timed out");
      }

      File file = new File(new File(minecraft.gameDirectory, "screenshots"), fileName);
      return file.getAbsolutePath() + (message[0] != null && !message[0].isBlank() ? " | " + message[0] : "");
   }
}
