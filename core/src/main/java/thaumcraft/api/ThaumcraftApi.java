package thaumcraft.api;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.CommonSetup;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.obj.BlockAspect;
import thaumcraft.api.aspects.obj.ComplexObjectAspect;
import thaumcraft.api.aspects.obj.DerivedObjectAspect;
import thaumcraft.api.aspects.obj.DirectBlockAspect;
import thaumcraft.api.aspects.obj.EntityAspect;
import thaumcraft.api.aspects.obj.IngredientAspect;
import thaumcraft.api.aspects.obj.ItemStackAspect;
import thaumcraft.api.aspects.obj.ObjectAspect;
import thaumcraft.api.aspects.obj.ObjectAspectLookupStage;
import thaumcraft.api.aspects.obj.TagAspect;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.api.crafting.IInfusionModifierRecipe;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.crafting.ShapedArcaneRecipe;
import thaumcraft.api.crafting.ShapelessArcaneRecipe;
import thaumcraft.api.internal.WeightedRandomLoot;
import thaumcraft.api.material.ArmorMaterialImpl;
import thaumcraft.api.material.ToolMaterial;
import thaumcraft.api.research.IScanEventHandler;
import thaumcraft.api.research.ResearchApi;
import thaumcraft.api.research.ResearchCategoryList;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.api.research.ResearchPage;
import thaumcraft.api.research.page.ArcaneRecipePage;
import thaumcraft.api.research.page.ArrayCraftPage;
import thaumcraft.api.research.page.CraftingRecipePage;
import thaumcraft.api.research.page.CrucibleRecipePage;
import thaumcraft.api.research.page.FortressArmorUpgradeRecipePage;
import thaumcraft.api.research.page.InfusionEnchantmentRecipePage;
import thaumcraft.api.research.page.InfusionRecipePage;
import thaumcraft.api.research.page.RunicAugmentRecipePage;
import thaumcraft.api.research.page.SmeltingRecipePage;
import thaumcraft.init.registry.ModItems;
import thaumcraft.network.PacketHandler;
import thaumcraft.network.fx.PacketFXBlockBubble;
import thaumcraft.network.fx.PacketFXBlockSparkle;
import thaumcraft.research.PlayerKnowledge;
import thaumcraft.util.ItemStackData;

