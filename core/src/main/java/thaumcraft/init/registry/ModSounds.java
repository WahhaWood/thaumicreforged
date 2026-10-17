package thaumcraft.init.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
   private static final DeferredRegister<SoundEvent> REGISTER = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, "thaumcraft");
   public static final DeferredHolder<SoundEvent, SoundEvent> PAGE = registry("page");
   public static final DeferredHolder<SoundEvent, SoundEvent> BUBBLE = registry("bubble");
   public static final DeferredHolder<SoundEvent, SoundEvent> ALEMBIC_KNOCK = registry("alembicknock");
   public static final DeferredHolder<SoundEvent, SoundEvent> URN_BREAK = registry("urnbreak");
   public static final DeferredHolder<SoundEvent, SoundEvent> HEARTBEAT = registry("heartbeat");
   public static final DeferredHolder<SoundEvent, SoundEvent> LEARN = registry("learn");
   public static final DeferredHolder<SoundEvent, SoundEvent> WHISPERS = registry("whispers");
   public static final DeferredHolder<SoundEvent, SoundEvent> ZAP = registry("zap");
   public static final DeferredHolder<SoundEvent, SoundEvent> WAND = registry("wand");
   public static final DeferredHolder<SoundEvent, SoundEvent> WAND_FAIL = registry("wandfail");
   public static final DeferredHolder<SoundEvent, SoundEvent> WIND = registry("wind");
   public static final DeferredHolder<SoundEvent, SoundEvent> SWING = registry("swing");
   public static final DeferredHolder<SoundEvent, SoundEvent> JAR = registry("jar");
   public static final DeferredHolder<SoundEvent, SoundEvent> CAMERA_CLACK = registry("cameraclack");
   public static final DeferredHolder<SoundEvent, SoundEvent> CAMERA_TICKS = registry("cameraticks");
   public static final DeferredHolder<SoundEvent, SoundEvent> ERASE = registry("erase");
   public static final DeferredHolder<SoundEvent, SoundEvent> WRITE = registry("write");
   public static final DeferredHolder<SoundEvent, SoundEvent> KEY = registry("key");
   public static final DeferredHolder<SoundEvent, SoundEvent> COINS = registry("coins");
   public static final DeferredHolder<SoundEvent, SoundEvent> ROOTS = registry("roots");
   public static final DeferredHolder<SoundEvent, SoundEvent> CRAFT_START = registry("craftstart");
   public static final DeferredHolder<SoundEvent, SoundEvent> CRAFT_FAIL = registry("craftfail");
   public static final DeferredHolder<SoundEvent, SoundEvent> HHON = registry("hhon");
   public static final DeferredHolder<SoundEvent, SoundEvent> HHOFF = registry("hhoff");
   public static final DeferredHolder<SoundEvent, SoundEvent> RUNIC_SHIELD_EFFECT = registry("runic_shield_effect");
   public static final DeferredHolder<SoundEvent, SoundEvent> RUNIC_SHIELD_CHARGE = registry("runic_shield_charge");
   public static final DeferredHolder<SoundEvent, SoundEvent> PECH_TRADE = registry("pech_trade");
   public static final DeferredHolder<SoundEvent, SoundEvent> PECH_IDLE = registry("pech_idle");
   public static final DeferredHolder<SoundEvent, SoundEvent> PECH_HIT = registry("pech_hit");
   public static final DeferredHolder<SoundEvent, SoundEvent> PECH_DEATH = registry("pech_death");
   public static final DeferredHolder<SoundEvent, SoundEvent> SHOCK = registry("shock");
   public static final DeferredHolder<SoundEvent, SoundEvent> CRAB_TALK = registry("crabtalk");
   public static final DeferredHolder<SoundEvent, SoundEvent> WISP_LIVE = registry("wisplive");
   public static final DeferredHolder<SoundEvent, SoundEvent> WISP_DEAD = registry("wispdead");
   public static final DeferredHolder<SoundEvent, SoundEvent> SWARM = registry("swarm");
   public static final DeferredHolder<SoundEvent, SoundEvent> SWARM_ATTACK = registry("swarmattack");
   public static final DeferredHolder<SoundEvent, SoundEvent> GORE = registry("gore");
   public static final DeferredHolder<SoundEvent, SoundEvent> TENTACLE = registry("tentacle");
   public static final DeferredHolder<SoundEvent, SoundEvent> EGIDLE = registry("egidle");
   public static final DeferredHolder<SoundEvent, SoundEvent> EGDEATH = registry("egdeath");
   public static final DeferredHolder<SoundEvent, SoundEvent> CRAB_CLAW = registry("crabclaw");
   public static final DeferredHolder<SoundEvent, SoundEvent> CRAB_DEATH = registry("crabdeath");
   public static final DeferredHolder<SoundEvent, SoundEvent> MONOLITH = registry("monolith");
   public static final DeferredHolder<SoundEvent, SoundEvent> EGATTACK = registry("egattack");
   public static final DeferredHolder<SoundEvent, SoundEvent> JACOBS = registry("jacobs");
   public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTAL = registry("crystal");
   public static final DeferredHolder<SoundEvent, SoundEvent> INFUSERSTART = registry("infuserstart");
   public static final DeferredHolder<SoundEvent, SoundEvent> INFUSER = registry("infuser");
   public static final DeferredHolder<SoundEvent, SoundEvent> TOOL = registry("tool");
   public static final DeferredHolder<SoundEvent, SoundEvent> BRAIN = registry("brain");
   public static final DeferredHolder<SoundEvent, SoundEvent> DOOR_FAIL = registry("doorfail");
   public static final DeferredHolder<SoundEvent, SoundEvent> CREAK = registry("creak");
   public static final DeferredHolder<SoundEvent, SoundEvent> RUMBLE = registry("rumble");
   public static final DeferredHolder<SoundEvent, SoundEvent> PUMP = registry("pump");
   public static final DeferredHolder<SoundEvent, SoundEvent> SQUEEK = registry("squeek");
   public static final DeferredHolder<SoundEvent, SoundEvent> ICE = registry("ice");

   public static void init(IEventBus bus) {
      REGISTER.register(bus);
   }

   private static DeferredHolder<SoundEvent, SoundEvent> registry(String name) {
      return registry(name, name);
   }

   private static DeferredHolder<SoundEvent, SoundEvent> registry(String name, String soundPath) {
      return REGISTER.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("thaumcraft", soundPath)));
   }
}
