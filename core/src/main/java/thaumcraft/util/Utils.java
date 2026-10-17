package thaumcraft.util;

import it.unimi.dsi.fastutil.Pair;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.BonemealEvent;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.LevelBlockPos;
import thaumcraft.api.internal.WeightedRandomLoot;
import thaumcraft.init.registry.ModItems;
import thaumcraft.mixin.common.LevelChunkSectionAccessor;
import thaumcraft.network.PacketHandler;
import thaumcraft.network.fx.PacketFXVisDrain;

public class Utils {
   public static HashMap<Ingredient, Pair<ItemStack, Float>> specialMiningResult = new HashMap<>();
   public static final String[] colorNames = new String[]{
      "White", "Orange", "Magenta", "Light Blue", "Yellow", "Lime", "Pink", "Gray", "Light Gray", "Cyan", "Purple", "Blue", "Brown", "Green", "Red", "Black"
   };
   public static final int[] colors = new int[]{
      15790320, 15435844, 12801229, 6719955, 14602026, 4312372, 14188952, 4408131, 10526880, 2651799, 8073150, 2437522, 5320730, 3887386, 11743532, 1973019
   };
   public static HashMap<LevelBlockPos, Long> effectBuffer = new HashMap<>();
   static ResourceLocation particleTexture = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/particles.png");
   static ResourceLocation particleTexture1 = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/particles2.png");

   public static InteractionResult useBonemealAtLoc(Level level, Player player, BlockPos blockPos) {
      BlockState blockState = level.getBlockState(blockPos);
      BonemealEvent bonemealEvent = new BonemealEvent(player, level, blockPos, blockState, new ItemStack(Items.BONE_MEAL));
      NeoForge.EVENT_BUS.post(bonemealEvent);
      if (bonemealEvent.isCanceled()) {
         return bonemealEvent.isSuccessful() ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS;
      } else if (blockState.getBlock() instanceof BonemealableBlock growable && growable.isValidBonemealTarget(level, blockPos, blockState)) {
         if (level instanceof ServerLevel serverLevel && growable.isBonemealSuccess(serverLevel, level.random, blockPos, blockState)) {
            growable.performBonemeal(serverLevel, level.random, blockPos, blockState);
         }

         return InteractionResult.sidedSuccess(level.isClientSide);
      } else {
         return InteractionResult.PASS;
      }
   }

   public static ResourceLocation getParticles() {
      return particleTexture;
   }

   public static ResourceLocation getParticles1() {
      return particleTexture1;
   }

   public static boolean setBiomeAt(Level level, BlockPos pos, ResourceKey<Biome> biomeKey) {
      if (biomeKey == null) {
         return false;
      } else {
         Registry<Biome> registry = level.registryAccess().registryOrThrow(Registries.BIOME);
         return !registry.containsKey(biomeKey) ? false : setBiomeAt(level, pos, registry.getHolderOrThrow(biomeKey));
      }
   }

   public static boolean setBiomeAt(Level level, BlockPos pos, Holder<Biome> biome) {
      if (biome == null) {
         return false;
      } else {
         LevelChunk chunk = level.getChunkAt(pos);
         return level instanceof ServerLevel serverLevel ? setBiomeAtServer(serverLevel, chunk, pos, biome) : setBiomeAtDirect(chunk, pos, biome);
      }
   }

   private static boolean setBiomeAtServer(ServerLevel level, LevelChunk chunk, BlockPos pos, Holder<Biome> biome) {
      int targetQuartX = QuartPos.fromBlock(pos.getX());
      int targetQuartZ = QuartPos.fromBlock(pos.getZ());
      int minQuartY = QuartPos.fromBlock(level.getMinBuildHeight());
      Holder<Biome>[] snapshot = snapshotChunkBiomes(chunk);
      int height = chunk.getSections().length * 4;
      boolean changed = false;

      for (int localY = 0; localY < height; localY++) {
         if (snapshot[snapshotIndex(targetQuartX & 3, localY, targetQuartZ & 3)] != null
            && !snapshot[snapshotIndex(targetQuartX & 3, localY, targetQuartZ & 3)].equals(biome)) {
            changed = true;
            break;
         }
      }

      if (!changed) {
         return false;
      } else {
         BiomeResolver resolver = (quartX, quartY, quartZ, sampler) -> {
            if (quartX == targetQuartX && quartZ == targetQuartZ) {
               return biome;
            } else {
               int localYx = quartY - minQuartY;
               if (localYx >= 0 && localYx < height) {
                  Holder<Biome> current = snapshot[snapshotIndex(quartX & 3, localYx, quartZ & 3)];
                  return current != null ? current : biome;
               } else {
                  return biome;
               }
            }
         };
         chunk.fillBiomesFromNoise(resolver, level.getChunkSource().randomState().sampler());
         chunk.setUnsaved(true);
         level.getChunkSource().chunkMap.resendBiomesForChunks(List.of(chunk));
         return true;
      }
   }

