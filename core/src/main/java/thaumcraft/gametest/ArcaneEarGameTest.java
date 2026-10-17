package thaumcraft.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.Thaumcraft;
import thaumcraft.block.sensor.SensorBlockEntity;
import thaumcraft.init.registry.ModBlocks;

/** TC4 arcane ear (TileSensor): hears a note block playing its note and instrument and pulses redstone for 10 ticks. */
@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public class ArcaneEarGameTest {
   private static final BlockPos NOTE_BLOCK = new BlockPos(2, 2, 4);
   private static final BlockPos EAR = new BlockPos(6, 2, 4);
   private static final BlockPos LAMP = EAR.east();

   /** Same note and instrument: the ear lights a lamp next to it, and the lamp goes out after the pulse. */
   @GameTest(template = "empty_9x5x9")
   public static void earHearsMatchingNote(GameTestHelper helper) {
      setUp(helper);
      SensorBlockEntity ear = (SensorBlockEntity)helper.getBlockEntity(EAR);
      helper.startSequence()
         .thenExecute(() -> helper.setBlock(NOTE_BLOCK.west(), Blocks.REDSTONE_BLOCK))
         .thenWaitUntil(() -> helper.assertBlockProperty(LAMP, RedstoneLampBlock.LIT, true))
         .thenWaitUntil(() -> {
            helper.assertTrue(ear.redstoneSignal == 0, "the pulse must end");
            helper.assertBlockProperty(LAMP, RedstoneLampBlock.LIT, false);
         })
         .thenSucceed();
   }

   /** Five clicks raise the ear by five semitones; then the note block's note 0 leaves it silent. */
   @GameTest(template = "empty_9x5x9")
   public static void earIgnoresOtherNote(GameTestHelper helper) {
      setUp(helper);
      SensorBlockEntity ear = (SensorBlockEntity)helper.getBlockEntity(EAR);
      Player player = helper.makeMockPlayer(GameType.SURVIVAL);
      BlockPos abs = helper.absolutePos(EAR);
      for (int i = 0; i < 5; i++) {
         helper.getBlockState(EAR).useWithoutItem(helper.getLevel(), player, new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false));
      }

      helper.assertTrue(ear.note == 5, "five clicks must give note 5, got " + ear.note);
      helper.setBlock(NOTE_BLOCK.west(), Blocks.REDSTONE_BLOCK);
      helper.runAfterDelay(20, () -> {
         helper.assertTrue(ear.redstoneSignal == 0, "a different note must not trigger the ear");
         helper.assertBlockProperty(LAMP, RedstoneLampBlock.LIT, false);
         helper.succeed();
      });
   }

   /** The ear takes the instrument a note block would get from the block below: stone a bass drum, then wood a bass. */
   @GameTest(template = "empty_9x5x9")
   public static void earToneFollowsBlockBelow(GameTestHelper helper) {
      setUp(helper);
      SensorBlockEntity ear = (SensorBlockEntity)helper.getBlockEntity(EAR);
      helper.assertTrue(ear.tone == NoteBlockInstrument.BASEDRUM.ordinal(), "ear on stone must listen for the bass drum, got " + ear.tone);
      helper.setBlock(EAR.below(), Blocks.OAK_PLANKS);
      helper.assertTrue(ear.tone == NoteBlockInstrument.BASS.ordinal(), "ear on planks must listen for the bass, got " + ear.tone);
      helper.succeed();
   }

   /** A note block on stone (bass drum, note 0) and an ear on stone with a lamp beside it. */
   private static void setUp(GameTestHelper helper) {
      helper.setBlock(NOTE_BLOCK, Blocks.NOTE_BLOCK);
      helper.setBlock(NOTE_BLOCK.below(), Blocks.STONE);
      helper.setBlock(EAR.below(), Blocks.STONE);
      helper.setBlock(EAR, ModBlocks.ARCANE_SENSOR.get());
      helper.setBlock(LAMP, Blocks.REDSTONE_LAMP);
   }
}
