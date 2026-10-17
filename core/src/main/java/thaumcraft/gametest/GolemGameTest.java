package thaumcraft.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.Thaumcraft;
import thaumcraft.entities.smart.golem.EntityGolemBase;
import thaumcraft.init.registry.ModEntities;

@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public class GolemGameTest {

   /** The golem home survives save/load (chunk unload, server restart), as TC4 HomeX/Y/Z do. */
   @GameTest(template = "empty_1x1x1")
   public static void homeSurvivesReload(GameTestHelper helper) {
      EntityGolemBase golem = ModEntities.GOLEM.get().create(helper.getLevel());
      BlockPos home = helper.absolutePos(new BlockPos(0, 1, 0));
      golem.setHomeArea(home.getX(), home.getY(), home.getZ(), 24);

      CompoundTag saved = golem.saveWithoutId(new CompoundTag());
      EntityGolemBase reloaded = ModEntities.GOLEM.get().create(helper.getLevel());
      reloaded.load(saved);

      helper.assertTrue(home.equals(reloaded.getHomePosition()), "home must survive reload, got " + reloaded.getHomePosition());
      helper.assertTrue(reloaded.getRestrictRadius() == 24.0F, "home range must survive reload");
      helper.succeed();
   }
}
