package thaumcraft.api.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public interface IGolemHookHandler {
   void addDefaultSyncEntries(@Nonnull IGolemHookSyncRegistry var1);

   void bellLeftClicked(@Nonnull Entity var1, @Nullable Object var2, @Nonnull ItemStack var3, @Nonnull Player var4, boolean var5, boolean var6);

   @Nonnull
   IGolemHookHandler.InteractionLevel canHandleInteraction(@Nonnull Entity var1, @Nullable Object var2, @Nonnull Player var3, boolean var4);

   @Nullable
   Object customInteraction(@Nonnull Entity var1, @Nullable Object var2, @Nonnull IGolemHookSyncRegistry var3, @Nonnull Player var4, boolean var5);

   void golemTick(@Nonnull Entity var1, @Nullable Object var2, @Nonnull IGolemHookSyncRegistry var3);

   boolean needsDynamicUpdates();

   boolean needsRenderer();

   @Nullable
   Object readEntityFromNBT(@Nonnull Entity var1, @Nonnull CompoundTag var2);

   @OnlyIn(Dist.CLIENT)
   void renderGolem(@Nonnull Entity var1, @Nullable Object var2, @Nonnull PoseStack var3, @Nonnull MultiBufferSource var4, int var5, float var6);

   @Nullable
   Object setupGolem(@Nonnull Entity var1, @Nullable Object var2, @Nonnull IGolemHookSyncRegistry var3, boolean var4);

   @Nullable
   Object spawnGolemFromItemStack(@Nonnull Entity var1, @Nonnull ItemStack var2, boolean var3);

   @OnlyIn(Dist.CLIENT)
   Object syncDataChanged(@Nonnull IGolemHookSyncRegistry var1, @Nullable Object var2);

   void writeEntityNBT(@Nonnull Entity var1, @Nullable Object var2, @Nonnull CompoundTag var3);

   public static enum InteractionLevel {
      NoInteraction,
      BasicInteraction,
      SyncInteraction,
      FullInteraction;
   }
}
