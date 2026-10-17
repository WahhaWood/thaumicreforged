package thaumcraft.init.registry;

import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.BaseFlowingFluid.Flowing;
import net.neoforged.neoforge.fluids.BaseFlowingFluid.Properties;
import net.neoforged.neoforge.fluids.BaseFlowingFluid.Source;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries.Keys;
import thaumcraft.block.fluid.FiniteFlowingFluid;

public final class ModFluids {
   public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(Keys.FLUID_TYPES, "thaumcraft");
   public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(BuiltInRegistries.FLUID, "thaumcraft");
   public static final ModFluids.ThaumFluidEntry FLUX_GOO = new ModFluids.ThaumFluidEntry("flux_goo", 3000, 6000, true)
      .block(ModBlocks.FLUX_GOO)
      .bucket(ModItems.FLUX_GOO_BUCKET);
   public static final ModFluids.ThaumFluidEntry FLUX_GAS = new ModFluids.ThaumFluidEntry("flux_gas", -100, 200, true)
      .block(ModBlocks.FLUX_GAS)
      .bucket(ModItems.FLUX_GAS_BUCKET);
   public static final ModFluids.ThaumFluidEntry FLUID_PURE = new ModFluids.ThaumFluidEntry("pure", 1000, 1000)
      .block(ModBlocks.FLUID_PURE)
      .bucket(ModItems.FLUID_PURE_BUCKET);
   public static final ModFluids.ThaumFluidEntry FLUID_DEATH = new ModFluids.ThaumFluidEntry("death", 2000, 2500, true)
      .block(ModBlocks.FLUID_DEATH)
      .bucket(ModItems.FLUID_DEATH_BUCKET);

   public static void register(IEventBus bus) {
      FLUID_TYPES.register(bus);
      FLUIDS.register(bus);
   }

   private ModFluids() {
   }

   public static class ThaumFluidEntry {
      public final DeferredHolder<FluidType, FluidType> type;
      public DeferredHolder<Fluid, FlowingFluid> source = null;
      public DeferredHolder<Fluid, FlowingFluid> flowing = null;
      private final Properties properties;

      public ThaumFluidEntry(String textureName, int density, int viscosity) {
         this(textureName, density, viscosity, false);
      }

      public ThaumFluidEntry(String textureName, int density, int viscosity, boolean finite) {
         this.type = ModFluids.FLUID_TYPES
            .register(
               textureName,
               () -> new ModFluids.ThaumcraftFluidType(
                  ResourceLocation.fromNamespaceAndPath("thaumcraft", "block/fluid/" + textureName),
                  ResourceLocation.fromNamespaceAndPath("thaumcraft", "block/fluid/" + textureName),
                  -1,
                  net.neoforged.neoforge.fluids.FluidType.Properties.create().density(density).viscosity(viscosity).canPushEntity(true).canSwim(true)
               )
            );
         this.properties = new Properties(this.type, () -> (Fluid)this.source.get(), () -> (Fluid)this.flowing.get())
            .tickRate(20)
            .slopeFindDistance(textureName.equals("pure") ? 4 : 2)
            .levelDecreasePerBlock(1);
         if (finite) {
            this.source = ModFluids.FLUIDS.register(textureName, () -> new FiniteFlowingFluid.Source(this.properties));
            this.flowing = ModFluids.FLUIDS.register("flowing_" + textureName, () -> new FiniteFlowingFluid.Flowing(this.properties));
         } else {
            this.source = ModFluids.FLUIDS.register(textureName, () -> new Source(this.properties));
            this.flowing = ModFluids.FLUIDS.register("flowing_" + textureName, () -> new Flowing(this.properties));
         }
      }

      public ModFluids.ThaumFluidEntry block(Supplier<? extends LiquidBlock> block) {
         this.properties.block(block);
         return this;
      }

      public ModFluids.ThaumFluidEntry bucket(Supplier<? extends Item> bucket) {
         this.properties.bucket(bucket);
         return this;
      }

      public DeferredHolder<Fluid, FlowingFluid> getSource() {
         return this.source;
      }

      public DeferredHolder<Fluid, FlowingFluid> getFlowing() {
         return this.flowing;
      }
   }

   public static class ThaumcraftFluidType extends FluidType {
      protected final ResourceLocation still;
      protected final ResourceLocation flowing;
      protected final int tint;

      public ThaumcraftFluidType(ResourceLocation still, ResourceLocation flowing, int tint, net.neoforged.neoforge.fluids.FluidType.Properties properties) {
         super(properties);
         this.still = still;
         this.flowing = flowing;
         this.tint = tint;
      }

      public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
         consumer.accept(new IClientFluidTypeExtensions() {
            public ResourceLocation getStillTexture() {
               return ThaumcraftFluidType.this.still;
            }

            public ResourceLocation getFlowingTexture() {
               return ThaumcraftFluidType.this.flowing;
            }

            public int getTintColor() {
               return ThaumcraftFluidType.this.tint;
            }
         });
      }
   }
}
