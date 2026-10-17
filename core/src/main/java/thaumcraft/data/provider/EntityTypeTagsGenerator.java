package thaumcraft.data.provider;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.Tags.EntityTypes;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import thaumcraft.init.registry.ModEntities;

public class EntityTypeTagsGenerator extends EntityTypeTagsProvider {
   public EntityTypeTagsGenerator(PackOutput output, CompletableFuture<Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
      super(output, lookupProvider, "thaumcraft", existingFileHelper);
   }

   protected void addTags(@NotNull Provider provider) {
      this.tag(EntityTypes.BOSSES)
         .add(
            new EntityType[]{
               (EntityType)ModEntities.ELDRITCH_WARDEN.get(),
               (EntityType)ModEntities.ELDRITCH_GOLEM.get(),
               (EntityType)ModEntities.CULTIST_PORTAL.get(),
               (EntityType)ModEntities.TENTACLE_GIANT.get(),
               (EntityType)ModEntities.CULTIST_LEADER.get()
            }
         );
      this.tag(EntityTypeTags.ARTHROPOD).add((EntityType)ModEntities.ELDRITCH_CRAB.get());
   }
}
