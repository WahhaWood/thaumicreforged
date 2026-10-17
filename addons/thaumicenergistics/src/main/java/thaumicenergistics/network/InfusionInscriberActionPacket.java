package thaumicenergistics.network;

import thaumicenergistics.menu.InfusionInscriberMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class InfusionInscriberActionPacket implements ThEPacket {
   public static final byte MODE_FULL_UPDATE = 0;
   public static final byte MODE_SAVE_DELETE = 1;
   public static final byte MODE_CLEAR = 2;
   public static final byte MODE_JEI_RECIPE = 3;
   private final byte mode;
   private final ItemStack[] inputs;

   public InfusionInscriberActionPacket(byte mode) {
      this.mode = mode;
      this.inputs = null;
   }

   public InfusionInscriberActionPacket(ItemStack[] inputs) {
      this.mode = 3;
      this.inputs = copyInputs(inputs);
   }

   public InfusionInscriberActionPacket(RegistryFriendlyByteBuf buf) {
      this.mode = buf.readByte();
      if (this.mode == 3) {
         this.inputs = new ItemStack[17];

         for (int i = 0; i < this.inputs.length; i++) {
            this.inputs[i] = (ItemStack)ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
         }
      } else {
         this.inputs = null;
      }
   }

   @Override
   public void write(RegistryFriendlyByteBuf buf) {
      buf.writeByte(this.mode);
      if (this.mode == 3 && this.inputs != null) {
         for (ItemStack input : this.inputs) {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, input);
         }
      }
   }

   @Override
   public void handle(IPayloadContext context) {
      context.enqueueWork(() -> {
         if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof InfusionInscriberMenu menu && menu.stillValid(player)) {
            switch (this.mode) {
               case 0:
                  menu.onClientRequestSaveState();
                  break;
               case 1:
                  menu.onClientRequestSaveOrDelete(player);
                  break;
               case 2:
                  menu.onClientRequestClearGrid();
                  break;
               case 3:
                  if (this.inputs != null) {
                     menu.acceptJEIRecipe(this.inputs);
                  }
            }
         }
      });
   }

   public static void sendFullUpdateRequest() {
      ThEPacketHandler.sendToServer(new InfusionInscriberActionPacket((byte)0));
   }

   public static void sendSaveDelete() {
      ThEPacketHandler.sendToServer(new InfusionInscriberActionPacket((byte)1));
   }

   public static void sendClearGrid() {
      ThEPacketHandler.sendToServer(new InfusionInscriberActionPacket((byte)2));
   }

   public static void sendJEIRecipe(ItemStack[] inputs) {
      ThEPacketHandler.sendToServer(new InfusionInscriberActionPacket(inputs));
   }

   private static ItemStack[] copyInputs(ItemStack[] inputs) {
      ItemStack[] copied = new ItemStack[17];

      for (int i = 0; i < copied.length; i++) {
         ItemStack stack = inputs != null && i < inputs.length ? inputs[i] : ItemStack.EMPTY;
         copied[i] = stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1);
      }

      return copied;
   }
}
