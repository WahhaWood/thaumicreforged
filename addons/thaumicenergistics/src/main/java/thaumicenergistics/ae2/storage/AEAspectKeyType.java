package thaumicenergistics.ae2.storage;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Objects;
import thaumicenergistics.ThaumicEnergistics;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;

public class AEAspectKeyType extends AEKeyType {
   public static final AEAspectKeyType INSTANCE = new AEAspectKeyType();

   private AEAspectKeyType() {
      super(ThaumicEnergistics.location("aspect"), AEAspectKey.class, Component.translatable("thaumicenergistics.key_type.aspect"));
   }

   @Nullable
   public AEKey readFromPacket(RegistryFriendlyByteBuf input) {
      return AEAspectKey.fromPacket(input);
   }

   public MapCodec<AEAspectKey> codec() {
      return Codec.STRING.xmap(tag -> Objects.requireNonNull(AEAspectKey.of(Aspect.getAspect(tag))), key -> key.getAspect().getTag()).fieldOf("aspect");
   }

   @Nullable
   public AEKey loadKeyFromTag(Provider registries, CompoundTag tag) {
      return AEAspectKey.fromTag(tag);
   }

   public int getAmountPerOperation() {
      return 1;
   }

   public int getAmountPerByte() {
      return 8;
   }
}
