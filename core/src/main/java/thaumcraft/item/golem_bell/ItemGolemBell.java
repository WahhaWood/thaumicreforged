package thaumcraft.item.golem_bell;

import java.util.ArrayList;
import java.util.UUID;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.jetbrains.annotations.Nullable;
import thaumcraft.entities.smart.golem.EntityGolemBase;
import thaumcraft.entities.smart.golem.Marker;
import thaumcraft.entities.smart.travelling_trunk.EntityTravelingTrunk;
import thaumcraft.init.registry.ModItems;
import thaumcraft.item.golem_upgrade.GolemUpgrade;
import thaumcraft.item.golem_upgrade.ItemGolemUpgrade;
import thaumcraft.util.ItemStackData;
import thaumcraft.util.PlayerNotifications;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ItemGolemBell extends Item {
   public ItemGolemBell() {
      super(new Properties().stacksTo(1));
   }

   @Nullable
   public CompoundTag getShareTag(ItemStack stack) {
      return ItemStackData.getTag(stack);
   }

   public void readShareTag(ItemStack stack, @Nullable CompoundTag nbt) {
      ItemStackData.setTag(stack, nbt);
   }

   public static int getGolemId(ItemStack stack) {
      CompoundTag tag = ItemStackData.getTag(stack);
      return tag != null && tag.contains("golemid", 3) ? tag.getInt("golemid") : -1;
   }

   @Nullable
   public static Direction getGolemHomeFace(ItemStack stack) {
      CompoundTag tag = ItemStackData.getTag(stack);
      if (tag != null && tag.contains("golemhomeface", 3)) {
         int v = tag.getInt("golemhomeface");
         if (v >= 0 && v < Direction.values().length) {
            return Direction.values()[v];
         }
      }

      return null;
   }

   @Nullable
   public static BlockPos getGolemHomeCoords(ItemStack stack) {
      CompoundTag tag = ItemStackData.getTag(stack);
      return tag != null && tag.contains("golemhomex", 3) ? new BlockPos(tag.getInt("golemhomex"), tag.getInt("golemhomey"), tag.getInt("golemhomez")) : null;
   }

   public static ArrayList<Marker> getMarkers(ItemStack stack) {
      ArrayList<Marker> markers = new ArrayList<>();
      CompoundTag tag = ItemStackData.getTag(stack);
      if (tag != null && tag.contains("markers", 9)) {
         ListTag list = tag.getList("markers", 10);

         for (int i = 0; i < list.size(); i++) {
            markers.add(Marker.readFromNBT(list.getCompound(i)));
         }
      }

      return markers;
   }

   private static ListTag saveMarkersToList(ArrayList<Marker> markers) {
      ListTag list = new ListTag();

      for (Marker marker : markers) {
         list.add(marker.writeToNBT());
      }

      return list;
   }

   public void changeMarkers(ItemStack stack, Player player, Level level, BlockPos pos, Direction side) {
      ArrayList<Marker> markers = getMarkers(stack);
      int gid = getGolemId(stack);
      boolean markMultipleColors = false;
      if (gid > -1 && level.getEntity(gid) instanceof EntityGolemBase golem && golem.getUpgradeAmount(GolemUpgrade.ORDER.ordinal()) > 0) {
         markMultipleColors = true;
      }

      ResourceKey<Level> dimension = level.dimension();
      boolean changed = false;
      if (!markMultipleColors) {
         int idx = findMarkerIndex(markers, pos, dimension, side, (byte)-1);
         if (idx >= 0) {
            markers.remove(idx);
         } else {
            markers.add(new Marker(pos, dimension, side, (byte)-1));
         }

         changed = true;
      } else {
         ItemGolemBell.MarkerSearchResult searchResult = findExistingMarker(markers, pos, dimension, side);
         if (searchResult.found) {
            markers.remove(searchResult.index);
            boolean sneaking = player != null && player.isShiftKeyDown();
            int nextColor = searchResult.color + 1;
            if (!sneaking && nextColor <= 15) {
               markers.add(new Marker(pos, dimension, side, (byte)nextColor));
               this.notifyColorChange(level, nextColor);
            }

            changed = true;
         } else {
            markers.add(new Marker(pos, dimension, side, (byte)-1));
            changed = true;
         }
      }

      if (changed) {
         saveMarkersToStack(stack, markers, gid, level);
      }
   }

   @Deprecated
   public void changeMarkers(ItemStack stack, Player player, Level level, int x, int y, int z, int side) {
      this.changeMarkers(stack, player, level, new BlockPos(x, y, z), Direction.from3DDataValue(side));
   }

   private static int findMarkerIndex(ArrayList<Marker> markers, BlockPos pos, ResourceKey<Level> dimension, Direction side, byte color) {
      for (int i = 0; i < markers.size(); i++) {
         Marker m = markers.get(i);
         if (m.matchesPosition(pos, dimension, side) && m.getColor() == color) {
            return i;
         }
      }

      return -1;
   }

   private static ItemGolemBell.MarkerSearchResult findExistingMarker(ArrayList<Marker> markers, BlockPos pos, ResourceKey<Level> dimension, Direction side) {
      for (int color = -1; color <= 15; color++) {
         int idx = findMarkerIndex(markers, pos, dimension, side, (byte)color);
         if (idx >= 0) {
            return new ItemGolemBell.MarkerSearchResult(true, idx, color);
         }
      }

      return new ItemGolemBell.MarkerSearchResult(false, -1, -1);
   }

   /** Port of ItemGolemBell.resetMarkers (TC4): the F key with a bell clears every marker of the bound golem. */
   public static void resetMarkers(ItemStack stack, Level level, Player player) {
      int gid = getGolemId(stack);
      if (gid > -1 && level.getEntity(gid) instanceof EntityGolemBase) {
         saveMarkersToStack(stack, new ArrayList<>(), gid, level);
         level.playSound(
            null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS,
            0.7F, 1.0F + level.random.nextFloat() * 0.1F
         );
      }
   }

   private static void saveMarkersToStack(ItemStack stack, ArrayList<Marker> markers, int golemId, Level level) {
      CompoundTag tag = ItemStackData.getOrCreateTag(stack);
      tag.put("markers", saveMarkersToList(markers));
      if (golemId > -1) {
         if (level.getEntity(golemId) instanceof EntityGolemBase golem) {
            golem.replaceMarkers(markers);
         } else {
            clearGolemData(tag);
         }
      }

      ItemStackData.setTag(stack, tag);
   }

   private static void clearGolemData(CompoundTag tag) {
      tag.remove("golemid");
      tag.remove("markers");
      tag.remove("golemhomex");
      tag.remove("golemhomey");
      tag.remove("golemhomez");
      tag.remove("golemhomeface");
   }

   private void notifyColorChange(Level level, int color) {
      if (level.isClientSide) {
         this.notifyMarkerChange(color);
      }
   }

   public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
      Level level = context.getLevel();
      Player player = context.getPlayer();
      BlockPos pos = context.getClickedPos();
      Direction face = context.getClickedFace();
      this.changeMarkers(stack, player, level, pos, face);
      return InteractionResult.sidedSuccess(level.isClientSide);
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      BlockHitResult hit = getPlayerPOVHitResult(level, player, Fluid.ANY, 5.0);
      if (hit.getType() == Type.BLOCK && isMarkableFluid(level, hit.getBlockPos())) {
         this.changeMarkers(stack, player, level, hit.getBlockPos(), hit.getDirection());
         return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
      } else {
         return InteractionResultHolder.pass(stack);
      }
   }

   /** ПКМ колоколом по голему: первый клик — выбрать (подтянуть метки), второй — снять выбор. */
   public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
      if (!(target instanceof EntityGolemBase golem)) {
         return InteractionResult.PASS;
      }

      if (target.level().isClientSide) {
         if (player != null) {
            player.swing(hand);
         }

         return InteractionResult.SUCCESS;
      }

      CompoundTag tag = ItemStackData.getOrCreateTag(stack);
      int currentId = getGolemId(stack);
      if (currentId != golem.getId()) {
         clearGolemData(tag);
         tag.putInt("golemid", golem.getId());
         BlockPos home = golem.blockPosition();
         tag.putInt("golemhomex", home.getX());
         tag.putInt("golemhomey", home.getY());
         tag.putInt("golemhomez", home.getZ());
         tag.putInt("golemhomeface", golem.homeFacing.ordinal());
         tag.put("markers", saveMarkersToList(golem.getMarkers()));
         ItemStackData.setTag(stack, tag);
         if (player != null) {
            player.displayClientMessage(Component.translatable("tc.golem.selected"), true);
         }
      } else {
         clearGolemData(tag);
         ItemStackData.setTag(stack, tag);
         if (player != null) {
            player.displayClientMessage(Component.translatable("tc.golem.deselected"), true);
         }
      }

      if (player != null) {
         player.swing(hand);
      }

      return InteractionResult.SUCCESS;
   }

   public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
      if (entity instanceof EntityTravelingTrunk trunk && entity.isAlive()) {
         return this.handleTrunkPickup(trunk, player, entity);
      } else {
         return entity instanceof EntityGolemBase golem && entity.isAlive() ? this.handleGolemPickup(golem, player, entity) : false;
      }
   }

   private boolean handleTrunkPickup(EntityTravelingTrunk trunk, Player player, Entity entity) {
      byte upgrade = (byte)trunk.getUpgrade();
      if (upgrade == GolemUpgrade.WATER.ordinal() && entity instanceof OwnableEntity ownable) {
         UUID owner = ownable.getOwnerUUID();
         if (owner != null && player != null && !owner.equals(player.getUUID())) {
            return false;
         }
      }

      if (entity.level().isClientSide) {
         spawnExplosionParticle(entity);
         return false;
      } else {
         ItemStack dropped = new ItemStack((ItemLike)ModItems.TRUNK_SPAWNER.get());
         boolean dismantling = player != null && player.isShiftKeyDown();
         if (dismantling) {
            if (upgrade > -1 && entity.level().random.nextBoolean()) {
               dropUpgrade(entity, GolemUpgrade.values()[upgrade]);
            }
         } else {
            if (trunk.hasCustomName()) {
               dropped.set(DataComponents.CUSTOM_NAME, trunk.getCustomName());
            }

            CompoundTag t = ItemStackData.getOrCreateTag(dropped);
            t.putByte("upgrade", upgrade);
            if (upgrade == GolemUpgrade.ORDER.ordinal()) {
               t.put("inventory", trunk.getInventory().serializeNBT());
            }
         }

         entity.spawnAtLocation(dropped, 0.5F);
         if (upgrade != GolemUpgrade.ORDER.ordinal() || dismantling) {
            trunk.getInventory().dropAllItems(trunk);
         }

         playZapSound(entity.level(), entity.blockPosition());
         entity.discard();
         return true;
      }
   }

   private boolean handleGolemPickup(EntityGolemBase golem, Player player, Entity entity) {
      if (entity.level().isClientSide) {
         spawnExplosionParticle(entity);
         return false;
      } else {
         return true;
      }
   }

   private static void dropUpgrade(Entity entity, GolemUpgrade upgrade) {
      ItemStack upgradeStack = ItemGolemUpgrade.getStackByUpgrade(upgrade);
      entity.spawnAtLocation(upgradeStack, 0.5F);
   }

   private static void spawnExplosionParticle(Entity entity) {
      entity.level().addParticle(ParticleTypes.EXPLOSION, entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ(), 0.0, 0.0, 0.0);
   }

   private static void playZapSound(Level level, BlockPos pos) {
      SoundEvent sound = (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("thaumcraft", "zap"));
      if (sound != null) {
         level.playSound(null, pos, sound, SoundSource.PLAYERS, 0.5F, 1.0F);
      }
   }

   private static boolean isMarkableFluid(Level level, BlockPos pos) {
      return !level.getFluidState(pos).isEmpty() && level.getBlockState(pos).getBlock() instanceof LiquidBlock;
   }

   private static BlockHitResult getPlayerPOVHitResult(Level level, Player player, Fluid fluidMode, double range) {
      Vec3 startVec = player.getEyePosition();
      Vec3 lookVec = player.getLookAngle();
      Vec3 endVec = startVec.add(lookVec.x * range, lookVec.y * range, lookVec.z * range);
      return level.clip(new ClipContext(startVec, endVec, Block.OUTLINE, fluidMode, player));
   }

   @OnlyIn(Dist.CLIENT)
   public void notifyMarkerChange(int color) {
      String text;
      if (color > -1) {
         String base = I18n.get("tc.markerchange", new Object[0]);
         String name;
         if (color >= 0 && color <= 15) {
            DyeColor dye = DyeColor.byId(color);
            name = I18n.get("color.minecraft." + dye.getName(), new Object[0]);
         } else {
            name = String.valueOf(color);
         }

         text = base.replace("%n", name);
      } else {
         text = I18n.get("tc.markerchangeany", new Object[0]);
      }

      PlayerNotifications.addNotification(Component.literal(text));
   }

   private record MarkerSearchResult(boolean found, int index, int color) {
   }
}
