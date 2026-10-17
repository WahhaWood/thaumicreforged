package thaumicenergistics.client;

import com.mojang.blaze3d.vertex.PoseStack;
import thaumicenergistics.block.arcane_assembler.AdvancedArcaneAssemblerBlock;
import thaumicenergistics.block.arcane_assembler.ArcaneAssemblerBlock;
import thaumicenergistics.block.arcane_assembler.ArcaneAssemblerBlockEntity;
import thaumicenergistics.block.gearbox.GearBoxBlock;
import thaumicenergistics.block.gearbox.GolemGearBoxBlock;
import thaumicenergistics.block.gearbox.entity.GearBoxBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class ItemBlockEntityRenderer extends BlockEntityWithoutLevelRenderer implements IClientItemExtensions {
   private static ItemBlockEntityRenderer instance;
   private GearBoxBlockEntity standardGear;
   private GearBoxBlockEntity golemGear;
   private ArcaneAssemblerBlockEntity arcaneAssembler;

   public static ItemBlockEntityRenderer getInstance() {
      if (instance == null) {
         instance = new ItemBlockEntityRenderer();
      }

      return instance;
   }

   private ItemBlockEntityRenderer() {
      super(null, null);
   }

   public void renderByItem(
      @NotNull ItemStack stack,
      @NotNull ItemDisplayContext context,
      @NotNull PoseStack pose,
      @NotNull MultiBufferSource source,
      int brightnessOverride,
      int overlayTexture
   ) {
      if (stack.getItem() instanceof BlockItem blockItem) {
         Block block = blockItem.getBlock();
         BlockEntityRenderDispatcher dispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
         if (block instanceof GolemGearBoxBlock) {
            if (this.golemGear == null) {
               this.golemGear = new GearBoxBlockEntity(BlockPos.ZERO, block.defaultBlockState());
            }

            this.golemGear.setBlockState(block.defaultBlockState());
            dispatcher.renderItem(this.golemGear, pose, source, brightnessOverride, overlayTexture);
         } else if (block instanceof GearBoxBlock) {
            if (this.standardGear == null) {
               this.standardGear = new GearBoxBlockEntity(BlockPos.ZERO, block.defaultBlockState());
            }

            this.standardGear.setBlockState(block.defaultBlockState());
            dispatcher.renderItem(this.standardGear, pose, source, brightnessOverride, overlayTexture);
         } else if (block instanceof ArcaneAssemblerBlock && !(block instanceof AdvancedArcaneAssemblerBlock)) {
            if (this.arcaneAssembler == null) {
               this.arcaneAssembler = new ArcaneAssemblerBlockEntity(BlockPos.ZERO, block.defaultBlockState());
            }

            this.arcaneAssembler.setBlockState(block.defaultBlockState());
            dispatcher.renderItem(this.arcaneAssembler, pose, source, brightnessOverride, overlayTexture);
         }
      }
   }

   public BlockEntityWithoutLevelRenderer getCustomRenderer() {
      return this;
   }
}
