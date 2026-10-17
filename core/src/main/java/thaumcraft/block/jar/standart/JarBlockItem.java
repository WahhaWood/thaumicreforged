package thaumcraft.block.jar.standart;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssenceContainerItem;
import thaumcraft.block.jar.render.JarItemStackRenderer;
import thaumcraft.research.PlayerKnowledge;
import thaumcraft.util.ItemStackData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class JarBlockItem extends BlockItem implements IEssenceContainerItem {
   public JarBlockItem(Block block) {
      super(block, new Properties());
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
      Player player = Minecraft.getInstance().player;
      if (player != null) {
         this.appendHoverAspects(stack, components);
         CompoundTag tag = ItemStackData.getTag(stack);
         if (tag != null && tag.contains("AspectFilter")) {
            Aspect a = Aspect.getAspect(tag.getString("AspectFilter"));
            if (a != null && PlayerKnowledge.of(player).hasDiscoveredAspect(a)) {
               components.add(Component.literal("В§5" + a.getName()));
            } else {
               components.add(Component.literal("В§5" + I18n.get("tc.aspect.unknown", new Object[0])));
            }
         }
      }
   }

   @Override
   protected boolean updateCustomBlockEntityTag(
      @NotNull BlockPos pos, @NotNull Level level, @Nullable Player player, @NotNull ItemStack stack, @NotNull BlockState state
   ) {
      boolean result = super.updateCustomBlockEntityTag(pos, level, player, stack, state);
      if (level.getBlockEntity(pos) instanceof JarBlockEntity jar) {
         thaumcraft.api.aspects.AspectList aspects = this.getAspects(stack);
         if (aspects != null && aspects.size() > 0) {
            Aspect asp = aspects.getAspects()[0];
            if (asp != null) {
               jar.aspect = asp;
               jar.amount = aspects.getAmount(asp);
            }
         }
         Aspect filter = this.getFilter(stack);
         if (filter != null) {
            jar.aspectFilter = filter;
         }
         jar.setChanged();
         jar.markForUpdate();
      }
      return result;
   }

   @Nullable
   public Aspect getFilter(@NotNull ItemStack stack) {
      CompoundTag tag = ItemStackData.getTag(stack);
      return tag != null ? Aspect.getAspect(tag.getString("AspectFilter")) : null;
   }

   public int getMaxStackSize(@NotNull ItemStack stack) {
      return this.getAspects(stack) == null && this.getFilter(stack) == null ? super.getMaxStackSize(stack) : 1;
   }
}