   private static boolean setBiomeAtDirect(LevelChunk chunk, BlockPos pos, Holder<Biome> biome) {
      int qxLocal = QuartPos.fromBlock(pos.getX()) & 3;
      int qzLocal = QuartPos.fromBlock(pos.getZ()) & 3;
      boolean changed = false;

      for (LevelChunkSection section : chunk.getSections()) {
         if (section != null) {
            PalettedContainerRO<Holder<Biome>> currentBiomes = section.getBiomes();
            PalettedContainer<Holder<Biome>> updatedBiomes = currentBiomes.recreate();
            boolean sectionChanged = false;

            for (int qyLocal = 0; qyLocal < 4; qyLocal++) {
               if (!((Holder)currentBiomes.get(qxLocal, qyLocal, qzLocal)).equals(biome)) {
                  updatedBiomes.set(qxLocal, qyLocal, qzLocal, biome);
                  sectionChanged = true;
               }
            }

            if (sectionChanged) {
               ((LevelChunkSectionAccessor)section).setBiomes(updatedBiomes);
               changed = true;
            }
         }
      }

      if (changed) {
         chunk.setUnsaved(true);
      }

      return changed;
   }

   private static Holder<Biome>[] snapshotChunkBiomes(LevelChunk chunk) {
      LevelChunkSection[] sections = chunk.getSections();
      Holder<Biome>[] snapshot = new Holder[sections.length * 64];

      for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
         LevelChunkSection section = sections[sectionIndex];
         if (section != null) {
            PalettedContainerRO<Holder<Biome>> biomes = section.getBiomes();

            for (int localY = 0; localY < 4; localY++) {
               int globalLocalY = sectionIndex * 4 + localY;

               for (int localZ = 0; localZ < 4; localZ++) {
                  for (int localX = 0; localX < 4; localX++) {
                     snapshot[snapshotIndex(localX, globalLocalY, localZ)] = (Holder<Biome>)biomes.get(localX, localY, localZ);
                  }
               }
            }
         }
      }

