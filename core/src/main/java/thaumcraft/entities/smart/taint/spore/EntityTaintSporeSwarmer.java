package thaumcraft.entities.smart.taint.spore;

import java.util.Iterator;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import thaumcraft.Thaumcraft;
import thaumcraft.init.registry.ModEntities;

public class EntityTaintSporeSwarmer extends EntityTaintSpore {
   private int spawnCounter = 500;
   private static final int SPAWN_INTERVAL = 500;
   private static final byte EVENT_SWARM_BURST = 6;

   public EntityTaintSporeSwarmer(EntityType<? extends EntityTaintSporeSwarmer> type, Level level) {
      super(type, level);
      this.setSporeSize(10);
   }

   public EntityTaintSporeSwarmer(Level level) {
      this((EntityType<? extends EntityTaintSporeSwarmer>)ModEntities.TAINT_SPORE_SWARMER.get(), level);
   }

   @Override
   public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
      return super.getAddEntityPacket(entity);
   }

   public boolean hurt(DamageSource source, float amount) {
      if (this.level().isClientSide) {
         this.sploosh(10);
      }

      return super.hurt(source, amount);
   }

   @Override
   protected void sporeOnUpdate() {
      this.checkInsideBlocks();
      if (this.spawnCounter > 0) {
         this.spawnCounter--;
      }

      if (this.level().isClientSide) {
         Iterator unused = this.swarm.iterator();

         while (unused.hasNext()) {
            Object _x = unused.next();
            if (_x == null) {
               unused.remove();
            } else if (_x instanceof Entity _xx && _xx.isRemoved()) {
               unused.remove();
            }
         }

         if (this.swarm.size() < (500 - this.spawnCounter) / 25) {
            this.swarm.add(Thaumcraft.setup.swarmParticleFX(this.level(), this, 0.1F, 10.0F, 0.0F));
         }
      }
   }

   @Override
   public void playerTouch(Player player) {
   }

   public void handleEntityEvent(byte id) {
      if (id == 6) {
         this.spawnCounter = 500;
         this.sploosh(25);

         for (Object unused : this.swarm) {
            if (unused instanceof Entity _x) {
               _x.discard();
            }
         }

         this.swarm.clear();
      } else {
         super.handleEntityEvent(id);
      }
   }

   @Override
   public int getAmbientSoundInterval() {
      return 200;
   }

   @Override
   protected SoundEvent getAmbientSound() {
      return getThaumcraftSound("roots");
   }

   private static SoundEvent getThaumcraftSound(String path) {
      return (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("thaumcraft", path));
   }

   private static void playThaumcraftSound(Entity e, String path, float volume, float pitch) {
      SoundEvent se = getThaumcraftSound(path);
      if (se != null) {
         e.level().playSound(null, e.blockPosition(), se, SoundSource.HOSTILE, volume, pitch);
      }
   }
}
