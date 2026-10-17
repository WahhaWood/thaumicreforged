package thaumcraft.api.material;

import java.util.function.Supplier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class ToolMaterial implements Tier {
   private final int level;
   private final int uses;
   private final float speed;
   private final float attackDamageBonus;
   private final int enchantmentValue;
   @NotNull
   private final TagKey<Block> incorrectBlocksForDrops;
   @NotNull
   private final Supplier<Ingredient> repairIngredient;

   public ToolMaterial(int level, int uses, float speed, float attackDamageBonus, int enchantmentValue, @NotNull Supplier<Ingredient> repairIngredient) {
      this(level, uses, speed, attackDamageBonus, enchantmentValue, getIncorrectBlocksForLevel(level), repairIngredient);
   }

   public ToolMaterial(
      int level,
      int uses,
      float speed,
      float attackDamageBonus,
      int enchantmentValue,
      @NotNull TagKey<Block> incorrectBlocksForDrops,
      @NotNull Supplier<Ingredient> repairIngredient
   ) {
      this.level = level;
      this.uses = uses;
      this.speed = speed;
      this.attackDamageBonus = attackDamageBonus;
      this.enchantmentValue = enchantmentValue;
      this.incorrectBlocksForDrops = incorrectBlocksForDrops;
      this.repairIngredient = repairIngredient;
   }

   private static TagKey<Block> getIncorrectBlocksForLevel(int level) {
      if (level >= 4) {
         return BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
      } else if (level >= 3) {
         return BlockTags.INCORRECT_FOR_DIAMOND_TOOL;
      } else if (level >= 2) {
         return BlockTags.INCORRECT_FOR_IRON_TOOL;
      } else {
         return level >= 1 ? BlockTags.INCORRECT_FOR_STONE_TOOL : BlockTags.INCORRECT_FOR_WOODEN_TOOL;
      }
   }

   public ToolMaterial copy(TagKey<Block> incorrectBlocksForDrops) {
      return this.copy(incorrectBlocksForDrops, this.repairIngredient);
   }

   public ToolMaterial copy(TagKey<Block> incorrectBlocksForDrops, Supplier<Ingredient> repairIngredient) {
      return new ToolMaterial(this.level, this.uses, this.speed, this.attackDamageBonus, this.enchantmentValue, incorrectBlocksForDrops, repairIngredient);
   }

   public int getUses() {
      return this.uses;
   }

   public float getSpeed() {
      return this.speed;
   }

   public float getAttackDamageBonus() {
      return this.attackDamageBonus;
   }

   public int getLevel() {
      return this.level;
   }

   public int getEnchantmentValue() {
      return this.enchantmentValue;
   }

   @NotNull
   public TagKey<Block> getTag() {
      return this.incorrectBlocksForDrops;
   }

   public TagKey<Block> getIncorrectBlocksForDrops() {
      return this.incorrectBlocksForDrops;
   }

   @NotNull
   public Ingredient getRepairIngredient() {
      return this.repairIngredient.get();
   }

   @Override
   public String toString() {
      return "ToolMaterial[level="
         + this.level
         + ", uses="
         + this.uses
         + ", speed="
         + this.speed
         + ", attackDamageBonus="
         + this.attackDamageBonus
         + ", enchantmentValue="
         + this.enchantmentValue
         + ", incorrectBlocksForDrops="
         + this.incorrectBlocksForDrops
         + ", repairIngredient="
         + this.repairIngredient
         + "]";
   }
}
