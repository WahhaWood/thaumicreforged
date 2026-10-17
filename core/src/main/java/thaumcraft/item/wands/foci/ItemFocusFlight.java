package thaumcraft.item.wands.foci;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.item.wands.ItemWandCasting;

public class ItemFocusFlight extends ItemFocusBasic {
   private static final AspectList visUsage = new AspectList().add(Aspect.AIR, 15);

   public ItemFocusFlight() {
      super("flight");
   }

   @Override
   public String getSortingHelper(ItemStack itemstack) {
      return "FLIGHT" + super.getSortingHelper(itemstack);
   }

   @Override
   public int getFocusColor(ItemStack stack) {
      return 10416895;
   }

   @Override
   public AspectList getVisCost(ItemStack stack) {
      return visUsage;
   }

   @Override
   public ItemFocusBasic.WandFocusAnimation getAnimation(ItemStack stack) {
      return ItemFocusBasic.WandFocusAnimation.WAVE;
   }

   @Override
   public ItemStack onFocusRightClick(ItemStack wandStack, Level level, Player player, InteractionHand hand) {
      ItemWandCasting wand = (ItemWandCasting)wandStack.getItem();
      if (wand.consumeAllVis(wandStack, player, this.getVisCost(wandStack), true, false)) {
         Vec3 look = player.getLookAngle();
         double force = 0.6666666666666666 * (1.0 + wand.getFocusPotency(wandStack) * 0.2);
         player.setDeltaMovement(look.scale(force));
         player.fallDistance = 0.0F;
         player.hurtMarked = true;
         if (player instanceof ServerPlayer sp) {
            try {
               ObfuscationReflectionHelper.setPrivateValue(ServerGamePacketListenerImpl.class, sp.connection, 0, "aboveGroundTickCount");
            } catch (Exception var11) {
            }
         }

         for (int i = 0; i < 5; i++) {
            Thaumcraft.setup
               .smokeSpiral(
                  level,
                  player.getX(),
                  player.getY() - player.getDeltaMovement().y,
                  player.getZ(),
                  2.0F,
                  (int)(Math.random() * 360.0),
                  (int)player.getY(),
                  647935
               );
         }

         ResourceLocation soundId = ResourceLocation.fromNamespaceAndPath("thaumcraft", "wind");
         SoundEvent se = (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(soundId);
         if (se != null) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), se, SoundSource.PLAYERS, 0.4F, 1.0F);
         }
      }

      if (level.isClientSide) {
         player.swing(hand);
      }

      return wandStack;
   }

   @Override
   public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack itemStack, int rank) {
      return switch (rank) {
         case 1, 2, 3, 4, 5 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency};
         default -> null;
      };
   }
}
