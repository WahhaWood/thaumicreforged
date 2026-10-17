package thaumcraft.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.aspects.Aspect;

public interface IVisDiscountGear {
   int getVisDiscount(ItemStack var1, Player var2, Aspect var3);
}
