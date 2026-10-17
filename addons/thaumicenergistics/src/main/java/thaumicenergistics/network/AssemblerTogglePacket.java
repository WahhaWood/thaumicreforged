package thaumicenergistics.network;

import thaumicenergistics.ae2.recipe.AssemblerRecipes;
import thaumicenergistics.ae2.recipe.RecipeAssembler;
import thaumicenergistics.menu.RecipeAssemblerMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

/** Client → server: a click in an assembler GUI (one recipe on/off, or all of them). */
public class AssemblerTogglePacket implements ThEPacket {
   private final int containerId;
   private final AssemblerRecipes.Action action;
   @Nullable
   private final ResourceLocation recipe;

   public AssemblerTogglePacket(int containerId, AssemblerRecipes.Action action, @Nullable ResourceLocation recipe) {
      this.containerId = containerId;
      this.action = action;
      this.recipe = recipe;
   }

   public AssemblerTogglePacket(RegistryFriendlyByteBuf buf) {
      this.containerId = buf.readVarInt();
      this.action = buf.readEnum(AssemblerRecipes.Action.class);
      this.recipe = buf.readBoolean() ? buf.readResourceLocation() : null;
   }

   @Override
   public void write(RegistryFriendlyByteBuf buf) {
      buf.writeVarInt(this.containerId);
      buf.writeEnum(this.action);
      buf.writeBoolean(this.recipe != null);
      if (this.recipe != null) {
         buf.writeResourceLocation(this.recipe);
      }
   }

   @Override
   public void handle(IPayloadContext context) {
      // The channel is bidirectional: only a client may send this.
      if (context.flow().isServerbound() && context.player() instanceof ServerPlayer player) {
         context.enqueueWork(() -> this.execute(player));
      }
   }

   private void execute(ServerPlayer player) {
      // only the menu the player really has open, and only while it is still valid
      AbstractContainerMenu open = player.containerMenu;
      if (open instanceof RecipeAssemblerMenu menu && open.containerId == this.containerId && open.stillValid(player)) {
         RecipeAssembler assembler = menu.recipeAssembler();
         assembler.recipeBook().apply(assembler, this.action, this.recipe);
      }
   }
}
