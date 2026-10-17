package thaumicenergistics.block.provider;

import thaumicenergistics.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectSource;
import thaumcraft.network.PacketHandler;
import thaumcraft.network.fx.PacketFXBlockSparkle;

public class InfusionProviderBlockEntity extends ProviderBaseBlockEntity implements IAspectSource {
   public InfusionProviderBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType<?>)ModBlockEntities.INFUSION_PROVIDER.get(), pos, state);
   }

   @Override
   protected double getIdlePowerUsage() {
      return 5.0;
   }

   public AspectList getAspects() {
      return new AspectList();
   }

   public void setAspects(AspectList aspects) {
   }

   public boolean doesContainerAccept(Aspect aspect) {
      return false;
   }

   public int addToContainer(Aspect aspect, int amount) {
      return 0;
   }

   public boolean takeFromContainer(Aspect aspect, int amount) {
      if (this.extractEssentiaFromNetwork(aspect, amount, true) == amount) {
         this.spawnRuneParticles(aspect.getColor());
         if (this.level instanceof net.minecraft.server.level.ServerLevel) {
            PacketHandler.sendToAllAround(
               new PacketFXBlockSparkle(this.worldPosition, aspect.getColor()), this.level, this.worldPosition, 24.0
            );
         }

         return true;
      } else {
         return false;
      }
   }

   @Deprecated
   public boolean takeFromContainer(AspectList list) {
      return false;
   }

   public boolean doesContainerContainAmount(Aspect aspect, int amount) {
      return false;
   }

   @Deprecated
   public boolean doesContainerContain(AspectList list) {
      return false;
   }

   public int containerContains(Aspect aspect) {
      return 0;
   }

   private void spawnRuneParticles(int aspectColor) {
      if (this.level != null) {
         float red = (float)(aspectColor >> 16 & 0xFF) / 255.0F;
         float green = (float)(aspectColor >> 8 & 0xFF) / 255.0F;
         float blue = (float)(aspectColor & 0xFF) / 255.0F;

         for (int i = 0; i < 5; i++) {
            ThaumcraftApi.getSetup()
               .blockRunes(
                  this.level,
                  (double)this.worldPosition.getX(),
                  (double)this.worldPosition.getY(),
                  (double)this.worldPosition.getZ(),
                  red,
                  green,
                  blue,
                  15,
                  -0.1F
               );
         }

         for (int i = 0; i < 5; i++) {
            ThaumcraftApi.getSetup()
               .blockRunes(
                  this.level,
                  (double)this.worldPosition.getX(),
                  (double)this.worldPosition.getY(),
                  (double)this.worldPosition.getZ(),
                  red,
                  green,
                  blue,
                  15,
                  0.1F
               );
         }
      }
   }
}
