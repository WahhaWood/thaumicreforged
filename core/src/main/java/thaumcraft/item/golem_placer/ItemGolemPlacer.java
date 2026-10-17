package thaumcraft.item.golem_placer;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import thaumcraft.entities.smart.golem.EntityGolemBase;
import thaumcraft.entities.smart.golem.EnumGolemType;
import thaumcraft.item.golem_core.GolemCore;
import thaumcraft.item.golem_bell.ItemGolemBell;
import thaumcraft.init.registry.ModItems;
import thaumcraft.item.golem_decoration.GolemDecorationType;
import thaumcraft.util.ItemStackData;

public class ItemGolemPlacer extends Item {
   private final EnumGolemType golemType;

   public ItemGolemPlacer(EnumGolemType golemType) {
      super(new Properties().stacksTo(1));
      this.golemType = golemType;
   }

   public EnumGolemType getGolemType() {
      return this.golemType;
   }

   public static Item getItemForType(EnumGolemType type) {
      return switch (type) {
         case STRAW -> (Item)ModItems.GOLEM_STRAW.get();
         case WOOD -> (Item)ModItems.GOLEM_WOOD.get();
         case TALLOW -> (Item)ModItems.GOLEM_TALLOW.get();
         case CLAY -> (Item)ModItems.GOLEM_CLAY.get();
         case FLESH -> (Item)ModItems.GOLEM_FLESH.get();
         case STONE -> (Item)ModItems.GOLEM_STONE.get();
         case IRON -> (Item)ModItems.GOLEM_IRON.get();
         case THAUMIUM -> (Item)ModItems.GOLEM_THAUMIUM.get();
      };
   }

