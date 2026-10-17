package thaumcraft.recipe;

import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.ThrowablePotionItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssenceContainerItem;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.api.crafting.IInfusionModifierRecipe;
import thaumcraft.api.crafting.InfusionEnchantmentRecipe;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.crafting.ShapedArcaneRecipe;
import thaumcraft.api.crafting.ShapelessArcaneRecipe;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.research.PlayerKnowledge;

public class ThaumcraftCraftingManager {
   private static final ArcaneWandRecipe ARCANE_WAND_RECIPE = ArcaneWandRecipe.INSTANCE;
   private static final ArcaneSceptreRecipe ARCANE_SCEPTRE_RECIPE = ArcaneSceptreRecipe.INSTANCE;

   public static CrucibleRecipe findCrucibleRecipe(AspectList aspects, ItemStack lastDrop, @Nullable Player player) {
      return findCrucibleRecipe(aspects, lastDrop, player == null ? null : PlayerKnowledge.of(player).getResearchCompleted());
   }

   /** {@code researchCompleted == null} skips the research check. */
   public static CrucibleRecipe findCrucibleRecipe(AspectList aspects, ItemStack lastDrop, @Nullable Set<String> researchCompleted) {
      int highest = 0;
      CrucibleRecipe findRecipe = null;

      for (RecipeHolder<CrucibleRecipe> holder : ThaumcraftApi.getRecipeManager().getAllRecipesFor(CrucibleRecipe.TYPE)) {
         CrucibleRecipe recipe = (CrucibleRecipe)holder.value();
         if ((researchCompleted == null || recipe.researchKey.isEmpty() || researchCompleted.contains(recipe.researchKey)) && recipe.matches(aspects, lastDrop)
            )
          {
            int result = recipe.aspects.size();
            if (result > highest) {
               highest = result;
               findRecipe = recipe;
            }
         }
      }

      return findRecipe;
   }

   public static InfusionRecipe findInfusionRecipe(List<ItemStack> items, ItemStack input, @Nullable Player player) {
      Set<String> researchCompleted = player == null ? null : PlayerKnowledge.of(player).getResearchCompleted();

      for (RecipeHolder<InfusionRecipe> holder : ThaumcraftApi.getRecipeManager().getAllRecipesFor(InfusionRecipe.TYPE)) {
         InfusionRecipe recipe = (InfusionRecipe)holder.value();
         if ((researchCompleted == null || recipe.researchKey.isEmpty() || researchCompleted.contains(recipe.researchKey)) && recipe.matches(items, input)) {
            return recipe;
         }
      }

      return null;
   }

   /** Поиск рецепта зачарования на алтаре (вторая ветка {@code TileInfusionMatrix#craftingStart}). */
   @Nullable
   public static thaumcraft.api.crafting.InfusionEnchantmentRecipe findInfusionEnchantmentRecipe(
      List<ItemStack> items, ItemStack input, @Nullable Player player
   ) {
      Set<String> researchCompleted = player == null ? null : PlayerKnowledge.of(player).getResearchCompleted();

      for (RecipeHolder<thaumcraft.api.crafting.InfusionEnchantmentRecipe> holder : ThaumcraftApi.getRecipeManager()
         .getAllRecipesFor(thaumcraft.api.crafting.InfusionEnchantmentRecipe.TYPE)) {
         thaumcraft.api.crafting.InfusionEnchantmentRecipe recipe = holder.value();
         if ((researchCompleted == null || recipe.researchKey.isEmpty() || researchCompleted.contains(recipe.researchKey))
            && recipe.matches(items, input)) {
            return recipe;
         }
      }

      return null;
   }