public class ThaumcraftApi {
   public static final int WILDCARD_VALUE = 32767;
   public static final ToolMaterial TOOL_MATERIAL_THAUMIUM = new ToolMaterial(
      3, 400, 7.0F, 2.0F, 22, () -> Ingredient.of(new ItemLike[]{(ItemLike)ModItems.THAUMIUM_INGOT.get()})
   );
   public static final ToolMaterial TOOL_MATERIAL_VOID = new ToolMaterial(
      4, 150, 8.0F, 3.0F, 10, () -> Ingredient.of(new ItemLike[]{(ItemLike)ModItems.VOID_METAL_INGOT.get()})
   );
   public static final ToolMaterial TOOL_MATERIAL_ELEMENTAL = new ToolMaterial(
      3, 1500, 10.0F, 3.0F, 18, () -> Ingredient.of(new ItemLike[]{(ItemLike)ModItems.VOID_METAL_INGOT.get()})
   );
   public static final Holder<ArmorMaterial> ARMOR_MATERIAL_THAUMIUM = Holder.direct(
      ArmorMaterialImpl.create("THAUMIUM", 25, (EnumMap<Type, Integer>)Util.make(new EnumMap(Type.class), p_266655_ -> {
         p_266655_.put(Type.BOOTS, 2);
         p_266655_.put(Type.LEGGINGS, 5);
         p_266655_.put(Type.CHESTPLATE, 6);
         p_266655_.put(Type.HELMET, 2);
      }), 25, 0.0F, 0.0F, () -> Ingredient.of(new ItemLike[]{(ItemLike)ModItems.THAUMIUM_INGOT.get()}))
   );
   public static final Holder<ArmorMaterial> ARMOR_MATERIAL_SPECIAL = Holder.direct(
      ArmorMaterialImpl.create("SPECIAL", 25, (EnumMap<Type, Integer>)Util.make(new EnumMap(Type.class), p_266655_ -> {
         p_266655_.put(Type.BOOTS, 1);
         p_266655_.put(Type.LEGGINGS, 2);
         p_266655_.put(Type.CHESTPLATE, 3);
         p_266655_.put(Type.HELMET, 1);
      }), 25, 0.0F, 0.0F, () -> Ingredient.of(new ItemLike[]{(ItemLike)ModItems.CLOTH.get()}))
   );
   public static final Holder<ArmorMaterial> ARMOR_MATERIAL_THAUMIUM_FORTRESS = Holder.direct(
      ArmorMaterialImpl.create("FORTRESS", 40, (EnumMap<Type, Integer>)Util.make(new EnumMap(Type.class), p_266655_ -> {
         p_266655_.put(Type.BOOTS, 3);
         p_266655_.put(Type.LEGGINGS, 6);
         p_266655_.put(Type.CHESTPLATE, 7);
         p_266655_.put(Type.HELMET, 3);
      }), 25, 0.0F, 0.0F, null)
   );
   public static final Holder<ArmorMaterial> ARMOR_MATERIAL_VOID = Holder.direct(
      ArmorMaterialImpl.create("VOID", 10, (EnumMap<Type, Integer>)Util.make(new EnumMap(Type.class), p_266655_ -> {
         p_266655_.put(Type.BOOTS, 3);
         p_266655_.put(Type.LEGGINGS, 6);
         p_266655_.put(Type.CHESTPLATE, 7);
         p_266655_.put(Type.HELMET, 3);
      }), 10, 0.0F, 0.0F, () -> Ingredient.of(new ItemLike[]{(ItemLike)ModItems.VOID_METAL_INGOT.get()}))
   );
   public static final ToolMaterial TOOL_MATERIAL_ICHOR = new ToolMaterial(
      5, 0, 12.0F, 5.0F, 20, () -> Ingredient.of(new ItemLike[]{(ItemLike)ModItems.ICHORIUM.get()})
   );
   public static ArrayList<Block> portableHoleBlackList = new ArrayList<>();
   public static ArrayList<IScanEventHandler> scanEventhandlers = new ArrayList<>();
   /** Smelted item or item tag -> bonus item or item tag (a tag resolves to its first item, like TC4's ore dictionary). */
   private static final HashMap<Object, Object> smeltingBonus = new HashMap<>();
   private static final List<ObjectAspect> itemAspects = new ArrayList<>();
   private static final List<BlockAspect> blockAspects = new ArrayList<>();
   private static final List<EntityAspect> entityAspects = new ArrayList<>();
   private static final List<ObjectAspectLookupStage> OBJECT_LOOKUP_ORDER = List.of(
      ObjectAspectLookupStage.EXACT_COMPAT, ObjectAspectLookupStage.META_GROUP_COMPAT, ObjectAspectLookupStage.WILDCARD_COMPAT, ObjectAspectLookupStage.GENERIC
   );
   private static final HashMap<Object, Integer> warpMap = new HashMap<>();
   private static final List<RecipeType<? extends IInfusionModifierRecipe>> infusionModifierRecipeTypes = new ArrayList<>();
   private static CommonSetup setup = new CommonSetup();

   public static CommonSetup getSetup() {
      return setup;
   }

   public static void blockSparkle(Level world, int x, int y, int z, int colorRGB, int count) {
      if (world != null) {
         if (world.isClientSide) {
            setup.blockSparkle(world, x, y, z, colorRGB, count);
         } else {
            if (ServerLifecycleHooks.getCurrentServer() != null) {
               PacketHandler.sendToAllAround(new PacketFXBlockSparkle(new BlockPos(x, y, z), colorRGB), world, x + 0.5, y + 0.5, z + 0.5, 32.0);
            }
         }
      }
   }

