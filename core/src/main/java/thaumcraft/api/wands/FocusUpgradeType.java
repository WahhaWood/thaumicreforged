package thaumcraft.api.wands;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

public class FocusUpgradeType {
   public static FocusUpgradeType[] types = new FocusUpgradeType[20];
   public static FocusUpgradeType potency = new FocusUpgradeType(
      0, Thaumcraft.location("textures/foci/potency.png"), "focus.upgrade.potency.name", "focus.upgrade.potency.text", new AspectList().add(Aspect.WEAPON, 1)
   );
   public static FocusUpgradeType frugal = new FocusUpgradeType(
      1, Thaumcraft.location("textures/foci/frugal.png"), "focus.upgrade.frugal.name", "focus.upgrade.frugal.text", new AspectList().add(Aspect.HUNGER, 1)
   );
   public static FocusUpgradeType treasure = new FocusUpgradeType(
      2, Thaumcraft.location("textures/foci/treasure.png"), "focus.upgrade.treasure.name", "focus.upgrade.treasure.text", new AspectList().add(Aspect.GREED, 1)
   );
   public static FocusUpgradeType enlarge = new FocusUpgradeType(
      3, Thaumcraft.location("textures/foci/enlarge.png"), "focus.upgrade.enlarge.name", "focus.upgrade.enlarge.text", new AspectList().add(Aspect.TRAVEL, 1)
   );
   public static FocusUpgradeType alchemistsfire = new FocusUpgradeType(
      4,
      Thaumcraft.location("textures/foci/alchemistsfire.png"),
      "focus.upgrade.alchemistsfire.name",
      "focus.upgrade.alchemistsfire.text",
      new AspectList().add(Aspect.ENERGY, 1).add(Aspect.SLIME, 1)
   );
   public static FocusUpgradeType alchemistsfrost = new FocusUpgradeType(
      5,
      Thaumcraft.location("textures/foci/alchemistsfrost.png"),
      "focus.upgrade.alchemistsfrost.name",
      "focus.upgrade.alchemistsfrost.text",
      new AspectList().add(Aspect.COLD, 1).add(Aspect.TRAP, 1)
   );
   public static FocusUpgradeType architect = new FocusUpgradeType(
      6,
      Thaumcraft.location("textures/foci/architect.png"),
      "focus.upgrade.architect.name",
      "focus.upgrade.architect.text",
      new AspectList().add(Aspect.CRAFT, 1)
   );
   public static FocusUpgradeType extend = new FocusUpgradeType(
      7, Thaumcraft.location("textures/foci/extend.png"), "focus.upgrade.extend.name", "focus.upgrade.extend.text", new AspectList().add(Aspect.EXCHANGE, 1)
   );
   public static FocusUpgradeType silktouch = new FocusUpgradeType(
      8,
      Thaumcraft.location("textures/foci/silktouch.png"),
      "focus.upgrade.silktouch.name",
      "focus.upgrade.silktouch.text",
      new AspectList().add(Aspect.GREED, 1)
   );
   public short id;
   public ResourceLocation icon;
   public String name;
   public String text;
   public AspectList aspects;

   public FocusUpgradeType(int n, ResourceLocation icon, String name, String text, AspectList aspects) {
      this.id = (short)n;
      this.icon = icon;
      this.name = name;
      this.text = text;
      this.aspects = aspects;
      if (n < FocusUpgradeType.types.length && FocusUpgradeType.types[n] != null) {
         LogManager.getLogger("THAUMCRAFT").fatal("Focus Upgrade id " + n + " already occupied. Ignoring.");
      } else {
         if (n >= FocusUpgradeType.types.length) {
            FocusUpgradeType[] types = new FocusUpgradeType[n + 1];
            System.arraycopy(FocusUpgradeType.types, 0, types, 0, FocusUpgradeType.types.length);
            FocusUpgradeType.types = types;
         }

         FocusUpgradeType.types[n] = this;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public String getLocalizedName() {
      return I18n.get(this.name, new Object[0]);
   }

   @OnlyIn(Dist.CLIENT)
   public String getLocalizedText() {
      return I18n.get(this.text, new Object[0]);
   }

   @Override
   public boolean equals(Object o) {
      return o instanceof FocusUpgradeType type && this.id == type.id;
   }
}
