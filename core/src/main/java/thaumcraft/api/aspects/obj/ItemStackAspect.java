package thaumcraft.api.aspects.obj;

import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.AspectList;

public class ItemStackAspect extends ObjectAspect {
   public final Item item;
   public final int meta;
   public final boolean strictNbt;
   @Nullable
   private final CompoundTag scanTag;

   public ItemStackAspect(ItemStack stack, AspectList aspects, boolean strictNbt) {
      this(stack.getItem(), ObjectAspectUtils.getLegacyMeta(stack), aspects, strictNbt, ObjectAspectUtils.getScanTag(stack));
   }

   public ItemStackAspect(Item item, int meta, AspectList aspects) {
      this(item, meta, aspects, false, null);
   }

   private ItemStackAspect(Item item, int meta, AspectList aspects, boolean strictNbt, @Nullable CompoundTag scanTag) {
      super(aspects);
      this.item = item;
      this.meta = meta;
      this.strictNbt = strictNbt;
      this.scanTag = scanTag == null ? null : scanTag.copy();
   }

   @Override
   public boolean matches(ItemStack stack) {
      if (!ObjectAspectUtils.matchesBase(stack, this.item, this.meta)) {
         return false;
      } else {
         return !this.strictNbt ? true : Objects.equals(this.scanTag, ObjectAspectUtils.getScanTag(stack));
      }
   }

   @Override
   public int matchPriority(ItemStack stack) {
      if (!this.matches(stack)) {
         return Integer.MIN_VALUE;
      } else if (this.strictNbt) {
         return 400;
      } else {
         return this.meta == 32767 ? 150 : 300;
      }
   }

   @Override
   public ObjectAspectLookupStage lookupStage() {
      return this.meta == 32767 ? ObjectAspectLookupStage.WILDCARD_COMPAT : ObjectAspectLookupStage.EXACT_COMPAT;
   }

   @Nullable
   @Override
   public String getScanKey(ItemStack stack) {
      return !this.strictNbt && stack.getItem() instanceof PotionItem ? null : ObjectAspectUtils.buildExactScanKey(stack, this.meta, this.strictNbt);
   }

   @Override
   public boolean equals(Object o) {
      return !(o instanceof ItemStackAspect that)
         ? super.equals(o)
         : this.item == that.item && this.meta == that.meta && this.strictNbt == that.strictNbt && Objects.equals(this.scanTag, that.scanTag);
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.item, this.meta, this.strictNbt, this.scanTag);
   }
}
