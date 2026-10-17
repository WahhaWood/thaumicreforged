package thaumcraft.item.alumentum;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import org.jetbrains.annotations.NotNull;
import thaumcraft.entities.brainless.alumentum.AlumentumEntity;

public class AlumentumDispenseBehavior extends DefaultDispenseItemBehavior {
   @NotNull
   protected ItemStack execute(BlockSource source, ItemStack stack) {
      Position position = DispenserBlock.getDispensePosition(source);
      Level level = source.level();
      Projectile projectile = new AlumentumEntity(level, position.x(), position.y(), position.z());
      projectile.shoot(
         ((Direction)source.state().getValue(DispenserBlock.FACING)).getStepX(),
         ((Direction)source.state().getValue(DispenserBlock.FACING)).getStepY(),
         ((Direction)source.state().getValue(DispenserBlock.FACING)).getStepZ(),
         1.1F,
         6.0F
      );
      level.addFreshEntity(projectile);
      stack.shrink(1);
      return stack;
   }

   protected void playSound(BlockSource source) {
      source.level().levelEvent(1018, source.pos(), 0);
   }
}
