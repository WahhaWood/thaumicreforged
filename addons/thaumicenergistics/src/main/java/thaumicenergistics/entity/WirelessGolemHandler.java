package thaumicenergistics.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import thaumicenergistics.init.ModItems;
import thaumicenergistics.item.ItemGolemWirelessBackpack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thaumcraft.api.entities.IGolemHookHandler;
import thaumcraft.api.entities.IGolemHookSyncRegistry;
import thaumcraft.api.entities.IGolemHookHandler.InteractionLevel;
import thaumcraft.entities.smart.golem.EntityGolemBase;
import thaumcraft.item.golem_core.GolemCore;

public class WirelessGolemHandler implements IGolemHookHandler {
   private static final Logger LOG = LoggerFactory.getLogger(WirelessGolemHandler.class);
   private static WirelessGolemHandler INSTANCE = null;
   private static final String NBTKEY_ACCESS_POINT = "wifiBackpackAP";
   private static final char SYNCFLAG_HAS_WIFI_IN_RANGE = 'i';
   private static final char SYNCFLAG_HAS_WIFI_OUT_OF_RANGE = 'o';
   private static final char SYNCFLAG_NO_WIFI = 'n';
   private static final char SYNCFIELD_SKIN = 'a';
   private int wifiSyncID = -1;
   private int skinSyncID = -1;

   private WirelessGolemHandler() {
   }

