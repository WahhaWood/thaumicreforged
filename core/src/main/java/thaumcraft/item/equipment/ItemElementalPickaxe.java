package thaumcraft.item.equipment;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.api.IRepairable;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.init.registry.ModSounds;

/**
 * Кирка огня (Pickaxe of the Core) — порт {@code ItemElementalPickaxe} из TC4 1.7.10.
 * ПКМ — подсветка руды (скан 8 блоков, 5 секунд), удар — поджигает цель.
 */
public class ItemElementalPickaxe extends PickaxeItem implements IRepairable {
   public ItemElementalPickaxe() {
      super(
         ThaumcraftApi.TOOL_MATERIAL_ELEMENTAL,
         new Properties().rarity(Rarity.RARE).attributes(DiggerItem.createAttributes(ThaumcraftApi.TOOL_MATERIAL_ELEMENTAL, 1.0F, -2.8F))
      );
   }

   @NotNull
   public InteractionResult useOn(@NotNull UseOnContext context) {
      Level level = context.getLevel();
      BlockPos pos = context.getClickedPos();
      Player player = context.getPlayer();
      ItemStack stack = context.getItemInHand();
      if (player == null) {
         return InteractionResult.PASS;
      }

      if (!level.isClientSide) {
         stack.hurtAndBreak(5, player, EquipmentSlot.MAINHAND);
         level.playSound(
            null,
            pos.getX() + 0.5,
            pos.getY() + 0.5,
            pos.getZ() + 0.5,
            ModSounds.WAND_FAIL.get(),
            SoundSource.BLOCKS,
            0.2F,
            0.2F + level.random.nextFloat() * 0.2F
         );
      } else {
         // Подсветка руды вокруг (как startScan в оригинале).
         Thaumcraft.instance.renderEventHandler.startScan(player, pos, Util.getMillis() + 5000L, 8);
      }

      player.swing(context.getHand(), true);
      return InteractionResult.SUCCESS;
   }

   @Override
   public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
      // Порт onLeftClickEntity: поджигаем цель на 2 секунды (кроме игроков без PvP).
      boolean pvp = !(target instanceof Player)
         || target.level().getServer() != null && target.level().getServer().isPvpAllowed();
      if (!target.level().isClientSide && pvp) {
         target.igniteForSeconds(2.0F);
      }

      return super.hurtEnemy(stack, target, attacker);
   }
}
