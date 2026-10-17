package thaumcraft.data.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ICondition.IContext;
import thaumcraft.Thaumcraft;
import thaumcraft.init.registry.Config;

public class ConfigBooleanCondition implements ICondition {
   public static final ResourceLocation ID = Thaumcraft.location("config_flag");
   public static final MapCodec<ConfigBooleanCondition> CODEC = RecordCodecBuilder.mapCodec(
      builder -> builder.group(ExtraCodecs.NON_EMPTY_STRING.fieldOf("flag").forGetter(value -> value.flag)).apply(builder, ConfigBooleanCondition::new)
   );
   private final String flag;

   public ConfigBooleanCondition(String flag) {
      this.flag = flag;
   }

   public static ConfigBooleanCondition allowMirrors() {
      return new ConfigBooleanCondition("allowMirrors");
   }

   public static ConfigBooleanCondition enableKami() {
      return new ConfigBooleanCondition("enableKami");
   }

   public ResourceLocation getID() {
      return ID;
   }

   public boolean test(IContext context) {
      String var2 = this.flag;

      return switch (var2) {
         case "allowMirrors" -> Config.allowMirrors;
         case "enableKami" -> Config.enableKami;
         default -> false;
      };
   }

   public MapCodec<? extends ICondition> codec() {
      return CODEC;
   }
}
