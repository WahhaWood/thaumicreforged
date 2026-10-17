package thaumcraft.block.ore;

import java.util.List;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.render.ITIntColor;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModItems;
import thaumcraft.item.equipment.ItemElementalPickaxe;
import thaumcraft.item.equipment.ItemPrimalCrusher;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.item.wands.foci.ItemFocusExcavation;
import thaumcraft.util.Utils;
import thaumcraft.util.UtilsFX;

public class CustomOreBlock extends Block implements ITIntColor {
   private final boolean particle;
   private final int color;

   public CustomOreBlock() {
      this(false);
   }

   public CustomOreBlock(boolean particle) {
      this(particle, 16777215);
   }

   public CustomOreBlock(boolean particle, int color) {
      super(Properties.of().mapColor(MapColor.STONE).sound(SoundType.STONE).strength(1.5F, 5.0F).randomTicks());
      this.particle = particle;
      this.color = color;
   }

   public void attack(@NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull Player player) {
      if (this.particle && level.isClientSide) {
         UtilsFX.infusedStoneSparkle(level, blockPos, 0);
      }
   }

   @Override
   public int getColor(int tint) {
      return tint == 0 ? this.color : -1;
   }

   @NotNull
   public List<ItemStack> getDrops(@NotNull BlockState state, @NotNull Builder builder) {
      if (!state.is((Block)ModBlocks.CINNABAR_ORE.get())) {
         return super.getDrops(state, builder);
      } else {
         ItemStack tool = (ItemStack)builder.getOptionalParameter(LootContextParams.TOOL);
         ItemStack drop = new ItemStack((ItemLike)ModItems.CINNABAR_ORE.get());
         boolean silkTouch = tool != null
            && !tool.isEmpty()
            && tool.getEnchantments().entrySet().stream().anyMatch(entry -> ((Holder)entry.getKey()).is(Enchantments.SILK_TOUCH) && entry.getIntValue() > 0);
         if (silkTouch) {
            return List.of(drop);
         } else {
            if (isSpecialMiningTool(tool)) {
               float fortune = 0.2F + getSpecialMiningFortune(tool) * 0.075F;
               drop = Utils.findSpecialMiningResult(drop, fortune, RandomSource.create());
            }

            return List.of(drop);
         }
      }
   }

   private static boolean isSpecialMiningTool(ItemStack tool) {
      if (tool == null || tool.isEmpty()) {
         return false;
      } else if (!(tool.getItem() instanceof ItemElementalPickaxe) && !(tool.getItem() instanceof ItemPrimalCrusher)) {
         if (!(tool.getItem() instanceof ItemWandCasting wand)) {
            return false;
         } else {
            ItemStack focusStack = wand.getFocusItem(tool);
            return focusStack.getItem() instanceof ItemFocusBasic focus && focus.isUpgradedWith(focusStack, ItemFocusExcavation.dowsing);
         }
      } else {
         return true;
      }
   }

   private static int getSpecialMiningFortune(ItemStack tool) {
      if (tool != null && !tool.isEmpty()) {
         if (tool.getItem() instanceof ItemWandCasting wand) {
            ItemStack focusStack = wand.getFocusItem(tool);
            return focusStack.getItem() instanceof ItemFocusBasic focus ? focus.getUpgradeLevel(focusStack, FocusUpgradeType.treasure) : 0;
         } else {
            return tool.getEnchantments()
               .entrySet()
               .stream()
               .filter(entry -> ((Holder)entry.getKey()).is(Enchantments.FORTUNE))
               .mapToInt(Entry::getValue)
               .findFirst()
               .orElse(0);
         }
      } else {
         return 0;
      }
   }
}