   public static void blockBubble(Level world, int x, int y, int z, int colorRGB) {
      if (world != null) {
         if (!world.isClientSide) {
            if (ServerLifecycleHooks.getCurrentServer() != null) {
               PacketHandler.sendToAllAround(new PacketFXBlockBubble(new BlockPos(x, y, z), colorRGB), world, x + 0.5, y + 0.5, z + 0.5, 32.0);
            }
         } else {
            RandomSource random = world.random;
            Color color = new Color(colorRGB);
            float r = color.getRed() / 255.0F;
            float g = color.getGreen() / 255.0F;
            float b = color.getBlue() / 255.0F;

            for (int i = 0; i < getSetup().particleCount(1); i++) {
               getSetup().crucibleBubble(world, x, y + random.nextFloat(), z + random.nextFloat(), r, g, b);
               getSetup().crucibleBubble(world, x + 1, y + random.nextFloat(), z + random.nextFloat(), r, g, b);
               getSetup().crucibleBubble(world, x + random.nextFloat(), y + random.nextFloat(), z, r, g, b);
               getSetup().crucibleBubble(world, x + random.nextFloat(), y + random.nextFloat(), z + 1, r, g, b);
            }
         }
      }
   }

   public static void setSetup(CommonSetup setup) {
      ThaumcraftApi.setup = Objects.requireNonNull(setup);
   }

   public static void registerScanEventhandler(IScanEventHandler scanEventHandler) {
      scanEventhandlers.add(scanEventHandler);
   }

   public static void registerAspectTag(ObjectAspect objectAspect) {
      registerAspectTag(objectAspect, false);
   }

   public static void registerAspectTag(ObjectAspect objectAspect, boolean prepend) {
      registerAspectTag(objectAspect, prepend, false);
   }

   private static void registerAspectTag(ObjectAspect objectAspect, boolean prepend, boolean replaceEqual) {
      synchronized (itemAspects) {
         if (replaceEqual) {
            itemAspects.removeIf(existing -> existing.equals(objectAspect));
         }

         if (prepend) {
            itemAspects.add(0, objectAspect);
         } else {
            itemAspects.add(objectAspect);
         }
      }
   }

   public static void unregisterAspectTags(Collection<? extends ObjectAspect> objectAspects) {
      if (objectAspects != null && !objectAspects.isEmpty()) {
         synchronized (itemAspects) {
            itemAspects.removeAll(objectAspects);
         }
      }
   }

   private static void registerBlockAspectTag(BlockAspect blockAspect, boolean prepend, boolean replaceEqual) {
      synchronized (blockAspects) {
         if (replaceEqual) {
            blockAspects.removeIf(existing -> existing.equals(blockAspect));
         }

         if (prepend) {
            blockAspects.add(0, blockAspect);
         } else {
            blockAspects.add(blockAspect);
         }
      }
   }

   public static void unregisterBlockAspectTags(Collection<? extends BlockAspect> aspects) {
      if (aspects != null && !aspects.isEmpty()) {
         synchronized (blockAspects) {
            blockAspects.removeAll(aspects);
         }
      }
   }

   public static void registerEntityTag(EntityType<? extends Entity> entityType, AspectList aspects) {
      registerEntityTag(entityType, aspects, null, null);
   }

   public static void registerEntityTag(
      EntityType<? extends Entity> entityType, AspectList aspects, @Nullable Predicate<Entity> predicate, @Nullable String hashKey
   ) {
      registerEntityTag(new EntityAspect(entityType, aspects, predicate, hashKey), false);
   }

   public static void registerEntityTag(EntityAspect entityAspect, boolean prepend) {
      synchronized (entityAspects) {
         if (prepend) {
            entityAspects.add(0, entityAspect);
         } else {
            entityAspects.add(entityAspect);
         }
      }
   }

   public static void unregisterEntityTags(Collection<EntityAspect> aspects) {
      if (aspects != null && !aspects.isEmpty()) {
         synchronized (entityAspects) {
            entityAspects.removeAll(aspects);
         }
      }
   }

