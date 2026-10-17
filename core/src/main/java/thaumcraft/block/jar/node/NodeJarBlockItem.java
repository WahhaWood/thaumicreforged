package thaumcraft.block.jar.node;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssenceContainerItem;
import thaumcraft.api.nodes.NodeModifier;
import thaumcraft.api.nodes.NodeType;
import thaumcraft.block.jar.render.JarItemStackRenderer;
import thaumcraft.util.ItemStackData;

public class NodeJarBlockItem extends BlockItem implements IEssenceContainerItem {
   public NodeJarBlockItem(Block block) {
      super(block, new Properties().stacksTo(1));
   }

   @Nullable
   @Override
   public AspectList getAspects(ItemStack stack) {
      AspectList aspects = IEssenceContainerItem.super.getAspects(stack);
      if (aspects == null) {
         return null;
      } else {
         aspects.setMaxAmount(256);
         return aspects;
      }
   }

   @Override
   public void setAspects(ItemStack stack, @Nullable AspectList aspects) {
      if (aspects == null) {
         IEssenceContainerItem.super.setAspects(stack, null);
      } else {
         AspectList normalized = aspects.copy();
         normalized.setMaxAmount(256);
         IEssenceContainerItem.super.setAspects(stack, normalized);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
      consumer.accept(new IClientItemExtensions() {
         private final BlockEntityWithoutLevelRenderer renderer = new JarItemStackRenderer();

         public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            return this.renderer;
         }
      });
   }

   @OnlyIn(Dist.CLIENT)
   public void appendHoverText(@NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> components, @NotNull TooltipFlag flags) {
      Level level = context.level();
      NodeType type = this.getNodeType(stack);
      NodeModifier mod = this.getNodeModifier(stack);
      MutableComponent desc = Component.translatable("nodetype." + type + ".name").setStyle(Style.EMPTY.withColor(ChatFormatting.BLUE));
      if (mod != null) {
         desc = desc.append(", ").append(Component.translatable("nodemod." + mod + ".name"));
      }

      components.add(desc);
      this.appendHoverAspects(stack, components);
   }

   public void setNodeAttributes(@NotNull ItemStack stack, @NotNull NodeType type, @Nullable NodeModifier mod, @NotNull String id) {
      ItemStackData.addTagElement(stack, "nodetype", IntTag.valueOf(type.ordinal()));
      if (mod != null) {
         ItemStackData.addTagElement(stack, "nodemod", IntTag.valueOf(mod.ordinal()));
      } else {
         ItemStackData.removeTagKey(stack, "nodemod");
      }

      ItemStackData.addTagElement(stack, "nodeid", StringTag.valueOf(id));
   }

   /** Preset nodes: the energized output survives the trip in the jar. */
   public void setAwakenPrimalVisAmount(@NotNull ItemStack stack, int amount) {
      if (amount > 0) {
         ItemStackData.addTagElement(stack, "nodeawaken", IntTag.valueOf(amount));
      } else {
         ItemStackData.removeTagKey(stack, "nodeawaken");
      }
   }

   public int getAwakenPrimalVisAmount(@NotNull ItemStack stack) {
      CompoundTag tag = ItemStackData.getTag(stack);
      return tag == null ? 0 : tag.getInt("nodeawaken");
   }

   @Nullable
   public NodeType getNodeType(@NotNull ItemStack stack) {
      CompoundTag tag = ItemStackData.getTag(stack);
      if (tag == null) {
         return NodeType.NORMAL;
      } else {
         int i = tag.getInt("nodetype");
         NodeType[] types = NodeType.values();
         return i >= 0 && i < types.length ? types[i] : NodeType.NORMAL;
      }
   }

   /** TC4 ItemJarNode.onItemUse: the placed jar takes the node stored in the item. */
   protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, @Nullable Player player, ItemStack stack, BlockState state) {
      boolean changed = super.updateCustomBlockEntityTag(pos, level, player, stack, state);
      AspectList aspects = this.getAspects(stack);
      if (aspects != null && level.getBlockEntity(pos) instanceof NodeJarBlockEntity jar) {
         jar.setAspects(aspects);
         jar.setNodeType(this.getNodeType(stack));
         jar.setNodeModifier(this.getNodeModifier(stack));
         jar.setId(this.getNodeId(stack));
         jar.setAwakenPrimalVisAmount(this.getAwakenPrimalVisAmount(stack));
         return true;
      } else {
         return changed;
      }
   }

   @Nullable
   public NodeModifier getNodeModifier(@NotNull ItemStack stack) {
      CompoundTag tag = ItemStackData.getTag(stack);
      if (tag == null || !tag.contains("nodemod")) {
         return null;
      } else {
         int i = tag.getInt("nodemod");
         NodeModifier[] mods = NodeModifier.values();
         return i >= 0 && i < mods.length ? mods[i] : null;
      }
   }

   @NotNull
   public String getNodeId(@NotNull ItemStack stack) {
      CompoundTag tag = ItemStackData.getTag(stack);
      return tag == null ? "0" : tag.getString("nodeid");
   }
}
