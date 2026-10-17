package thaumcraft.item.wands;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.function.BiPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.api.IArchitect;
import thaumcraft.api.IVisDiscountGear;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.IWandTriggerManager;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.init.registry.ModMobEffect;
import thaumcraft.init.registry.ModSounds;
import thaumcraft.item.wands.foci.ItemFocusTrade;
import thaumcraft.network.PacketHandler;
import thaumcraft.network.fx.PacketFXBlockSparkle;
import thaumcraft.util.ItemStackData;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

public class WandManager implements IWandTriggerManager {
   public static float getTotalVisDiscount(Player player, Aspect aspect) {
      if (player != null && aspect != null) {
         int total = 0;

         for (ItemStack stack : player.getInventory().armor) {
            if (stack.getItem() instanceof IVisDiscountGear gear) {
               total += gear.getVisDiscount(stack, player, aspect);
            }
         }

         // Скидки бижутерии из Curios-слотов (кольца/амулеты/пояса), а не только броня.
         // Curios — required-зависимость (neoforge.mods.toml), вызовы прямые.
         for (top.theillusivec4.curios.api.SlotResult result : top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player)
            .map(handler -> handler.findCurios(stack -> stack.getItem() instanceof IVisDiscountGear))
            .orElse(java.util.List.of())) {
            ItemStack stack = result.stack();
            if (stack.getItem() instanceof IVisDiscountGear gear) {
               total += gear.getVisDiscount(stack, player, aspect);
            }
         }

         MobEffectInstance visExhaust = player.getEffect(ModMobEffect.VIS_EXHAUST);
         MobEffectInstance infectionsVisExhaust = player.getEffect(ModMobEffect.INFECTIOUS_VIS_EXHAUST);
         if (visExhaust != null || infectionsVisExhaust != null) {
            int level1 = visExhaust != null ? visExhaust.getAmplifier() : 0;
            int level2 = infectionsVisExhaust != null ? infectionsVisExhaust.getAmplifier() : 0;
            total -= (Math.max(level1, level2) + 1) * 10;
         }

         return total / 100.0F;
      } else {
         return 0.0F;
      }
   }

   private static void sendSparklePacket(Level level, BlockPos pos) {
      if (!level.isClientSide) {
         PacketHandler.sendToAllAround(new PacketFXBlockSparkle(pos, -9999), level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 32.0);
      }
   }

   public static int getAreaDim(ItemStack stack) {
      return ItemStackData.hasTag(stack) && ItemStackData.getTag(stack).contains("aread") ? ItemStackData.getTag(stack).getInt("aread") : 0;
   }

   public static int getAreaX(ItemStack stack) {
      return getArea(stack, "areax");
   }

   public static int getAreaY(ItemStack stack) {
      return getArea(stack, "areay");
   }

   public static int getAreaZ(ItemStack stack) {
      return getArea(stack, "areaz");
   }

   private static int getArea(ItemStack stack, String key) {
      if (stack.getItem() instanceof ItemWandCasting wand) {
         ItemFocusBasic focus = wand.getFocus(stack);
         if (focus != null) {
            int maxArea = focus.getMaxAreaSize(wand.getFocusItem(stack));
            CompoundTag compound = ItemStackData.getTag(stack);
            if (compound != null && compound.contains(key, 3)) {
               return Math.min(compound.getInt(key), maxArea);
            }

            return maxArea;
         }
      }

      return 0;
   }

   public static void setAreaX(ItemStack stack, int area) {
      getOrCreateTag(stack).putInt("areax", area);
   }

   public static void setAreaY(ItemStack stack, int area) {
      getOrCreateTag(stack).putInt("areay", area);
   }

   public static void setAreaZ(ItemStack stack, int area) {
      getOrCreateTag(stack).putInt("areaz", area);
   }

   public static void setAreaDim(ItemStack stack, int dim) {
      getOrCreateTag(stack).putInt("aread", dim);
   }

   private static CompoundTag getOrCreateTag(ItemStack stack) {
      return ItemStackData.getOrCreateTag(stack);
   }

   /**
    * Port of WandManager.changeFocus (TC4 4.2.3.5): swaps the wand focus with the focus whose sorting key
    * matches (or follows) {@code focusKey}, taken from the main inventory or from a focus pouch (inventory or
    * Curios belt). "REMOVE" puts the current focus away. The old focus goes to a pouch first, then the inventory.
    */
   public static void changeFocus(ItemStack wandStack, Level level, Player player, String focusKey) {
      ItemWandCasting wand = (ItemWandCasting)wandStack.getItem();
      TreeMap<String, Integer> foci = new TreeMap<>();
      List<ItemStack> pouches = new ArrayList<>();
      for (ItemStack pouch : wornFocusPouches(player)) {
         collectPouchFoci(pouch, pouches, foci);
      }

      NonNullList<ItemStack> items = player.getInventory().items;
      for (int slot = 0; slot < items.size(); slot++) {
         ItemStack item = items.get(slot);
         if (item.getItem() instanceof ItemFocusBasic focus) {
            foci.put(focus.getSortingHelper(item), slot);
         } else if (item.getItem() instanceof ItemFocusPouch) {
            collectPouchFoci(item, pouches, foci);
         }
      }

      if (!"REMOVE".equals(focusKey) && !foci.isEmpty()) {
         String key = foci.containsKey(focusKey) ? focusKey : foci.higherKey(focusKey);
         if (key == null) {
            key = foci.firstKey();
         }

         int ref = foci.get(key);
         ItemStack picked;
         if (ref < 1000) {
            picked = items.get(ref).copy();
            items.set(ref, ItemStack.EMPTY);
         } else {
            picked = fetchFocusFromPouch(pouches.get(ref / 1000 - 1), ref % 1000);
         }

         if (picked.isEmpty()) {
            return;
         }

         level.playSound(null, player.blockPosition(), ModSounds.CAMERA_TICKS.get(), SoundSource.PLAYERS, 0.3F, 1.0F);
         ItemStack old = wand.getFocusItem(wandStack);
         if (!old.isEmpty() && (addFocusToPouch(old, pouches) || player.getInventory().add(old))) {
            wand.setFocus(wandStack, null);
         }

         if (wand.getFocus(wandStack) == null) {
            wand.setFocus(wandStack, picked);
         } else if (!addFocusToPouch(picked, pouches) && !player.getInventory().add(picked)) {
            player.drop(picked, false);
         }
      } else {
         ItemStack old = wand.getFocusItem(wandStack);
         if (!old.isEmpty() && (addFocusToPouch(old, pouches) || player.getInventory().add(old))) {
            wand.setFocus(wandStack, null);
            level.playSound(null, player.blockPosition(), ModSounds.CAMERA_TICKS.get(), SoundSource.PLAYERS, 0.3F, 0.9F);
         }
      }

      player.getInventory().setChanged();
   }

   /** Focus pouches worn in Curios slots (the TC4 baubles inventory); the stacks are the live slot contents. */
   private static List<ItemStack> wornFocusPouches(Player player) {
      return CuriosApi.getCuriosInventory(player)
         .map(handler -> handler.findCurios(stack -> stack.getItem() instanceof ItemFocusPouch).stream().map(SlotResult::stack).toList())
         .orElse(List.of());
   }

   /** Registers a pouch (id = 1-based index) and its foci under {@code slot + id * 1000}, as TC4 does. */
   private static void collectPouchFoci(ItemStack pouch, List<ItemStack> pouches, TreeMap<String, Integer> foci) {
      pouches.add(pouch);
      int id = pouches.size();
      NonNullList<ItemStack> contents = ((ItemFocusPouch)pouch.getItem()).getInventory(pouch);
      for (int slot = 0; slot < contents.size(); slot++) {
         ItemStack item = contents.get(slot);
         if (item.getItem() instanceof ItemFocusBasic focus) {
            foci.put(focus.getSortingHelper(item), slot + id * 1000);
         }
      }
   }

   private static ItemStack fetchFocusFromPouch(ItemStack pouch, int slot) {
      ItemFocusPouch pouchItem = (ItemFocusPouch)pouch.getItem();
      NonNullList<ItemStack> contents = pouchItem.getInventory(pouch);
      ItemStack focus = contents.get(slot);
      if (!(focus.getItem() instanceof ItemFocusBasic)) {
         return ItemStack.EMPTY;
      }

      contents.set(slot, ItemStack.EMPTY);
      pouchItem.setInventory(pouch, contents);
      return focus.copy();
   }

   private static boolean addFocusToPouch(ItemStack focus, List<ItemStack> pouches) {
      for (ItemStack pouch : pouches) {
         ItemFocusPouch pouchItem = (ItemFocusPouch)pouch.getItem();
         NonNullList<ItemStack> contents = pouchItem.getInventory(pouch);
         for (int slot = 0; slot < contents.size(); slot++) {
            if (contents.get(slot).isEmpty()) {
               contents.set(slot, focus.copy());
               pouchItem.setInventory(pouch, contents);
               return true;
            }
         }
      }

      return false;
   }

   /**
    * Port of WandManager.toggleMisc (TC4 4.2.3.5): the misc key cycles the area of an architect focus.
    * Sneaking switches the edited dimension (0 all, 1 x, 2 z, 3 y; Equal Trade has no y); otherwise the
    * selected dimension grows and wraps to 0 past the focus maximum.
    */
   public static void toggleMisc(ItemStack wandStack, Player player) {
      if (!(wandStack.getItem() instanceof ItemWandCasting wand)) {
         return;
      }

      ItemFocusBasic focus = wand.getFocus(wandStack);
      ItemStack focusStack = wand.getFocusItem(wandStack);
      if (!(focus instanceof IArchitect) || !focus.isUpgradedWith(focusStack, FocusUpgradeType.architect)) {
         return;
      }

      int dim = getAreaDim(wandStack);
      if (player.isShiftKeyDown()) {
         if (++dim > (focus instanceof ItemFocusTrade ? 2 : 3)) {
            dim = 0;
         }

         setAreaDim(wandStack, dim);
         return;
      }

      int max = focus.getMaxAreaSize(focusStack);
      int areaX = getAreaX(wandStack);
      int areaY = getAreaY(wandStack);
      int areaZ = getAreaZ(wandStack);
      if (dim == 0) {
         areaX++;
         areaZ++;
         areaY++;
      } else if (dim == 1) {
         areaX++;
      } else if (dim == 2) {
         areaZ++;
      } else if (dim == 3) {
         areaY++;
      }

      setAreaX(wandStack, areaX > max ? 0 : areaX);
      setAreaY(wandStack, areaY > max ? 0 : areaY);
      setAreaZ(wandStack, areaZ > max ? 0 : areaZ);
   }

   private static void forEachInfernalFurnace(BlockPos blockPos, Direction direction, BlockPos det, BiPredicate<BlockPos, Integer> predicate) {
      int index = 1;
      boolean revers = direction == Direction.WEST || direction == Direction.EAST;
      int depth = revers ? det.getX() : det.getZ();
      int width = revers ? det.getZ() : det.getX();
      int height = det.getY();

      for (int d = 0; d <= 2; d++) {
         int offD = depth > 0 ? d : -d;

         for (int h = 0; h <= 2; h++) {
            int offH = height > 0 ? h : -h;

            for (int w = 0; w <= 2; w++) {
               int offW = width > 0 ? w : -w;
               BlockPos target = revers ? blockPos.offset(offD, offH, offW) : blockPos.offset(offW, offH, offD);
               if (predicate.test(target, index)) {
                  index++;
               }
            }
         }
      }
   }

   /**
    * Оплата вис из амулета/жезлов в инвентаре (порт {@code WandManager#consumeVisFromInventory}).
    * Сначала амулет вис (Curios), затем жезлы из инвентаря.
    */
   public static boolean consumeVisFromInventory(Player player, AspectList cost) {
      if (player == null || cost == null || cost.size() == 0) {
         return false;
      }

      boolean[] paid = new boolean[1];
      try {
         top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player)
            .map(handler -> {
               var found = handler.findFirstCurio(
                  stack -> stack.getItem() instanceof thaumcraft.item.curios.ItemAmuletVis
               );
               if (found.isPresent()) {
                  ItemStack amulet = found.get().stack();
                  if (((thaumcraft.item.curios.ItemAmuletVis)amulet.getItem())
                     .consumeAllVis(amulet, player, cost, true, true)) {
                     paid[0] = true;
                  }
               }

               return paid[0];
            })
            .orElse(false);
      } catch (Exception e) {
         // Curios нет — идём к жезлам.
      }

      if (paid[0]) {
         return true;
      }

      for (ItemStack item : player.getInventory().items) {
         if (item.getItem() instanceof thaumcraft.item.wands.ItemWandCasting wand
            && wand.consumeAllVis(item, player, cost, true, true)) {
            return true;
         }
      }

      return false;
   }

   /**
    * ПКМ жезлом по блоку — механики «создания» из оригинального TC4
    * (книжная полка → таумономикон, котёл → тигель).
    *
    * <p>Проверки исследования перенесены 1:1 из {@code performTrigger}
    * (1.7.10, строки 874-912): триггеры 0 и 1 в оригинале тоже не гейтятся,
    * а 2..7 без соответствующего исследования просто не срабатывают. Раньше
    * проверок не было, и игрок мог собрать инфузионный алтарь, банку узла, окулус
    * или тауматорий стартовым жезлом, минуя половину артефактной ветки.
    */
   @Override
   public boolean performTrigger(
      Level level, ItemStack wand, Player player,
      net.minecraft.core.BlockPos pos, net.minecraft.core.Direction side, int event
   ) {
      switch (event) {
         case 0:
            return createThaumonomicon(wand, level, pos);
         case 1:
            return createCrucible(wand, level, pos);
         case 2:
            if (thaumcraft.research.ResearchManager.isResearchComplete(player, "infernal_furnace")) {
               return createArcaneFurnace(wand, player, level, pos, side);
            }

            return false;
         case 3:
            if (thaumcraft.research.ResearchManager.isResearchComplete(player, "infusion")) {
               return createInfusionAltar(wand, player, level, pos);
            }

            return false;
         case 4:
            if (thaumcraft.research.ResearchManager.isResearchComplete(player, "node_jar")) {
               return createNodeJar(wand, player, level, pos);
            }

            return false;
         case 5:
            if (thaumcraft.research.ResearchManager.isResearchComplete(player, "thaumatorium")) {
               return createThaumatorium(wand, player, level, pos, side);
            }

            return false;
         case 6:
            if (thaumcraft.research.ResearchManager.isResearchComplete(player, "oculus")) {
               return createOculus(wand, player, level, pos, side);
            }

            return false;
         case 7:
            if (thaumcraft.research.ResearchManager.isResearchComplete(player, "adv_alchemy_furnace")) {
               return createAdvancedAlchemicalFurnace(wand, player, level, pos, side);
            }

            return false;
         case 8:
            if (thaumcraft.research.ResearchManager.isResearchComplete(player, "arcane_workbench")) {
               return createArcaneWorkbench(wand, player, level, pos);
            }

            return false;
         default:
            return false;
      }
   }

   /** Книжная полка + жезл без фокуса → книга таумономикона (как в 1.7.10). */
   public static boolean createThaumonomicon(ItemStack wandStack, Level level, net.minecraft.core.BlockPos pos) {
      if (level.isClientSide() || !(wandStack.getItem() instanceof ItemWandCasting wand) || wand.getFocus(wandStack) != null) {
         return false;
      }

      level.removeBlock(pos, false);
      thaumcraft.entities.brainless.special.SpecialItemEntity entity = new thaumcraft.entities.brainless.special.SpecialItemEntity(
         level, pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5,
         new ItemStack(thaumcraft.init.registry.ModItems.THAUMONOMICON.get())
      );
      entity.setDeltaMovement(0.0, 0.0, 0.0);
      level.addFreshEntity(entity);
      level.playSound(
         null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
         thaumcraft.init.registry.ModSounds.WAND.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F
      );
      return true;
   }


   /** Триггер 7: структура вокруг алхимической печи → продвинутая алхимическая печь (порт 1.7.10). */
   public static boolean createAdvancedAlchemicalFurnace(
      ItemStack wandStack, Player player, Level level, BlockPos clicked, Direction side
   ) {
      if (level.isClientSide() || !(wandStack.getItem() instanceof ItemWandCasting wand)) {
         return false;
      }

      net.minecraft.world.level.block.Block furnaceBlock = thaumcraft.init.registry.ModBlocks.ALCHEMY_FURNACE.get();
      BlockPos core = null;

      for (int dx = -1; dx <= 1 && core == null; dx++) {
         for (int dy = -1; dy <= 1 && core == null; dy++) {
            for (int dz = -1; dz <= 1 && core == null; dz++) {
               BlockPos candidate = clicked.offset(dx, dy, dz);
               if (level.getBlockState(candidate).is(furnaceBlock)) {
                  core = candidate;
               }
            }
         }
      }

      if (core == null || !fitAdvancedAlchemicalFurnace(level, core)) {
         return false;
      }

      AspectList cost = new AspectList().add(Aspect.FIRE, 50).add(Aspect.WATER, 50).add(Aspect.ORDER, 50);
      if (!wand.consumeAllVisCrafting(wandStack, player, cost, true)) {
         return false;
      }

      replaceAdvancedAlchemicalFurnace(level, core);
      level.playSound(
         null, core.getX() + 0.5, core.getY() + 0.5, core.getZ() + 0.5,
         thaumcraft.init.registry.ModSounds.WAND.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F
      );
      return true;
   }

   /** Структура: 8 продвинутых конструктов вокруг печи; сверху по рёбрам конструкты, по углам алембики. */
   private static boolean fitAdvancedAlchemicalFurnace(Level level, BlockPos core) {
      net.minecraft.world.level.block.Block advanced = thaumcraft.init.registry.ModBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get();
      net.minecraft.world.level.block.Block construct = thaumcraft.init.registry.ModBlocks.ALCHEMICAL_CONSTRUCT.get();
      net.minecraft.world.level.block.Block alembic = thaumcraft.init.registry.ModBlocks.ARCANE_ALEMBIC.get();

      for (int dx = -1; dx <= 1; dx++) {
         for (int dz = -1; dz <= 1; dz++) {
            if (dx == 0 && dz == 0) {
               continue;
            }

            if (!level.getBlockState(core.offset(dx, 0, dz)).is(advanced)) {
               return false;
            }

            if (dx != 0 && dz != 0) {
               if (!level.getBlockState(core.offset(dx, 1, dz)).is(alembic)) {
                  return false;
               }
            } else if (!level.getBlockState(core.offset(dx, 1, dz)).is(construct)) {
               return false;
            }
         }
      }

      return true;
   }

   /** Замена структуры на детали adv_alch (центр/клапаны/основания/топки/баки). */
   private static void replaceAdvancedAlchemicalFurnace(Level level, BlockPos core) {
      net.minecraft.world.level.block.Block advBlock = thaumcraft.init.registry.ModBlocks.ADVANCED_ALCHEMY_FURNACE.get();
      net.minecraft.world.level.block.Block burnBlock = thaumcraft.init.registry.ModBlocks.ADVANCED_ALCHEMY_FURNACE_BURN.get();
      net.minecraft.world.level.block.Block tankBlock = thaumcraft.init.registry.ModBlocks.ADVANCED_ALCHEMY_FURNACE_TANK.get();
      class Holder {
         static net.minecraft.world.level.block.state.properties.EnumProperty<thaumcraft.block.advanced_alchemy_furnace.AdvAlchemyFurnaceBlock.Element> element() {
            return thaumcraft.block.advanced_alchemy_furnace.AdvAlchemyFurnaceBlock.ELEMENT;
         }
      }

      // Ставим флагом UPDATE_CLIENTS, без уведомления соседей, и разсылаем их отдельным
      // проходом в конце. Иначе первая же вставленная деталь разбудит уже стоящее ядро,
      // и печь рассыплется прямо во время сборки.
      level.setBlock(
         core,
         advBlock.defaultBlockState()
            .setValue(Holder.element(), thaumcraft.block.advanced_alchemy_furnace.AdvAlchemyFurnaceBlock.Element.CENTER)
            .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH),
         net.minecraft.world.level.block.Block.UPDATE_CLIENTS
      );

      for (int dx = -1; dx <= 1; dx++) {
         for (int dz = -1; dz <= 1; dz++) {
            if (dx == 0 && dz == 0) {
               continue;
            }

            boolean diagonal = dx != 0 && dz != 0;
            Direction facing = diagonal ? baseFacing(dx, dz) : facingToCore(dx, dz);
            net.minecraft.world.level.block.state.BlockState baseState = advBlock.defaultBlockState()
               .setValue(
                  Holder.element(),
                  diagonal
                     ? thaumcraft.block.advanced_alchemy_furnace.AdvAlchemyFurnaceBlock.Element.BASE
                     : thaumcraft.block.advanced_alchemy_furnace.AdvAlchemyFurnaceBlock.Element.VALVE
               )
               .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING, facing);
            level.setBlock(core.offset(dx, 0, dz), baseState, net.minecraft.world.level.block.Block.UPDATE_CLIENTS);

            if (diagonal) {
               level.setBlock(
                  core.offset(dx, 1, dz),
                  tankBlock.defaultBlockState()
                     .setValue(thaumcraft.block.advanced_alchemy_furnace.AdvAlchemyFurnaceTankBlock.FILL, 0)
                     .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING, facing),
                  net.minecraft.world.level.block.Block.UPDATE_CLIENTS
               );
            } else {
               level.setBlock(
                  core.offset(dx, 1, dz),
                  burnBlock.defaultBlockState()
                     .setValue(thaumcraft.block.advanced_alchemy_furnace.AdvAlchemyFurnaceBurnBlock.BURN, 0)
                     .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING, facing),
                  net.minecraft.world.level.block.Block.UPDATE_CLIENTS
               );
            }
         }
      }

      // Печь собрана целиком — только теперь будим соседей: ядро проверит состав и,
      // убедившись, что все 17 деталей на месте, ничего не разберёт.
      for (int dx = -1; dx <= 1; dx++) {
         for (int dz = -1; dz <= 1; dz++) {
            if (dx == 0 && dz == 0) {
               continue;
            }

            level.updateNeighborsAt(core.offset(dx, 0, dz), advBlock);
            level.updateNeighborsAt(core.offset(dx, 1, dz), advBlock);
         }
      }
   }

   /** Направление с позиции-детали на ядро (для клапанов/топок). */
   private static Direction facingToCore(int dx, int dz) {
      if (dx != 0) {
         return dx > 0 ? Direction.WEST : Direction.EAST;
      }

      return dz > 0 ? Direction.NORTH : Direction.SOUTH;
   }

   /** Направление основания/бака по диагонали (dir + ccw(dir) указывает на ядро). */
   private static Direction baseFacing(int dx, int dz) {
      if (dx < 0) {
         return dz < 0 ? Direction.SOUTH : Direction.EAST;
      }

      return dz < 0 ? Direction.WEST : Direction.NORTH;
   }


   /** Триггер 2: 3x3x3 обсидиан/нетер-кирпич с лавой, решёткой и дырой → инфернальная печь (порт 1.7.10). */
   public static boolean createArcaneFurnace(
      ItemStack wandStack, Player player, Level level, BlockPos clicked, Direction side
   ) {
      if (level.isClientSide() || !(wandStack.getItem() instanceof ItemWandCasting wand)) {
         return false;
      }

      for (int xx = clicked.getX() - 2; xx <= clicked.getX(); xx++) {
         for (int yy = clicked.getY() - 2; yy <= clicked.getY(); yy++) {
            for (int zz = clicked.getZ() - 2; zz <= clicked.getZ(); zz++) {
               BlockPos base = new BlockPos(xx, yy, zz);
               if (fitArcaneFurnace(level, base)
                  && wand.consumeAllVisCrafting(
                     wandStack, player, new AspectList().add(Aspect.FIRE, 50).add(Aspect.EARTH, 50), true
                  )) {
                  replaceArcaneFurnace(level, base);
                  level.playSound(
                     null, base.getX() + 1.5, base.getY() + 1.5, base.getZ() + 1.5,
                     thaumcraft.init.registry.ModSounds.WAND.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F
                  );
                  return true;
               }
            }
         }
      }

      return false;
   }

   /** Структура вокруг ядра: верх — НБ по углам/обсидиан по рёбрам/дыра в центре; середина — лава в центре,
    *  НБ по углам, обсидиан по рёбрам (одно ребро — решётка); низ — НБ по углам, обсидиан в остальном. */
   private static boolean fitArcaneFurnace(Level level, BlockPos base) {
      int bars = 0;

      for (int yy = 0; yy < 3; yy++) {
         for (int zz = 0; zz < 3; zz++) {
            for (int xx = 0; xx < 3; xx++) {
               BlockPos pos = base.offset(xx, 2 - yy, zz);
               net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);
               boolean corner = xx != 1 && zz != 1;
               boolean center = xx == 1 && zz == 1;

               if (yy == 0) {
                  if (corner) {
                     if (!state.is(net.minecraft.world.level.block.Blocks.NETHER_BRICKS)) {
                        return false;
                     }
                  } else if (center) {
                     if (!state.isAir() && !(state.getBlock() instanceof thaumcraft.block.hole.HoleBlock)) {
                        return false;
                     }
                  } else if (!state.is(net.minecraft.world.level.block.Blocks.OBSIDIAN)) {
                     return false;
                  }
               } else if (yy == 1) {
                  if (center) {
                     if (!state.is(net.minecraft.world.level.block.Blocks.LAVA)
                        && !state.is(thaumcraft.init.registry.ModBlocks.INFERNAL_FURNACE.get())) {
                        return false;
                     }
                  } else if (corner) {
                     if (!state.is(net.minecraft.world.level.block.Blocks.NETHER_BRICKS)) {
                        return false;
                     }
                  } else if (state.is(net.minecraft.world.level.block.Blocks.IRON_BARS)) {
                     bars++;
                  } else if (!state.is(net.minecraft.world.level.block.Blocks.OBSIDIAN)) {
                     return false;
                  }
               } else if (corner) {
                  if (!state.is(net.minecraft.world.level.block.Blocks.NETHER_BRICKS)) {
                     return false;
                  }
               } else if (!state.is(net.minecraft.world.level.block.Blocks.OBSIDIAN)) {
                  return false;
               }
            }
         }
      }

      return bars == 1;
   }

   /** Замена структуры деталями печи с индексами (эталон — решётка на востоке ядра). */
   private static void replaceArcaneFurnace(Level level, BlockPos base) {
      BlockPos core = base.offset(1, 1, 1);
      Direction opening = null;

      for (Direction dir : Direction.Plane.HORIZONTAL) {
         if (level.getBlockState(core.relative(dir)).is(net.minecraft.world.level.block.Blocks.IRON_BARS)) {
            opening = dir;
            break;
         }
      }

      if (opening == null) {
         opening = Direction.EAST;
      }

      int steps = 0;
      Direction cursor = Direction.EAST;

      while (cursor != opening) {
         cursor = cursor.getClockWise();
         steps++;
      }

      net.minecraft.world.level.block.Block furnaceBlock = thaumcraft.init.registry.ModBlocks.INFERNAL_FURNACE.get();
      // Блоки ставятся без обновления соседей (в 1.7.10 флаг 0), уведомления — отдельным
      // проходом ниже. Иначе каждая поставленная деталь будит проверку целостности у
      // уже стоящих соседей и печь разваливается прямо во время сборки.
      level.setBlock(
         core.above(), thaumcraft.init.registry.ModBlocks.HOLE.get().defaultBlockState(),
         net.minecraft.world.level.block.Block.UPDATE_CLIENTS
      );

      for (int dy = -1; dy <= 1; dy++) {
         for (int dz = -1; dz <= 1; dz++) {
            for (int dx = -1; dx <= 1; dx++) {
               if (dx == 0 && dz == 0 && dy != -1) {
                  continue;
               }

               int index = arcaneFurnaceIndex(dx, dz, dy, steps);
               level.setBlock(
                  core.offset(dx, dy, dz),
                  furnaceBlock.defaultBlockState()
                     .setValue(thaumcraft.block.infernal_furnace.InfernalFurnaceBlock.INDEX, index)
                     .setValue(
                        net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING, opening
                     ),
                  net.minecraft.world.level.block.Block.UPDATE_CLIENTS
               );
            }
         }
      }

      level.setBlock(
         core,
         furnaceBlock.defaultBlockState()
            .setValue(thaumcraft.block.infernal_furnace.InfernalFurnaceBlock.INDEX, 0)
            .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING, opening),
         net.minecraft.world.level.block.Block.UPDATE_CLIENTS
      );

      // Теперь конструкция цела — можно разослать уведомления соседям (1.7.10: func_147452_c).
      for (int dy = -1; dy <= 1; dy++) {
         for (int dz = -1; dz <= 1; dz++) {
            for (int dx = -1; dx <= 1; dx++) {
               if (dx == 0 && dz == 0 && dy != -1) {
                  continue;
               }

               level.updateNeighborsAt(core.offset(dx, dy, dz), furnaceBlock);
               // TC4: sparkles over every block of the new furnace (event 1).
               level.blockEvent(core.offset(dx, dy, dz), furnaceBlock, 1, 4);
            }
         }
      }

      level.blockEvent(core, furnaceBlock, 1, 4);
   }

   /** Индекс детали по смещению от ядра (эталон: решётка на востоке; steps — повороты восток→opening). */
   private static int arcaneFurnaceIndex(int dx, int dz, int dy, int steps) {
      int rx = dx;
      int rz = dz;

      for (int i = 0; i < steps; i++) {
         int nx = rz;
         int nz = -rx;
         rx = nx;
         rz = nz;
      }

      if (dy < 0) {
         return (rz + 1) * 3 + (rx + 1) + 1;
      }

      int[][] ring = new int[][]{{-1, -1}, {0, -1}, {1, -1}, {-1, 0}, {1, 0}, {-1, 1}, {0, 1}, {1, 1}};

      for (int i = 0; i < ring.length; i++) {
         if (ring[i][0] == rx && ring[i][1] == rz) {
            return (dy == 0 ? 10 : 18) + i;
         }
      }

      return -1;
   }


   /** Триггер 6 (окулус): алтарь с 4 глазами + тёмный узел сверху → портал в Outer Lands (порт 1.7.10). */
   public static boolean createOculus(
      ItemStack wandStack, Player player, Level level, BlockPos clicked, Direction side
   ) {
      if (level.isClientSide() || !(level instanceof net.minecraft.server.level.ServerLevel serverLevel)
         || !(wandStack.getItem() instanceof ItemWandCasting wand)) {
         return false;
      }

      // клик по алтарю или по блоку/узлу рядом с ним: находим алтарь
      thaumcraft.block.eldritch.altar.EldritchAltarBlockEntity altar = null;
      BlockPos altarPos = null;

      if (level.getBlockEntity(clicked) instanceof thaumcraft.block.eldritch.altar.EldritchAltarBlockEntity found) {
         altar = found;
         altarPos = clicked;
      } else {
         for (Direction dir : Direction.values()) {
            if (level.getBlockEntity(clicked.relative(dir)) instanceof thaumcraft.block.eldritch.altar.EldritchAltarBlockEntity found) {
               altar = found;
               altarPos = clicked.relative(dir);
               break;
            }
         }
      }

      // В TC4 переход требует РОВНО четырёх глаз, закрытый алтарь и уже собранный
      // лабиринт: checkForMaze() стоит в условии и возвращает false, пока
      // генерация не завершилась. Размер и сид берутся у алтаря, а не бросаются
      // заново на каждую попытку.
      if (altar == null || altar.getEyes() != 4 || altar.isOpen()) {
         return false;
      }

      BlockPos portalPos = altarPos.above();

      // 1.7.10 (WandManager.createOculus, строки 528-556): над алтарём обязан
      // стоять узел ауры, и именно тёмный. Без этого условия переход доступен
      // где угодно — кольцо элдричей ставит DARK-узел ради этого требования.
      if (!(level.getBlockEntity(portalPos) instanceof thaumcraft.api.nodes.INode nodeAbove)
         || nodeAbove.getNodeType() != thaumcraft.api.nodes.NodeType.DARK) {
         return false;
      }

      if (!altar.checkForMaze()) {
         return false;
      }

      // выделяем область лабиринта в Outer Lands и запоминаем линк
      thaumcraft.dimension.outer_lands.data.PortalLinksData links =
         thaumcraft.dimension.outer_lands.data.PortalLinksData.get(serverLevel);
      if (links != null) {
         links.getOrCreate(serverLevel, portalPos, altar.getMazeSeed(), altar.getMazeW() / 2, altar.getMazeH() / 2);
      }

      AspectList cost = new AspectList()
         .add(Aspect.AIR, 100)
         .add(Aspect.FIRE, 100)
         .add(Aspect.EARTH, 100)
         .add(Aspect.WATER, 100)
         .add(Aspect.ORDER, 100)
         .add(Aspect.ENTROPY, 100);
      if (!wand.consumeAllVisCrafting(wandStack, player, cost, true)) {
         return false;
      }

      level.playSound(
         null, altarPos.getX() + 0.5, altarPos.getY() + 0.5, altarPos.getZ() + 0.5,
         thaumcraft.init.registry.ModSounds.WAND.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F
      );
      altar.setOpen(true);
      altar.setChanged();
      altar.markForUpdate();
      level.removeBlock(portalPos, false);
      level.setBlock(portalPos, thaumcraft.init.registry.ModBlocks.ELDRITCH_PORTAL.get().defaultBlockState(), 3);
      return true;
   }

   /** Стол + жезл без фокуса → магический верстак (как в 1.7.10: ПКМ жезлом по столу). */
   public static boolean createArcaneWorkbench(
      ItemStack wandStack, Player player, Level level, net.minecraft.core.BlockPos pos
   ) {
      if (level.isClientSide()
         || !(wandStack.getItem() instanceof ItemWandCasting wand)
         || wand.getFocus(wandStack) != null) {
         return false;
      }

      level.playSound(
         null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
         thaumcraft.init.registry.ModSounds.WAND.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F
      );
      level.removeBlock(pos, false);
      level.setBlock(pos, thaumcraft.init.registry.ModBlocks.ARCANE_WORKBENCH.get().defaultBlockState(), 3);
      return true;
   }

   /** Котёл + жезл → тигель (как в 1.7.10). */
   public static boolean createCrucible(ItemStack wandStack, Level level, net.minecraft.core.BlockPos pos) {
      if (level.isClientSide()) {
         return false;
      }

      level.playSound(
         null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
         thaumcraft.init.registry.ModSounds.WAND.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F
      );
      level.removeBlock(pos, false);
      level.setBlock(pos, thaumcraft.init.registry.ModBlocks.CRUCIBLE.get().defaultBlockState(), 3);
      // TC4: sparkles over the new crucible (TileCrucible event 1).
      level.blockEvent(pos, thaumcraft.init.registry.ModBlocks.CRUCIBLE.get(), 1, 1);
      return true;
   }


   /** Стекло + узел + деревянные плиты → узел в банке (порт из 1.7.10). */
   public static boolean createNodeJar(ItemStack wandStack, Player player, Level level, net.minecraft.core.BlockPos clicked) {
      for (int xx = clicked.getX() - 2; xx <= clicked.getX(); xx++) {
         for (int yy = clicked.getY() - 3; yy <= clicked.getY(); yy++) {
            for (int zz = clicked.getZ() - 2; zz <= clicked.getZ(); zz++) {
               if (fitNodeJar(level, xx, yy, zz)
                  && wandStack.getItem() instanceof ItemWandCasting wand
                  && wand.consumeAllVisCrafting(
                     wandStack,
                     player,
                     new AspectList()
                        .add(Aspect.FIRE, 70)
                        .add(Aspect.EARTH, 70)
                        .add(Aspect.ORDER, 70)
                        .add(Aspect.AIR, 70)
                        .add(Aspect.ENTROPY, 70)
                        .add(Aspect.WATER, 70),
                     true
                  )) {
                  if (!level.isClientSide()) {
                     replaceNodeJar(level, xx, yy, zz);
                     return true;
                  }

                  return false;
               }
            }
         }
      }

      return false;
   }

   /** Слои: сверху 3x3 деревянных плит, ниже 3x3 стекла, в центре — узел. */
   public static boolean fitNodeJar(Level level, int x, int y, int z) {
      int[][][] blueprint = new int[][][]{
         {{1, 1, 1}, {1, 1, 1}, {1, 1, 1}},
         {{2, 2, 2}, {2, 2, 2}, {2, 2, 2}},
         {{2, 2, 2}, {2, 3, 2}, {2, 2, 2}},
         {{2, 2, 2}, {2, 2, 2}, {2, 2, 2}}
      };

      for (int yy = 0; yy < 4; yy++) {
         for (int xx = 0; xx < 3; xx++) {
            for (int zz = 0; zz < 3; zz++) {
               net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(x + xx, y - yy + 2, z + zz);
               net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);
               int kind = blueprint[yy][xx][zz];
               if (kind == 1 && !state.is(net.minecraft.tags.BlockTags.WOODEN_SLABS)) {
                  return false;
               }

               if (kind == 2 && !state.is(net.minecraft.world.level.block.Blocks.GLASS)) {
                  return false;
               }

               if (kind == 3) {
                  if (!(level.getBlockEntity(pos) instanceof thaumcraft.api.nodes.INode node)
                     || level.getBlockEntity(pos) instanceof thaumcraft.block.jar.node.NodeJarBlockEntity) {
                     return false;
                  }
               }
            }
         }
      }

      return true;
   }

   public static void replaceNodeJar(Level level, int x, int y, int z) {
      if (level.isClientSide()) {
         return;
      }

      int[][][] blueprint = new int[][][]{
         {{1, 1, 1}, {1, 1, 1}, {1, 1, 1}},
         {{2, 2, 2}, {2, 2, 2}, {2, 2, 2}},
         {{2, 2, 2}, {2, 3, 2}, {2, 2, 2}},
         {{2, 2, 2}, {2, 2, 2}, {2, 2, 2}}
      };

      for (int yy = 0; yy < 4; yy++) {
         for (int xx = 0; xx < 3; xx++) {
            for (int zz = 0; zz < 3; zz++) {
               net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(x + xx, y - yy + 2, z + zz);
               if (blueprint[yy][xx][zz] != 3) {
                  level.removeBlock(pos, false);
                  continue;
               }

               if (!(level.getBlockEntity(pos) instanceof thaumcraft.api.nodes.INode node)) {
                  continue;
               }

               AspectList na = node.getAspects().copy();
               int nt = node.getNodeType().ordinal();
               int nm = node.getNodeModifier() != null ? node.getNodeModifier().ordinal() : -1;
               if (level.random.nextFloat() < 0.75F) {
                  if (node.getNodeModifier() == null) {
                     nm = thaumcraft.api.nodes.NodeModifier.PALE.ordinal();
                  } else if (node.getNodeModifier() == thaumcraft.api.nodes.NodeModifier.BRIGHT) {
                     nm = -1;
                  } else if (node.getNodeModifier() == thaumcraft.api.nodes.NodeModifier.PALE) {
                     nm = thaumcraft.api.nodes.NodeModifier.FADING.ordinal();
                  }
               }

               String nid = node.getId();
               int awaken = node instanceof thaumcraft.block.airy.aura_node.AuraNodeBlockEntity aura ? aura.getAwakenPrimalVisAmount() : 0;
               node.setAspects(new AspectList());
               level.removeBlock(pos, false);
               level.setBlock(
                  pos, thaumcraft.init.registry.ModBlocks.NODE_JAR.get().defaultBlockState(), 3
               );
               if (level.getBlockEntity(pos) instanceof thaumcraft.block.jar.node.NodeJarBlockEntity jar) {
                  jar.setAspects(na);
                  if (nm >= 0) {
                     jar.setNodeModifier(thaumcraft.api.nodes.NodeModifier.values()[nm]);
                  }

                  jar.setNodeType(thaumcraft.api.nodes.NodeType.values()[nt]);
                  jar.setId(nid);
                  jar.setAwakenPrimalVisAmount(awaken);
                  // TC4: enchanting glyphs around the sealed jar (event 9).
                  level.blockEvent(pos, thaumcraft.init.registry.ModBlocks.NODE_JAR.get(), 9, 0);
               }
            }
         }
      }

      level.playSound(
         null, x + 0.5, y + 0.5, z + 0.5,
         thaumcraft.init.registry.ModSounds.WAND.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F
      );
   }

   /** Алхимическая конструкция (2 блока столбиком) + жезл → тауматорий (порт из 1.7.10). */
   public static boolean createThaumatorium(
      ItemStack wandStack, Player player, Level level, net.minecraft.core.BlockPos clicked, net.minecraft.core.Direction side
   ) {
      net.minecraft.world.level.block.Block construct = thaumcraft.init.registry.ModBlocks.ALCHEMICAL_CONSTRUCT.get();
      net.minecraft.core.BlockPos base = null;
      if (level.getBlockState(clicked).is(construct) && level.getBlockState(clicked.above()).is(construct)) {
         base = clicked;
      } else if (level.getBlockState(clicked.below()).is(construct) && level.getBlockState(clicked).is(construct)) {
         base = clicked.below();
      }

      if (base == null || !(wandStack.getItem() instanceof ItemWandCasting wand)) {
         return false;
      }

      // Тигель в основании колонны: так рисуют страница исследования и текст
      // («The crucible that forms its base»), без него construction-колонна
      // тауматорием не становится.
      if (!level.getBlockState(base.below()).is(thaumcraft.init.registry.ModBlocks.CRUCIBLE.get())) {
         return false;
      }

      AspectList cost = new AspectList().add(Aspect.FIRE, 15).add(Aspect.ORDER, 30).add(Aspect.WATER, 30);
      if (!wand.consumeAllVisCrafting(wandStack, player, cost, true)) {
         return false;
      }

      if (level.isClientSide()) {
         return false;
      }

      net.minecraft.core.Direction facing = side.getAxis().isHorizontal() ? side : player.getDirection();
      // Колонна двухблочная: центр и верх (оригинал ставит md 10 и md 11). Ставим без
      // уведомления соседей, как func_147465_d(..., 0) в 1.7.10, и разсылаем их
      // отдельным проходом — иначе половина, вставленная первой, разбудит проверку
      // структуры у соседей и тауматорий рассыплется прямо во время сборки.
      net.minecraft.world.level.block.state.BlockState column = thaumcraft.init.registry.ModBlocks.THAUMATORIUM.get()
         .defaultBlockState()
         .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING, facing);
      net.minecraft.world.level.block.state.BlockState bottom = column.setValue(
         thaumcraft.block.thaumatorium.ThaumatoriumBlock.ELEMENT, thaumcraft.block.thaumatorium.ThaumatoriumBlock.Element.CENTER
      );
      net.minecraft.world.level.block.state.BlockState top = column.setValue(
         thaumcraft.block.thaumatorium.ThaumatoriumBlock.ELEMENT, thaumcraft.block.thaumatorium.ThaumatoriumBlock.Element.TOP
      );
      level.setBlock(base, bottom, net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
      level.setBlock(base.above(), top, net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
      level.updateNeighborsAt(base, thaumcraft.init.registry.ModBlocks.THAUMATORIUM.get());
      level.updateNeighborsAt(base.above(), thaumcraft.init.registry.ModBlocks.THAUMATORIUM.get());
      level.playSound(
         null, base.getX() + 0.5, base.getY() + 0.5, base.getZ() + 0.5,
         thaumcraft.init.registry.ModSounds.WAND.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F
      );
      return true;
   }


   /** Рунная матрица + 4 аркан-кирпича + пьедестал → алтарь инфузии (порт из 1.7.10). */
   public static boolean createInfusionAltar(ItemStack wandStack, Player player, Level level, net.minecraft.core.BlockPos matrixPos) {
      if (!(wandStack.getItem() instanceof ItemWandCasting wand)) {
         return false;
      }

      net.minecraft.core.BlockPos anchor = matrixPos.offset(-1, -2, -1);
      if (!fitInfusionAltar(level, anchor)) {
         return false;
      }

      AspectList cost = new AspectList()
         .add(Aspect.FIRE, 25)
         .add(Aspect.EARTH, 25)
         .add(Aspect.ORDER, 25)
         .add(Aspect.AIR, 25)
         .add(Aspect.ENTROPY, 25)
         .add(Aspect.WATER, 25);
      if (!wand.consumeAllVisCrafting(wandStack, player, cost, true)) {
         return false;
      }

      if (level.isClientSide()) {
         return false;
      }

      replaceInfusionAltar(level, anchor);
      level.playSound(
         null, matrixPos.getX() + 0.5, matrixPos.getY() + 0.5, matrixPos.getZ() + 0.5,
         thaumcraft.init.registry.ModSounds.WAND.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F
      );
      return true;
   }

   /**
    * Каноническая проверка структуры алтаря (порт {@code WandManager.fitInfusionAltar} из 1.7.10):
    * сверху — руническая матрица, средний ярус по углам — магический камень (Arcane Stone),
    * нижний ярус по углам — каменные кирпичи (Arcane Stone Bricks), в центре — пьедестал.
    */
   private static boolean fitInfusionAltar(Level level, net.minecraft.core.BlockPos anchor) {
      for (int yy = 0; yy < 3; yy++) {
         for (int xx = 0; xx < 3; xx++) {
            for (int zz = 0; zz < 3; zz++) {
               net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(
                  anchor.getX() + xx, anchor.getY() + 2 - yy, anchor.getZ() + zz
               );
               boolean corner = xx != 1 && zz != 1;
               boolean center = xx == 1 && zz == 1;
               if (yy == 0) {
                  if (center) {
                     if (!level.getBlockState(pos).is(thaumcraft.init.registry.ModBlocks.RUNIC_MATRIX.get())) {
                        return false;
                     }
                  } else if (!level.getBlockState(pos).isAir()) {
                     return false;
                  }
               } else if (yy == 1) {
                  if (corner) {
                     if (!level.getBlockState(pos).is(thaumcraft.init.registry.ModBlocks.ARCANE_STONE.get())) {
                        return false;
                     }
                  } else if (!level.getBlockState(pos).isAir()) {
                     return false;
                  }
               } else {
                  if (corner) {
                     if (!level.getBlockState(pos).is(thaumcraft.init.registry.ModBlocks.ARCANE_BRICKS.get())) {
                        return false;
                     }
                  } else if (center) {
                     if (!(level.getBlockEntity(pos) instanceof thaumcraft.block.arcane_pedestal.ArcanePedestalBlockEntity)) {
                        return false;
                     }
                  } else if (!level.getBlockState(pos).isAir()) {
                     return false;
                  }
               }
            }
         }
      }

      return true;
   }

   /**
    * Каноническая трансформация углов алтаря в резные пилоны (порт {@code WandManager.replaceInfusionAltar}
    * из 1.7.10). Ориентации 2..5 из оригинала строго соответствуют направлению «лицом к центру»:
    * северо-запад → SOUTH, северо-восток → WEST, юго-запад → EAST, юго-восток → NORTH.
    * После трансформации матрица открывается ({@code active = true}).
    */
   private static void replaceInfusionAltar(Level level, net.minecraft.core.BlockPos anchor) {
      for (int yy = 1; yy <= 2; yy++) {
         boolean top = yy == 1;
         for (int xx = 0; xx < 3; xx++) {
            for (int zz = 0; zz < 3; zz++) {
               boolean corner = xx != 1 && zz != 1;
               if (!corner) {
                  continue;
               }

               net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(
                  anchor.getX() + xx, anchor.getY() + 2 - yy, anchor.getZ() + zz
               );
               net.minecraft.world.level.block.state.BlockState state = thaumcraft.init.registry.ModBlocks.INFUSION_PILLAR
                  .get()
                  .defaultBlockState()
                  .setValue(thaumcraft.block.infusion_pillar.InfusionPillarBlock.TOP, top)
                  .setValue(
                     net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING,
                     infusionPillarFacing(xx, zz)
                  );
               level.setBlock(pos, state, 3);
               level.sendBlockUpdated(pos, state, state, 2);
               // TC4 BlockStoneDevice event 1: sparkles and stone-brick dust on each new pillar.
               level.blockEvent(pos, state.getBlock(), 1, 0);
            }
         }
      }

      // В оригинале TileInfusionMatrix.active = true (матрица «раскрывается»).
      net.minecraft.core.BlockPos matrixPos = anchor.offset(1, 2, 1);
      if (level.getBlockEntity(matrixPos) instanceof thaumcraft.block.infusion_matrix.RunicMatrixBlockEntity matrix) {
         matrix.setActive(true);
      }
   }

   /**
    * Ориентация пилона «лицом к центру» матрицы (ориентации 2..5 из 1.7.10 → facing модели).
    * Матрица находится в центре 3x3, поэтому:
    * NW-угол смотрит на юг, NE — на запад, SW — на восток, SE — на север.
    */
   private static net.minecraft.core.Direction infusionPillarFacing(int xx, int zz) {
      if (xx == 0 && zz == 0) {
         return net.minecraft.core.Direction.SOUTH;
      } else if (xx == 2 && zz == 0) {
         return net.minecraft.core.Direction.WEST;
      } else if (xx == 0 && zz == 2) {
         return net.minecraft.core.Direction.EAST;
      } else {
         return net.minecraft.core.Direction.NORTH;
      }
   }

}