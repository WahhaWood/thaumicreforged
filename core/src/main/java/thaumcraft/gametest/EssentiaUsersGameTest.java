package thaumcraft.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.block.alchemy_furnace.AlchemyFurnaceBlockEntity;
import thaumcraft.block.arcane_alembic.ArcaneAlembicBlockEntity;
import thaumcraft.block.jar.standart.JarBlockEntity;
import thaumcraft.block.lamp.fertility.ArcaneLampFertilityBlockEntity;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModItems;

/** TC4 essentia producers and users around the tube network: alchemical furnace, void jar, lamp of fertility, crystallizer. */
@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public class EssentiaUsersGameTest {
   /** TC4 TileAlchemyFurnace: fuel and an item with aspects distil into the alembic stacked on top. */
   @GameTest(template = "empty_9x5x9", timeoutTicks = 400)
   public static void alchemicalFurnaceFillsAlembic(GameTestHelper helper) {
      BlockPos furnacePos = new BlockPos(4, 1, 4);
      helper.setBlock(furnacePos, ModBlocks.ALCHEMY_FURNACE.get());
      helper.setBlock(furnacePos.above(), ModBlocks.ARCANE_ALEMBIC.get());
      AlchemyFurnaceBlockEntity furnace = (AlchemyFurnaceBlockEntity)helper.getBlockEntity(furnacePos);
      furnace.setItem(0, new ItemStack(Items.COBBLESTONE, 4));
      furnace.setItem(1, new ItemStack(Items.COAL, 4));
      ArcaneAlembicBlockEntity alembic = (ArcaneAlembicBlockEntity)helper.getBlockEntity(furnacePos.above());
      helper.succeedWhen(() -> helper.assertTrue(alembic.amount > 0 && alembic.aspect != null, "alembic must collect essentia from the furnace"));
   }

   /** TC4 TileJarFillableVoid: a full void jar keeps drawing and destroys the excess. */
   @GameTest(template = "empty_9x5x9", timeoutTicks = 300)
   public static void fullVoidJarKeepsDraining(GameTestHelper helper) {
      BlockPos alembicPos = new BlockPos(1, 2, 4);
      helper.setBlock(alembicPos, ModBlocks.ARCANE_ALEMBIC.get());
      ArcaneAlembicBlockEntity alembic = (ArcaneAlembicBlockEntity)helper.getBlockEntity(alembicPos);
      alembic.aspect = Aspect.FIRE;
      alembic.amount = 16;
      helper.setBlock(new BlockPos(2, 2, 4), ModBlocks.ESSENCE_TUBE.get());
      helper.setBlock(new BlockPos(3, 2, 4), ModBlocks.ESSENCE_TUBE.get());
      helper.setBlock(new BlockPos(3, 1, 4), ModBlocks.VOID_JAR.get());
      JarBlockEntity jar = (JarBlockEntity)helper.getBlockEntity(new BlockPos(3, 1, 4));
      jar.aspect = Aspect.FIRE;
      jar.amount = jar.maxAmount;
      helper.succeedWhen(() -> {
         helper.assertTrue(alembic.amount < 8, "full void jar must keep draining the alembic, it still has " + alembic.amount);
         helper.assertTrue(jar.amount == jar.maxAmount, "the void jar stays full");
      });
   }

   /** TC4 TileArcaneLampFertility: victus from the side it faces, then two free cows nearby fall in love. */
   @GameTest(template = "empty_9x5x9", timeoutTicks = 200)
   public static void fertilityLampBreedsAnimals(GameTestHelper helper) {
      BlockPos lampPos = new BlockPos(4, 1, 4);
      helper.setBlock(lampPos, ModBlocks.ARCANE_LAMP_FERTILITY.get().defaultBlockState().setValue(BlockStateProperties.FACING, Direction.EAST));
      helper.setBlock(lampPos.east(), ModBlocks.ARCANE_ALEMBIC.get());
      ArcaneAlembicBlockEntity alembic = (ArcaneAlembicBlockEntity)helper.getBlockEntity(lampPos.east());
      alembic.aspect = Aspect.LIFE;
      alembic.amount = 8;
      Cow first = helper.spawn(EntityType.COW, new BlockPos(2, 1, 2));
      Cow second = helper.spawn(EntityType.COW, new BlockPos(6, 1, 6));
      ArcaneLampFertilityBlockEntity lamp = (ArcaneLampFertilityBlockEntity)helper.getBlockEntity(lampPos);
      helper.succeedWhen(() -> {
         helper.assertTrue(lamp.charges >= 0, "lamp charges must not go negative, got " + lamp.charges);
         helper.assertTrue(first.isInLove() && second.isInLove(), "both cows must fall in love");
      });
   }

   /** TC4 TileEssentiaCrystalizer: essentia from below becomes a crystal of that aspect thrown out on top. */
   @GameTest(template = "empty_9x5x9", timeoutTicks = 1500)
   public static void crystallizerMakesCrystal(GameTestHelper helper) {
      BlockPos alembicPos = new BlockPos(4, 1, 4);
      helper.setBlock(alembicPos, ModBlocks.ARCANE_ALEMBIC.get());
      ArcaneAlembicBlockEntity alembic = (ArcaneAlembicBlockEntity)helper.getBlockEntity(alembicPos);
      alembic.aspect = Aspect.WATER;
      alembic.amount = 8;
      helper.setBlock(alembicPos.above(), ModBlocks.ESSENCE_CRYSTALLIZER.get());
      helper.setBlock(alembicPos.above(2), Blocks.AIR);
      helper.succeedWhen(() -> {
         boolean crystal = !helper.getLevel()
            .getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(alembicPos)).inflate(3.0), e -> e.getItem().is(ModItems.CRYSTAL_ESSENCE.get()))
            .isEmpty();
         helper.assertTrue(crystal, "crystallizer must throw out an essence crystal");
      });
   }
}