   /** TC4 infernal furnace bonus: smelting {@code in} may also give some {@code out} (the furnace rolls the count). */
   public static void addSmeltingBonus(ItemLike in, ItemLike out) {
      smeltingBonus.put(in.asItem(), out.asItem());
   }

   public static void addSmeltingBonus(ItemLike in, TagKey<Item> out) {
      smeltingBonus.put(in.asItem(), out);
   }

   public static void addSmeltingBonus(TagKey<Item> in, ItemLike out) {
      smeltingBonus.put(in, out.asItem());
   }

   public static void addSmeltingBonus(TagKey<Item> in, TagKey<Item> out) {
      smeltingBonus.put(in, out);
   }

   /** One of the bonus item for {@code in} (the item first, then its tags), or EMPTY. TC4 kept a count-0 template, which 1.21 treats as empty. */
   @SuppressWarnings("unchecked")
   public static ItemStack getSmeltingBonus(ItemStack in) {
      Object out = smeltingBonus.get(in.getItem());
      if (out == null) {
         out = in.getTags().map(smeltingBonus::get).filter(Objects::nonNull).findFirst().orElse(null);
      }

      if (out instanceof Item item) {
         return new ItemStack(item);
      }

      if (out instanceof TagKey<?> tag) {
         return BuiltInRegistries.ITEM.getTag((TagKey<Item>)tag).flatMap(set -> set.stream().findFirst()).map(ItemStack::new).orElse(ItemStack.EMPTY);
      }

      return ItemStack.EMPTY;
   }

   public static InfusionRecipe getInfusionRecipe(ItemStack stack) {
      for (RecipeHolder<InfusionRecipe> holder : getRecipeManager().getAllRecipesFor(InfusionRecipe.TYPE)) {
         InfusionRecipe r = (InfusionRecipe)holder.value();
         if (matchesRecipeResult(r.result, stack)) {
            return r;
         }
      }

      return null;
   }

   public static CrucibleRecipe getCrucibleRecipe(ItemStack stack) {
      for (RecipeHolder<CrucibleRecipe> holder : getRecipeManager().getAllRecipesFor(CrucibleRecipe.TYPE)) {
         CrucibleRecipe r = (CrucibleRecipe)holder.value();
         if (matchesRecipeResult(r.result, stack)) {
            return r;
         }
      }

      return null;
   }

   public static CrucibleRecipe getCrucibleRecipeFromInput(ItemStack stack) {
      for (RecipeHolder<CrucibleRecipe> holder : getRecipeManager().getAllRecipesFor(CrucibleRecipe.TYPE)) {
         CrucibleRecipe r = (CrucibleRecipe)holder.value();
         if (r.catalystMatches(stack)) {
            return r;
         }
      }

      return null;
   }

   public static CrucibleRecipe getCrucibleRecipeFromId(ResourceLocation id) {
      return getRecipe(CrucibleRecipe.TYPE, id);
   }

   public static IArcaneRecipe getArcaneRecipe(ItemStack stack) {
      for (RecipeHolder<ShapedArcaneRecipe> holder : getRecipeManager().getAllRecipesFor(ShapedArcaneRecipe.TYPE)) {
         ShapedArcaneRecipe r = (ShapedArcaneRecipe)holder.value();
         if (matchesRecipeResult(r.getResultItem(), stack)) {
            return r;
         }
      }

      for (RecipeHolder<ShapelessArcaneRecipe> holderx : getRecipeManager().getAllRecipesFor(ShapelessArcaneRecipe.TYPE)) {
         ShapelessArcaneRecipe r = (ShapelessArcaneRecipe)holderx.value();
         if (matchesRecipeResult(r.getResultItem(), stack)) {
            return r;
         }
      }

      return null;
   }

