package thaumicenergistics.network;

import thaumicenergistics.menu.CrucibleInscriberMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

public class CrucibleInscriberActionPacket implements ThEPacket {
   public static final byte MODE_FULL_UPDATE = 0;
   public static final byte MODE_SAVE_DELETE = 1;
   public static final byte MODE_SCROLL_BACK = 2;
   public static final byte MODE_SCROLL_NEXT = 3;
   public static final byte MODE_JEI_RECIPE = 4;
   private final byte mode;
   private ItemStack catalyst;
   private ItemStack output;
   private AspectList aspects;

   public CrucibleInscriberActionPacket(byte mode) {
      this.mode = mode;
   }

   public CrucibleInscriberActionPacket(ItemStack catalyst, ItemStack output, AspectList aspects) {
      this.mode = 4;
      this.catalyst = catalyst;
      this.output = output;
      this.aspects = aspects;
   }

   public CrucibleInscriberActionPacket(RegistryFriendlyByteBuf buf) {
      this.mode = buf.readByte();
      if (this.mode == 4) {
         this.catalyst = (ItemStack)ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
         this.output = (ItemStack)ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
         int aspectCount = buf.readVarInt();
         this.aspects = new AspectList();

         for (int i = 0; i < aspectCount; i++) {
            String tag = buf.readUtf();
            int count = buf.readVarInt();
            Aspect aspect = Aspect.getAspect(tag);
            if (aspect != null) {
               this.aspects.add(aspect, count);
            }
         }
      }
   }

   @Override
   public void write(RegistryFriendlyByteBuf buf) {
      buf.writeByte(this.mode);
      if (this.mode == 4) {
         ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, this.catalyst);
         ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, this.output);
         buf.writeVarInt(this.aspects.size());

         for (Aspect aspect : this.aspects.getAspects()) {
            buf.writeUtf(aspect.getTag());
            buf.writeVarInt(this.aspects.getAmount(aspect));
         }
      }
   }

   @Override
   public void handle(IPayloadContext context) {
      context.enqueueWork(() -> {
         if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof CrucibleInscriberMenu menu && menu.stillValid(player)) {
            switch (this.mode) {
               case 0:
                  menu.onClientRequestSaveState();
                  break;
               case 1:
                  menu.onClientRequestSaveOrDelete(player);
                  break;
               case 2:
                  menu.scrollCurrentRecipe(false);
                  break;
               case 3:
                  menu.scrollCurrentRecipe(true);
                  break;
               case 4:
                  if (this.catalyst != null && this.output != null && this.aspects != null) {
                     menu.acceptJEIRecipe(this.catalyst, this.aspects, this.output);
                  }
            }
         }
      });
   }

   public static void sendFullUpdateRequest() {
      ThEPacketHandler.sendToServer(new CrucibleInscriberActionPacket((byte)0));
   }

   public static void sendSaveDelete() {
      ThEPacketHandler.sendToServer(new CrucibleInscriberActionPacket((byte)1));
   }

   public static void sendScrollBack() {
      ThEPacketHandler.sendToServer(new CrucibleInscriberActionPacket((byte)2));
   }

   public static void sendScrollNext() {
      ThEPacketHandler.sendToServer(new CrucibleInscriberActionPacket((byte)3));
   }

   public static void sendJEIRecipe(ItemStack catalyst, ItemStack output, AspectList aspects) {
      ThEPacketHandler.sendToServer(new CrucibleInscriberActionPacket(catalyst, output, aspects));
   }
}
