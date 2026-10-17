package thaumcraft.network.playerdata;

import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.research.ResearchApi;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.network.BasePacket;
import thaumcraft.research.PlayerKnowledge;
import net.minecraft.world.item.ItemStack;
import thaumcraft.init.registry.Config;
import thaumcraft.research.ResearchManager;

public class PacketPlayerCompleteToServer extends BasePacket {
   private final String key;
   private final byte type;

   public PacketPlayerCompleteToServer(String key, byte type) {
      this.key = key;
      this.type = type;
   }

   public PacketPlayerCompleteToServer(FriendlyByteBuf buf) {
      this.key = buf.readUtf();
      this.type = buf.readByte();
   }

   @Override
   public void write(FriendlyByteBuf buf) {
      buf.writeUtf(this.key);
      buf.writeByte(this.type);
   }

   /**
    * Клик по исследованию в таумономиконе — порт
    * {@code PacketPlayerCompleteToServer.onMessage} (1.7.10).
    *
    * <p>Тип решает, чем игрок платит за исследование:
    * <ul>
    *   <li><b>0</b> — аспектами: стоимость ({@code tags}) вычитается из пула,
    *       исследование завершается. Так работает покупка, когда аспектов хватает.</li>
    *   <li><b>1</b> — аспектов не хватает, поэтому выдаётся <b>бумажная заметка</b>
    *       (тратятся чернила и бумага). Её кладут на стол исследования и
    *       разыгрывают мини-игру со сборкой аспектов.</li>
    * </ul>
    *
    * <p>Раньше тип полностью игнорировался и исследование завершалось всегда —
    * поэтому по клику изучалось то, что в TC4 требовало мини-игры.
    *
    * <p>Кроме ключа завершаются «соседи» ({@code siblings}) с выполненными
    * пререквизитами. Родители НЕ выдаются — как и в оригинале.
    */
   public void executeServer(net.minecraft.server.level.ServerPlayer player) {
      if (player == null || this.key == null || this.key.isEmpty()) {
         return;
      }

      if (ResearchManager.isResearchComplete(player, this.key)) {
         return;
      }

      if (!ResearchManager.doesPlayerHaveRequisites(player, this.key)) {
         player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("tc.researcherror"));
         return;
      }

      // The server repeats the checks of the Thaumonomicon instead of trusting the click (TC4 did not):
      // type 0 buys a research with aspects, type 2 is the operator-only debug completion.
      if (this.type == 0 && !canBuyWithAspects(player, ResearchApi.getResearch(this.key))
         || this.type == 2 && !player.hasPermissions(2)
         || this.type < 0 || this.type > 2) {
         return;
      }

      net.minecraft.server.level.ServerLevel level = (net.minecraft.server.level.ServerLevel)player.level();

      if (this.type == 1) {
         // Аспектов не хватает — выдаём бумажную заметку вместо изучения.
         ItemStack note = ResearchManager.createResearchNoteForPlayer(level, player, this.key);
         if (!note.isEmpty()) {
            PlayerKnowledgeSync.syncFull(player);
         }

         return;
      }

      // Аспектов хватает: списываем стоимость исследования.
      ResearchItem research = ResearchApi.getResearch(this.key);
      thaumcraft.research.PlayerKnowledge knowledge = thaumcraft.research.PlayerKnowledge.of(player);
      if (research != null && research.tags != null) {
         for (thaumcraft.api.aspects.Aspect aspect : research.tags.getAspects()) {
            int amount = research.tags.getAmount(aspect);
            if (amount <= 0) {
               continue;
            }

            knowledge.addAspectPool(aspect, (short)(-amount));
            ResearchManager.scheduleSave(player);
            thaumcraft.network.PacketHandler.sendTo(
               new thaumcraft.network.playerdata.PacketAspectPool(
                  aspect.getTag(), (short)(-amount), knowledge.getAspectPoolFor(aspect)
               ), player
            );
         }
      }

      giveResearchAndSiblings(player, this.key);
      level.playSound(null, player.getX(), player.getY(), player.getZ(),
         (net.minecraft.sounds.SoundEvent)thaumcraft.init.registry.ModSounds.LEARN.get(),
         net.minecraft.sounds.SoundSource.PLAYERS, 0.75F, 1.0F);
      PlayerKnowledgeSync.syncFull(player);
   }

   /**
    * Rules of the Thaumonomicon click: aspects buy a research when the difficulty is -1 (everything) or 0 and
    * the research is secondary, and the pool holds the full cost. Everything else goes through research notes.
    */
   public static boolean canBuyWithAspects(Player player, @Nullable ResearchItem research) {
      if (research == null || research.tags == null || research.tags.size() == 0
         || !(Config.researchDifficulty == -1 || Config.researchDifficulty == 0 && research.isSecondary())) {
         return false;
      }

      PlayerKnowledge knowledge = PlayerKnowledge.of(player);
      for (Aspect aspect : research.tags.getAspects()) {
         if (knowledge.getAspectPoolFor(aspect) < research.tags.getAmount(aspect)) {
            return false;
         }
      }

      return true;
   }

   /** Завершает исследование и всех «соседей» с выполненными пререквизитами. */
   private static void giveResearchAndSiblings(net.minecraft.server.level.ServerPlayer player, String key) {
      if (ResearchManager.isResearchComplete(player, key)) {
         return;
      }

      ResearchApi.completeResearch(player, key);
      ResearchItem research = ResearchApi.getResearch(key);
      if (research != null && research.siblings != null) {
         for (String sibling : research.siblings) {
            if (!ResearchManager.isResearchComplete(player, sibling)
               && ResearchManager.doesPlayerHaveRequisites(player, sibling)) {
               ResearchApi.completeResearch(player, sibling);
            }
         }
      }
   }
}