   /**
    * Infusion recipes that modify the catalyst (runic augmentation, fortress helm upgrades). The enchantment
    * recipes share the interface but have their own matrix branch, so they are skipped here.
    */
   @Nullable
   public static RecipeHolder<? extends IInfusionModifierRecipe> findInfusionModifierRecipe(List<ItemStack> items, ItemStack input, @Nullable Player player) {
      Set<String> researchCompleted = player == null ? null : PlayerKnowledge.of(player).getResearchCompleted();
      RecipeManager recipeManager = ThaumcraftApi.getRecipeManager();

      for (RecipeType<? extends IInfusionModifierRecipe> type : ThaumcraftApi.getInfusionModifierRecipeTypes()) {
         for (RecipeHolder<? extends IInfusionModifierRecipe> holder : recipeManager.getAllRecipesFor(type)) {
            IInfusionModifierRecipe recipe = (IInfusionModifierRecipe)holder.value();
            if (!(recipe instanceof InfusionEnchantmentRecipe)
               && (researchCompleted == null || recipe.getResearchKey().isEmpty() || researchCompleted.contains(recipe.getResearchKey()))
               && recipe.matches(items, input)) {
               return holder;
            }
         }
      }

      return null;
   }

   @Nullable
   public static IArcaneRecipe findArcaneRecipe(CraftingContainer container, Level level, @Nullable Player player) {
      if (level == null) {
         return null;
      } else {
         Set<String> researchCompleted = player == null ? null : PlayerKnowledge.of(player).getResearchCompleted();
         CraftingInput input = toInput(container);

         for (RecipeHolder<ShapedArcaneRecipe> holder : level.getRecipeManager().getAllRecipesFor(ShapedArcaneRecipe.TYPE)) {
            ShapedArcaneRecipe recipe = (ShapedArcaneRecipe)holder.value();
            if ((researchCompleted == null || recipe.researchKey.isEmpty() || researchCompleted.contains(recipe.researchKey)) && recipe.matches(input, level)) {
               return recipe;
            }
         }

         for (RecipeHolder<ShapelessArcaneRecipe> holderx : level.getRecipeManager().getAllRecipesFor(ShapelessArcaneRecipe.TYPE)) {
            ShapelessArcaneRecipe recipe = (ShapelessArcaneRecipe)holderx.value();
            if ((researchCompleted == null || recipe.researchKey.isEmpty() || researchCompleted.contains(recipe.researchKey)) && recipe.matches(input, level)) {
               return recipe;
            }
         }

         if (ARCANE_WAND_RECIPE.matches(container, level, player)) {
            return ARCANE_WAND_RECIPE;
         } else {
            return ARCANE_SCEPTRE_RECIPE.matches(container, level, player) ? ARCANE_SCEPTRE_RECIPE : null;
         }
      }
   }

   @NotNull
   public static ItemStack findArcaneRecipeResult(CraftingContainer container, Level level, @Nullable Player player) {
      IArcaneRecipe recipe = findArcaneRecipe(container, level, player);
      return recipe == null ? ItemStack.EMPTY : recipe.getResultItem(container, level.registryAccess()).copy();
   }

   @NotNull
   public static AspectList findArcaneRecipeAspects(CraftingContainer container, Level level, @Nullable Player player) {
      IArcaneRecipe recipe = findArcaneRecipe(container, level, player);
      return recipe == null ? new AspectList() : recipe.getAspects(container);
   }

   @NotNull
   public static AspectList getObjectTags(ItemStack stack) {
      AspectList baseTags = getBaseObjectTags(stack);
      AspectList enrichedTags = applyIntrinsicObjectBonuses(stack, baseTags);
      return capAspects(enrichedTags, 64);
   }

   @NotNull
   private static AspectList getBaseObjectTags(ItemStack stack) {
      AspectList registeredTags = ThaumcraftApi.getObjectTags(stack);
      if (ThaumcraftApi.exists(stack)) {
         return registeredTags;
      } else {
         AspectList generatedTags = ThaumcraftApiHelper.generateTags(stack);
         return generatedTags != null ? generatedTags : registeredTags;
      }
   }