   public static Object[] getCraftingRecipeKey(Player player, ItemStack stack) {
      if (stack == null) {
         return null;
      } else {
         Set<String> researchCompleted = PlayerKnowledge.of(player).getResearchCompleted();

         for (ResearchCategoryList rcl : ResearchApi.getResearchLists().values()) {
            for (ResearchItem ri : rcl.research.values()) {
               if (ri.getPages() != null) {
                  for (int a = 0; a < ri.getPages().length; a++) {
                     if (matchesResearchPageOutput(ri.getPages()[a], stack) && researchCompleted.contains(ri.key)) {
                        return new Object[]{ri.key, a};
                     }
                  }
               }
            }
         }

         return null;
      }
   }

   private static boolean matchesResearchPageOutput(ResearchPage page, ItemStack stack) {
      if (page == null) {
         return false;
      } else if (page instanceof ArrayCraftPage arrayPage) {
         for (ResearchPage nested : arrayPage.getPages()) {
            if (matchesResearchPageOutput(nested, stack)) {
               return true;
            }
         }

         return false;
      } else {
         Recipe<?> recipe = null;
         if (page instanceof CraftingRecipePage craftingPage) {
            recipe = craftingPage.getRecipe();
         } else if (page instanceof SmeltingRecipePage smeltingPage) {
            recipe = smeltingPage.getRecipe();
         } else if (page instanceof ArcaneRecipePage arcanePage) {
            recipe = arcanePage.getRecipe();
         } else if (page instanceof CrucibleRecipePage cruciblePage) {
            recipe = cruciblePage.getRecipe();
         } else if (page instanceof InfusionRecipePage infusionPage) {
            recipe = infusionPage.getRecipe();
         } else if (page instanceof InfusionEnchantmentRecipePage enchantmentPage) {
            recipe = enchantmentPage.getRecipe();
         } else {
            if (page instanceof FortressArmorUpgradeRecipePage fortressPage) {
               ItemStack display = fortressPage.getDisplayResult();
               return !display.isEmpty() && ItemStackData.sameItemSameTags(display, stack);
            }

            if (page instanceof RunicAugmentRecipePage runicAugmentPage) {
               ItemStack display = runicAugmentPage.getDisplayResult();
               return !display.isEmpty() && ItemStackData.sameItemSameTags(display, stack);
            }
         }

         return recipe != null && ItemStackData.sameItemSameTags(recipe.getResultItem(RegistryAccess.EMPTY), stack);
      }
   }

   private static boolean matchesRecipeResult(ItemStack recipeResult, ItemStack stack) {
      if (!ItemStack.isSameItem(recipeResult, stack)) {
         return false;
      } else {
         return recipeResult.is((Item)ModItems.MOB_ASPECT.get()) ? ItemStackData.sameItemSameTags(recipeResult, stack) : true;
      }
   }

   public static boolean exists(ItemStack item) {
      return exists(item, null);
   }

   static boolean exists(ItemStack item, @Nullable ObjectAspect excludedAspect) {
      return findObjectAspect(item, excludedAspect) != null;
   }

   public static boolean exists(Entity entity) {
      return findEntityAspect(entity) != null;
   }

   public static boolean exists(BlockState state) {
      return findBlockAspect(state) != null;
   }

   public static AspectList getObjectTags(ItemStack stack) {
      return getObjectTags(stack, null);
   }

   static AspectList getObjectTags(ItemStack stack, @Nullable ObjectAspect excludedAspect) {
      ObjectAspect objectAspect = findObjectAspect(stack, excludedAspect);
      return objectAspect != null ? objectAspect.getAspects(stack) : new AspectList();
   }

   public static AspectList getBlockTags(BlockState state) {
      BlockAspect blockAspect = findBlockAspect(state);
      return blockAspect != null ? blockAspect.getAspects(state) : new AspectList();
   }

   public static AspectList getObjectTags(Entity entity) {
      EntityAspect entityAspect = findEntityAspect(entity);
      return entityAspect != null ? entityAspect.aspects().copy() : new AspectList();
   }

