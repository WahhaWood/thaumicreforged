package thaumicenergistics.item;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartHelper;
import java.util.function.Function;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;

public class ThEPartItem<T extends IPart> extends Item implements IPartItem<T> {
   private final Class<T> partClass;
   private final Function<IPartItem<T>, T> factory;

   public ThEPartItem(Properties properties, Class<T> partClass, Function<IPartItem<T>, T> factory) {
      super(properties);
      this.partClass = partClass;
      this.factory = factory;
   }

   @NotNull
   public InteractionResult useOn(@NotNull UseOnContext context) {
      return PartHelper.usePartItem(context);
   }

   public Class<T> getPartClass() {
      return this.partClass;
   }

   public T createPart() {
      return this.factory.apply(this);
   }
}