      return snapshot;
   }

   private static int snapshotIndex(int localX, int localY, int localZ) {
      return localY * 16 + localZ * 4 + localX;
   }

   public static boolean hasColor(byte[] array) {
      int length = array.length;

      for (int i = 0; i < length; i++) {
         if (array[i] >= 0) {
            return true;
         }
      }

      return false;
   }

   public static int getFirstUncoveredY(LevelAccessor world, int x, int z) {
      int minY = Math.max(world.getMinBuildHeight() + 5, 5);
      int maxY = world.getMaxBuildHeight() - 2;
      int y = minY;

      while (y < maxY && !world.isEmptyBlock(new BlockPos(x, y + 1, z))) {
         y++;
      }

      return y;
   }

   public static boolean isEETransmutionItem(Item item) {
      try {
         if (Class.forName("com.pahimar.ee3.item.ITransmutationStone").isAssignableFrom(item.getClass())) {
            return true;
         }
      } catch (Exception var2) {
      }

      return false;
   }

   public static void copyFile(File file, File file2) throws IOException {
      if (!file2.exists()) {
         file2.createNewFile();
      }

      FileInputStream inputFile = new FileInputStream(file);
      FileInputStream inputFile2 = new FileInputStream(file2);

      try (
         FileChannel channel = inputFile.getChannel();
         FileChannel channel2 = inputFile2.getChannel();
      ) {
         channel2.transferFrom(channel, 0L, channel.size());
      }

      inputFile.close();
      inputFile2.close();
   }

   public static int getFirstUncoveredBlockHeight(LevelAccessor world, int x, int z) {
      int minY = Math.max(world.getMinBuildHeight() + 10, 10);
      int maxY = Math.min(world.getMaxBuildHeight() - 2, 250);
      int y = minY;

      while (y < maxY && !world.isEmptyBlock(new BlockPos(x, y + 1, z))) {
         y++;
      }

      return y;
   }

   public static void addSpecialMiningResult(ItemStack input, ItemStack output, float chance) {
      specialMiningResult.put(Ingredient.of(new ItemStack[]{input}), Pair.of(output, chance));
   }

   public static void addSpecialMiningResult(TagKey<Item> input, ItemStack output, float chance) {
      specialMiningResult.put(Ingredient.of(input), Pair.of(output, chance));
   }

   public static ItemStack findSpecialMiningResult(ItemStack input, float fortune, RandomSource random) {
      for (Entry<Ingredient, Pair<ItemStack, Float>> entry : specialMiningResult.entrySet()) {
         if (entry.getKey().test(input)) {
            Pair<ItemStack, Float> pair = entry.getValue();
            if (random.nextFloat() <= fortune * (Float)pair.value()) {
               ItemStack stack = ((ItemStack)pair.key()).copy();
               stack.setCount(stack.getCount() * input.getCount());
               return stack;
            }

            return input;
         }
      }

      return input;
   }

   public static float clamp_float(float n, float n2, float n3) {
      return n < n2 ? n2 : (n > n3 ? n3 : n);
   }

   public static boolean isWoodLog(BlockGetter blockAccess, int n, int n2, int n3) {
      BlockPos pos = new BlockPos(n, n2, n3);
      BlockState state = blockAccess.getBlockState(pos);
      return state.is(BlockTags.LOGS) && state.getDestroySpeed(blockAccess, pos) >= 0.0F;
   }

   public static void resetFloatCounter(ServerPlayer player) {
      try {
         ObfuscationReflectionHelper.setPrivateValue(ServerGamePacketListenerImpl.class, player.connection, 0, "aboveGroundTickCount");
      } catch (Exception var2) {
      }
   }

   public static boolean getBit(int n, int n2) {
      return (n & 1 << n2) != 0;
   }

   public static int setBit(int n, int n2) {
      return n | 1 << n2;
   }

   public static int clearBit(int n, int n2) {
      return n & ~(1 << n2);
   }

   public static int toggleBit(int n, int n2) {
      return n ^ 1 << n2;
   }

   public static byte pack(boolean[] array) {
      byte b = 0;
      int length = array.length;

      for (int i = 0; i < length; i++) {
         b = (byte)(b << 1 | (array[i] ? 1 : 0));
      }

      return b;
   }

   public static boolean[] unpack(byte b) {
      boolean[] array = new boolean[8];

      for (int i = 0; i < 8; i++) {
         array[i] = (byte)(b >> 7 - i & 1) == 1;
      }

      return array;
   }

   public static Object getNBTDataFromId(CompoundTag nbtTagCompound, byte b, String s) {
      return switch (b) {
         case 1 -> nbtTagCompound.getByte(s);
         case 2 -> nbtTagCompound.getShort(s);
         case 3 -> nbtTagCompound.getInt(s);
         case 4 -> nbtTagCompound.getLong(s);
         case 5 -> nbtTagCompound.getFloat(s);
         case 6 -> nbtTagCompound.getDouble(s);
         case 7 -> nbtTagCompound.getByteArray(s);
         case 8 -> nbtTagCompound.getString(s);
         case 9 -> nbtTagCompound.getList(s, 10);
         case 10 -> nbtTagCompound.get(s);
         case 11 -> nbtTagCompound.getIntArray(s);
         default -> null;
      };
   }

   public static void generateVisEffect(Level level, BlockPos blockPos, BlockPos blockPosNode, int n8) {
      LevelBlockPos worldCoordinates = new LevelBlockPos(blockPos, level.dimension());
      Long value = System.currentTimeMillis();
      Random random = new Random(value);
      if (effectBuffer.containsKey(worldCoordinates)) {
         if (effectBuffer.get(worldCoordinates) < value) {
            effectBuffer.remove(worldCoordinates);
         }
      } else {
         effectBuffer.put(worldCoordinates, value + 500L + random.nextInt(100));
         PacketHandler.sendToAllAround(new PacketFXVisDrain(blockPos, blockPosNode, n8), level, blockPos, 64.0);
      }
   }

   public static boolean isLyingInCone(Vec3 array, Vec3 array2, Vec3 array3, float n) {
      double n2 = n / 2.0F;
      Vec3 dif = array2.subtract(array);
      Vec3 dif2 = array2.subtract(array3);
      double dotProd = dif.multiply(dif2.x, dif2.y, dif2.z).lengthSqr();
      double magn = dif.length();
      double magn2 = dif2.length();
      return dotProd / magn / magn2 > Math.cos(n2) && dotProd / magn2 < magn2;
   }

   public static double dotProd(double[] array, double[] array2) {
      return array[0] * array2[0] + array[1] * array2[1] + array[2] * array2[2];
   }

   public static double[] dif(double[] array, double[] array2) {
      return new double[]{array[0] - array2[0], array[1] - array2[1], array[2] - array2[2]};
   }

   public static double magn(double[] array) {
      return Math.sqrt(array[0] * array[0] + array[1] * array[1] + array[2] * array[2]);
   }

   public static Vec3 calculateVelocity(Vec3 vec3, Vec3 vec4, double n, double n2) {
      double n3 = vec4.y - vec3.y;
      double sqrt = Math.sqrt(distanceSquared2d(vec3, vec4));
      double n4 = n > n3 + n ? n : n3 + n;
      double n5 = -sqrt * sqrt / (4.0 * n4);
      double n7 = -sqrt / (2.0 * n5) - Math.sqrt(sqrt * sqrt - 4.0 * n5 * -n3) / (2.0 * n5);
      double sqrt2 = Math.sqrt(n4 * n2);
      double n8 = sqrt2 / n7;
      double n9 = vec4.x - vec3.x;
      double n10 = vec4.z - vec3.z;
      double sqrt3 = Math.sqrt(n9 * n9 + n10 * n10);
      return new Vec3(n8 * (n9 / sqrt3), sqrt2, n8 * (n10 / sqrt3));
   }

   public static double distanceSquared2d(Vec3 vec3, Vec3 vec4) {
      double n = vec4.x - vec3.x;
      double n2 = vec4.z - vec3.z;
      return n * n + n2 * n2;
   }

   public static double distanceSquared3d(Vec3 vec3, Vec3 vec4) {
      double n = vec4.x - vec3.x;
      double n2 = vec4.y - vec3.y;
      double n3 = vec4.z - vec3.z;
      return n * n + n2 * n2 + n3 * n3;
   }

   @NotNull
   public static ItemStack generateLoot(int level, RandomSource random, RegistryAccess registryAccess) {
      ItemStack itemStack = ItemStack.EMPTY;
      if (level > 0 && random.nextFloat() < 0.025F * level) {
         itemStack = genGear(level, random, registryAccess);
         if (itemStack == null) {
            itemStack = generateLoot(level, random, registryAccess);
         }
      } else {
         itemStack = WeightedRandom.getRandomItem(random, switch (level) {
            case 1 -> WeightedRandomLoot.lootBagUncommon;
            case 2 -> WeightedRandomLoot.lootBagRare;
            default -> WeightedRandomLoot.lootBagCommon;
         }).map(loot -> loot.item).orElse(ItemStack.EMPTY);
      }

      if (itemStack.getItem() == Items.BOOK) {
         EnchantmentHelper.enchantItem(random, itemStack, (int)(5.0F + level * 0.75F * random.nextInt(18)), enchantments(registryAccess));
      }

      return itemStack.copy();
   }

   private static ItemStack genGear(int n, RandomSource random, RegistryAccess registryAccess) {
      int nextInt = random.nextInt(2);
      if (random.nextFloat() < 0.2F) {
         nextInt++;
      }

      if (random.nextFloat() < 0.15F) {
         nextInt++;
      }

      if (random.nextFloat() < 0.1F) {
         nextInt++;
      }

      if (random.nextFloat() < 0.095F) {
         nextInt++;
      }

      if (random.nextFloat() < 0.095F) {
         nextInt++;
      }

      Item item = getGearItemForSlot(random.nextInt(5), nextInt);
      if (item != null) {
         ItemStack stack = new ItemStack(item);
         int maxDamage = stack.getMaxDamage();
         if (maxDamage > 0) {
            stack.setDamageValue(random.nextInt(1 + maxDamage / 6));
         }

         if (random.nextInt(4) < n) {
            EnchantmentHelper.enchantItem(random, stack, (int)(5.0F + n * 0.75F * random.nextInt(18)), enchantments(registryAccess));
         }

         return stack;
      } else {
         return null;
      }
   }

   private static Stream<Holder<Enchantment>> enchantments(RegistryAccess registryAccess) {
      return registryAccess.lookupOrThrow(Registries.ENCHANTMENT).listElements().map(holder -> (Holder<Enchantment>)holder);
   }

   private static Item getGearItemForSlot(int n, int n2) {
      switch (n) {
         case 0:
            if (n2 == 0) {
               return Items.IRON_AXE;
            }

            if (n2 == 1) {
               return Items.IRON_SWORD;
            }

            if (n2 == 2) {
               return Items.GOLDEN_AXE;
            }

            if (n2 == 3) {
               return Items.GOLDEN_SWORD;
            }

            if (n2 == 4) {
               return (Item)ModItems.THAUMIUM_SWORD.get();
            }

            if (n2 == 5) {
               return Items.DIAMOND_SWORD;
            }

            if (n2 == 6) {
               return (Item)ModItems.VOID_SWORD.get();
            }
            break;
         case 1:
            if (n2 == 0) {
               return Items.LEATHER_BOOTS;
            }

            if (n2 == 1) {
               return Items.GOLDEN_BOOTS;
            }

            if (n2 == 2) {
               return Items.CHAINMAIL_BOOTS;
            }

            if (n2 == 3) {
               return Items.IRON_BOOTS;
            }

            if (n2 == 4) {
               return (Item)ModItems.THAUMIUM_BOOTS.get();
            }

            if (n2 == 5) {
               return Items.DIAMOND_BOOTS;
            }

            if (n2 == 6) {
               return (Item)ModItems.VOID_BOOTS.get();
            }
            break;
         case 2:
            if (n2 == 0) {
               return Items.LEATHER_LEGGINGS;
            }

            if (n2 == 1) {
               return Items.GOLDEN_LEGGINGS;
            }

            if (n2 == 2) {
               return Items.CHAINMAIL_LEGGINGS;
            }

            if (n2 == 3) {
               return Items.IRON_LEGGINGS;
            }

            if (n2 == 4) {
               return (Item)ModItems.THAUMIUM_LEGGINGS.get();
            }

            if (n2 == 5) {
               return Items.DIAMOND_LEGGINGS;
            }

            if (n2 == 6) {
               return (Item)ModItems.VOID_LEGGINGS.get();
            }
            break;
         case 3:
            if (n2 == 0) {
               return Items.LEATHER_CHESTPLATE;
            }

            if (n2 == 1) {
               return Items.GOLDEN_CHESTPLATE;
            }

            if (n2 == 2) {
               return Items.CHAINMAIL_CHESTPLATE;
            }

            if (n2 == 3) {
               return Items.IRON_CHESTPLATE;
            }

            if (n2 == 4) {
               return (Item)ModItems.THAUMIUM_CHEST.get();
            }

            if (n2 == 5) {
               return Items.DIAMOND_CHESTPLATE;
            }

            if (n2 == 6) {
               return (Item)ModItems.VOID_CHEST.get();
            }
            break;
         case 4:
            if (n2 == 0) {
               return Items.LEATHER_HELMET;
            }

            if (n2 == 1) {
               return Items.GOLDEN_HELMET;
            }

            if (n2 == 2) {
               return Items.CHAINMAIL_HELMET;
            }

            if (n2 == 3) {
               return Items.IRON_HELMET;
            }

            if (n2 == 4) {
               return (Item)ModItems.THAUMIUM_HELMET.get();
            }

            if (n2 == 5) {
               return Items.DIAMOND_HELMET;
            }

            if (n2 == 6) {
               return (Item)ModItems.VOID_HELMET.get();
            }
      }

      return null;
   }

   private Utils() {
   }
}