   public static void registerObjectTag(@NotNull Object obj, @NotNull AspectList aspects) {
      if (aspects == null) {
         aspects = new AspectList();
      }

      AspectList safeAspects = aspects.copy();
      ObjectAspect objectAspect = createObjectAspect(obj, safeAspects);
      if (objectAspect != null) {
         registerAspectTag(objectAspect, false, shouldReplaceEqual(obj));
      } else {
         BlockAspect blockAspect = createBlockAspect(obj, safeAspects);
         if (blockAspect != null) {
            registerBlockAspectTag(blockAspect, false, shouldReplaceEqual(obj));
         }
      }
   }

   @Nullable
   private static ObjectAspect findObjectAspect(ItemStack stack) {
      return findObjectAspect(stack, null);
   }

   @Nullable
   private static ObjectAspect findObjectAspect(ItemStack stack, @Nullable ObjectAspect excludedAspect) {
      for (ObjectAspectLookupStage lookupStage : OBJECT_LOOKUP_ORDER) {
         ObjectAspect stageMatch = findObjectAspect(stack, excludedAspect, lookupStage);
         if (stageMatch != null) {
            return stageMatch;
         }
      }

      return null;
   }

   @Nullable
   private static ObjectAspect findObjectAspect(ItemStack stack, @Nullable ObjectAspect excludedAspect, ObjectAspectLookupStage lookupStage) {
      ObjectAspect bestMatch = null;
      int bestPriority = Integer.MIN_VALUE;
      synchronized (itemAspects) {
         for (ObjectAspect objectAspect : itemAspects) {
            if (objectAspect != excludedAspect && objectAspect.lookupStage() == lookupStage) {
               int priority = objectAspect.matchPriority(stack);
               if (priority > bestPriority) {
                  bestMatch = objectAspect;
                  bestPriority = priority;
               }
            }
         }

         return bestMatch;
      }
   }

   @Nullable
   private static BlockAspect findBlockAspect(BlockState state) {
      BlockAspect bestMatch = null;
      int bestPriority = Integer.MIN_VALUE;
      synchronized (blockAspects) {
         for (BlockAspect blockAspect : blockAspects) {
            int priority = blockAspect.matchPriority(state);
            if (priority > bestPriority) {
               bestMatch = blockAspect;
               bestPriority = priority;
            }
         }

         return bestMatch;
      }
   }

   @Nullable
   private static EntityAspect findEntityAspect(Entity entity) {
      EntityAspect predicateMatch = findEntityAspect(entity, true);
      return predicateMatch != null ? predicateMatch : findEntityAspect(entity, false);
   }

   @Nullable
   private static EntityAspect findEntityAspect(Entity entity, boolean predicateSpecific) {
      synchronized (entityAspects) {
         for (EntityAspect entityAspect : entityAspects) {
            if (entityAspect.isPredicateSpecific() == predicateSpecific && entityAspect.matches(entity)) {
               return entityAspect;
            }
         }

         return null;
      }
   }

   public static void registerComplexObjectTag(ItemLike itemLike, AspectList aspects) {
      registerComplexObjectTag(new ItemStack(itemLike), aspects);
   }

   public static void registerComplexObjectTag(ItemStack stack, AspectList aspects) {
      registerComplexObjectTag(stack, stack, aspects);
   }

   public static void registerComplexObjectTag(ItemLike itemLike, ItemLike stack, AspectList aspects) {
      registerComplexObjectTag(itemLike, new ItemStack(stack), aspects);
   }

   public static void registerComplexObjectTag(ItemLike itemLike, ItemStack stack, AspectList aspects) {
      registerComplexObjectTag((Object)itemLike, stack, aspects);
   }

   public static void registerComplexObjectTag(TagKey<Item> tagKey, ItemLike stack, AspectList aspects) {
      registerComplexObjectTag(tagKey, new ItemStack(stack), aspects);
   }

   public static void registerComplexObjectTag(TagKey<Item> tagKey, ItemStack stack, AspectList aspects) {
      registerComplexObjectTag((Object) tagKey, stack, aspects);
   }

