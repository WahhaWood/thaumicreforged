package thaumcraft.gametest;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.block.airy.aura_node.AuraNodeBlockEntity;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModItems;

/** TC4 BlockCosmeticSolid meta 8: the obsidian totem with a live node inside. */
@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public class ObsidianTotemNodeGameTest {
   private static final BlockPos TOTEM = new BlockPos(4, 2, 4);

   /** The node in the totem ticks; with no vis left it is gone at the next 1200-tick check (TC4) and a plain totem stays. */
   @GameTest(template = "empty_9x5x9", timeoutTicks = 1300)
   public static void emptyTotemNodeLeavesPlainTotem(GameTestHelper helper) {
      helper.setBlock(TOTEM, ModBlocks.CHARGED_OBSIDIAN_TOTEM.get());
      helper.succeedWhen(() -> helper.assertBlockPresent(ModBlocks.OBSIDIAN_TOTEM.get(), TOTEM));
   }

   /** Broken, it drops a plain totem and wisp essence of its vis (TC4 damageDropped 8 -> 1, onBlockHarvested). */
   @GameTest(template = "empty_9x5x9")
   public static void totemDropsPlainTotemAndEssence(GameTestHelper helper) {
      helper.setBlock(TOTEM, ModBlocks.CHARGED_OBSIDIAN_TOTEM.get());
      ((AuraNodeBlockEntity)helper.getBlockEntity(TOTEM)).setAspects(new AspectList().add(Aspect.AIR, 20));
      helper.runAfterDelay(2, () -> {
         helper.assertBlockPresent(ModBlocks.CHARGED_OBSIDIAN_TOTEM.get(), TOTEM);
         helper.getLevel().destroyBlock(helper.absolutePos(TOTEM), true);
         List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(TOTEM)).inflate(2.0));
         helper.assertTrue(drops.stream().anyMatch(e -> e.getItem().is(ModItems.OBSIDIAN_TOTEM.get())), "must drop a plain totem");
         helper.assertTrue(drops.stream().noneMatch(e -> e.getItem().is(ModItems.CHARGED_OBSIDIAN_TOTEM.get())), "must not drop the node totem");
         helper.assertTrue(drops.stream().anyMatch(e -> e.getItem().is(ModItems.WISP_ESSENCE.get())), "must drop wisp essence of the node's aer");
         helper.succeed();
      });
   }
}
