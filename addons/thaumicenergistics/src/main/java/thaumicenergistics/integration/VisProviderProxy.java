package thaumicenergistics.integration.tc;

import java.util.HashSet;
import thaumicenergistics.part.PartVisInterface;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.LevelBlockPos;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.visnet.TileVisNode;
import thaumcraft.api.visnet.VisNetHandler;
import thaumcraft.block.vis_relay.VisRelayBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.init.registry.ModBlocks;

public class VisProviderProxy extends TileVisNode {
   private static final int VIS_RANGE = 1;
   private static final HashSet<VisProviderProxy> visitedProxies = new HashSet<>();
   private PartVisInterface visInterface;
   private LevelBlockPos location;
   private byte attunement = -1;

   public VisProviderProxy(PartVisInterface parent) {
      super((BlockEntityType)ModBlockEntities.VIS_RELAY.get(), computeProxyPos(parent), computeProxyState(parent));
      this.visInterface = parent;
      Direction face = this.visInterface.getSide();
      BlockPos hostPos = this.visInterface.getBlockEntity().getBlockPos();
      BlockPos proxyPos = hostPos.relative(face);
      Level level = this.visInterface.getLevel();
      this.setLevel(level);
      this.location = new LevelBlockPos(proxyPos, level.dimension());
   }

   private static BlockPos computeProxyPos(PartVisInterface parent) {
      Direction face = parent.getSide();
      BlockPos hostPos = parent.getBlockEntity().getBlockPos();
      return hostPos.relative(face);
   }

   private static BlockState computeProxyState(PartVisInterface parent) {
      VisRelayBlockEntity relay = parent.getRelay();
      if (relay != null) {
         BlockState relayState = relay.getBlockState();
         if (((BlockEntityType)ModBlockEntities.VIS_RELAY.get()).isValid(relayState)) {
            return relayState;
         }
      }

      return ((Block)ModBlocks.VIS_RELAY.get()).defaultBlockState();
   }

   public LevelBlockPos getLocation() {
      return this.location;
   }

   public int consumeVis(Aspect aspect, int amountRequested) {
      if (this.isRemoved() || this.visInterface == null || !this.visInterface.isVisProvider() || this.visInterface.getRelay() == null) {
         return 0;
      } else if (!visitedProxies.add(this)) {
         return 0;
      } else {
         int var3;
         try {
            var3 = this.visInterface.consumeVis(aspect, amountRequested);
         } finally {
            visitedProxies.remove(this);
         }

         return var3;
      }
   }

   public byte getAttunement() {
      return this.attunement;
   }

   public int getRange() {
      return 1;
   }

   public boolean isSource() {
      return this.visInterface != null && this.visInterface.isVisProvider();
   }

   public void tick() {
      byte relayAttunement = -1;
      if (this.visInterface != null) {
         VisRelayBlockEntity relay = this.visInterface.getRelay();
         if (relay != null) {
            relayAttunement = relay.getAttunement();
         }
      }

      if (this.attunement != relayAttunement) {
         this.attunement = relayAttunement;
         this.removeThisNode();
         this.nodeRefresh = true;
         Level level = this.getLevel();
         if (level != null) {
            VisNetHandler.addSource(level, this);
         }
      }
   }
}
