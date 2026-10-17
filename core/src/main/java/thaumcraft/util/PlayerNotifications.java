package thaumcraft.util;

import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.init.registry.Config;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class PlayerNotifications {
   public static ArrayList<PlayerNotifications.Notification> notificationList = new ArrayList<>();
   public static ArrayList<PlayerNotifications.AspectNotification> aspectList = new ArrayList<>();

   public static void addNotification(Component text) {
      addNotification(text, null, 16777215);
   }

   @OnlyIn(Dist.CLIENT)
   public static void addAspectNotification(Aspect aspect) {
      long n = System.nanoTime() / 1000000L + Minecraft.getInstance().player.getRandom().nextInt(1000);
      aspectList.add(
         new PlayerNotifications.AspectNotification(
            aspect,
            0.4F + Minecraft.getInstance().player.getRandom().nextFloat() * 0.2F,
            0.4F + Minecraft.getInstance().player.getRandom().nextFloat() * 0.2F,
            n,
            n + 1500L
         )
      );
   }

   public static void addNotification(Component text, Aspect aspect) {
      addNotification(text, aspect.getImage(), aspect.getColor());
   }

   public static void addNotification(Component text, ResourceLocation image) {
      addNotification(text, image, 16777215);
   }

   public static void addNotification(Component text, ResourceLocation image, int color) {
      long n2 = System.nanoTime() / 1000000L;
      notificationList.add(
         new PlayerNotifications.Notification(
            text,
            image,
            n2 + Config.notificationDelay + (notificationList.size() == 0 ? Config.notificationDelay / 2 : 0L),
            n2 + Config.notificationDelay / 4,
            color
         )
      );
   }

   public static ArrayList<PlayerNotifications.Notification> getListAndUpdate(long time) {
      if (!notificationList.isEmpty()) {
         PlayerNotifications.Notification first = notificationList.get(0);
         boolean firstDelay = first.expire >= time;
         notificationList.removeIf(notification -> notification.expire < time);
         notificationList.replaceAll(
            notification -> firstDelay && notification == first
               ? notification
               : new PlayerNotifications.Notification(
                  notification.text, notification.image, time + Config.notificationDelay, notification.created, notification.color
               )
         );
      }

      return notificationList;
   }

   public static ArrayList<PlayerNotifications.AspectNotification> getAspectListAndUpdate(long time) {
      aspectList.removeIf(aspectNotification -> aspectNotification.expire < time);
      return aspectList;
   }

   public static class AspectNotification {
      public Aspect aspect;
      public float startX;
      public float startY;
      public long expire;
      public long created;

      public AspectNotification(Aspect aspect, float startX, float startY, long created, long expire) {
         this.aspect = aspect;
         this.startX = startX;
         this.startY = startY;
         this.expire = expire;
         this.created = created;
      }
   }

   public static class Notification {
      public Component text;
      public ResourceLocation image;
      public long expire;
      public long created;
      public int color;

      public Notification(Component text, ResourceLocation image, long expire, long created, int color) {
         this.text = text;
         this.image = image;
         this.expire = expire;
         this.created = created;
         this.color = color;
      }
   }
}