   @NotNull
   private static AspectList applyIntrinsicObjectBonuses(ItemStack stack, AspectList sourceTags) {
      AspectList enrichedTags = sourceTags == null ? new AspectList() : sourceTags.copy();
      if (stack.getItem() instanceof ItemWandCasting wand) {
         enrichedTags.merge(Aspect.MAGIC, (wand.getRod(stack).getCraftCost() + wand.getCap(stack).getCraftCost()) / 2);
         enrichedTags.merge(Aspect.TOOL, (wand.getRod(stack).getCraftCost() + wand.getCap(stack).getCraftCost()) / 3);
      }

      if (stack.getItem() instanceof PotionItem) {
         enrichedTags.merge(Aspect.WATER, 1);
         PotionContents contents = (PotionContents)stack.get(DataComponents.POTION_CONTENTS);
         if (contents != null) {
            for (MobEffectInstance effect : contents.getAllEffects()) {
               if (stack.getItem() instanceof ThrowablePotionItem) {
                  enrichedTags.merge(Aspect.ENTROPY, 2);
               }

               addPotionEffectAspects(enrichedTags, effect);
            }
         }
      }

      return enrichedTags;
   }

   private static AspectList capAspects(AspectList sourcetags, int amount) {
      if (sourcetags == null) {
         return null;
      } else {
         AspectList out = new AspectList();

         for (Aspect aspect : sourcetags.getAspects()) {
            out.merge(aspect, Math.min(amount, sourcetags.getAmount(aspect)));
         }

         return out;
      }
   }

