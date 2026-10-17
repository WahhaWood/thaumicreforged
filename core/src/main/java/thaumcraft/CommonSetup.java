package thaumcraft;

import net.minecraft.client.particle.Particle;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import thaumcraft.block.crucible.CrucibleBlockEntity;
import thaumcraft.init.registry.ModItems;
import thaumcraft.item.wands.WandManager;
import thaumcraft.research.ResearchManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class CommonSetup {
   public ResearchManager researchManager;
   public WandManager wandManager = new WandManager();

   public ResearchManager getResearchManager() {
      return this.researchManager;
   }

   public void registerDisplayInformation() {
   }

   public void registerHandlers() {
   }

   public Level getClientWorld() {
      return null;
   }

   public void blockSparkle(Level world, int n, int n2, int n3, int n4, int n5) {
   }

   public void sparkle(float n, float n2, float n3, float n4, int n5, float n6) {
   }

   public void sparkle(float n, float n2, float n3, int n4) {
   }

   public void sparkle(float x, float y, float z, float tx, float ty, float tz, float size, int colorRGB, int multiplier, float gravity) {
   }

   public void crucibleBoil(Level world, int n, int n2, int n3, CrucibleBlockEntity tileCrucible, int n4) {
   }

   public void crucibleBoilSound(Level world, int n, int n2, int n3) {
   }

   public void crucibleBubble(Level world, float n, float n2, float n3, float n4, float n5, float n6) {
   }

   public int particleCount(int n) {
      return 0;
   }

   public void wispFX(Level world, double n, double n2, double n3, float n4, float n5, float n6, float n7) {
   }

   public void wispFX(Level world, double x, double y, double z, float r, float g, float b, float a, float gravity) {
   }

   public void sourceStreamFX(Level world, double n, double n2, double n3, float n4, float n5, float n6, int n7) {
   }

   public void bolt(Level world, Entity entity, Entity entity2) {
   }

   public void furnaceLavaFx(Level world, int n, int n2, int n3, int n4, int n5) {
   }

   public void wispFX2(Level world, double n, double n2, double n3, float n4, int n5, boolean b, boolean b2, float n6) {
   }

   public void crucibleFroth(Level world, float n, float n2, float n3) {
   }

   public void crucibleFrothDown(Level world, float n, float n2, float n3) {
   }

   public Object beamCont(Level world, Player entityPlayer, double n, double n2, double n3, int n4, int n5, boolean b, float n6, Object o, int n7) {
      return null;
   }

   public void excavateFX(int n, int n2, int n3, Player entityPlayer, int n4, int n5, int n6) {
   }

   public void burst(Level world, double n, double n2, double n3, float n4) {
   }

   public void wispFX3(Level world, double n, double n2, double n3, double n4, double n5, double n6, float n7, int n8, boolean b, float n9) {
   }

   public void smokeSpiral(Level world, double n, double n2, double n3, float n4, int n5, int n6, int n7) {
   }

   public void beam(Level world, double n, double n2, double n3, double n4, double n5, double n6, int n7, int n8, boolean b, float n9, int n10) {
   }

   public void blockRunes(Level world, double n, double n2, double n3, float n4, float n5, float n6, int n7, float n8) {
   }

   public Object beamBore(Level world, double n, double n2, double n3, double n4, double n5, double n6, int n7, int n8, boolean b, float n9, Object o, int n10) {
      return null;
   }

   public void boreDigFx(Level world, int n, int n2, int n3, int n4, int n5, int n6, Block block, int n7) {
   }

   public void soulTrail(Level world, Entity entity, Entity entity2, float n, float n2, float n3) {
   }

   public void nodeBolt(Level world, float n, float n2, float n3, Entity entity) {
   }

   public void splooshFX(Entity entity) {
   }

   public void tentacleAriseFX(Entity entity) {
   }

   public void slimeJumpFX(Entity entity, int n) {
   }

   public void dropletFX(Level world, float n, float n2, float n3, float n4, float n5, float n6) {
   }

   public void taintLandFX(Entity entity) {
   }

   @OnlyIn(Dist.CLIENT)
   public Particle swarmParticleFX(Level world, Entity entity, float n, float n2, float n3) {
      return null;
   }

   public void taintsplosionFX(Entity entity) {
   }

   public void registerCustomIcons() {
   }

   public void hungryNodeFX(Level world, int n, int n2, int n3, int n4, int n5, int n6, Block block, int n7) {
   }

   public void essenceTrailFx(Level world, int n, int n2, int n3, int n4, int n5, int n6, int n7, int n8, float n9) {
   }

   public void splooshFX(Level world, int n, int n2, int n3) {
   }

   public void nodeBolt(Level world, float n, float n2, float n3, float n4, float n5, float n6) {
   }

   public void drawInfusionParticles1(Level world, double n, double n2, double n3, int n4, int n5, int n6, Item item) {
   }

   public void drawInfusionParticles2(Level world, double n, double n2, double n3, int n4, int n5, int n6, Block block) {
   }

   public void drawInfusionParticles3(Level world, double n, double n2, double n3, int n4, int n5, int n6) {
   }

   public void drawInfusionParticles4(Level world, double n, double n2, double n3, int n4, int n5, int n6) {
   }

   public void drawVentParticles(Level world, double n, double n2, double n3, double n4, double n5, double n6, int n7) {
   }

   public void blockWard(Level world, double n, double n2, double n3, Direction forgeDirection, float n4, float n5, float n6) {
   }

   public void wispFX4(Level world, double n, double n2, double n3, Entity entity, int n4, boolean b, float n5) {
   }

   public Object beamPower(Level world, double n, double n2, double n3, double n4, double n5, double n6, float n7, float n8, float n9, boolean b, Object o) {
      return null;
   }

   public boolean isShiftKeyDown() {
      return false;
   }

   public void wispFXEG(Level world, double n, double n2, double n3, Entity entity) {
   }

   public void reservoirBubble(Level world, int n, int n2, int n3, int n4) {
   }

   public void spark(float n, float n2, float n3, float n4, float n5, float n6, float n7, float n8) {
   }

   public void drawVentParticles(Level world, double n, double n2, double n3, double n4, double n5, double n6, int n7, float n8) {
   }

   public void bottleTaintBreak(Level world, double n, double n2, double n3) {
      if (world instanceof ServerLevel serverLevel) {
         ItemStack stack = new ItemStack((ItemLike)ModItems.BOTTLE_TAINT.get());
         serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, stack), n, n2, n3, 8, 0.15, 0.2, 0.15, 0.05);
      }
   }

   public void drawGenericParticles(
      Level world,
      double n,
      double n2,
      double n3,
      double n4,
      double n5,
      double n6,
      float n7,
      float n8,
      float n9,
      float n10,
      boolean b,
      int n11,
      int n12,
      int n13,
      int n14,
      int n15,
      float n16
   ) {
   }

   public void arcLightning(Level world, double n, double n2, double n3, double n4, double n5, double n6, float n7, float n8, float n9, float n10) {
   }
}
