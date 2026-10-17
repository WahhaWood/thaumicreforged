package thaumcraft.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ThaumcraftBlockRegistry {
   private final DeferredRegister<Block> blockRegister;
   private final DeferredRegister<Item> itemRegister;
   private final DeferredRegister<BlockEntityType<?>> typeRegister;

   public static ThaumcraftBlockRegistry create(DeferredRegister<Item> itemRegister) {
      return new ThaumcraftBlockRegistry(
         DeferredRegister.create(BuiltInRegistries.BLOCK, "thaumcraft"),
         itemRegister,
         DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, "thaumcraft")
      );
   }

   protected ThaumcraftBlockRegistry(
      DeferredRegister<Block> blockRegister, DeferredRegister<Item> itemRegister, DeferredRegister<BlockEntityType<?>> typeRegister
   ) {
      this.blockRegister = blockRegister;
      this.itemRegister = itemRegister;
      this.typeRegister = typeRegister;
   }

   public ThaumcraftBlockRegistry.Builder register(String name) {
      return new ThaumcraftBlockRegistry.Builder(name);
   }

   public void register(IEventBus bus) {
      this.blockRegister.register(bus);
      this.itemRegister.register(bus);
      this.typeRegister.register(bus);
   }

   public static class BlockObject {
      private DeferredHolder<Block, Block> block;
      private DeferredHolder<Item, BlockItem> item;
      private DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> type;

      public Block getBlock() {
         return (Block)this.block.get();
      }

      public BlockItem getItem() {
         return (BlockItem)this.item.get();
      }

      public BlockEntityType<?> getType() {
         return (BlockEntityType<?>)this.type.get();
      }

      public boolean isEmpty() {
         return false;
      }
   }

   public class Builder {
      private final String name;
      private String entityName;
      private Supplier<Block> blockFactory;
      private Function<Block, BlockItem> itemFactory;
      private BlockEntitySupplier<?> entityFactory;
      private ThaumcraftBlockRegistry.BlockObject[] blockObjects;

      private Builder(String name) {
         this.name = name;
      }

      public ThaumcraftBlockRegistry.Builder block(Supplier<Block> factory) {
         this.blockFactory = factory;
         return this;
      }

      public ThaumcraftBlockRegistry.Builder item(Function<Block, BlockItem> factory) {
         this.itemFactory = factory;
         return this;
      }

      public ThaumcraftBlockRegistry.Builder entity(BlockEntitySupplier<?> factory) {
         this.entityFactory = factory;
         return this;
      }

      public ThaumcraftBlockRegistry.Builder entity(String name, BlockEntitySupplier<?> factory, ThaumcraftBlockRegistry.BlockObject... blockObjects) {
         this.entityName = name;
         this.blockObjects = blockObjects;
         return this.entity(factory);
      }

      public ThaumcraftBlockRegistry.BlockObject build() {
         ThaumcraftBlockRegistry.BlockObject object = new ThaumcraftBlockRegistry.BlockObject();
         object.block = ThaumcraftBlockRegistry.this.blockRegister.register(this.name, this.blockFactory);
         if (this.itemFactory != null) {
            object.item = ThaumcraftBlockRegistry.this.itemRegister.register(this.name, () -> this.itemFactory.apply((Block)object.block.get()));
         }

         if (this.entityFactory != null) {
            object.type = ThaumcraftBlockRegistry.this.typeRegister.register(this.entityName == null ? this.name : this.entityName, () -> {
               Block block = (Block)object.block.get();
               BlockEntityType<?> type;
               if (this.blockObjects != null && this.blockObjects.length != 0) {
                  List<Block> blocks = new ArrayList<>();
                  blocks.add(block);
                  blocks.addAll(Arrays.stream(this.blockObjects).map(ThaumcraftBlockRegistry.BlockObject::getBlock).toList());
                  type = net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(this.entityFactory, blocks.toArray(new Block[0])).build(null);
               } else {
                  type = net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(this.entityFactory, new Block[]{block}).build(null);
               }

               return type;
            });
            if (this.blockObjects != null) {
               for (ThaumcraftBlockRegistry.BlockObject blockObject : this.blockObjects) {
                  blockObject.type = object.type;
               }
            }
         }

         return object;
      }

      public Map<DyeColor, ThaumcraftBlockRegistry.BlockObject> buildColors() {
         Map<DyeColor, ThaumcraftBlockRegistry.BlockObject> map = new HashMap<>();

         for (DyeColor value : DyeColor.values()) {
            String name = this.name + "_" + value.getName();
            ThaumcraftBlockRegistry.BlockObject object = new ThaumcraftBlockRegistry.BlockObject();
            object.block = ThaumcraftBlockRegistry.this.blockRegister.register(name, this.blockFactory);
            if (this.itemFactory != null) {
               object.item = ThaumcraftBlockRegistry.this.itemRegister.register(name, () -> this.itemFactory.apply((Block)object.block.get()));
            }

            map.put(value, object);
         }

         return map;
      }

      public Map<DyeColor, ThaumcraftBlockRegistry.BlockObject> buildColors(BlockEntitySupplier<?> supplier) {
         Map<DyeColor, ThaumcraftBlockRegistry.BlockObject> map = this.buildColors();
         DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> type = ThaumcraftBlockRegistry.this.typeRegister
            .register(
               this.name,
               () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(
                     this.entityFactory, map.values().stream().map(ThaumcraftBlockRegistry.BlockObject::getBlock).toArray(Block[]::new)
                  )
                  .build(null)
            );
         map.values().forEach(blockObject -> blockObject.type = type);
         return map;
      }
   }
}
