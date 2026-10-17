package thaumicenergistics.client.screen;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AmountFormat;
import appeng.api.stacks.GenericStack;
import appeng.client.gui.me.common.StackSizeRenderer;
import appeng.core.AEConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.ae2.storage.AEAspectKey;
import thaumicenergistics.block.infusion_assembler.InfusionAssemblerBlockEntity;
import thaumicenergistics.menu.InfusionAssemblerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.InfusionRecipe;

@OnlyIn(Dist.CLIENT)
public class InfusionAssemblerScreen extends RecipeAssemblerScreen<InfusionAssemblerMenu> {
   private static final int MAIN_GUI_WIDTH = 176;
   private static final int UPGRADE_GUI_WIDTH = 58;
   private static final int GUI_HEIGHT = 197;
   private static final int UPGRADE_GUI_HEIGHT = 104;
   private static final ResourceLocation GUI_TEXTURE = ThaumicEnergistics.location("textures/screen/matrix_assembler.png");
   private List<GenericStack> cacheAspects = new ArrayList<>();
   private ItemStack lastCostTarget = ItemStack.EMPTY;

   public InfusionAssemblerScreen(InfusionAssemblerMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title, new RecipeGridPanel.Layout(8, 8, 9, 4, 211));
      this.imageWidth = 234;
      this.imageHeight = 197;
      this.inventoryLabelY = 103;
   }

   protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      graphics.blit(GUI_TEXTURE, this.leftPos, this.topPos, 0, 0, 176, this.imageHeight);
      graphics.blit(GUI_TEXTURE, this.leftPos + 176, this.topPos, 176, 0, 58, 104);
      // the brain core slot is gone: paint the panel over its art
      graphics.fill(this.leftPos + 186, this.topPos + 7, this.leftPos + 204, this.topPos + 25, 0xFFC6C6C6);
      this.renderRecipes(graphics);
   }

   protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
      InfusionAssemblerBlockEntity assembler = ((InfusionAssemblerMenu)this.menu).assembler;
      ItemStack stack = assembler.craftingInventory.getStackInSlot(0);
      if (stack.isEmpty()) {
         this.cacheAspects.clear();
         this.lastCostTarget = ItemStack.EMPTY;
      } else {
         // Кэш чистился только на пустом слоте: смена цели без очистки
         // показывала stale-аспекты прошлого рецепта до первого опустения.
         if (!ItemStack.isSameItemSameComponents(stack, this.lastCostTarget)) {
            this.cacheAspects.clear();
            this.lastCostTarget = stack.copy();
         }

         if (this.cacheAspects.isEmpty()) {
            InfusionRecipe recipe = ThaumcraftApi.getInfusionRecipe(stack);
            if (recipe != null) {
               AspectList aspectList = recipe.aspects;
               if (aspectList != null) {
                  for (Entry<Aspect, Integer> entry : aspectList.aspects.entrySet()) {
                     Aspect aspect = entry.getKey();
                     int count = entry.getValue();
                     if (aspect != null && count > 0) {
                        this.cacheAspects.add(new GenericStack(AEAspectKey.of(aspect), (long)count));
                     }
                  }
               }
            }
         }
      }

      if (!this.cacheAspects.isEmpty()) {
         float filled = assembler.getPercentComplete();
         int x = 0;

         for (GenericStack aspectStack : this.cacheAspects) {
            if (aspectStack != null && x < 7) {
               long amount = aspectStack.amount() - (long)Math.round((float)aspectStack.amount() * filled);
               AEKeyRendering.drawInGui(this.minecraft, graphics, 8 + x * 18, 88, aspectStack.what());
               boolean useLargeFonts = AEConfig.instance().isUseLargeFonts();
               AmountFormat format = useLargeFonts ? AmountFormat.SLOT_LARGE_FONT : AmountFormat.SLOT;
               String text = aspectStack.what().formatAmount(amount, format);
               StackSizeRenderer.renderSizeLabel(graphics, this.font, (float)(8 + x * 18), 88.0F, text, useLargeFonts);
               x++;
            }
         }
      }
   }
}
