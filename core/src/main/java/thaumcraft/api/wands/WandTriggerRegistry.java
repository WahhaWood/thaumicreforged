package thaumcraft.api.wands;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.HashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class WandTriggerRegistry {
   private static final HashMap<Block, IntList> TRIGGERS = new HashMap<>();

   public static void registerWandBlockTrigger(int event, Block block) {
      TRIGGERS.computeIfAbsent(block, s1 -> new IntArrayList()).add(event);
   }

   public static boolean hasTrigger(Block block) {
      return TRIGGERS.containsKey(block);
   }

   /** Диспетчер: перебирает зарегистрированные события блока и зовёт WandManager (как в 1.7.10). */
   public static InteractionResult performTrigger(
      Level level, ItemStack itemStack, Player player, BlockPos blockPos, Direction direction, Block block
   ) {
      IntList events = TRIGGERS.get(block);
      if (events == null) {
         return InteractionResult.PASS;
      }

      IWandTriggerManager manager = thaumcraft.Thaumcraft.setup.wandManager;
      if (manager == null) {
         return InteractionResult.PASS;
      }

      for (int i = 0; i < events.size(); i++) {
         int event = events.getInt(i);
         if (manager.performTrigger(level, itemStack, player, blockPos, direction, event)) {
            return InteractionResult.sidedSuccess(level.isClientSide());
         }
      }

      return InteractionResult.PASS;
   }

   private WandTriggerRegistry() {
   }
}
