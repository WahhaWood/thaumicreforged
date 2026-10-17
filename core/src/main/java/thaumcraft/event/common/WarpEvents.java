package thaumcraft.event.common;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.entities.smart.eldrich_guardian.EntityEldritchGuardian;
import thaumcraft.entities.smart.mind_spider.EntityMindSpider;
import thaumcraft.init.registry.ModMobEffect;
import thaumcraft.item.armor.thaumium_fortress.ItemFortressArmor;
import thaumcraft.network.PacketHandler;
import thaumcraft.network.misc.PacketMiscEvent;
import thaumcraft.network.playerdata.PacketAspectPool;
import thaumcraft.network.playerdata.PacketResearchComplete;
import thaumcraft.network.playerdata.PacketSyncWarp;
import thaumcraft.network.playerdata.PacketWarpMessage;
import thaumcraft.research.PlayerKnowledge;
import thaumcraft.research.ResearchManager;
import thaumcraft.util.ItemStackData;
import top.theillusivec4.curios.api.CuriosApi;

public class WarpEvents {
   public static void checkWarpEvent(ServerPlayer player) {
      PlayerKnowledge knowledge = PlayerKnowledge.of(player);
      int warp = knowledge.getWarpTotal();
      int actualwarp = knowledge.getWarpPerm() + knowledge.getWarpSticky();
      warp += getWarpFromGear(player);
      int warpCounter = knowledge.getWarpCounter();
      int r = player.level().random.nextInt(100);
      if (warpCounter > 0 && warp > 0 && (double)r <= Math.sqrt((double)warpCounter)) {
         warp = Math.min(100, (warp + warp + warpCounter) / 3);
         warpCounter = (int)((double)warpCounter - Math.max(5.0, Math.sqrt((double)warpCounter) * 2.0));
         knowledge.setWarpCounter(warpCounter);
         int eff = player.level().random.nextInt(warp);
         ItemStack helm = player.getItemBySlot(EquipmentSlot.HEAD);
         if (!helm.isEmpty() && helm.getItem() instanceof ItemFortressArmor) {
            CompoundTag compound = ItemStackData.getTag(helm);
            if (compound != null && compound.contains("mask") && compound.getInt("mask") == 0) {
               eff -= 2 + player.level().random.nextInt(4);
            }
         }

         PacketHandler.sendTo(new PacketMiscEvent(PacketMiscEvent.Type.WARP), player);
         if (eff > 0) {
            if (eff <= 4) {
               grantResearch(player, 1);
               player.sendSystemMessage(Component.literal("§5§o").append(Component.translatable("warp.text.3")));
            } else if (eff > 8) {
               if (eff <= 12) {
                  player.sendSystemMessage(Component.literal("§5§o").append(Component.translatable("warp.text.11")));
               } else if (eff <= 16) {
                  player.addEffect(new MobEffectInstance(ModMobEffect.VIS_EXHAUST, 5000, Math.min(3, warp / 15), true, true));
                  player.sendSystemMessage(Component.literal("§5§o").append(Component.translatable("warp.text.1")));
               } else if (eff <= 20) {
                  player.addEffect(new MobEffectInstance(ModMobEffect.THAUMARHIA, Math.min(32000, 10 * warp), 0, true, true));
                  player.sendSystemMessage(Component.literal("§5§o").append(Component.translatable("warp.text.15")));
               } else if (eff <= 24) {
                  player.addEffect(new MobEffectInstance(ModMobEffect.UNNATURAL_HUNGER, 5000, Math.min(3, warp / 15), true, true));
                  player.sendSystemMessage(Component.literal("§5§o").append(Component.translatable("warp.text.2")));
               } else if (eff <= 28) {
                  player.sendSystemMessage(Component.literal("§5§o").append(Component.translatable("warp.text.12")));
               } else if (eff <= 32) {
                  spawnMist(player, warp, 1);
               } else if (eff <= 36) {
                  player.addEffect(new MobEffectInstance(ModMobEffect.BLURRED, Math.min(32000, 10 * warp), 0, true, true));
               } else if (eff <= 40) {
                  player.addEffect(new MobEffectInstance(ModMobEffect.SUN_SCORNED, 5000, Math.min(3, warp / 15), true, true));
                  player.sendSystemMessage(Component.literal("§5§o").append(Component.translatable("warp.text.5")));
               } else if (eff <= 44) {
                  player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 1200, Math.min(3, warp / 15), true, true));
                  player.sendSystemMessage(Component.literal("§5§o").append(Component.translatable("warp.text.9")));
               } else if (eff <= 48) {
                  player.addEffect(new MobEffectInstance(ModMobEffect.INFECTIOUS_VIS_EXHAUST, 6000, Math.min(3, warp / 15), false, true));
                  player.sendSystemMessage(Component.literal("§5§o").append(Component.translatable("warp.text.1")));
               } else if (eff <= 52) {
                  player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, Math.min(40 * warp, 6000), 0, true, true));
                  player.sendSystemMessage(Component.literal("§5§o").append(Component.translatable("warp.text.10")));
               } else if (eff <= 56) {
                  player.addEffect(new MobEffectInstance(ModMobEffect.DEATH_GAZE, 6000, Math.min(3, warp / 15), true, true));
                  player.sendSystemMessage(Component.literal("§5§o").append(Component.translatable("warp.text.4")));
               } else if (eff <= 60) {
                  suddenlySpiders(player, warp, false);
               } else if (eff <= 64) {
                  player.sendSystemMessage(Component.literal("§5§o").append(Component.translatable("warp.text.13")));
               } else if (eff <= 68) {
                  spawnMist(player, warp, warp / 30);
               } else if (eff <= 72) {
                  player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, Math.min(32000, 5 * warp), 0, true, true));
               } else if (eff == 76) {
                  if (knowledge.getWarpSticky() > 0) {
                     knowledge.addWarpSticky(-1);
                     PacketHandler.sendTo(new PacketSyncWarp(player, (byte)1), player);
                     PacketHandler.sendTo(new PacketWarpMessage((byte)1, -1), player);
                  }
                  player.sendSystemMessage(Component.literal("§5§o").append(Component.translatable("warp.text.14")));
               } else if (eff <= 80) {
                  player.addEffect(new MobEffectInstance(ModMobEffect.UNNATURAL_HUNGER, 6000, Math.min(3, warp / 15), true, true));
                  player.sendSystemMessage(Component.literal("§5§o").append(Component.translatable("warp.text.2")));
               } else if (eff <= 84) {
                  grantResearch(player, warp / 10);
                  player.sendSystemMessage(Component.literal("§5§o").append(Component.translatable("warp.text.3")));
               } else if (eff > 88) {
                  if (eff <= 92) {
                     suddenlySpiders(player, warp, true);
                  } else {
                     spawnMist(player, warp, warp / 15);
                  }
               }
            }
         }

         if (actualwarp > 10
            && !ResearchManager.isResearchComplete(player, "bath_salts")
            && !ResearchManager.isResearchComplete(player, "@bath_salts")) {
            player.sendSystemMessage(Component.literal("§5§o").append(Component.translatable("warp.text.8")));
            PacketHandler.sendTo(new PacketResearchComplete("@bath_salts"), player);
            Thaumcraft.setup.getResearchManager().completeResearch(player, "@bath_salts");
         }

         if (actualwarp > 25 && !ResearchManager.isResearchComplete(player, "eldritch_minor")) {
            grantResearch(player, 10);
            PacketHandler.sendTo(new PacketResearchComplete("eldritch_minor"), player);
            Thaumcraft.setup.getResearchManager().completeResearch(player, "eldritch_minor");
         }

         if (actualwarp > 50 && !ResearchManager.isResearchComplete(player, "eldritch_major")) {
            grantResearch(player, 20);
            PacketHandler.sendTo(new PacketResearchComplete("eldritch_major"), player);
            Thaumcraft.setup.getResearchManager().completeResearch(player, "eldritch_major");
         }
      }

      knowledge.addWarpTemp(-1);
      PacketHandler.sendTo(new PacketSyncWarp(player, (byte)2), player);
   }

   public static void checkDeathGaze(ServerPlayer player) {
      MobEffectInstance pe = player.getEffect(ModMobEffect.DEATH_GAZE);
      if (pe != null) {
         int level = pe.getAmplifier();
         int range = Math.min(8 + level * 3, 24);
         AABB aabb = player.getBoundingBox().inflate((double)range);
         List<LivingEntity> list = player.level().getEntitiesOfClass(LivingEntity.class, aabb);

         for (LivingEntity entity : list) {
            if (entity != player
               && entity.isAlive()
               && player.hasLineOfSight(entity)
               && !entity.hasEffect(MobEffects.WITHER)) {
               if (entity instanceof Mob mob) {
                  mob.setTarget(player);
               }
               entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 80));
            }
         }
      }
   }

   private static void spawnMist(ServerPlayer player, int warp, int guardian) {
      PacketHandler.sendTo(new PacketMiscEvent(PacketMiscEvent.Type.MIST), player);
      if (guardian > 0) {
         guardian = Math.min(8, guardian);
         for (int a = 0; a < guardian; a++) {
            spawnGuardian(player);
         }
      }
      player.sendSystemMessage(Component.literal("§5§o").append(Component.translatable("warp.text.6")));
   }

   private static void grantResearch(ServerPlayer player, int times) {
      int amt = 1 + player.level().random.nextInt(Math.max(1, times));
      PlayerKnowledge knowledge = PlayerKnowledge.of(player);
      for (int a = 0; a < amt; a++) {
         List<Aspect> primals = Aspect.getPrimalAspects();
         Aspect aspect = primals.get(player.level().random.nextInt(primals.size()));
         if (knowledge.addAspectPool(aspect, (short)1)) {
            PacketHandler.sendTo(new PacketAspectPool(aspect.getTag(), (short)1, knowledge.getAspectPoolFor(aspect)), player);
            Thaumcraft.setup.getResearchManager().completeAspect(player, aspect, knowledge.getAspectPoolFor(aspect));
         }
      }
      ResearchManager.scheduleSave(player);
   }

   private static void spawnGuardian(ServerPlayer player) {
      EntityEldritchGuardian eg = new EntityEldritchGuardian(player.level());
      int i = Mth.floor(player.getX());
      int j = Mth.floor(player.getY());
      int k = Mth.floor(player.getZ());

      for (int l = 0; l < 50; l++) {
         int i1 = i + Mth.nextInt(player.level().random, 7, 24) * (player.level().random.nextBoolean() ? 1 : -1);
         int j1 = j + Mth.nextInt(player.level().random, 7, 24) * (player.level().random.nextBoolean() ? 1 : -1);
         int k1 = k + Mth.nextInt(player.level().random, 7, 24) * (player.level().random.nextBoolean() ? 1 : -1);
         BlockPos p = new BlockPos(i1, j1 - 1, k1);
         if (player.level().getBlockState(p).isSolid()) {
            eg.moveTo((double)i1 + 0.5, (double)j1, (double)k1 + 0.5, player.level().random.nextFloat() * 360.0F, 0.0F);
            if (player.level().noCollision(eg)) {
               eg.setTarget(player);
               player.level().addFreshEntity(eg);
               break;
            }
         }
      }
   }

   private static void suddenlySpiders(ServerPlayer player, int warp, boolean real) {
      int spawns = Math.min(50, warp);

      for (int a = 0; a < spawns; a++) {
         EntityMindSpider spider = new EntityMindSpider(player.level());
         int i = Mth.floor(player.getX());
         int j = Mth.floor(player.getY());
         int k = Mth.floor(player.getZ());
         boolean success = false;

         for (int l = 0; l < 50; l++) {
            int i1 = i + Mth.nextInt(player.level().random, 7, 24) * (player.level().random.nextBoolean() ? 1 : -1);
            int j1 = j + Mth.nextInt(player.level().random, 7, 24) * (player.level().random.nextBoolean() ? 1 : -1);
            int k1 = k + Mth.nextInt(player.level().random, 7, 24) * (player.level().random.nextBoolean() ? 1 : -1);
            BlockPos p = new BlockPos(i1, j1 - 1, k1);
            if (player.level().getBlockState(p).isSolid()) {
               spider.moveTo((double)i1 + 0.5, (double)j1, (double)k1 + 0.5, player.level().random.nextFloat() * 360.0F, 0.0F);
               if (player.level().noCollision(spider)) {
                  success = true;
                  break;
               }
            }
         }

         if (success) {
            spider.setTarget(player);
            if (!real) {
               spider.setViewer(player.getGameProfile().getName());
               spider.setHarmless(true);
            }
            player.level().addFreshEntity(spider);
         }
      }

      player.sendSystemMessage(Component.literal("§5§o").append(Component.translatable("warp.text.7")));
   }

   private static int getWarpFromGear(Player player) {
      int w = EventHandlerRunic.getFinalWarp(player.getMainHandItem(), player);
      w += EventHandlerRunic.getFinalWarp(player.getOffhandItem(), player);
      for (EquipmentSlot slot : EquipmentSlot.values()) {
         if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
            w += EventHandlerRunic.getFinalWarp(player.getItemBySlot(slot), player);
         }
      }
      var curiosOpt = CuriosApi.getCuriosInventory(player);
      if (curiosOpt.isPresent()) {
         var handler = curiosOpt.get();
         for (int i = 0; i < handler.getEquippedCurios().getSlots(); i++) {
            w += EventHandlerRunic.getFinalWarp(handler.getEquippedCurios().getStackInSlot(i), player);
         }
      }
      return w;
   }
}
