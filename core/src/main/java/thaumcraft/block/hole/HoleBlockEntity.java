package thaumcraft.block.hole;

import com.mojang.authlib.GameProfile;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.ITickable;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.init.registry.ModBlocks;

public class HoleBlockEntity extends BlockEntity implements ITickable {
   private static final UUID DEFAULT_UUID = UUID.nameUUIDFromBytes("thaumcraft:portable_hole".getBytes(StandardCharsets.UTF_8));
   private static final GameProfile DEFAULT_PROFILE = new GameProfile(DEFAULT_UUID, "[Thaumcraft]");
   private BlockState storedState = null;
   public short countdown = 0;
   public short countdownMax = 120;
   public byte count = 0;
   public byte direction = 0;
   @Nullable
   private UUID owner;

   public HoleBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)ModBlockEntities.HOLE.get(), pos, state);
   }

   public BlockState getStoredState() {
      return this.storedState;
   }

   public void init(BlockState oldState, short max, byte count, byte direction, @Nullable Player player) {
      this.storedState = oldState;
      this.countdown = 0;
      this.countdownMax = max;
      this.count = count;
      this.direction = direction;
      this.owner = player != null ? player.getUUID() : null;
   }

   public static boolean canCarve(@Nullable Player player, ServerLevel level, BlockPos pos) {
      return !level.isOutsideBuildHeight(pos);
   }

   @Override
   public void clientTick(Level level, BlockPos pos, BlockState state) {
      RandomSource r = level.getRandom();
      if (r.nextInt(4) == 0) {
         for (Direction d : Direction.values()) {
            BlockPos npos = pos.relative(d);
            BlockState ns = level.getBlockState(npos);
            if (!ns.isAir() && !ns.is((Block)ModBlocks.HOLE.get()) && ns.isSolidRender(level, npos)) {
               double ox = pos.getX() + 0.5;
               double oy = pos.getY() + 0.5;
               double oz = pos.getZ() + 0.5;
               double px = ox + d.getStepX() * 0.55 + (r.nextDouble() - 0.5) * 0.8;
               double py = oy + d.getStepY() * 0.55 + (r.nextDouble() - 0.5) * 0.8;
               double pz = oz + d.getStepZ() * 0.55 + (r.nextDouble() - 0.5) * 0.8;
               level.addParticle(ParticleTypes.PORTAL, px, py, pz, (r.nextDouble() - 0.5) * 0.2, (r.nextDouble() - 0.5) * 0.2, (r.nextDouble() - 0.5) * 0.2);
            }
         }
      }
   }

   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      if (this.countdown == 0 && this.count > 1 && this.direction != -1) {
         int ii = pos.getX();
         int jj = pos.getY();
         int kk = pos.getZ();
         switch (this.direction) {
            case 0:
            case 1:
               for (int axx = 0; axx < 9; axx++) {
                  if (axx / 3 != 1 || axx % 3 != 1) {
                     thaumcraft.item.wands.foci.ItemFocusPortableHole.createHole(level, ii - 1 + axx / 3, jj, kk - 1 + axx % 3, -1, (byte)1, this.countdownMax);
                  }
               }
               break;
            case 2:
            case 3:
               for (int ax = 0; ax < 9; ax++) {
                  if (ax / 3 != 1 || ax % 3 != 1) {
                     thaumcraft.item.wands.foci.ItemFocusPortableHole.createHole(level, ii - 1 + ax / 3, jj - 1 + ax % 3, kk, -1, (byte)1, this.countdownMax);
                  }
               }
               break;
            case 4:
            case 5:
               for (int a = 0; a < 9; a++) {
                  if (a / 3 != 1 || a % 3 != 1) {
                     thaumcraft.item.wands.foci.ItemFocusPortableHole.createHole(level, ii, jj - 1 + a / 3, kk - 1 + a % 3, -1, (byte)1, this.countdownMax);
                  }
               }
         }

         switch (this.direction) {
            case 0:
               jj++;
               break;
            case 1:
               jj--;
               break;
            case 2:
               kk++;
               break;
            case 3:
               kk--;
               break;
            case 4:
               ii++;
               break;
            case 5:
               ii--;
         }

         if (!thaumcraft.item.wands.foci.ItemFocusPortableHole.createHole(level, ii, jj, kk, this.direction, (byte)(this.count - 1), this.countdownMax)) {
            this.count = 0;
         }
      }

      this.countdown++;
      if (this.countdown >= this.countdownMax) {
         BlockState restore = this.storedState != null ? this.storedState : net.minecraft.world.level.block.Blocks.STONE.defaultBlockState();
         level.setBlock(pos, restore, 3);
      }
   }

   protected void saveAdditional(CompoundTag tag, Provider provider) {
      super.saveAdditional(tag, provider);
      tag.putShort("countdown", this.countdown);
      tag.putShort("countdownMax", this.countdownMax);
      tag.putByte("count", this.count);
      tag.putByte("direction", this.direction);
      if (this.owner != null) {
         tag.putUUID("owner", this.owner);
      }

      if (this.storedState != null) {
         tag.put("storedState", NbtUtils.writeBlockState(this.storedState));
      }
   }

   protected void loadAdditional(CompoundTag tag, Provider provider) {
      super.loadAdditional(tag, provider);
      this.countdown = tag.getShort("countdown");
      this.countdownMax = tag.getShort("countdownMax");
      this.count = tag.getByte("count");
      this.direction = tag.getByte("direction");
      this.owner = tag.hasUUID("owner") ? tag.getUUID("owner") : null;
      if (tag.contains("storedState")) {
         this.storedState = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("storedState"));
      } else {
         this.storedState = null;
      }
   }
}