   public static WirelessGolemHandler getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new WirelessGolemHandler();
      }

      return INSTANCE;
   }

   private char backpackSkinToSyncChar(ItemGolemWirelessBackpack.BackpackSkins skin) {
      int id = skin != null ? skin.ordinal() : 0;
      return (char)(97 + id);
   }

   private ItemGolemWirelessBackpack.BackpackSkins syncCharToBackpackSkin(char skinSyncChar) {
      int id = skinSyncChar - 'a';
      if (id < 0 || id >= ItemGolemWirelessBackpack.BackpackSkins.VALUES.length) {
         id = 0;
      }

      return ItemGolemWirelessBackpack.BackpackSkins.VALUES[id];
   }

   private static EntityGolemBase asGolem(Entity entity) {
      return (EntityGolemBase)entity;
   }

   private InteractionLevel canHandleInteraction_Backpack(EntityGolemBase golem, Object handlerData, Player player, boolean isClientSide) {
      if (handlerData != null) {
         return InteractionLevel.NoInteraction;
      } else {
         GolemCore core = golem.getCore();
         if (core == null) {
            return InteractionLevel.NoInteraction;
         } else {
            switch (core) {
               case GATHER:
               case FILL:
               case LIQUID:
               case ESSENCE:
                  if (isClientSide) {
                     player.swing(InteractionHand.MAIN_HAND);
                     return InteractionLevel.NoInteraction;
                  }

                  return InteractionLevel.FullInteraction;
               default:
                  return InteractionLevel.NoInteraction;
            }
         }
      }
   }

   public void addDefaultSyncEntries(@Nonnull IGolemHookSyncRegistry syncRegistry) {
      this.wifiSyncID = syncRegistry.registerSyncChar(this, 'n');
      this.skinSyncID = syncRegistry.registerSyncChar(this, this.backpackSkinToSyncChar(ItemGolemWirelessBackpack.BackpackSkins.Thaumium));
   }

   public void bellLeftClicked(
      @Nonnull Entity entity,
      @Nullable Object handlerData,
      @Nonnull ItemStack itemGolemPlacer,
      @Nonnull Player player,
      boolean dismantled,
      boolean isClientSide
   ) {
      EntityGolemBase golem = asGolem(entity);
      if (handlerData instanceof WirelessGolemHandler.WirelessServerData wsd) {
         ItemStack backpack = new ItemStack((ItemLike)ModItems.GOLEM_WIFI_BACKPACK.get());
         ItemGolemWirelessBackpack.setLinkedPosition(backpack, wsd.linkedPosition);
         golem.spawnAtLocation(backpack, 0.5F);
      }
   }

   @Nonnull
   public InteractionLevel canHandleInteraction(@Nonnull Entity entity, @Nullable Object handlerData, @Nonnull Player player, boolean isClientSide) {
      EntityGolemBase golem = asGolem(entity);
      ItemStack heldItem = player.getMainHandItem();
      if (heldItem.isEmpty()) {
         return InteractionLevel.NoInteraction;
      } else {
         return heldItem.getItem() instanceof ItemGolemWirelessBackpack
            ? this.canHandleInteraction_Backpack(golem, handlerData, player, isClientSide)
            : InteractionLevel.NoInteraction;
      }
   }

   @Nullable
   public Object customInteraction(
      @Nonnull Entity entity, @Nullable Object handlerData, @Nonnull IGolemHookSyncRegistry syncData, @Nonnull Player player, boolean isClientSide
   ) {
      EntityGolemBase golem = asGolem(entity);
      ItemStack heldItem = player.getMainHandItem();
      if (heldItem.getItem() instanceof ItemGolemWirelessBackpack) {
         GlobalPos linkedPos = ItemGolemWirelessBackpack.getLinkedPosition(heldItem);
         if (linkedPos != null) {
            this.playSound(golem, "upgrade");
            if (!player.getAbilities().instabuild) {
               heldItem.shrink(1);
            }

            return new WirelessGolemHandler.WirelessServerData(linkedPos);
         }
      }

      return null;
   }

   public void golemTick(@Nonnull Entity entity, @Nullable Object serverHandlerData, @Nonnull IGolemHookSyncRegistry syncData) {
      EntityGolemBase golem = asGolem(entity);
      if (serverHandlerData instanceof WirelessGolemHandler.WirelessServerData wsd && ++wsd.tickCounter > 18) {
         wsd.tickCounter = 0;
         syncData.updateSyncChar(this, this.wifiSyncID, (char)(wsd.isInRange ? 'i' : 'o'));
         syncData.updateSyncChar(this, this.skinSyncID, this.backpackSkinToSyncChar(wsd.skin));
      }
   }

   public boolean needsDynamicUpdates() {
      return true;
   }

   public boolean needsRenderer() {
      return true;
   }

   @Nullable
   public Object readEntityFromNBT(@Nonnull Entity entity, @Nonnull CompoundTag nbtTag) {
      if (nbtTag.contains("wifiBackpackAP", 10)) {
         GlobalPos pos = GlobalPos.CODEC
            .decode(NbtOps.INSTANCE, nbtTag.get("wifiBackpackAP"))
            .resultOrPartial(Util.prefix("Golem backpack AP", LOG::error))
            .<GlobalPos>map(Pair::getFirst)
            .orElse(null);
         if (pos != null) {
            return new WirelessGolemHandler.WirelessServerData(pos);
         }
      }

      return null;
   }

   @OnlyIn(Dist.CLIENT)
   public void renderGolem(
      @Nonnull Entity entity,
      @Nullable Object clientHandlerData,
      @Nonnull PoseStack poseStack,
      @Nonnull MultiBufferSource bufferSource,
      int packedLight,
      float partialTick
   ) {
      EntityGolemBase golem = asGolem(entity);
      if (clientHandlerData instanceof WirelessGolemHandler.WirelessClientData wcd) {
         boolean golemActive = wcd.isInRange && !golem.inactive;
         if (golemActive && !Minecraft.getInstance().isPaused()) {
            wcd.pearlRotation += partialTick * 2.0F;
            if (wcd.pearlRotation >= 360.0F) {
               wcd.pearlRotation -= 360.0F;
            }
         }

         GolemBackpackRenderer.render(golem, wcd, poseStack, bufferSource, packedLight, partialTick);
      }
   }

   @Nullable
   public Object setupGolem(@Nonnull Entity entity, @Nullable Object handlerData, @Nonnull IGolemHookSyncRegistry syncData, boolean isClientSide) {
      EntityGolemBase golem = asGolem(entity);
      return isClientSide ? handlerData : handlerData;
   }

   @Nullable
   public Object spawnGolemFromItemStack(@Nonnull Entity entity, @Nonnull ItemStack itemGolemPlacer, boolean isClientSide) {
      return null;
   }

   @OnlyIn(Dist.CLIENT)
   public Object syncDataChanged(@Nonnull IGolemHookSyncRegistry syncData, @Nullable Object clientHandlerData) {
      char syncFlag = syncData.getSyncCharOrDefault(this.wifiSyncID, 'n');
      boolean inRange;
      if (syncFlag == 'i') {
         inRange = true;
      } else {
         if (syncFlag != 'o') {
            return null;
         }

         inRange = false;
      }

      WirelessGolemHandler.WirelessClientData wcd;
      if (clientHandlerData instanceof WirelessGolemHandler.WirelessClientData existing) {
         wcd = existing;
      } else {
         wcd = new WirelessGolemHandler.WirelessClientData();
      }

      wcd.isInRange = inRange;
      char skinChar = syncData.getSyncCharOrDefault(this.skinSyncID, 'a');
      wcd.skin = this.syncCharToBackpackSkin(skinChar);
      return wcd;
   }

   public void writeEntityNBT(@Nonnull Entity entity, @Nullable Object serverHandlerData, @Nonnull CompoundTag nbtTag) {
      if (serverHandlerData instanceof WirelessGolemHandler.WirelessServerData wsd) {
         GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, wsd.linkedPosition).result().ifPresent(tag -> nbtTag.put("wifiBackpackAP", tag));
      }
   }

   private void playSound(EntityGolemBase golem, String path) {
      SoundEvent se = (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("thaumcraft", path));
      if (se != null) {
         golem.level().playSound(null, golem.blockPosition(), se, SoundSource.NEUTRAL, 0.5F, 1.0F);
      }
   }

   public static class WirelessClientData {
      public boolean isInRange = false;
      public float pearlRotation = 0.0F;
      public ItemGolemWirelessBackpack.BackpackSkins skin = ItemGolemWirelessBackpack.BackpackSkins.Thaumium;
   }

   public static class WirelessServerData {
      public final GlobalPos linkedPosition;
      public int tickCounter = 0;
      public boolean isInRange = false;
      public ItemGolemWirelessBackpack.BackpackSkins skin = ItemGolemWirelessBackpack.BackpackSkins.Thaumium;

      public WirelessServerData(GlobalPos linkedPos) {
         this.linkedPosition = linkedPos;
      }
   }
}
