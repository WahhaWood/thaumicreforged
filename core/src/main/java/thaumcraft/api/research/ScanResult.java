package thaumcraft.api.research;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class ScanResult {
   public ScanResult.Type type;
   public ItemStack stack;
   public Block block;
   public Entity entity;
   public String phenomena;

   public ScanResult(@NotNull ScanResult.Type type, ItemStack stack, Block block, Entity entity, String phenomena) {
      this.type = type;
      this.stack = stack;
      this.block = block;
      this.entity = entity;
      this.phenomena = phenomena;
   }

   @Override
   public boolean equals(Object o) {
      if (o instanceof ScanResult result) {
         if (this.type != result.type) {
            return false;
         } else {
            return switch (this.type) {
               case ITEM -> this.stack == result.stack;
               case BLOCK -> this.block == result.block;
               case ENTITY -> this.entity.getId() == result.entity.getId();
               case PHENOMENA -> this.phenomena.equals(result.phenomena);
            };
         }
      } else {
         return false;
      }
   }

   public static ScanResult ofItem(@NotNull ItemStack value) {
      return new ScanResult(ScanResult.Type.ITEM, value, null, null, null);
   }

   public static ScanResult ofBlock(@NotNull Block value) {
      return new ScanResult(ScanResult.Type.BLOCK, ItemStack.EMPTY, value, null, null);
   }

   public static ScanResult ofBlock(@NotNull Block value, @NotNull ItemStack displayStack) {
      return new ScanResult(ScanResult.Type.BLOCK, displayStack, value, null, null);
   }

   public static ScanResult ofEntity(@NotNull Entity value) {
      return new ScanResult(ScanResult.Type.ENTITY, null, null, value, null);
   }

   public static ScanResult ofPhenomena(@NotNull String value) {
      return new ScanResult(ScanResult.Type.PHENOMENA, null, null, null, value);
   }

   public static enum Type {
      ITEM,
      BLOCK,
      ENTITY,
      PHENOMENA;
   }
}
