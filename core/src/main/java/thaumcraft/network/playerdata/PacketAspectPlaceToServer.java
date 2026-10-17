package thaumcraft.network.playerdata;

import net.minecraft.network.FriendlyByteBuf;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.network.BasePacket;

public class PacketAspectPlaceToServer extends BasePacket {
   Aspect aspect;
   byte q;
   byte r;

   public PacketAspectPlaceToServer(byte q, byte r, Aspect aspect) {
      this.aspect = aspect;
      this.q = q;
      this.r = r;
   }

   public PacketAspectPlaceToServer(FriendlyByteBuf buf) {
      this.aspect = Aspect.getAspect(buf.readUtf());
      this.q = buf.readByte();
      this.r = buf.readByte();
   }

   @Override
   public void write(FriendlyByteBuf buf) {
      buf.writeUtf(this.aspect == null ? "null" : this.aspect.getTag());
      buf.writeByte(this.q);
      buf.writeByte(this.r);
   }

   @Override
   public void executeServer(net.minecraft.server.level.ServerPlayer player) {
      if (player == null || !(player.containerMenu instanceof thaumcraft.block.table.ResearchTableMenu menu)) {
         return;
      }

      net.minecraft.world.item.ItemStack scribeTools = menu.getSlot(0).getItem();
      net.minecraft.world.item.ItemStack noteStack = menu.getSlot(1).getItem();

      if (scribeTools.isEmpty() || !(scribeTools.getItem() instanceof thaumcraft.api.IScribeTools)) {
         return;
      }
      if (scribeTools.getDamageValue() >= scribeTools.getMaxDamage()) {
         return;
      }

      if (noteStack.isEmpty() || !noteStack.is((net.minecraft.world.item.Item)thaumcraft.init.registry.ModItems.RESEARCH_NOTES.get())) {
         return;
      }

      thaumcraft.research.ResearchNoteData data = thaumcraft.research.ResearchManager.getData(noteStack);
      if (data == null || data.complete) {
         return;
      }

      thaumcraft.util.HexUtils.Hex hex = new thaumcraft.util.HexUtils.Hex(this.q, this.r);
      String hexKey = hex.toString();
      if (!data.hexes.containsKey(hexKey)) {
         return;
      }

      thaumcraft.research.PlayerKnowledge knowledge = thaumcraft.research.PlayerKnowledge.of(player);

      if (this.aspect != null) {
         thaumcraft.research.ResearchManager.HexEntry entry = data.hexEntries.get(hexKey);
         if (entry == null || entry.type != 0) {
            return;
         }

         // Приоритет списания как в оригинале: сначала пул игрока и только при
         // пустом пуле — бонусные аспекты стола. Мастерство (researcher2) даёт
         // 10% шанс бесплатной установки.
         boolean free = thaumcraft.research.ResearchManager.isResearchComplete(player, "researcher2")
            && player.level().random.nextFloat() < 0.1F;
         boolean fromPlayer = knowledge.getAspectPoolFor(this.aspect) > 0;
         boolean fromTable = !fromPlayer && menu.table != null
            && menu.table.bonusAspects.getAmount(this.aspect) > 0;
         if (!free && !fromTable && !fromPlayer) {
            return;
         }

         if (!free) {
            if (fromPlayer) {
               knowledge.addAspectPool(this.aspect, (short)-1);
               thaumcraft.network.PacketHandler.sendTo(
                  new PacketAspectPool(this.aspect.getTag(), (short)-1, knowledge.getAspectPoolFor(this.aspect)), player
               );
            } else {
               menu.table.bonusAspects.reduce(this.aspect, 1);
               menu.table.setChanged();
               menu.table.markForUpdate();
            }
         }

         entry.aspect = this.aspect;
         entry.type = 2;
         scribeTools.setDamageValue(scribeTools.getDamageValue() + 1);

         boolean completed = thaumcraft.research.ResearchManager.checkResearchCompletion(noteStack, data, player);
         if (completed) {
            // Завершённая записка меняет текстуру (discovery) — в оригинале это
            // damage 64. Слот синхронизируется через broadcastChanges ниже.
            if (noteStack.getItem() instanceof thaumcraft.item.research_notes.ItemResearchNotes notes) {
               notes.setDiscovery(noteStack);
            }

            if (menu.table != null) {
               menu.table.playLearnEffect();
            } else {
               player.level().playSound(
                  null, player.getX(), player.getY(), player.getZ(),
                  (net.minecraft.sounds.SoundEvent)thaumcraft.init.registry.ModSounds.LEARN.get(),
                  net.minecraft.sounds.SoundSource.PLAYERS, 0.8F, 1.2F
               );
            }
         }
      } else {
         thaumcraft.research.ResearchManager.HexEntry entry = data.hexEntries.get(hexKey);
         if (entry != null && entry.type == 2) {
            // Возврат аспекта при стирании: мастерство 25%, совершенство 50%.
            float roll = player.level().random.nextFloat();
            boolean r1 = thaumcraft.research.ResearchManager.isResearchComplete(player, "researcher1");
            boolean r2 = thaumcraft.research.ResearchManager.isResearchComplete(player, "researcher2");
            if (entry.aspect != null && ((r1 && roll < 0.25F) || (r2 && roll < 0.5F))) {
               knowledge.addAspectPool(entry.aspect, (short)1);
               thaumcraft.network.PacketHandler.sendTo(
                  new PacketAspectPool(entry.aspect.getTag(), (short)1, knowledge.getAspectPoolFor(entry.aspect)), player
               );
            }

            entry.aspect = null;
            entry.type = 0;
            scribeTools.setDamageValue(scribeTools.getDamageValue() + 1);
         }
      }

      thaumcraft.research.ResearchManager.updateData(noteStack, data);
      menu.broadcastChanges();
      if (menu.table != null) {
         menu.table.data = data;
         menu.table.setChanged();
         menu.table.markForUpdate();
      }
   }
}