   public static void registerComplexObjectTag(Ingredient ingredient, ItemStack stack, AspectList aspects) {
      registerComplexObjectTag((Object)ingredient, stack, aspects);
   }

   private static void registerComplexObjectTag(Object registrationKey, ItemStack stack, AspectList aspects) {
      AspectList merged = aspects == null ? new AspectList() : aspects.copy();
      ObjectAspect objectAspect = createObjectAspect(registrationKey, new AspectList());
      if (objectAspect != null) {
         registerAspectTag(new ComplexObjectAspect(objectAspect, stack, merged), false, false);
      }
   }

   public static void registerObjectTag(ItemStack stack, AspectList aspects) {
      if (stack != null && !stack.isEmpty()) {
         registerAspectTag(new ItemStackAspect(stack, aspects == null ? new AspectList() : aspects.copy(), false), false, true);
      }
   }

   public static void registerStrictObjectTag(ItemStack stack, AspectList aspects) {
      if (stack != null && !stack.isEmpty()) {
         registerAspectTag(new ItemStackAspect(stack, aspects == null ? new AspectList() : aspects.copy(), true), false, true);
      }
   }

   public static void registerDerivedObjectTag(ItemLike target, ItemLike source, UnaryOperator<AspectList> transformer) {
      registerDerivedObjectTag(new ItemStack(target), new ItemStack(source), transformer);
   }

   public static void registerDerivedObjectTag(ItemLike target, ItemStack source, UnaryOperator<AspectList> transformer) {
      registerDerivedObjectTag(new ItemStack(target), source, transformer);
   }

   public static void registerDerivedObjectTag(ItemStack target, ItemLike source, UnaryOperator<AspectList> transformer) {
      registerDerivedObjectTag(target, new ItemStack(source), transformer);
   }

   public static void registerDerivedObjectTag(ItemStack target, ItemStack source, UnaryOperator<AspectList> transformer) {
      if (target != null && !target.isEmpty() && source != null && !source.isEmpty()) {
         ObjectAspect objectAspect = createObjectAspect(target, new AspectList());
         if (objectAspect != null) {
            registerAspectTag(new DerivedObjectAspect(objectAspect, source, transformer), false, true);
         }
      }
   }

   @Nullable
   public static String getObjectAspectScanKey(ItemStack stack) {
      ObjectAspect objectAspect = findObjectAspect(stack);
      return objectAspect == null ? null : objectAspect.getScanKey(stack);
   }

   @Nullable
   public static String getBlockAspectScanKey(BlockState state) {
      BlockAspect blockAspect = findBlockAspect(state);
      return blockAspect == null ? null : blockAspect.getScanKey(state);
   }

   @Nullable
   public static String getEntityAspectScanKey(Entity entity) {
      EntityAspect entityAspect = findEntityAspect(entity);
      return entityAspect == null ? null : entityAspect.getScanHashKey(entity);
   }

   public static void addWarpToItem(ItemStack craftresult, int amount) {
      warpMap.put(craftresult.getItem(), amount);
   }

   public static void addWarpToResearch(String research, int amount) {
      warpMap.put(research, amount);
   }

   public static void clearResearchWarp() {
      warpMap.entrySet().removeIf(entry -> entry.getKey() instanceof String);
   }

   public static int getWarp(Object in) {
      if (in == null) {
         return 0;
      } else {
         if (in instanceof ItemStack stack) {
            Integer warp = warpMap.get(stack.getItem());
            if (warp != null) {
               return warp;
            }
         } else {
            Integer warp = warpMap.get(in);
            if (warp != null) {
               return warp;
            }
         }

         return 0;
      }
   }

