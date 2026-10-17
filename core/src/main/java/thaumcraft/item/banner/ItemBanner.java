package thaumcraft.item.banner;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.block.banner.BannerBlockEntity;
import thaumcraft.item.render.ItemBlockEntityRenderer;
import thaumcraft.util.ItemStackData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ItemBanner extends StandingAndWallBlockItem {
   public ItemBanner(Block base, Block wall) {
      super(base, wall, new Properties().stacksTo(16), Direction.DOWN);
   }

   public DyeColor getColor() {
      return ((AbstractBannerBlock)this.getBlock()).getColor();
   }

   protected boolean placeBlock(@NotNull BlockPlaceContext context, @NotNull BlockState state) {
      boolean result = super.placeBlock(context, state);
      if (result) {
         Level level = context.getLevel();
         BlockPos blockPos = context.getClickedPos();
         if (level.getBlockEntity(blockPos) instanceof BannerBlockEntity banner) {
            ItemStack stack = context.getItemInHand();
            CompoundTag compound = ItemStackData.getTag(stack);
            if (compound != null && compound.contains("aspect", 8)) {
               banner.setAspect(Aspect.getAspect(compound.getString("aspect")));
            }

            banner.setChanged();
            level.blockEntityChanged(blockPos);
         }
      }

      return result;
   }

   @OnlyIn(Dist.CLIENT)
   public void initializeClient(Consumer<IClientItemExtensions> consumer) {
      consumer.accept(ItemBlockEntityRenderer.getInstance());
   }
}
