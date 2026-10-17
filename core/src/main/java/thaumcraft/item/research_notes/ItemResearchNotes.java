package thaumcraft.item.research_notes;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.research.ResearchApi;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.research.ResearchManager;
import thaumcraft.research.ResearchNoteData;
import thaumcraft.util.ItemStackData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ItemResearchNotes extends Item {
   public ItemResearchNotes() {
      super(new Properties().stacksTo(1));
   }

   public void setDiscovery(ItemStack stack) {
      ItemStackData.putBoolean(stack, "discovery", true);
   }

   public boolean isDiscovery(ItemStack stack) {
      return ItemStackData.hasTag(stack) && ItemStackData.getTag(stack).getBoolean("discovery");
   }

   public void setKnowledgeFragment(ItemStack stack) {
      ItemStackData.putBoolean(stack, "knowledge_fragment", true);
   }

   public boolean isKnowledgeFragment(ItemStack stack) {
      return ItemStackData.hasTag(stack) && ItemStackData.getTag(stack).getBoolean("knowledge_fragment");
   }

   @NotNull
   public Component getName(@NotNull ItemStack stack) {
      return (Component)(this.isDiscovery(stack) ? Component.translatable("item.discovery.name") : super.getName(stack));
   }

   @OnlyIn(Dist.CLIENT)
   public void appendHoverText(
      @NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> components, @NotNull TooltipFlag tooltipFlag
   ) {
      Level level = context.level();
      if (this.isKnowledgeFragment(stack)) {
         components.add(Component.translatable("item.researchnotes.unknown.1").setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)));
         components.add(Component.translatable("item.researchnotes.unknown.2").setStyle(Style.EMPTY.withColor(ChatFormatting.BLUE)));
      }

      ResearchNoteData rd = ResearchManager.getData(stack);
      if (rd != null && rd.key != null) {
         ResearchItem researchItem = ResearchApi.getResearch(rd.key);
         if (researchItem != null) {
            components.add(researchItem.getName().setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)));
            components.add(researchItem.getText().setStyle(Style.EMPTY.withItalic(true)));
            int warp = ThaumcraftApi.getWarp(rd.key);
            if (warp > 0) {
               if (warp > 5) {
                  warp = 5;
               }

               String ws = I18n.get("tc.forbidden", new Object[0]);
               String wr = I18n.get("tc.forbidden.level." + warp, new Object[0]);
               String wte = ws.replaceAll("%n", wr);
               components.add(Component.literal(wte).setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE)));
            }
         }
      }
   }

   @NotNull
   @Override
   public net.minecraft.world.InteractionResultHolder<ItemStack> use(Level level, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (this.isKnowledgeFragment(stack)) {
         if (!level.isClientSide) {
            thaumcraft.research.PlayerKnowledge knowledge = thaumcraft.research.PlayerKnowledge.of(player);
            java.util.List<Aspect> primals = new java.util.ArrayList<>(Aspect.getPrimalAspects());
            Aspect gained = primals.get(level.random.nextInt(primals.size()));
            knowledge.addAspectPool(gained, (short)(1 + level.random.nextInt(3)));
            if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
               thaumcraft.network.PacketHandler.sendTo(
                  new thaumcraft.network.playerdata.PacketAspectPool(gained.getTag(), (short)1, knowledge.getAspectPoolFor(gained)), sp
               );
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), (net.minecraft.sounds.SoundEvent)thaumcraft.init.registry.ModSounds.LEARN.get(), net.minecraft.sounds.SoundSource.PLAYERS, 0.7F, 1.0F);
            if (!player.getAbilities().instabuild) {
               stack.shrink(1);
            }
         }
         return net.minecraft.world.InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
      }

      ResearchNoteData rd = ResearchManager.getData(stack);
      if (rd != null && rd.key != null) {
         if (rd.complete) {
            if (!level.isClientSide) {
               if (ResearchManager.isResearchComplete(player, rd.key)) {
                  player.displayClientMessage(
                     Component.literal("\u00a75[Thaumcraft] \u00a7eYou already know this research."), false
                  );
               } else if (!ResearchManager.doesPlayerHaveRequisites(player, rd.key)) {
                  // В 1.7.10 без этой проверки игрок «изучал» исследование,
                  // на которое условия не выполнены.
                  player.displayClientMessage(Component.translatable("tc.researcherror"), false);
               } else {
                  ResearchApi.completeResearch(player, rd.key);
                  // Вместе с ключевым закрываются «соседи» с выполненными условиями.
                  ResearchItem research = ResearchApi.getResearch(rd.key);
                  if (research != null && research.siblings != null) {
                     for (String sibling : research.siblings) {
                        if (!ResearchManager.isResearchComplete(player, sibling)
                           && ResearchManager.doesPlayerHaveRequisites(player, sibling)) {
                           ResearchApi.completeResearch(player, sibling);
                        }
                     }
                  }

                  if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
                     thaumcraft.network.playerdata.PlayerKnowledgeSync.syncFull(sp);
                  }

                  // The research warp is already added by completeResearch (split into permanent and sticky).
                  player.displayClientMessage(
                     Component.literal("\u00a75[Thaumcraft] \u00a7aResearch Completed: \u00a7d"
                        + (research != null ? research.getName().getString() : rd.key)), false
                  );
                  level.playSound(null, player.getX(), player.getY(), player.getZ(),
                     (net.minecraft.sounds.SoundEvent)thaumcraft.init.registry.ModSounds.LEARN.get(),
                     net.minecraft.sounds.SoundSource.PLAYERS, 0.75F, 1.0F);
                  if (!player.getAbilities().instabuild) {
                     stack.shrink(1);
                  }
               }
            }
            return net.minecraft.world.InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
         } else {
            // Незавершённый лист относят на стол исследований — там мини-игра.
            if (!level.isClientSide) {
               player.displayClientMessage(
                  Component.literal("\u00a7cThis research is incomplete.\u00a7r Place it on a Research Table."),
                  false
               );
            }
            return net.minecraft.world.InteractionResultHolder.fail(stack);
         }
      }

      return net.minecraft.world.InteractionResultHolder.pass(stack);
   }

   @NotNull
   public Rarity getRarity(@NotNull ItemStack stack) {
      return this.isDiscovery(stack) ? Rarity.EPIC : Rarity.RARE;
   }
}
