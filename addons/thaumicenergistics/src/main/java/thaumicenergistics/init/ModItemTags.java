package thaumicenergistics.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModItemTags {
   public static final TagKey<Item> IRON_GEAR = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "gears/iron"));
}
