package thaumicenergistics.ae2.storage;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;

public final class AEAspectKey extends AEKey {
   private static final String NBT_ASPECT_TAG = "aspect";
   private final Aspect aspect;

   private AEAspectKey(Aspect aspect) {
      this.aspect = Objects.requireNonNull(aspect, "aspect must not be null");
   }

   @Nullable
   public static AEAspectKey of(@Nullable Aspect aspect) {
      return aspect == null ? null : new AEAspectKey(aspect);
   }

   @Nullable
   public static AEAspectKey fromTag(CompoundTag tag) {
      String aspectTag = tag.getString("aspect");
      if (aspectTag.isEmpty()) {
         return null;
      } else {
         Aspect aspect = Aspect.getAspect(aspectTag);
         return aspect != null ? new AEAspectKey(aspect) : null;
      }
   }

   @Nullable
   public static AEAspectKey fromPacket(RegistryFriendlyByteBuf buf) {
      String aspectTag = buf.readUtf();
      Aspect aspect = Aspect.getAspect(aspectTag);
      return aspect != null ? new AEAspectKey(aspect) : null;
   }

   public Aspect getAspect() {
      return this.aspect;
   }

   public AEKeyType getType() {
      return AEAspectKeyType.INSTANCE;
   }

   public AEKey dropSecondary() {
      return this;
   }

   public CompoundTag toTag(Provider registries) {
      CompoundTag tag = new CompoundTag();
      tag.putString("aspect", this.aspect.getTag());
      return tag;
   }

   public Object getPrimaryKey() {
      return this.aspect;
   }

   public ResourceLocation getId() {
      return ResourceLocation.fromNamespaceAndPath("thaumcraft", this.aspect.getTag());
   }

   public void writeToPacket(RegistryFriendlyByteBuf data) {
      data.writeUtf(this.aspect.getTag());
   }

   public boolean hasComponents() {
      return false;
   }

   protected Component computeDisplayName() {
      return this.aspect.getName();
   }

   public void addDrops(long amount, List<ItemStack> drops, Level level, BlockPos pos) {
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         AEAspectKey that = (AEAspectKey)o;
         return this.aspect == that.aspect;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return System.identityHashCode(this.aspect);
   }

   public String toString() {
      return "AEAspectKey{" + this.aspect.getTag() + "}";
   }
}
