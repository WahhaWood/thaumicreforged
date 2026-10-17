package thaumcraft.loot.function;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.entities.smart.wisp.EntityWisp;
import thaumcraft.init.registry.ModItems;
import thaumcraft.init.registry.ModLootFunctions;
import thaumcraft.item.wisp_essence.ItemWispEssence;

public class SetWispEssenceAspectsFunction extends LootItemConditionalFunction {
   public static final MapCodec<SetWispEssenceAspectsFunction> CODEC = RecordCodecBuilder.mapCodec(
      builder -> commonFields(builder).apply(builder, SetWispEssenceAspectsFunction::new)
   );

   public SetWispEssenceAspectsFunction(List<LootItemCondition> predicates) {
      super(predicates);
   }

   @NotNull
   protected ItemStack run(@NotNull ItemStack stack, @NotNull LootContext context) {
      if (!stack.is((Item)ModItems.WISP_ESSENCE.get())) {
         return stack;
      } else {
         Entity entity = (Entity)context.getParamOrNull(LootContextParams.THIS_ENTITY);
         if (entity instanceof EntityWisp wisp) {
            Aspect aspect = Aspect.getAspect(wisp.getAspectTag());
            if (aspect == null) {
               return stack;
            } else {
               ((ItemWispEssence)stack.getItem()).setAspects(stack, new AspectList().add(aspect, 2));
               return stack;
            }
         } else {
            return stack;
         }
      }
   }

   @NotNull
   public LootItemFunctionType getType() {
      return (LootItemFunctionType)ModLootFunctions.SET_WISP_ESSENCE_ASPECTS.get();
   }

   @NotNull
   public Set<LootContextParam<?>> getReferencedContextParams() {
      return Collections.singleton(LootContextParams.THIS_ENTITY);
   }
}
