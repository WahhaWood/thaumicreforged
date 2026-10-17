package thaumcraft.block.arcane_workbench;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.recipe.ThaumcraftCraftingManager;
import thaumcraft.util.UtilsFX;

@OnlyIn(Dist.CLIENT)
public class ArcaneWorkbenchScreen extends AbstractContainerScreen<ArcaneWorkbenchMenu> {
   private final int[][] aspectLocs = new int[][]{{72, 21}, {24, 43}, {24, 102}, {72, 124}, {120, 102}, {120, 43}};
   private final ArrayList<Aspect> primals = Aspect.getPrimalAspects();
   private final ResourceLocation texture = Thaumcraft.location("textures/screen/arcane_workbench.png");

   public ArcaneWorkbenchScreen(ArcaneWorkbenchMenu menu, Inventory inventory, Component component) {
      super(menu, inventory, component);
      this.imageWidth = 190;
      this.imageHeight = 234;
   }

   protected void renderBg(@NotNull GuiGraphics graphics, float pt, int mx, int my) {
      Player player = ((ArcaneWorkbenchMenu)this.menu).inventory.player;
      MagicWorkbenchBlockEntity workbench = ((ArcaneWorkbenchMenu)this.menu).workbench;
      ItemStack wandStack = workbench.getItem(9);
      ItemWandCasting wand = wandStack.getItem() instanceof ItemWandCasting casting ? casting : null;
      RenderSystem.enableBlend();
      graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      graphics.blit(this.texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
      RenderSystem.disableBlend();
      ItemStack outputStack = ThaumcraftCraftingManager.findArcaneRecipeResult(workbench, player.level(), player);
      AspectList matchingArcaneRecipeAspects = null;
      if (!outputStack.isEmpty()) {
         matchingArcaneRecipeAspects = ThaumcraftCraftingManager.findArcaneRecipeAspects(workbench, player.level(), player);
         int n6 = 0;

         for (Aspect aspect : this.primals) {
            float n7 = matchingArcaneRecipeAspects.getAmount(aspect);
            if (matchingArcaneRecipeAspects.getAmount(aspect) > 0) {
               float n8 = 0.5F + (Mth.sin((player.tickCount + n6 * 10) / 2.0F) * 0.2F - 0.2F);
               if (wand != null) {
                  n7 *= wand.getConsumptionModifier(wandStack, player, aspect, true);
                  if (n7 * 100.0F <= wand.getVis(wandStack, aspect)) {
                     n8 = 1.0F;
                  }
               }

               UtilsFX.drawTag(
                  graphics, this.leftPos + this.aspectLocs[n6][0] - 8, this.topPos + this.aspectLocs[n6][1] - 8, aspect, n7, 0, 0.0F, 771, n8, false
               );
            }

            if (++n6 > 5) {
               break;
            }
         }
      }

      if (wand != null && matchingArcaneRecipeAspects != null && !wand.consumeAllVisCrafting(wandStack, player, matchingArcaneRecipeAspects, false)) {
         PoseStack pose = graphics.pose();
         pose.pushPose();
         float n9 = 0.33F;
         graphics.setColor(0.33F, 0.33F, 0.33F, 0.66F);
         graphics.renderItem(outputStack, this.leftPos + 160, this.topPos + 64);
         graphics.renderItemDecorations(this.font, outputStack, this.leftPos + 160, this.topPos + 64);
         pose.popPose();
         pose.pushPose();
         pose.translate(this.leftPos + 168, this.topPos + 46, 0.0F);
         pose.scale(0.5F, 0.5F, 0.0F);
         Component text = Component.translatable("tc.arcana_workbench.insufficient_vis");
         graphics.drawString(this.font, text, -(this.font.width(text) / 2), 0, 15625838);
         pose.popPose();
      }

      this.renderTooltip(graphics, mx, my);
   }

   protected void renderLabels(GuiGraphics p_281635_, int p_282681_, int p_283686_) {
   }
}
