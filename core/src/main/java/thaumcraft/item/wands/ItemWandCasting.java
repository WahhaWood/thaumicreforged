package thaumcraft.item.wands;

import com.mojang.blaze3d.vertex.PoseStack;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.IArchitect;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.IWandable;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.api.wands.WandTriggerRegistry;
import thaumcraft.block.opaque.OwnedBlockEntity;
import thaumcraft.block.warded.WardedBlockEntity;
import thaumcraft.init.registry.Config;
import thaumcraft.init.registry.ModItems;
import thaumcraft.item.wands.render.WandRender;
import thaumcraft.util.ItemStackData;

public class ItemWandCasting extends Item implements IArchitect {
   private static final DecimalFormat FORMATTER = new DecimalFormat("#######.##");
   public ItemFocusBasic.WandFocusAnimation animation = null;

   public ItemWandCasting() {
      super(new Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
   }

   public int getMaxVis(ItemStack stack) {
      return this.getRod(stack).getCapacity() * (this.isSceptre(stack) ? 150 : 100);
   }

   /**
    * TC4 getItemStackDisplayName: "%CAP %ROD %OBJ". Built from translatable parts so the server names the wand too
    * (chat, death messages); the client translates them. Was client-only with I18n, so the server fell back to the bare item name.
    */
   @NotNull
   public Component getName(@NotNull ItemStack stack) {
      String rod = this.getRod(stack).getTag();
      if (rod.contains("_staff")) {
         rod = rod.substring(0, rod.indexOf("_staff"));
      }

      String obj = this.isStaff(stack) ? "item.Wand.staff.obj" : (this.isSceptre(stack) ? "item.Wand.sceptre.obj" : "item.Wand.wand.obj");
      return Component.empty()
         .append(Component.translatable("item.Wand." + this.getCap(stack).getTag() + ".cap"))
         .append(" ")
         .append(Component.translatable("item.Wand." + rod + ".rod"))
         .append(" ")
         .append(Component.translatable(obj));
   }

   @OnlyIn(Dist.CLIENT)
   public void appendHoverText(
      @NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> components, @NotNull TooltipFlag tooltipFlag
   ) {
      Level level = context.level();
      if (useStructuredTooltipFormatting()) {
         this.appendStructuredHoverText(stack, level, components, tooltipFlag);
      } else {
         int pos = components.size();
         String tt2 = "";
         CompoundTag tag = ItemStackData.getTag(stack);
         if (tag != null) {
            StringBuilder line = new StringBuilder();
            int tot = 0;
            int num = 0;
            Player clientPlayer = Minecraft.getInstance().player;

            for (Aspect aspect : Aspect.getPrimalAspects()) {
               if (tag.contains(aspect.getTag(), 3)) {
                  String amount = FORMATTER.format(tag.getInt(aspect.getTag()) / 100.0F);
                  float mod = this.getConsumptionModifier(stack, clientPlayer, aspect, false);
                  String consumption = FORMATTER.format(mod * 100.0F);
                  num++;
                  tot = (int)(tot + mod * 100.0F);
                  String extra = "";
                  ItemStack focus = this.getFocusItem(stack);
                  if (!focus.isEmpty() && focus.getItem() instanceof ItemFocusBasic fb) {
                     int amt = fb.getVisCost(focus).getAmount(aspect);
                     if (amt > 0) {
                        extra = "В§r, "
                           + FORMATTER.format(amt * mod / 100.0F)
                           + " "
                           + I18n.get(fb.isVisCostPerTick(focus) ? "item.Focus.cost2" : "item.Focus.cost1", new Object[0]);
                     }
                  }

                  if (Screen.hasShiftDown()) {
                     components.add(
                        Component.literal(
                           " "
                              + aspect.getTooltipColor()
                              + aspect.getName()
                              + "В§r x "
                              + amount
                              + ", В§o("
                              + consumption
                              + "% "
                              + I18n.get("tc.vis.cost", new Object[0])
                              + ")"
                              + extra
                        )
                     );
                  } else {
                     if (line.length() > 0) {
                        line.append(" | ");
                     }

                     line.append(aspect.getTooltipColor()).append(amount).append("В§r");
                  }
               }
            }

            if (!Screen.hasShiftDown() && num > 0) {
               components.add(Component.literal(line.toString()));
               tot /= num;
               tt2 = " (" + tot + "% " + I18n.get("tc.vis.costavg", new Object[0]) + ")";
            }
         }

         components.add(
            pos, Component.literal(ChatFormatting.GOLD + I18n.get("item.capacity.text", new Object[0]) + " " + this.getMaxVis(stack) / 100 + "В§r" + tt2)
         );
         ItemFocusBasic focusx = this.getFocus(stack);
         if (focusx != null) {
            ItemStack focusItem = this.getFocusItem(stack);
            components.add(Component.literal(ChatFormatting.BOLD.toString() + ChatFormatting.ITALIC + ChatFormatting.GREEN + focusx.getName(focusItem)));
            if (Screen.hasShiftDown()) {
               focusx.addFocusInformation(focusItem, level, components, tooltipFlag);
            }
         }
      }
   }

   private static boolean useStructuredTooltipFormatting() {
      return true;
   }

   @OnlyIn(Dist.CLIENT)
   private void appendStructuredHoverText(
      @NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> components, @NotNull TooltipFlag tooltipFlag
   ) {
      int pos = components.size();
      String tt2 = "";
      CompoundTag tag = ItemStackData.getTag(stack);
      if (tag != null) {
         MutableComponent compactLine = Component.empty();
         int tot = 0;
         int num = 0;
         Player clientPlayer = Minecraft.getInstance().player;

         for (Aspect aspect : Aspect.getPrimalAspects()) {
            if (tag.contains(aspect.getTag(), 3)) {
               String amount = FORMATTER.format(tag.getInt(aspect.getTag()) / 100.0F);
               float mod = this.getConsumptionModifier(stack, clientPlayer, aspect, false);
               String consumption = FORMATTER.format(mod * 100.0F);
               num++;
               tot = (int)(tot + mod * 100.0F);
               String extra = "";
               ItemStack focus = this.getFocusItem(stack);
               if (!focus.isEmpty() && focus.getItem() instanceof ItemFocusBasic fb) {
                  int amt = fb.getVisCost(focus).getAmount(aspect);
                  if (amt > 0) {
                     extra = ", "
                        + FORMATTER.format(amt * mod / 100.0F)
                        + " "
                        + I18n.get(fb.isVisCostPerTick(focus) ? "item.Focus.cost2" : "item.Focus.cost1", new Object[0]);
                  }
               }

               if (Screen.hasShiftDown()) {
                  components.add(
                     Component.literal(
                           aspect.getStringName() + " x " + amount + ", (" + consumption + "% " + I18n.get("tc.vis.cost", new Object[0]) + ")" + extra
                        )
                        .setStyle(Style.EMPTY.withColor(aspect.getColor()))
                  );
               } else {
                  if (!compactLine.getSiblings().isEmpty() || !compactLine.getString().isEmpty()) {
                     compactLine.append(Component.literal(" | ").withStyle(ChatFormatting.GRAY));
                  }

                  compactLine.append(Component.literal(amount).setStyle(Style.EMPTY.withColor(aspect.getColor())));
               }
            }
         }

         if (!Screen.hasShiftDown() && num > 0) {
            components.add(compactLine);
            tot /= num;
            tt2 = " (" + tot + "% " + I18n.get("tc.vis.costavg", new Object[0]) + ")";
         }
      }

      components.add(
         pos,
         Component.literal(I18n.get("item.capacity.text", new Object[0]) + " " + this.getMaxVis(stack) / 100 + tt2)
            .setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD))
      );
      ItemFocusBasic focusx = this.getFocus(stack);
      if (focusx != null) {
         ItemStack focusItem = this.getFocusItem(stack);
         components.add(focusx.getName(focusItem).copy().setStyle(Style.EMPTY.withBold(true).withItalic(true).withColor(ChatFormatting.GREEN)));
         if (Screen.hasShiftDown()) {
            focusx.addFocusInformation(focusItem, level, components, tooltipFlag);
         }
      }
   }

   public AspectList getAllVis(ItemStack is) {
      AspectList out = new AspectList(this.getMaxVis(is));
      CompoundTag tag = ItemStackData.getTag(is);

      for (Aspect aspect : Aspect.getPrimalAspects()) {
         if (tag != null && tag.contains(aspect.getTag(), 3)) {
            out.merge(aspect, tag.getInt(aspect.getTag()));
         } else {
            out.merge(aspect, 0);
         }
      }

      return out;
   }

   public AspectList getAspectsWithRoom(ItemStack wandstack) {
      AspectList out = new AspectList();
      AspectList cur = this.getAllVis(wandstack);

      for (Aspect aspect : cur.getAspects()) {
         if (cur.getAmount(aspect) < this.getMaxVis(wandstack)) {
            out.add(aspect, 1);
         }
      }

      return out;
   }

   public void storeAllVis(ItemStack is, AspectList in) {
      for (Aspect aspect : in.getAspects()) {
         ItemStackData.addTagElement(is, aspect.getTag(), IntTag.valueOf(in.getAmount(aspect)));
      }
   }

   public int getVis(ItemStack is, Aspect aspect) {
      if (is != null && aspect != null) {
         CompoundTag tag = ItemStackData.getTag(is);
         if (tag == null) {
            return 0;
         } else {
            return tag.contains(aspect.getTag(), 3) ? tag.getInt(aspect.getTag()) : 0;
         }
      } else {
         return 0;
      }
   }

   public void storeVis(ItemStack is, Aspect aspect, int amount) {
      ItemStackData.addTagElement(is, aspect.getTag(), IntTag.valueOf(amount));
   }

   public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
      if (slotChanged) {
         return true;
      } else if (oldStack.getItem() != newStack.getItem()) {
         return true;
      } else if (oldStack.getItem() instanceof ItemWandCasting wand) {
         ItemWandRod var11 = wand.getRod(oldStack);
         ItemWandRod newRod = wand.getRod(newStack);
         if (var11 != newRod) {
            return true;
         } else {
            ItemWandCap oldCap = wand.getCap(oldStack);
            ItemWandCap newCap = wand.getCap(newStack);
            if (oldCap != newCap) {
               return true;
            } else {
               ItemFocusBasic oldFocus = wand.getFocus(oldStack);
               ItemFocusBasic newFocus = wand.getFocus(newStack);
               return oldFocus != newFocus;
            }
         }
      } else {
         return true;
      }
   }

   public float getConsumptionModifier(ItemStack is, @Nullable Player player, Aspect aspect, boolean crafting) {
      ItemWandCap cap = this.getCap(is);
      float mod;
      if (cap.getAspectsSpecialDiscount() != null && cap.getAspectsSpecialDiscount().contains(aspect)) {
         mod = cap.getSpecialDiscount();
      } else {
         mod = cap.getBasicDiscount();
      }

      if (player != null) {
         mod -= WandManager.getTotalVisDiscount(player, aspect);
      }

      if (this.getFocus(is) != null && !crafting) {
         mod -= this.getFocusUpgradeLevel(is, FocusUpgradeType.frugal) / 10.0F;
      }

      if (this.isSceptre(is)) {
         mod -= 0.1F;
      }

      return Math.max(mod, 0.1F);
   }

   public int getFocusPotency(ItemStack stack) {
      ItemStack focusStack = this.getFocusItem(stack);
      return focusStack.getItem() instanceof ItemFocusBasic fb ? fb.getUpgradeLevel(focusStack, FocusUpgradeType.potency) + (this.hasRunes(stack) ? 1 : 0) : 0;
   }

   public int getFocusTreasure(ItemStack stack) {
      ItemStack focusStack = this.getFocusItem(stack);
      return focusStack.getItem() instanceof ItemFocusBasic fb ? fb.getUpgradeLevel(focusStack, FocusUpgradeType.treasure) : 0;
   }

   public int getFocusFrugal(ItemStack stack) {
      ItemStack focusStack = this.getFocusItem(stack);
      return focusStack.getItem() instanceof ItemFocusBasic fb ? fb.getUpgradeLevel(focusStack, FocusUpgradeType.frugal) : 0;
   }

   public int getFocusEnlarge(ItemStack stack) {
      ItemStack focusStack = this.getFocusItem(stack);
      return focusStack.getItem() instanceof ItemFocusBasic fb ? fb.getUpgradeLevel(focusStack, FocusUpgradeType.enlarge) : 0;
   }

   public int getFocusExtend(ItemStack stack) {
      ItemStack focusStack = this.getFocusItem(stack);
      return focusStack.getItem() instanceof ItemFocusBasic fb ? fb.getUpgradeLevel(focusStack, FocusUpgradeType.extend) : 0;
   }

   public int getFocusUpgradeLevel(ItemStack stack, FocusUpgradeType type) {
      ItemStack focusStack = this.getFocusItem(stack);
      return focusStack.getItem() instanceof ItemFocusBasic fb ? fb.getUpgradeLevel(focusStack, type) : 0;
   }

   public boolean consumeVis(ItemStack is, Player player, Aspect aspect, int amount, boolean crafting) {
      amount = (int)(amount * this.getConsumptionModifier(is, player, aspect, crafting));
      if (this.getVis(is, aspect) >= amount) {
         if (!player.level().isClientSide) {
            this.storeVis(is, aspect, this.getVis(is, aspect) - amount);
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean consumeAllVisCrafting(ItemStack is, Player player, AspectList aspects, boolean doit) {
      if (aspects != null && aspects.size() != 0) {
         AspectList scaled = new AspectList();

         for (Aspect a : aspects.getAspects()) {
            scaled.add(a, aspects.getAmount(a) * 100);
         }

         return this.consumeAllVis(is, player, scaled, doit, true);
      } else {
         return false;
      }
   }

   public boolean consumeAllVis(ItemStack is, Player player, AspectList aspects, boolean doit, boolean crafting) {
      // Креатив не платит вис (как в оригинале) — и проверка, и списание.
      if (player != null && player.getAbilities().instabuild) {
         return true;
      }

      if (aspects != null && aspects.size() != 0) {
         AspectList nl = new AspectList();

         for (Aspect aspect : aspects.getAspects()) {
            int cost = aspects.getAmount(aspect);
            cost = (int)(cost * this.getConsumptionModifier(is, player, aspect, crafting));
            nl.add(aspect, cost);
         }

         for (Aspect aspect : nl.getAspects()) {
            if (this.getVis(is, aspect) < nl.getAmount(aspect)) {
               return false;
            }
         }

         if (doit) {
            for (Aspect aspectx : nl.getAspects()) {
               this.storeVis(is, aspectx, this.getVis(is, aspectx) - nl.getAmount(aspectx));
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public int addVis(ItemStack is, Aspect aspect, int amount, boolean doit) {
      if (aspect != null && aspect.isPrimal()) {
         int storeAmount = this.getVis(is, aspect) + amount * 100;
         int leftover = Math.max(storeAmount - this.getMaxVis(is), 0);
         if (doit) {
            this.storeVis(is, aspect, Math.min(storeAmount, this.getMaxVis(is)));
         }

         return leftover / 100;
      } else {
         return 0;
      }
   }

   public int addRealVis(ItemStack is, Aspect aspect, int amount, boolean doit) {
      if (aspect != null && aspect.isPrimal()) {
         int storeAmount = this.getVis(is, aspect) + amount;
         int leftover = Math.max(storeAmount - this.getMaxVis(is), 0);
         if (doit) {
            this.storeVis(is, aspect, Math.min(storeAmount, this.getMaxVis(is)));
         }

         return leftover;
      } else {
         return 0;
      }
   }

   public ItemFocusBasic getFocus(ItemStack stack) {
      // Called every frame by renderers and cost modifiers: read only the item id, do not decode the stack.
      CompoundTag compound = ItemStackData.getTag(stack);
      Item focus = compound != null && compound.contains("focus", 10) ? ItemStackData.peekItem(compound.getCompound("focus")) : null;
      return focus instanceof ItemFocusBasic fb ? fb : null;
   }

   @NotNull
   public ItemStack getFocusItem(ItemStack stack) {
      CompoundTag compound = ItemStackData.getTag(stack);
      if (compound != null && compound.contains("focus", 10)) {
         CompoundTag nbt = compound.getCompound("focus");
         ItemStack focusStack = ItemStackData.loadStack(nbt);
         if (!focusStack.isEmpty() && focusStack.getItem() instanceof ItemFocusBasic) {
            return focusStack;
         } else {
            compound.remove("focus");
            return ItemStack.EMPTY;
         }
      } else {
         return ItemStack.EMPTY;
      }
   }

   public void setFocus(ItemStack stack, @Nullable ItemStack focus) {
      if (focus != null && !focus.isEmpty()) {
         ItemStackData.addTagElement(stack, "focus", ItemStackData.saveStack(focus));
      } else {
         ItemStackData.removeTagKey(stack, "focus");
      }
   }

   @NotNull
   public ItemWandRod getRod(ItemStack stack) {
      CompoundTag compound = ItemStackData.getTag(stack);
      return compound != null && compound.contains("rod", 8) ? ItemWandRod.get(compound.getString("rod")) : (ItemWandRod)ModItems.WOOD_WAND_ROD.get();
   }

   public void setRod(ItemStack stack, ItemWandRod rod) {
      ItemStackData.addTagElement(stack, "rod", StringTag.valueOf(rod.getTag()));
      if (rod.isStaff()) {
         AttributeModifier am = new AttributeModifier(ResourceLocation.fromNamespaceAndPath("thaumcraft", "wand_attack_damage"), 6.0, Operation.ADD_VALUE);
         CompoundTag tag = am.save();
         tag.putString("AttributeName", BuiltInRegistries.ATTRIBUTE.getKey((Attribute)Attributes.ATTACK_DAMAGE.value()).toString());
         tag.putString("Slot", EquipmentSlot.MAINHAND.getName());
         ListTag list = new ListTag();
         list.add(tag);
         ItemStackData.addTagElement(stack, "AttributeModifiers", list);
      }
   }

   @NotNull
   public ItemWandCap getCap(ItemStack stack) {
      CompoundTag compound = ItemStackData.getTag(stack);
      return compound != null && compound.contains("cap", 8) ? ItemWandCap.get(compound.getString("cap")) : (ItemWandCap)ModItems.IRON_WAND_CAP.get();
   }

   public void setCap(ItemStack stack, ItemWandCap cap) {
      ItemStackData.addTagElement(stack, "cap", StringTag.valueOf(cap.getTag()));
   }

   public boolean isStaff(ItemStack stack) {
      return this.getRod(stack).isStaff();
   }

   public boolean isSceptre(ItemStack stack) {
      CompoundTag tag = ItemStackData.getTag(stack);
      return tag != null && tag.contains("sceptre");
   }

   public boolean hasRunes(ItemStack stack) {
      return this.getRod(stack).isRunes();
   }

   public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
      return 72000;
   }

   @NotNull
   public UseAnim getUseAnimation(@NotNull ItemStack stack) {
      return UseAnim.BOW;
   }

   public void onUseTick(@NotNull Level level, @NotNull LivingEntity entity, @NotNull ItemStack stack, int remainingUseTicks) {
      if (entity instanceof Player player) {
         IWandable tv = this.getObjectInUse(stack, level);
         if (tv != null) {
            this.animation = ItemFocusBasic.WandFocusAnimation.WAVE;
            tv.onUsingWandTick(stack, player, remainingUseTicks);
         } else {
            ItemFocusBasic focus = this.getFocus(stack);
            if (focus != null) {
               focus.onUsingFocusTick(stack, player, remainingUseTicks);
            }
         }
      }
   }

   public void onStopUsing(ItemStack stack, LivingEntity entity, int remainingUseTicks) {
      if (entity instanceof Player player) {
         Level level = player.level();
         IWandable tv = this.getObjectInUse(stack, level);
         if (tv != null) {
            tv.onWandStoppedUsing(stack, level, player, remainingUseTicks);
            this.animation = null;
         } else {
            ItemFocusBasic focus = this.getFocus(stack);
            if (focus != null) {
               focus.onPlayerStoppedUsingFocus(stack, level, player, remainingUseTicks);
            }

            this.animation = null;
         }

         this.clearObjectInUse(stack);
      }
   }

   public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
      Level level = context.getLevel();
      Player player = context.getPlayer();
      if (player == null) {
         return InteractionResult.PASS;
      } else {
         BlockPos pos = context.getClickedPos();
         BlockState state = level.getBlockState(pos);
         Block block = state.getBlock();
         if (block instanceof IWandable wandable) {
            int ret = wandable.onWandRightClick(level, player, context.getHand(), pos, context.getClickedFace());
            if (ret >= 0) {
               return ret == 1 ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS;
            }
         }

         BlockEntity be = level.getBlockEntity(pos);
         if (be instanceof IWandable wandablex) {
            int ret = wandablex.onWandRightClick(level, player, context.getHand(), pos, context.getClickedFace(), context.getClickLocation());
            if (ret >= 0) {
               return ret == 1 ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS;
            }
         }

         if (WandTriggerRegistry.hasTrigger(block)) {
            return WandTriggerRegistry.performTrigger(level, stack, player, pos, context.getClickedFace(), block);
         } else {
            if (!(be instanceof WardedBlockEntity)
               && be instanceof OwnedBlockEntity owned
               && (!Config.wardedStone || player.getScoreboardName().equals(owned.owner))) {
               if (!level.isClientSide) {
                  owned.safeToRemove = true;
                  level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(block)));
                  level.removeBlock(pos, true);
               } else {
                  player.swing(context.getHand());
               }
            }

            return InteractionResult.PASS;
         }
      }
   }

   @NotNull
   public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
      ItemStack wandStack = player.getItemInHand(hand);
      ItemStack focusStack = this.getFocusItem(wandStack);
      if (focusStack.getItem() instanceof ItemFocusBasic focus && !player.getCooldowns().isOnCooldown(focus)) {
         ItemStack ret = focus.onFocusRightClick(wandStack, level, player, hand);
         if (!ret.isEmpty()) {
            player.getCooldowns().addCooldown(focus, focus.getActivationCooldown(focusStack));
            return InteractionResultHolder.sidedSuccess(ret, level.isClientSide);
         }
      }

      return InteractionResultHolder.pass(wandStack);
   }

   @NotNull
   public InteractionResult interactLivingEntity(
      @NotNull ItemStack wandStack, @NotNull Player player, @NotNull LivingEntity entity, @NotNull InteractionHand hand
   ) {
      ItemStack focusStack = this.getFocusItem(wandStack);
      if (focusStack.getItem() instanceof ItemFocusBasic focus && !player.getCooldowns().isOnCooldown(focus)) {
         ItemStack ret = focus.onFocusEntityClick(wandStack, player.level(), player, hand, entity);
         if (!ret.isEmpty()) {
            player.getCooldowns().addCooldown(focus, focus.getActivationCooldown(focusStack));
            return InteractionResult.sidedSuccess(player.level().isClientSide);
         }
      }

      return InteractionResult.PASS;
   }

   @NotNull
   public InteractionResult useOn(@NotNull UseOnContext context) {
      Player player = context.getPlayer();
      if (player == null) {
         return InteractionResult.PASS;
      } else {
         Level level = context.getLevel();
         BlockPos pos = context.getClickedPos();
         ItemStack wandStack = player.getItemInHand(context.getHand());
         if (level.getBlockState(pos).getBlock() instanceof IWandable wandable) {
            ItemStack ret = wandable.onWandRightClick(level, player, context.getHand());
            if (!ret.isEmpty()) {
               return InteractionResult.CONSUME;
            }
         }

         if (level.getBlockEntity(pos) instanceof IWandable wandablex) {
            ItemStack ret = wandablex.onWandRightClick(level, player, context.getHand());
            if (!ret.isEmpty()) {
               return InteractionResult.CONSUME;
            }
         }

         ItemStack focusStack = this.getFocusItem(wandStack);
         if (focusStack.getItem() instanceof ItemFocusBasic focus && !player.getCooldowns().isOnCooldown(focus)) {
            ItemStack ret = focus.onFocusBlockClick(wandStack, level, player, context.getHand(), pos, context.getClickedFace());
            if (!ret.isEmpty()) {
               player.getCooldowns().addCooldown(focus, focus.getActivationCooldown(focusStack));
               return InteractionResult.sidedSuccess(level.isClientSide);
            }
         }

         return InteractionResult.PASS;
      }
   }

   public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
      if (entity instanceof Player player) {
         ItemStack focusStack = this.getFocusItem(stack);
         if (focusStack.getItem() instanceof ItemFocusBasic focus && !player.getCooldowns().isOnCooldown(focus)) {
            player.getCooldowns().addCooldown(focus, focus.getActivationCooldown(focusStack));
            return focusStack.getItem().onEntitySwing(focusStack, entity);
         }
      }

      return super.onEntitySwing(stack, entity);
   }

   public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
      ItemStack focusStack = this.getFocusItem(stack);
      if (focusStack.getItem() instanceof ItemFocusBasic focus && !player.getCooldowns().isOnCooldown(focus)) {
         player.getCooldowns().addCooldown(focus, focus.getActivationCooldown(focusStack));
         return focus.onFocusBlockStartBreak(stack, pos, player);
      } else {
         return false;
      }
   }

   public boolean canAttackBlock(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player) {
      ItemStack stack = player.getMainHandItem();
      ItemStack focusStack = this.getFocusItem(stack);
      return focusStack.getItem() instanceof ItemFocusBasic focus && focus.canAttackBlock(state, level, pos, player);
   }

   public float getDestroySpeed(@NotNull ItemStack stack, @NotNull BlockState state) {
      ItemStack focusStack = this.getFocusItem(stack);
      return focusStack.getItem() instanceof ItemFocusBasic focus ? focus.getDestroySpeed(stack, state) : super.getDestroySpeed(stack, state);
   }

   public void setObjectInUse(ItemStack stack, BlockPos pos) {
      ItemStackData.updateTag(stack, tag -> {
         tag.putInt("IIUX", pos.getX());
         tag.putInt("IIUY", pos.getY());
         tag.putInt("IIUZ", pos.getZ());
      });
   }

   public void clearObjectInUse(ItemStack stack) {
      ItemStackData.updateTag(stack, tag -> {
         tag.remove("IIUX");
         tag.remove("IIUY");
         tag.remove("IIUZ");
      });
   }

   @Nullable
   public IWandable getObjectInUse(ItemStack stack, Level level) {
      CompoundTag tag = ItemStackData.getTag(stack);
      if (tag != null && tag.contains("IIUX", 3)) {
         BlockPos pos = new BlockPos(tag.getInt("IIUX"), tag.getInt("IIUY"), tag.getInt("IIUZ"));
         if (level.getBlockEntity(pos) instanceof IWandable w) {
            return w;
         }
      }

      return null;
   }

   @Override
   public ArrayList<BlockPos> getArchitectBlocks(ItemStack stack, Level level, BlockPos blockPos, Direction direction, Player player) {
      ItemFocusBasic focus = this.getFocus(stack);
      return focus instanceof IArchitect architect && focus.isUpgradedWith(this.getFocusItem(stack), FocusUpgradeType.architect)
         ? architect.getArchitectBlocks(stack, level, blockPos, direction, player)
         : null;
   }

   @Override
   public boolean showAxis(ItemStack stack, Level level, Player player, Direction direction, IArchitect.EnumAxis axis) {
      ItemFocusBasic focus = this.getFocus(stack);
      return focus instanceof IArchitect architect && focus.isUpgradedWith(this.getFocusItem(stack), FocusUpgradeType.architect)
         ? architect.showAxis(stack, level, player, direction, axis)
         : false;
   }

   @OnlyIn(Dist.CLIENT)
   public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
      consumer.accept(
         new IClientItemExtensions() {
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
               return WandRender.INSTANCE;
            }

            public boolean applyForgeHandTransform(
               PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess
            ) {
               return WandRender.INSTANCE.applyForgeHandTransform(poseStack, player, arm, itemInHand, partialTick, equipProcess, swingProcess);
            }
         }
      );
   }

   public static ItemStack createWand(ItemWandRod rod, ItemWandCap cap) {
      ItemWandCasting casting = (ItemWandCasting)ModItems.WAND_CASTING.get();
      ItemStack stack = new ItemStack(casting);
      casting.setRod(stack, rod);
      casting.setCap(stack, cap);
      return stack;
   }

   public static ItemStack createWandFill(ItemWandRod rod, ItemWandCap cap) {
      ItemStack stack = createWand(rod, cap);
      ItemWandCasting casting = (ItemWandCasting)ModItems.WAND_CASTING.get();

      for (Aspect aspect : Aspect.getPrimalAspects()) {
         casting.addVis(stack, aspect, casting.getMaxVis(stack), true);
      }

      return stack;
   }

   public static ItemStack createSceptre(ItemWandRod rod, ItemWandCap cap) {
      ItemStack stack = createWand(rod, cap);
      ItemStackData.addTagElement(stack, "sceptre", ByteTag.valueOf((byte)1));
      return stack;
   }

   public static ItemStack createSceptreFill(ItemWandRod rod, ItemWandCap cap) {
      ItemStack stack = createSceptre(rod, cap);
      ItemWandCasting casting = (ItemWandCasting)ModItems.WAND_CASTING.get();

      for (Aspect aspect : Aspect.getPrimalAspects()) {
         casting.addVis(stack, aspect, casting.getMaxVis(stack), true);
      }

      return stack;
   }
}
