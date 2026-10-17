package thaumcraft.item.aura_node;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.nodes.NodeModifier;
import thaumcraft.api.nodes.NodeType;
import thaumcraft.block.airy.aura_node.AuraNodeBlockEntity;

public class PresetAuraNodeItem extends ItemAuraNode {
   private final AspectList aspects;
   private final NodeType nodeType;
   @Nullable
   private final NodeModifier nodeModifier;
   private final int awakenPrimalVisAmount;
   private final String tooltipBaseKey;
   private final String tooltipEnergizedKey;

   public PresetAuraNodeItem(
      Block block,
      AspectList aspects,
      NodeType nodeType,
      @Nullable NodeModifier nodeModifier,
      int awakenPrimalVisAmount,
      String tooltipBaseKey,
      String tooltipEnergizedKey
   ) {
      super(block);
      this.aspects = aspects.copy();
      this.nodeType = nodeType;
      this.nodeModifier = nodeModifier;
      this.awakenPrimalVisAmount = awakenPrimalVisAmount;
      this.tooltipBaseKey = tooltipBaseKey;
      this.tooltipEnergizedKey = tooltipEnergizedKey;
   }

   public static PresetAuraNodeItem primalSixteen(Block block) {
      // 256 of each primal (as the tooltip says) energizes to sqrt(256) = 16 the TC4 way.
      return primalNode(block, 256, 0, "item.thaumcraft.primal_16_aura_node.tooltip.base", "item.thaumcraft.primal_16_aura_node.tooltip.energized");
   }

   public static PresetAuraNodeItem primalTwoFiftySix(Block block) {
      return primalNode(block, 256, 256, "item.thaumcraft.primal_256_aura_node.tooltip.base", "item.thaumcraft.primal_256_aura_node.tooltip.energized");
   }

   private static PresetAuraNodeItem primalNode(Block block, int baseAmount, int awakenPrimalVisAmount, String tooltipBaseKey, String tooltipEnergizedKey) {
      return new PresetAuraNodeItem(block, primalAspects(baseAmount), NodeType.NORMAL, null, awakenPrimalVisAmount, tooltipBaseKey, tooltipEnergizedKey);
   }

   private static AspectList primalAspects(int amount) {
      return new AspectList(Math.max(amount, 256))
         .add(Aspect.AIR, amount)
         .add(Aspect.EARTH, amount)
         .add(Aspect.FIRE, amount)
         .add(Aspect.WATER, amount)
         .add(Aspect.ORDER, amount)
         .add(Aspect.ENTROPY, amount);
   }

   public void configurePlacedNode(@NotNull Level level, @NotNull BlockPos pos, @Nullable LivingEntity entity) {
      if (level.getBlockEntity(pos) instanceof AuraNodeBlockEntity node) {
         node.setNodeType(this.nodeType);
         node.setNodeModifier(this.nodeModifier);
         node.setAspects(this.aspects.copy());
         node.setAwakenPrimalVisAmount(this.awakenPrimalVisAmount);
         if (entity instanceof Player player) {
            boolean var10000 = false;
         }

         node.setChanged();
         node.markForUpdate();
      }
   }

   @Override
   public void appendHoverText(
      @NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> components, @NotNull TooltipFlag tooltipFlag
   ) {
      Level level = context.level();
      components.add(Component.translatable(this.tooltipBaseKey));
      components.add(Component.translatable(this.tooltipEnergizedKey));
   }
}
