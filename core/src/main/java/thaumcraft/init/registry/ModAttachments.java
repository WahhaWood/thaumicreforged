package thaumcraft.init.registry;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries.Keys;
import thaumcraft.dimension.outer_lands.data.ChunkMazeData;
import thaumcraft.research.PlayerKnowledge;

public final class ModAttachments {
   public static final DeferredRegister<AttachmentType<?>> REGISTER = DeferredRegister.create(Keys.ATTACHMENT_TYPES, "thaumcraft");
   public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerKnowledge>> PLAYER_KNOWLEDGE = REGISTER.register(
      "knowledge", () -> AttachmentType.serializable(holder -> new PlayerKnowledge((Player)holder)).copyOnDeath().build()
   );
   public static final DeferredHolder<AttachmentType<?>, AttachmentType<ChunkMazeData.Impl>> CHUNK_MAZE_DATA = REGISTER.register(
      "maze_data", () -> AttachmentType.serializable(ChunkMazeData.Impl::new).build()
   );

   private ModAttachments() {
   }
}
