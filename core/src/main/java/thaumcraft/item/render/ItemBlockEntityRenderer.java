package thaumcraft.item.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import thaumcraft.block.banner.BannerBlock;
import thaumcraft.block.banner.BannerBlockEntity;
import thaumcraft.block.crystal.CrystalClusterBlock;
import thaumcraft.block.deconstruction_table.DeconstructionTableBlock;
import thaumcraft.block.deconstruction_table.DeconstructionTableBlockEntity;
import thaumcraft.block.hungry_chest.ChestHungryBlock;
import thaumcraft.block.hungry_chest.ChestHungryBlockEntity;
import thaumcraft.block.magnet.MagnetBlock;
import thaumcraft.block.magnet.MagnetBlockEntity;
import thaumcraft.block.magnet.MobMagnetBlockEntity;
import thaumcraft.block.repairer.RepairerBlock;
import thaumcraft.block.repairer.RepairerBlockEntity;
import thaumcraft.init.registry.ModBlocks;

@OnlyIn(Dist.CLIENT)
public class ItemBlockEntityRenderer extends BlockEntityWithoutLevelRenderer implements IClientItemExtensions {
   private static ItemBlockEntityRenderer instance;
   private BannerBlockEntity banner;
   private CrystalClusterBlock.CrystalClusterBlockEntity cluster;
   private RepairerBlockEntity repairer;
   private MagnetBlockEntity magnet;
   private MobMagnetBlockEntity mobMagnetBlockEntity;
   private ChestHungryBlockEntity hungryBlockEntity;
   private DeconstructionTableBlockEntity deconstructionTable;

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
      BlockEntity blockEntity = null;
      if (stack.getItem() instanceof BlockItem blockItem) {
         Block block = blockItem.getBlock();
         if (block instanceof BannerBlock bannerBlock) {
            if (this.banner == null) {
               this.banner = new BannerBlockEntity(BlockPos.ZERO, ((Block)ModBlocks.BANNER.get(DyeColor.WHITE).get()).defaultBlockState());
            }

            this.banner.fromColor(bannerBlock.getColor());
            blockEntity = this.banner;
         } else if (block instanceof CrystalClusterBlock) {
            // Фейковая BE для инвентаря: дефолт FACING=DOWN (рост вниз) рисовал
            // кластер вверх ногами — в руке/инвентаре показываем растущий вверх.
            net.minecraft.world.level.block.state.BlockState upState = block.defaultBlockState()
               .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING, net.minecraft.core.Direction.UP);
            if (this.cluster == null) {
               this.cluster = new CrystalClusterBlock.CrystalClusterBlockEntity(BlockPos.ZERO, upState);
            }

            this.cluster.setBlockState(upState);
            blockEntity = this.cluster;
         } else if (block instanceof RepairerBlock) {
            if (this.repairer == null) {
               this.repairer = new RepairerBlockEntity(BlockPos.ZERO, block.defaultBlockState());
            }

            this.repairer.setBlockState(block.defaultBlockState());
            blockEntity = this.repairer;
         } else if (block instanceof MagnetBlock) {
            if (((MagnetBlock)block).isMobMagnet()) {
               if (this.mobMagnetBlockEntity == null) {
                  this.mobMagnetBlockEntity = new MobMagnetBlockEntity(BlockPos.ZERO, block.defaultBlockState());
               }

               this.mobMagnetBlockEntity.setBlockState(block.defaultBlockState());
               blockEntity = this.mobMagnetBlockEntity;
            } else {
               if (this.magnet == null) {
                  this.magnet = new MagnetBlockEntity(BlockPos.ZERO, block.defaultBlockState());
               }

               this.magnet.setBlockState(block.defaultBlockState());
               blockEntity = this.magnet;
            }
         } else if (block instanceof ChestHungryBlock) {
            if (this.hungryBlockEntity == null) {
               this.hungryBlockEntity = new ChestHungryBlockEntity(BlockPos.ZERO, block.defaultBlockState());
            }

            this.hungryBlockEntity.setBlockState(block.defaultBlockState());
            blockEntity = this.hungryBlockEntity;
         } else if (block instanceof DeconstructionTableBlock) {
            if (this.deconstructionTable == null) {
               this.deconstructionTable = new DeconstructionTableBlockEntity(BlockPos.ZERO, block.defaultBlockState());
            }

            this.deconstructionTable.setBlockState(block.defaultBlockState());
            blockEntity = this.deconstructionTable;
         }
      }

      if (blockEntity != null) {
         Minecraft.getInstance().getBlockEntityRenderDispatcher().renderItem(blockEntity, pose, source, brightnessOverride, overlayTexture);
      }
   }

   public BlockEntityWithoutLevelRenderer getCustomRenderer() {
      return this;
   }
}