   @NotNull
   public static AspectList getBonusTags(ItemStack stack, AspectList sourceList) {
      AspectList bonusTags = new AspectList();
      Item item = stack.getItem();
      if (item == null) {
         return new AspectList();
      } else {
         if (item instanceof IEssenceContainerItem) {
            bonusTags = ((IEssenceContainerItem)item).getAspects(stack);
            if (bonusTags != null && bonusTags.size() > 0) {
               for (Aspect tag : bonusTags.copy().getAspects()) {
                  if (bonusTags.getAmount(tag) <= 0) {
                     bonusTags.remove(tag);
                  }
               }
            }
         }

         if (bonusTags == null) {
            bonusTags = new AspectList();
         }

         if (sourceList != null) {
            for (Aspect tagx : sourceList.getAspects()) {
               if (tagx != null) {
                  bonusTags.add(tagx, sourceList.getAmount(tagx));
               }
            }
         }

         if (item instanceof ArmorItem armorItem) {
            bonusTags.merge(Aspect.ARMOR, ((ArmorMaterial)armorItem.getMaterial().value()).getDefense(armorItem.getType()));
         } else if (item instanceof SwordItem swordItem && swordItem.getDamage(stack) + 1.0F > 0.0F) {
            bonusTags.merge(Aspect.WEAPON, (int)(swordItem.getDamage(stack) + 1.0F));
         } else if (item instanceof BowItem) {
            bonusTags.merge(Aspect.WEAPON, 3).merge(Aspect.FLIGHT, 1);
         } else if (item instanceof PickaxeItem pickaxeItem) {
            bonusTags.merge(Aspect.MINE, tierLevel(pickaxeItem.getTier()) + 1);
         } else if (item instanceof ShearsItem || item instanceof HoeItem) {
            if (item.getMaxDamage(stack) <= Tiers.WOOD.getUses()) {
               bonusTags.merge(Aspect.HARVEST, 1);
            } else if (item.getMaxDamage(stack) <= Tiers.STONE.getUses() || item.getMaxDamage(stack) <= Tiers.GOLD.getUses()) {
               bonusTags.merge(Aspect.HARVEST, 2);
            } else if (item.getMaxDamage(stack) <= Tiers.IRON.getUses()) {
               bonusTags.merge(Aspect.HARVEST, 3);
            } else {
               bonusTags.merge(Aspect.HARVEST, 4);
            }
         } else if (item instanceof DiggerItem diggerItem) {
            bonusTags.merge(Aspect.TOOL, tierLevel(diggerItem.getTier()) + 1);
         }

         int totalEnchantmentLevels = 0;

         for (Entry<Holder<Enchantment>> entry : stack.getEnchantments().entrySet()) {
            int lvl = entry.getIntValue();
            Holder<Enchantment> enchantment = (Holder<Enchantment>)entry.getKey();
            if (enchantment.is(Enchantments.AQUA_AFFINITY)) {
               bonusTags.merge(Aspect.WATER, lvl);
            } else if (enchantment.is(Enchantments.BANE_OF_ARTHROPODS)) {
               bonusTags.merge(Aspect.BEAST, lvl);
            } else if (enchantment.is(Enchantments.BLAST_PROTECTION)) {
               bonusTags.merge(Aspect.ARMOR, lvl);
            } else if (enchantment.is(Enchantments.EFFICIENCY)) {
               bonusTags.merge(Aspect.TOOL, lvl);
            } else if (enchantment.is(Enchantments.FEATHER_FALLING)) {
               bonusTags.merge(Aspect.FLIGHT, lvl);
            } else if (enchantment.is(Enchantments.FIRE_ASPECT)) {
               bonusTags.merge(Aspect.FIRE, lvl);
            } else if (enchantment.is(Enchantments.FIRE_PROTECTION)) {
               bonusTags.merge(Aspect.ARMOR, lvl);
            } else if (enchantment.is(Enchantments.FLAME)) {
               bonusTags.merge(Aspect.FIRE, lvl);
            } else if (enchantment.is(Enchantments.FORTUNE)) {
               bonusTags.merge(Aspect.GREED, lvl);
            } else if (enchantment.is(Enchantments.INFINITY)) {
               bonusTags.merge(Aspect.CRAFT, lvl);
            } else if (enchantment.is(Enchantments.KNOCKBACK)) {
               bonusTags.merge(Aspect.AIR, lvl);
            } else if (enchantment.is(Enchantments.LOOTING)) {
               bonusTags.merge(Aspect.GREED, lvl);
            } else if (enchantment.is(Enchantments.POWER)) {
               bonusTags.merge(Aspect.WEAPON, lvl);
            } else if (enchantment.is(Enchantments.PROJECTILE_PROTECTION)) {
               bonusTags.merge(Aspect.ARMOR, lvl);
            } else if (enchantment.is(Enchantments.PROTECTION)) {
               bonusTags.merge(Aspect.ARMOR, lvl);
            } else if (enchantment.is(Enchantments.PUNCH)) {
               bonusTags.merge(Aspect.AIR, lvl);
            } else if (enchantment.is(Enchantments.RESPIRATION)) {
               bonusTags.merge(Aspect.AIR, lvl);
            } else if (enchantment.is(Enchantments.SHARPNESS)) {
               bonusTags.merge(Aspect.WEAPON, lvl);
            } else if (enchantment.is(Enchantments.SILK_TOUCH)) {
               bonusTags.merge(Aspect.EXCHANGE, lvl);
            } else if (enchantment.is(Enchantments.THORNS)) {
               bonusTags.merge(Aspect.WEAPON, lvl);
            } else if (enchantment.is(Enchantments.SMITE)) {
               bonusTags.merge(Aspect.ENTROPY, lvl);
            } else if (enchantment.is(Enchantments.UNBREAKING)) {
               bonusTags.merge(Aspect.EARTH, lvl);
            }

            totalEnchantmentLevels += lvl;
         }

         if (totalEnchantmentLevels > 0) {
            bonusTags.merge(Aspect.MAGIC, totalEnchantmentLevels);
         }

         return ThaumcraftApiHelper.cullTags(bonusTags);
      }
   }

   private static CraftingInput toInput(CraftingContainer container) {
      NonNullList<ItemStack> grid = NonNullList.withSize(9, ItemStack.EMPTY);

      for (int i = 0; i < Math.min(9, container.getContainerSize()); i++) {
         grid.set(i, container.getItem(i));
      }

      return CraftingInput.of(3, 3, grid);
   }

   private static int tierLevel(Tier tier) {
      int uses = tier.getUses();
      if (uses >= Tiers.NETHERITE.getUses()) {
         return 4;
      } else if (uses >= Tiers.DIAMOND.getUses()) {
         return 3;
      } else if (uses >= Tiers.IRON.getUses()) {
         return 2;
      } else {
         return uses >= Tiers.STONE.getUses() ? 1 : 0;
      }
   }

