package thaumcraft.network.playerdata;

import net.minecraft.network.FriendlyByteBuf;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.network.BasePacket;

public class PacketAspectCombinationToServer extends BasePacket {
   Aspect aspect1;
   Aspect aspect2;
   boolean ab1;
   boolean ab2;

   public PacketAspectCombinationToServer(Aspect aspect1, Aspect aspect2, boolean ab1, boolean ab2) {
      this.aspect1 = aspect1;
      this.aspect2 = aspect2;
      this.ab1 = ab1;
      this.ab2 = ab2;
   }

   public PacketAspectCombinationToServer(FriendlyByteBuf buf) {
      this.aspect1 = Aspect.getAspect(buf.readUtf());
      this.aspect2 = Aspect.getAspect(buf.readUtf());
      this.ab1 = buf.readBoolean();
      this.ab2 = buf.readBoolean();
   }

   @Override
   public void write(FriendlyByteBuf buf) {
      buf.writeUtf(this.aspect1.getTag());
      buf.writeUtf(this.aspect2.getTag());
      buf.writeBoolean(this.ab1);
      buf.writeBoolean(this.ab2);
   }

   @Override
   public void executeServer(net.minecraft.server.level.ServerPlayer player) {
      if (player == null || this.aspect1 == null || this.aspect2 == null) {
         return;
      }

      thaumcraft.research.PlayerKnowledge knowledge = thaumcraft.research.PlayerKnowledge.of(player);
      boolean has1 = knowledge.getAspectPoolFor(this.aspect1) > 0;
      boolean has2 = knowledge.getAspectPoolFor(this.aspect2) > 0;

      thaumcraft.block.table.ResearchTableBlockEntity table = null;
      if (player.containerMenu instanceof thaumcraft.block.table.ResearchTableMenu menu) {
         table = menu.table;
      }

      boolean fromTable1 = this.ab1 && table != null && table.bonusAspects.getAmount(this.aspect1) > 0;
      boolean fromTable2 = this.ab2 && table != null && table.bonusAspects.getAmount(this.aspect2) > 0;

      if (!has1 && !fromTable1) {
         return;
      }
      if (!has2 && !fromTable2) {
         return;
      }

      if (fromTable1) {
         table.bonusAspects.reduce(this.aspect1, 1);
         table.setChanged();
         table.markForUpdate();
      } else {
         knowledge.addAspectPool(this.aspect1, (short)-1);
         thaumcraft.network.PacketHandler.sendTo(
            new PacketAspectPool(this.aspect1.getTag(), (short)-1, knowledge.getAspectPoolFor(this.aspect1)), player
         );
      }

      if (fromTable2) {
         table.bonusAspects.reduce(this.aspect2, 1);
         table.setChanged();
         table.markForUpdate();
      } else {
         knowledge.addAspectPool(this.aspect2, (short)-1);
         thaumcraft.network.PacketHandler.sendTo(
            new PacketAspectPool(this.aspect2.getTag(), (short)-1, knowledge.getAspectPoolFor(this.aspect2)), player
         );
      }

      Aspect combo = thaumcraft.research.ResearchManager.getCombinationResult(this.aspect1, this.aspect2);
      if (combo != null) {
         boolean newlyDiscovered = !knowledge.hasDiscoveredAspect(combo);
         if (newlyDiscovered) {
            knowledge.addDiscoveredAspect(combo);
            thaumcraft.network.PacketHandler.sendTo(new PacketAspectDiscovery(combo.getTag()), player);
         }

         knowledge.addAspectPool(combo, (short)1);
         thaumcraft.network.PacketHandler.sendTo(
            new PacketAspectPool(combo.getTag(), (short)1, knowledge.getAspectPoolFor(combo)), player
         );

         player.level().playSound(
            null, player.getX(), player.getY(), player.getZ(),
            (net.minecraft.sounds.SoundEvent)thaumcraft.init.registry.ModSounds.LEARN.get(),
            net.minecraft.sounds.SoundSource.PLAYERS, 0.6F, 1.0F
         );
      }
   }
}
