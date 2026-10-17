package thaumcraft.event.common;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import thaumcraft.item.armor.hover_harness.HoverLogic;
import thaumcraft.item.armor.hover_harness.ItemHoverHarness;
import thaumcraft.item.golem_bell.ItemGolemBell;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.network.PacketHandler;
import thaumcraft.network.misc.PacketFlyToServer;
import thaumcraft.network.misc.PacketFocusChangeToServer;
import thaumcraft.network.misc.PacketItemKeyToServer;

public class KeyHandler {
   public KeyMapping keyF;
   public KeyMapping keyH;
   public KeyMapping keyG;
   private boolean keyPressedF;
   private boolean keyPressedH;
   private boolean keyPressedG;
   public static boolean radialActive;
   public static boolean radialLock;
   public static long lastPressF;
   public static long lastPressH;
   public static long lastPressG;

   @OnlyIn(Dist.CLIENT)
   public void registerKeyMappings(RegisterKeyMappingsEvent event) {
      this.keyF = new KeyMapping("key.thaumcraft.change_focus", 70, "key.categories.thaumcraft");
      this.keyH = new KeyMapping("key.thaumcraft.hover_harness", 72, "key.categories.thaumcraft");
      this.keyG = new KeyMapping("key.thaumcraft.wand_toggle", 71, "key.categories.thaumcraft");
      event.register(this.keyF);
      event.register(this.keyH);
      event.register(this.keyG);
   }

   @SubscribeEvent
   @OnlyIn(Dist.CLIENT)
   public void playerTick(Post event) {
      Player player = Minecraft.getInstance().player;
      if (player != null) {
         while (this.keyF.consumeClick()) {
            if (!this.keyPressedF) {
               lastPressF = System.currentTimeMillis();
               radialLock = false;
               boolean handled = false;

               for (InteractionHand hand : InteractionHand.values()) {
                  ItemStack stack = player.getItemInHand(hand);
                  if (!radialLock && stack.getItem() instanceof ItemWandCasting wand && !wand.isSceptre(stack)) {
                     if (player.isShiftKeyDown()) {
                        PacketHandler.sendToServer(new PacketFocusChangeToServer("REMOVE"));
                     } else {
                        radialActive = true;
                     }

                     handled = true;
                     break;
                  }

                  if (stack.getItem() instanceof ItemGolemBell) {
                     PacketHandler.sendToServer(new PacketItemKeyToServer(0));
                     handled = true;
                     break;
                  }
               }
            }

            this.keyPressedF = true;
         }

         if (!this.keyF.isDown()) {
            if (this.keyPressedF) {
               radialActive = false;
               lastPressF = System.currentTimeMillis();
            }

            this.keyPressedF = false;
         }

         for (; this.keyH.consumeClick(); this.keyPressedH = true) {
            if (!this.keyPressedH) {
               lastPressH = System.currentTimeMillis();
               ItemStack stackx = player.getItemBySlot(EquipmentSlot.CHEST);
               if (stackx.getItem() instanceof ItemHoverHarness) {
                  boolean newState = HoverLogic.toggleHover(stackx);
                  PacketHandler.sendToServer(new PacketFlyToServer(newState));
                  player.displayClientMessage(Component.translatable(newState ? "thaumcraft.hover.on" : "thaumcraft.hover.off"), true);
               }
            }
         }

         if (!this.keyH.isDown()) {
            if (this.keyPressedH) {
               lastPressH = System.currentTimeMillis();
            }

            this.keyPressedH = false;
         }

         for (; this.keyG.consumeClick(); this.keyPressedG = true) {
            if (!this.keyPressedG) {
               lastPressG = System.currentTimeMillis();
               PacketHandler.sendToServer(new PacketItemKeyToServer(1));
            }
         }

         if (!this.keyG.isDown()) {
            if (this.keyPressedG) {
               lastPressG = System.currentTimeMillis();
            }

            this.keyPressedG = false;
         }
      }
   }
}