   public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
      return true;
   }

   public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext ctx) {
      return this.trySpawn(ctx);
   }

   public InteractionResult useOn(UseOnContext ctx) {
      return this.trySpawn(ctx);
   }

   /**
    * Поставить голема — порт {@code ItemGolemPlacer.onItemUseFirst} + {@code spawnCreature}
    * из 1.7.10.
    *
    * <p>Раньше здесь была заглушка: метод считал позицию и возвращал null, сущность
    * не создавалась вообще. Из-за этого вся вкладка Golemancy (39 исследований,
    * ядра, апгрейды, декорации) в обычной игре была бесполезна — голема нельзя
    * было поставить ни одним предметом. Регрессия этого не ловила, потому что
    * сценарий спавнит големов отладочным мостом, а не предметом.
    */
   private InteractionResult trySpawn(UseOnContext ctx) {
      Level level = ctx.getLevel();
      Player player = ctx.getPlayer();
      if (player == null || player.isShiftKeyDown()) {
         return InteractionResult.PASS;
      }

      // В 1.7.10 метод на сервере ВСЕГДА сообщал «обработано», даже если
      // поставить голема не удалось — чтобы предмет не превращался в обычную
      // установку блока. Сохраняем это поведение.
      if (level.isClientSide) {
         return InteractionResult.SUCCESS;
      }

      BlockPos clickedPos = ctx.getClickedPos();
      Direction face = ctx.getClickedFace();
      Block clickedBlock = level.getBlockState(clickedPos).getBlock();
      BlockPos spawnPos = clickedPos.relative(face);
      // На заборе голем стоит на полблока выше (как в оригинале).
      double yOff = 0.0;
      if (face == Direction.UP
         && (clickedBlock == Blocks.OAK_FENCE || clickedBlock == Blocks.NETHER_BRICK_FENCE)) {
         yOff = 0.5;
      }

      ItemStack stack = ctx.getItemInHand();
      boolean spawned = this.spawnCreature(
         level, spawnPos.getX() + 0.5, spawnPos.getY() + yOff, spawnPos.getZ() + 0.5, face, stack, player);
      if (spawned && !player.getAbilities().instabuild) {
         stack.shrink(1);
      }

      return InteractionResult.SUCCESS;
   }

   /** Создать и настроить голема по нбт предмета — порт {@code spawnCreature}. */
   public boolean spawnCreature(
      Level level, double x, double y, double z, Direction side, ItemStack stack, Player player
   ) {
      CompoundTag tag = ItemStackData.getTag(stack);
      boolean advanced = tag != null && tag.getBoolean("advanced");
      EntityGolemBase golem = new EntityGolemBase(level, this.golemType, advanced);
      golem.moveTo(x, y, z, level.random.nextFloat() * 360.0F, 0.0F);
      // Аналог func_110171_b(x, y, z, 32) — «дом» голема.
      golem.restrictTo(BlockPos.containing(x, y, z), 32);

      if (tag != null && tag.contains("core", 1)) {
         int ordinal = tag.getByte("core");
         GolemCore[] cores = GolemCore.values();
         golem.setCore(ordinal >= 0 && ordinal < cores.length ? cores[ordinal] : GolemCore.BLANK);
      }

      golem.setup(side);
      level.addFreshEntity(golem);

      if (tag != null && tag.contains("DecorationMask", 3)) {
         golem.setDecorationsFromMask(tag.getInt("DecorationMask"));
      }

      golem.setOwner(player.getName().getString());
      golem.setMarkers(ItemGolemBell.getMarkers(stack));

      if (tag != null && tag.contains("upgrades", 7)) {
         byte[] saved = tag.getByteArray("upgrades");
         int slots = golem.upgrades.length;

         for (int i = 0; i < saved.length; i++) {
            golem.setUpgrade(i, i < slots ? saved[i] : (byte)-1);
         }
      }

      if (stack.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME)) {
         golem.setCustomName(stack.getHoverName());
         golem.setPersistenceRequired();
      }

      if (tag != null && tag.contains("Inventory", 9)) {
         golem.getInventory().readFromNBT(tag.getList("Inventory", 10));
      }

      return true;
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
      Level level = context.level();
      CompoundTag tag = ItemStackData.getTag(stack);
      if (tag != null) {
         if (tag.contains("core", 1)) {
            int core = tag.getByte("core");
            MutableComponent line = Component.translatable("item.ItemGolemCore.name")
               .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
               .append(Component.translatable("item.ItemGolemCore." + core + ".name").withStyle(ChatFormatting.GOLD));
            tooltip.add(line);
         }

         if (tag.contains("advanced")) {
            tooltip.add(Component.translatable("tc.adv").withStyle(ChatFormatting.GOLD));
         }

         if (tag.contains("upgrades", 7)) {
            byte[] ba = tag.getByteArray("upgrades");
            MutableComponent line = Component.empty().withStyle(ChatFormatting.BLUE);

            for (byte b : ba) {
               if (b > -1) {
                  line.append(Component.translatable("item.ItemGolemUpgrade." + b + ".name").withStyle(ChatFormatting.BLUE));
                  line.append(Component.literal(" ").withStyle(ChatFormatting.BLUE));
               }
            }

            tooltip.add(line);
         }

         if (tag.contains("markers", 9)) {
            ListTag tl = tag.getList("markers", 10);
            MutableComponent line = Component.literal(String.valueOf(tl.size()))
               .append(Component.literal(" ").withStyle(ChatFormatting.DARK_PURPLE))
               .append(Component.translatable("tc.markedloc").withStyle(ChatFormatting.DARK_PURPLE))
               .withStyle(ChatFormatting.DARK_PURPLE);
            tooltip.add(line);
         }

         if (tag.contains("DecorationMask", 3)) {
            int mask = tag.getInt("DecorationMask");
            if (mask > 0) {
               MutableComponent line = Component.empty().withStyle(ChatFormatting.DARK_GREEN);

               for (GolemDecorationType type : GolemDecorationType.values()) {
                  if ((mask >> type.ordinal() & 1) == 1) {
                     line.append(Component.translatable("item.ItemGolemDecoration." + type.ordinal() + ".name").withStyle(ChatFormatting.DARK_GREEN))
                        .append(Component.literal(" ").withStyle(ChatFormatting.DARK_GREEN));
                  }
               }

               tooltip.add(line);
            }
         }
      }
   }

   public static float getOverlayPredicate(ItemStack stack) {
      CompoundTag tag = ItemStackData.getTag(stack);
      boolean adv = tag != null && tag.contains("advanced");
      boolean core = tag != null && tag.contains("core");
      if (adv && core) {
         return 3.0F;
      } else if (adv) {
         return 1.0F;
      } else {
         return core ? 2.0F : 0.0F;
      }
   }
}
