package thaumcraft.block.warded;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;
import thaumcraft.block.opaque.OwnedBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;

public class WardedBlockEntity extends OwnedBlockEntity {
   private BlockState mimic = Blocks.STONE.defaultBlockState();
   private int light = 0;
   private int ownerHash = 0;
   private UUID ownerUUID = null;

   public WardedBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)ModBlockEntities.WARDED.get(), pos, state);
   }

   public BlockState getMimic() {
      return this.mimic;
   }

   public int getLight() {
      return this.light;
   }

   public int getOwnerHash() {
      return this.ownerHash;
   }

   public UUID getOwnerUUID() {
      return this.ownerUUID;
   }

   public void setWardedData(BlockState mimic, int light, int ownerHash, UUID ownerUUID, String ownerName) {
      this.mimic = mimic == null ? Blocks.STONE.defaultBlockState() : mimic;
      this.light = Math.max(0, light);
      this.ownerHash = ownerHash;
      this.ownerUUID = ownerUUID;
      this.owner = ownerName == null ? "" : ownerName;
      this.setChanged();
      this.requestModelDataUpdate();
   }

   @Override
   public void loadCustom(CompoundTag tag, Provider provider) {
      super.loadCustom(tag, provider);
      if (tag.contains("mimic", 10)) {
         HolderGetter<Block> lookup = (HolderGetter<Block>)(this.level != null ? this.level.holderLookup(Registries.BLOCK) : BuiltInRegistries.BLOCK.asLookup());
         this.mimic = NbtUtils.readBlockState(lookup, tag.getCompound("mimic"));
      }

      if (tag.contains("light", 3)) {
         this.light = tag.getInt("light");
      } else if (tag.contains("ll", 1)) {
         this.light = tag.getByte("ll");
      }

      if (tag.contains("ownerHash", 3)) {
         this.ownerHash = tag.getInt("ownerHash");
      } else if (tag.contains("oi", 3)) {
         this.ownerHash = tag.getInt("oi");
      }

      if (tag.hasUUID("ownerUUID")) {
         this.ownerUUID = tag.getUUID("ownerUUID");
      } else if (tag.contains("ownerUUID", 8)) {
         String s = tag.getString("ownerUUID");
         if (!s.isEmpty()) {
            try {
               this.ownerUUID = UUID.fromString(s);
            } catch (Exception var5) {
            }
         }
      }
   }

   @Override
   public void saveCustom(CompoundTag tag, Provider provider) {
      super.saveCustom(tag, provider);
      tag.put("mimic", NbtUtils.writeBlockState(this.mimic));
      tag.putInt("light", this.light);
      tag.putInt("ownerHash", this.ownerHash);
      if (this.ownerUUID != null) {
         tag.putUUID("ownerUUID", this.ownerUUID);
      }
   }

   public static void spawnSparkle(ServerLevel level, BlockPos pos, int rgb) {
      float r = (rgb >> 16 & 0xFF) / 255.0F;
      float g = (rgb >> 8 & 0xFF) / 255.0F;
      float b = (rgb & 0xFF) / 255.0F;
      DustParticleOptions dust = new DustParticleOptions(new Vector3f(r, g, b), 1.0F);
      level.sendParticles(dust, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 16, 0.35, 0.35, 0.35, 0.0);
   }
}