   public static void addLootBagItem(ItemStack item, int weight, int... bagTypes) {
      if (bagTypes != null && bagTypes.length != 0) {
         for (int rarity : bagTypes) {
            switch (rarity) {
               case 0:
                  WeightedRandomLoot.lootBagCommon.add(new WeightedRandomLoot(item, weight));
                  break;
               case 1:
                  WeightedRandomLoot.lootBagUncommon.add(new WeightedRandomLoot(item, weight));
                  break;
               case 2:
                  WeightedRandomLoot.lootBagRare.add(new WeightedRandomLoot(item, weight));
            }
         }
      } else {
         WeightedRandomLoot.lootBagCommon.add(new WeightedRandomLoot(item, weight));
      }
   }

   public static <T extends IInfusionModifierRecipe> void registerInfusionModifierRecipeType(RecipeType<T> recipeType) {
      infusionModifierRecipeTypes.add(recipeType);
   }

   public static List<RecipeType<? extends IInfusionModifierRecipe>> getInfusionModifierRecipeTypes() {
      return Collections.unmodifiableList(infusionModifierRecipeTypes);
   }

   public static RecipeManager getRecipeManager() {
      RecipeManager recipeManager = getSafeRecipeManager();
      if (recipeManager == null) {
         throw new IllegalStateException("RecipeManager is not available yet");
      } else {
         return recipeManager;
      }
   }

   @Nullable
   public static <I extends RecipeInput, T extends Recipe<I>> T getRecipe(RecipeType<T> type, ResourceLocation id) {
      for (RecipeHolder<T> holder : getRecipeManager().getAllRecipesFor(type)) {
         if (holder.id().equals(id)) {
            return (T)holder.value();
         }
      }

      return null;
   }

   @Nullable
   public static RecipeManager getSafeRecipeManager() {
      if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
         return ServerLifecycleHooks.getCurrentServer() == null ? null : ServerLifecycleHooks.getCurrentServer().getRecipeManager();
      } else {
         return getClientRecipeManager();
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Nullable
   private static RecipeManager getClientRecipeManager() {
      Minecraft minecraft = Minecraft.getInstance();
      return minecraft.getConnection() == null ? null : minecraft.getConnection().getRecipeManager();
   }

   private static boolean shouldReplaceEqual(Object obj) {
      return obj instanceof ItemLike || obj instanceof ItemStack || obj instanceof Block;
   }

   @Nullable
   private static ObjectAspect createObjectAspect(@NotNull Object obj, @NotNull AspectList aspects) {
      if (obj instanceof ItemLike itemLike) {
         return itemLike.asItem() == Items.AIR ? null : new ItemStackAspect(new ItemStack(itemLike), aspects, false);
      } else if (obj instanceof ItemStack stack) {
         if (stack.isEmpty()) {
            return null;
         } else {
            return ItemStackData.hasTag(stack) ? new ItemStackAspect(stack, aspects, true) : new ItemStackAspect(stack, aspects, false);
         }
      } else if (obj instanceof Ingredient ingredient) {
         return new IngredientAspect(ingredient, aspects);
      } else {
         return obj instanceof TagKey<?> tagKey && tagKey.registry() == Registries.ITEM ? new TagAspect((TagKey<Item>)tagKey, aspects) : null;
      }
   }

   @Nullable
   private static BlockAspect createBlockAspect(@NotNull Object obj, @NotNull AspectList aspects) {
      if (obj instanceof Block block && block.asItem() == Items.AIR) {
         return new DirectBlockAspect(block, aspects);
      } else {
         return obj instanceof ItemLike itemLike && itemLike.asItem() == Items.AIR && itemLike instanceof Block block
            ? new DirectBlockAspect(block, aspects)
            : null;
      }
   }

   public static class EntityTags {
      public String entityName;
      public ThaumcraftApi.EntityTagsNBT[] nbts;
      public AspectList aspects;

      public EntityTags(String entityName, AspectList aspects, ThaumcraftApi.EntityTagsNBT... nbts) {
         this.entityName = entityName;
         this.nbts = nbts;
         this.aspects = aspects;
      }
   }

   public static class EntityTagsNBT {
      public String name;
      public Object value;

      public EntityTagsNBT(String name, Object value) {
         this.name = name;
         this.value = value;
      }
   }
}
