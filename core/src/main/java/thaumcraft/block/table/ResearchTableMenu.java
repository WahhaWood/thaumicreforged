package thaumcraft.block.table;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.IScribeTools;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.container.SlotLimitedByClass;
import thaumcraft.init.registry.ModMenuTypes;
import thaumcraft.item.research_notes.ItemResearchNotes;

public class ResearchTableMenu extends AbstractContainerMenu {
   public ResearchTableBlockEntity table;
   String[] aspects;
   Player player;

   public ResearchTableMenu(int windowsId, Inventory inventory, FriendlyByteBuf buf) {
      this(windowsId, inventory, clientTable(inventory, buf.readBlockPos()));
   }

   /**
    * Клиентский конструктор: блок-энтити может быть ещё не загружено (чанк не догружен),
    * и тогда слоты создались бы с null-контейнером, а экран упал бы с NPE прямо в
    * отрисовке. Поэтому подставляем пустышку — вход в GUI остаётся безопасным.
    */
   private static ResearchTableBlockEntity clientTable(Inventory inventory, net.minecraft.core.BlockPos pos) {
      Level level = inventory.player.level();
      if (level != null && level.getBlockEntity(pos) instanceof ResearchTableBlockEntity table) {
         return table;
      }

      return new ResearchTableBlockEntity(pos,
         thaumcraft.init.registry.ModBlocks.RESEARCH_TABLE.get().defaultBlockState());
   }

   public ResearchTableMenu(int windowsId, Inventory inventory, ResearchTableBlockEntity table) {
      super((MenuType)ModMenuTypes.RESEARCH_TABLE.get(), windowsId);
      this.player = inventory.player;
      this.table = table;
      this.aspects = Aspect.aspects.keySet().toArray(new String[0]);
      this.addSlot(new SlotLimitedByClass(IScribeTools.class, table, 0, 14, 10));
      this.addSlot(new SlotLimitedByClass(ItemResearchNotes.class, table, 1, 70, 10));
      this.bindPlayerInventory(inventory);
   }

   protected void bindPlayerInventory(Inventory inventoryPlayer) {
      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(inventoryPlayer, j + i * 9 + 9, 48 + j * 18, 175 + i * 18));
         }
      }

      for (int k = 0; k < 9; k++) {
         this.addSlot(new Slot(inventoryPlayer, k, 48 + k * 18, 233));
      }
   }

   @NotNull
   public ItemStack quickMoveStack(@NotNull Player player, int n) {
      ItemStack copy = ItemStack.EMPTY;
      Slot slot = this.getSlot(n);
      if (slot != null && slot.hasItem()) {
         ItemStack stack = slot.getItem();
         copy = stack.copy();
         if (n < 2) {
            if (!this.moveItemStackTo(stack, 2, this.slots.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(stack, 0, 2, false)) {
            return ItemStack.EMPTY;
         }

         if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }
      }

      return copy;
   }

   public boolean stillValid(@NotNull Player player) {
      return this.table.stillValid(player);
   }

   /**
    * Кнопки GUI стола: 5 — копирование завершённой записки.
    * Порт {@code ContainerResearchTable.func_75140_a} (1.7.10).
    */
   @Override
   public boolean clickMenuButton(@NotNull Player player, int button) {
      if (button == 5 && player instanceof net.minecraft.server.level.ServerPlayer) {
         this.duplicate(player);
         return true;
      }

      return super.clickMenuButton(player, button);
   }

   /**
    * Копия завершённой записки — порт {@code TileResearchTable.duplicate}.
    * Нужны бумага и чернила в инвентаре игрока, в пуле — теги исследования
    * плюс уже сделанные копии за каждый аспект.
    */
   public void duplicate(@NotNull Player player) {
      if (this.table == null) {
         return;
      }

      ItemStack noteStack = this.getSlot(1).getItem();
      if (noteStack.isEmpty() || !(noteStack.getItem() instanceof ItemResearchNotes)) {
         return;
      }

      thaumcraft.research.ResearchNoteData data = thaumcraft.research.ResearchManager.getData(noteStack);
      if (data == null || !data.complete) {
         return;
      }

      thaumcraft.api.research.ResearchItem research =
         thaumcraft.api.research.ResearchApi.getResearch(data.key);
      if (research == null || research.tags == null) {
         return;
      }

      thaumcraft.research.PlayerKnowledge knowledge = thaumcraft.research.PlayerKnowledge.of(player);
      for (thaumcraft.api.aspects.Aspect aspect : research.tags.getAspects()) {
         if (knowledge.getAspectPoolFor(aspect) < research.tags.getAmount(aspect) + data.copies) {
            return;
         }
      }

      int paper = this.findInInventory(player, net.minecraft.world.item.Items.PAPER);
      int ink = this.findInInventory(player, net.minecraft.world.item.Items.INK_SAC);
      if (paper < 0 || ink < 0) {
         return;
      }

      for (thaumcraft.api.aspects.Aspect aspect : research.tags.getAspects()) {
         short cost = (short)(-(research.tags.getAmount(aspect) + data.copies));
         knowledge.addAspectPool(aspect, cost);
         if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
            thaumcraft.network.PacketHandler.sendTo(
               new thaumcraft.network.playerdata.PacketAspectPool(
                  aspect.getTag(), cost, knowledge.getAspectPoolFor(aspect)), sp
            );
         }
      }

      player.getInventory().getItem(paper).shrink(1);
      player.getInventory().getItem(ink).shrink(1);
      data.copies++;
      thaumcraft.research.ResearchManager.updateData(noteStack, data);
      noteStack.grow(1);
      this.table.playLearnEffect();
      this.table.setChanged();
      this.table.markForUpdate();
      this.broadcastChanges();
   }

   /** Слот инвентаря игрока с предметом, -1 если нет. */
   private int findInInventory(Player player, net.minecraft.world.item.Item item) {
      for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
         if (player.getInventory().getItem(i).is(item)) {
            return i;
         }
      }

      return -1;
   }
}