   private static void addPotionEffectAspects(AspectList enrichedTags, MobEffectInstance effect) {
      enrichedTags.merge(Aspect.MAGIC, (effect.getAmplifier() + 1) * 2);
      if (effect.getEffect() == MobEffects.BLINDNESS) {
         enrichedTags.merge(Aspect.DARKNESS, (effect.getAmplifier() + 1) * 3);
      } else if (effect.getEffect() == MobEffects.CONFUSION) {
         enrichedTags.merge(Aspect.ELDRITCH, (effect.getAmplifier() + 1) * 3);
      } else if (effect.getEffect() == MobEffects.DAMAGE_BOOST) {
         enrichedTags.merge(Aspect.WEAPON, (effect.getAmplifier() + 1) * 3);
      } else if (effect.getEffect() == MobEffects.DIG_SLOWDOWN) {
         enrichedTags.merge(Aspect.TRAP, (effect.getAmplifier() + 1) * 3);
      } else if (effect.getEffect() == MobEffects.DIG_SPEED) {
         enrichedTags.merge(Aspect.TOOL, (effect.getAmplifier() + 1) * 3);
      } else if (effect.getEffect() == MobEffects.FIRE_RESISTANCE) {
         enrichedTags.merge(Aspect.ARMOR, effect.getAmplifier() + 1);
         enrichedTags.merge(Aspect.FIRE, (effect.getAmplifier() + 1) * 2);
      } else if (effect.getEffect() == MobEffects.HARM) {
         enrichedTags.merge(Aspect.DEATH, (effect.getAmplifier() + 1) * 3);
      } else if (effect.getEffect() == MobEffects.HEAL) {
         enrichedTags.merge(Aspect.HEAL, (effect.getAmplifier() + 1) * 3);
      } else if (effect.getEffect() == MobEffects.HUNGER) {
         enrichedTags.merge(Aspect.DEATH, (effect.getAmplifier() + 1) * 3);
      } else if (effect.getEffect() == MobEffects.INVISIBILITY) {
         enrichedTags.merge(Aspect.SENSES, (effect.getAmplifier() + 1) * 3);
      } else if (effect.getEffect() == MobEffects.JUMP) {
         enrichedTags.merge(Aspect.FLIGHT, (effect.getAmplifier() + 1) * 3);
      } else if (effect.getEffect() == MobEffects.MOVEMENT_SLOWDOWN) {
         enrichedTags.merge(Aspect.TRAP, (effect.getAmplifier() + 1) * 3);
      } else if (effect.getEffect() == MobEffects.MOVEMENT_SPEED) {
         enrichedTags.merge(Aspect.MOTION, (effect.getAmplifier() + 1) * 3);
      } else if (effect.getEffect() == MobEffects.NIGHT_VISION) {
         enrichedTags.merge(Aspect.SENSES, (effect.getAmplifier() + 1) * 3);
      } else if (effect.getEffect() == MobEffects.POISON) {
         enrichedTags.merge(Aspect.POISON, (effect.getAmplifier() + 1) * 3);
      } else if (effect.getEffect() == MobEffects.REGENERATION) {
         enrichedTags.merge(Aspect.HEAL, (effect.getAmplifier() + 1) * 3);
      } else if (effect.getEffect() == MobEffects.DAMAGE_RESISTANCE) {
         enrichedTags.merge(Aspect.ARMOR, (effect.getAmplifier() + 1) * 3);
      } else if (effect.getEffect() == MobEffects.WATER_BREATHING) {
         enrichedTags.merge(Aspect.AIR, (effect.getAmplifier() + 1) * 3);
      } else if (effect.getEffect() == MobEffects.WEAKNESS) {
         enrichedTags.merge(Aspect.DEATH, (effect.getAmplifier() + 1) * 3);
      }
   }

   @Nullable
   public static AspectList generateTags(ItemStack stack) {
      return ThaumcraftApiHelper.generateTags(stack);
   }

   @Nullable
   public static AspectList generateTags(ItemStack stack, ArrayList<ItemStack> history) {
      return ThaumcraftApiHelper.generateTags(stack);
   }
}
