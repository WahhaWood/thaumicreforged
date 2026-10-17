package thaumcraft.data.provider;

import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder.PartBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import thaumcraft.block.tube.TubeSide;
import thaumcraft.init.registry.ModBlocks;

public class TubeModelsAndBlockstatesGenerator extends BlockStateProvider {
   private static final EnumProperty<TubeSide> DOWN = EnumProperty.create("down", TubeSide.class);
   private static final EnumProperty<TubeSide> UP = EnumProperty.create("up", TubeSide.class);
   private static final EnumProperty<TubeSide> NORTH = EnumProperty.create("north", TubeSide.class);
   private static final EnumProperty<TubeSide> SOUTH = EnumProperty.create("south", TubeSide.class);
   private static final EnumProperty<TubeSide> WEST = EnumProperty.create("west", TubeSide.class);
   private static final EnumProperty<TubeSide> EAST = EnumProperty.create("east", TubeSide.class);
   private static final BooleanProperty HAS_ANY = BooleanProperty.create("has_any");

   public TubeModelsAndBlockstatesGenerator(PackOutput output, ExistingFileHelper exFileHelper) {
      super(output, "thaumcraft", exFileHelper);
   }

   protected void registerStatesAndModels() {
      this.buildTemplates();
      this.generateStandardTube(
         (Block)ModBlocks.ESSENCE_TUBE.get(), new TubeModelsAndBlockstatesGenerator.TubeDef("tube", tex("pipe_1"), tex("pipe_2"), tex("pipe_3"))
      );
      this.generateStandardTube(
         (Block)ModBlocks.RESTRICTED_ESSENCE_TUBE.get(),
         new TubeModelsAndBlockstatesGenerator.TubeDef("tube_restricted", tex("pipe_restrict"), tex("pipe_2"), tex("pipe_restrict"))
      );
      this.generateStandardTube(
         (Block)ModBlocks.DIRECTIONAL_ESSENCE_TUBE.get(),
         new TubeModelsAndBlockstatesGenerator.TubeDef("tube_oneway", tex("pipe_1"), tex("pipe_2"), tex("pipe_3"))
      );
      this.generateStandardTube(
         (Block)ModBlocks.ESSENCE_VALVE.get(), new TubeModelsAndBlockstatesGenerator.TubeDef("tube_valve", tex("pipe_1"), tex("pipe_2"), tex("pipe_2"))
      );
      this.generateFilterTube((Block)ModBlocks.FILTERED_ESSENCE_TUBE.get());
      this.generateBufferTube((Block)ModBlocks.ESSENCE_BUFFER.get());
   }

   private void generateStandardTube(@NotNull Block block, @NotNull TubeModelsAndBlockstatesGenerator.TubeDef def) {
      ModelFile arm = this.cutout(
         (BlockModelBuilder)((BlockModelBuilder)((BlockModelBuilder)((BlockModelBuilder)this.models()
                     .withExistingParent("block/tubes/" + def.name + "_arm", this.tpl("template_arm_north")))
                  .texture("texture", def.texArm))
               .texture("particle", def.texArm))
            .ao(false)
      );
      ModelFile armExt = this.cutout(
         (BlockModelBuilder)((BlockModelBuilder)((BlockModelBuilder)((BlockModelBuilder)this.models()
                     .withExistingParent("block/tubes/" + def.name + "_arm_extended", this.tpl("template_arm_extended")))
                  .texture("texture", def.texArm))
               .texture("particle", def.texArm))
            .ao(false)
      );
      ModelFile center = this.cutout(
         (BlockModelBuilder)((BlockModelBuilder)((BlockModelBuilder)((BlockModelBuilder)this.models()
                     .withExistingParent("block/tubes/" + def.name + "_center", this.tpl("template_center_2px")))
                  .texture("texture", def.texArm))
               .texture("particle", def.texArm))
            .ao(false)
      );
      ModelFile coreSingle = this.cutout(
         (BlockModelBuilder)((BlockModelBuilder)((BlockModelBuilder)((BlockModelBuilder)this.models()
                     .withExistingParent("block/tubes/" + def.name + "_core_single", this.tpl("template_core_single")))
                  .texture("texture", def.texCoreSingle))
               .texture("particle", def.texCoreSingle))
            .ao(false)
      );
      ModelFile ringConnected = this.cutout(
         (BlockModelBuilder)((BlockModelBuilder)((BlockModelBuilder)((BlockModelBuilder)this.models()
                     .withExistingParent("block/tubes/" + def.name + "_ring_connected", this.tpl("template_ring_connected")))
                  .texture("texture", def.texCoreConnected))
               .texture("particle", def.texCoreConnected))
            .ao(false)
      );
      MultiPartBlockStateBuilder b = this.getMultipartBuilder(block);
      ((PartBuilder)b.part().modelFile(center).addModel()).end();
      ((PartBuilder)b.part().modelFile(coreSingle).addModel()).condition(HAS_ANY, new Boolean[]{false}).end();
      ((PartBuilder)b.part().modelFile(ringConnected).addModel()).condition(HAS_ANY, new Boolean[]{true}).end();
      this.addArms(b, arm, armExt);
   }

   private void generateFilterTube(@NotNull Block block) {
      ResourceLocation armTex = tex("pipe_1");
      ModelFile arm = this.cutout(
         (BlockModelBuilder)((BlockModelBuilder)((BlockModelBuilder)((BlockModelBuilder)this.models()
                     .withExistingParent("block/tubes/tube_filter_arm", this.tpl("template_arm_north")))
                  .texture("texture", armTex))
               .texture("particle", armTex))
            .ao(false)
      );
      ModelFile armExt = this.cutout(
         (BlockModelBuilder)((BlockModelBuilder)((BlockModelBuilder)((BlockModelBuilder)this.models()
                     .withExistingParent("block/tubes/tube_filter_arm_extended", this.tpl("template_arm_extended")))
                  .texture("texture", armTex))
               .texture("particle", armTex))
            .ao(false)
      );
      ModelFile center = this.cutout(
         (BlockModelBuilder)((BlockModelBuilder)((BlockModelBuilder)((BlockModelBuilder)this.models()
                     .withExistingParent("block/tubes/tube_filter_center", this.tpl("template_center_2px")))
                  .texture("texture", armTex))
               .texture("particle", armTex))
            .ao(false)
      );
      ModelFile coreBase = this.cutout(
         (BlockModelBuilder)((BlockModelBuilder)((BlockModelBuilder)((BlockModelBuilder)this.models()
                     .withExistingParent("block/tubes/tube_filter_core_base", this.tpl("template_filter_core_base")))
                  .texture("texture", tex("pipe_filter")))
               .texture("particle", tex("pipe_filter")))
            .ao(false)
      );
      ModelFile coreOverlay = this.cutout(
         (BlockModelBuilder)((BlockModelBuilder)((BlockModelBuilder)((BlockModelBuilder)this.models()
                     .withExistingParent("block/tubes/tube_filter_core_overlay", this.tpl("template_filter_core_overlay")))
                  .texture("texture", tex("pipe_filter_core")))
               .texture("particle", tex("pipe_filter")))
            .ao(false)
      );
      MultiPartBlockStateBuilder b = this.getMultipartBuilder(block);
      ((PartBuilder)b.part().modelFile(center).addModel()).end();
      ((PartBuilder)b.part().modelFile(coreBase).addModel()).end();
      ((PartBuilder)b.part().modelFile(coreOverlay).addModel()).end();
      this.addArms(b, arm, armExt);
   }

   private void generateBufferTube(@NotNull Block block) {
      ResourceLocation tex = tex("pipe_buffer");
      ModelFile arm = this.cutout(
         (BlockModelBuilder)((BlockModelBuilder)((BlockModelBuilder)((BlockModelBuilder)this.models()
                     .withExistingParent("block/tubes/tube_buffer_arm", this.tpl("template_arm_north")))
                  .texture("texture", tex))
               .texture("particle", tex))
            .ao(false)
      );
      ModelFile armExt = this.cutout(
         (BlockModelBuilder)((BlockModelBuilder)((BlockModelBuilder)((BlockModelBuilder)this.models()
                     .withExistingParent("block/tubes/tube_buffer_arm_extended", this.tpl("template_arm_extended")))
                  .texture("texture", tex))
               .texture("particle", tex))
            .ao(false)
      );
      ModelFile center = this.cutout(
         (BlockModelBuilder)((BlockModelBuilder)((BlockModelBuilder)((BlockModelBuilder)this.models()
                     .withExistingParent("block/tubes/tube_buffer_center", this.tpl("template_center_2px")))
                  .texture("texture", tex))
               .texture("particle", tex))
            .ao(false)
      );
      ModelFile core = this.cutout(
         (BlockModelBuilder)((BlockModelBuilder)((BlockModelBuilder)((BlockModelBuilder)this.models()
                     .withExistingParent("block/tubes/tube_buffer_core", this.tpl("template_buffer_core")))
                  .texture("texture", tex))
               .texture("particle", tex))
            .ao(false)
      );
      MultiPartBlockStateBuilder b = this.getMultipartBuilder(block);
      ((PartBuilder)b.part().modelFile(center).addModel()).end();
      ((PartBuilder)b.part().modelFile(core).addModel()).end();
      this.addArms(b, arm, armExt);
   }

   private void addArms(@NotNull MultiPartBlockStateBuilder b, @NotNull ModelFile armNormal, @NotNull ModelFile armExtended) {
      this.addSide(b, NORTH, armNormal, armExtended, 0, 0);
      this.addSide(b, SOUTH, armNormal, armExtended, 0, 180);
      this.addSide(b, WEST, armNormal, armExtended, 0, 270);
      this.addSide(b, EAST, armNormal, armExtended, 0, 90);
      this.addSide(b, UP, armNormal, armExtended, 270, 0);
      this.addSide(b, DOWN, armNormal, armExtended, 90, 0);
   }

   private void addSide(@NotNull MultiPartBlockStateBuilder b, EnumProperty<TubeSide> sideProp, ModelFile armNormal, ModelFile armExtended, int rotX, int rotY) {
      ((PartBuilder)b.part().modelFile(armNormal).rotationX(rotX).rotationY(rotY).addModel()).condition(sideProp, new TubeSide[]{TubeSide.NORMAL}).end();
      ((PartBuilder)b.part().modelFile(armExtended).rotationX(rotX).rotationY(rotY).addModel()).condition(sideProp, new TubeSide[]{TubeSide.EXTENDED}).end();
   }

   private void buildTemplates() {
      BlockModelBuilder arm = this.cutout(
         (BlockModelBuilder)((BlockModelBuilder)((BlockModelBuilder)this.models().getBuilder("block/tubes/template_arm_north"))
               .parent(this.models().getExistingFile(this.mcLoc("block/block"))))
            .ao(false)
      );
      arm.element()
         .from(7.0F, 7.0F, 0.0F)
         .to(9.0F, 9.0F, 7.0F)
         .face(Direction.NORTH)
         .uvs(7.0F, 7.0F, 9.0F, 9.0F)
         .texture("#texture")
         .end()
         .face(Direction.SOUTH)
         .uvs(7.0F, 7.0F, 9.0F, 9.0F)
         .texture("#texture")
         .end()
         .face(Direction.WEST)
         .uvs(0.0F, 7.0F, 7.0F, 9.0F)
         .texture("#texture")
         .end()
         .face(Direction.EAST)
         .uvs(0.0F, 7.0F, 7.0F, 9.0F)
         .texture("#texture")
         .end()
         .face(Direction.UP)
         .uvs(7.0F, 0.0F, 9.0F, 7.0F)
         .texture("#texture")
         .end()
         .face(Direction.DOWN)
         .uvs(7.0F, 0.0F, 9.0F, 7.0F)
         .texture("#texture")
         .end()
         .end();
      BlockModelBuilder armExt = this.cutout(
         (BlockModelBuilder)((BlockModelBuilder)((BlockModelBuilder)this.models().getBuilder("block/tubes/template_arm_extended"))
               .parent(this.models().getExistingFile(this.mcLoc("block/block"))))
            .ao(false)
      );
      armExt.element()
         .from(7.0F, 7.0F, -4.0F)
         .to(9.0F, 9.0F, 7.0F)
         .face(Direction.NORTH)
         .uvs(7.0F, 7.0F, 9.0F, 9.0F)
         .texture("#texture")
         .end()
         .face(Direction.SOUTH)
         .uvs(7.0F, 7.0F, 9.0F, 9.0F)
         .texture("#texture")
         .end()
         .face(Direction.WEST)
         .uvs(0.0F, 7.0F, 11.0F, 9.0F)
         .texture("#texture")
         .end()
         .face(Direction.EAST)
         .uvs(0.0F, 7.0F, 11.0F, 9.0F)
         .texture("#texture")
         .end()
         .face(Direction.UP)
         .uvs(7.0F, 0.0F, 9.0F, 11.0F)
         .texture("#texture")
         .end()
         .face(Direction.DOWN)
         .uvs(7.0F, 0.0F, 9.0F, 11.0F)
         .texture("#texture")
         .end()
         .end();
      this.generateStaticTemplates();
   }

   private void generateStaticTemplates() {
      BlockModelBuilder center = this.cutout(
         (BlockModelBuilder)((BlockModelBuilder)((BlockModelBuilder)this.models().getBuilder("block/tubes/template_center_2px"))
               .parent(this.models().getExistingFile(this.mcLoc("block/block"))))
            .ao(false)
      );
      center.element()
         .from(7.0F, 7.0F, 7.0F)
         .to(9.0F, 9.0F, 9.0F)
         .face(Direction.NORTH)
         .uvs(7.0F, 7.0F, 9.0F, 9.0F)
         .texture("#texture")
         .end()
         .face(Direction.SOUTH)
         .uvs(7.0F, 7.0F, 9.0F, 9.0F)
         .texture("#texture")
         .end()
         .face(Direction.WEST)
         .uvs(7.0F, 7.0F, 9.0F, 9.0F)
         .texture("#texture")
         .end()
         .face(Direction.EAST)
         .uvs(7.0F, 7.0F, 9.0F, 9.0F)
         .texture("#texture")
         .end()
         .face(Direction.UP)
         .uvs(7.0F, 7.0F, 9.0F, 9.0F)
         .texture("#texture")
         .end()
         .face(Direction.DOWN)
         .uvs(7.0F, 7.0F, 9.0F, 9.0F)
         .texture("#texture")
         .end()
         .end();
      BlockModelBuilder coreSingle = this.cutout(
         (BlockModelBuilder)((BlockModelBuilder)((BlockModelBuilder)this.models().getBuilder("block/tubes/template_core_single"))
               .parent(this.models().getExistingFile(this.mcLoc("block/block"))))
            .ao(false)
      );
      coreSingle.element()
         .from(6.0F, 6.0F, 6.0F)
         .to(10.0F, 10.0F, 10.0F)
         .face(Direction.NORTH)
         .uvs(6.0F, 6.0F, 10.0F, 10.0F)
         .texture("#texture")
         .end()
         .face(Direction.SOUTH)
         .uvs(6.0F, 6.0F, 10.0F, 10.0F)
         .texture("#texture")
         .end()
         .face(Direction.WEST)
         .uvs(6.0F, 6.0F, 10.0F, 10.0F)
         .texture("#texture")
         .end()
         .face(Direction.EAST)
         .uvs(6.0F, 6.0F, 10.0F, 10.0F)
         .texture("#texture")
         .end()
         .face(Direction.UP)
         .uvs(6.0F, 6.0F, 10.0F, 10.0F)
         .texture("#texture")
         .end()
         .face(Direction.DOWN)
         .uvs(6.0F, 6.0F, 10.0F, 10.0F)
         .texture("#texture")
         .end()
         .end();
      BlockModelBuilder ring = this.cutout(
         (BlockModelBuilder)((BlockModelBuilder)((BlockModelBuilder)this.models().getBuilder("block/tubes/template_ring_connected"))
               .parent(this.models().getExistingFile(this.mcLoc("block/block"))))
            .ao(false)
      );
      ring.element()
         .from(6.5F, 6.5F, 6.5F)
         .to(9.5F, 9.5F, 9.5F)
         .face(Direction.NORTH)
         .uvs(6.5F, 6.5F, 9.5F, 9.5F)
         .texture("#texture")
         .end()
         .face(Direction.SOUTH)
         .uvs(6.5F, 6.5F, 9.5F, 9.5F)
         .texture("#texture")
         .end()
         .face(Direction.WEST)
         .uvs(6.5F, 6.5F, 9.5F, 9.5F)
         .texture("#texture")
         .end()
         .face(Direction.EAST)
         .uvs(6.5F, 6.5F, 9.5F, 9.5F)
         .texture("#texture")
         .end()
         .face(Direction.UP)
         .uvs(6.5F, 6.5F, 9.5F, 9.5F)
         .texture("#texture")
         .end()
         .face(Direction.DOWN)
         .uvs(6.5F, 6.5F, 9.5F, 9.5F)
         .texture("#texture")
         .end()
         .end();
      BlockModelBuilder filterBase = this.cutout(
         (BlockModelBuilder)((BlockModelBuilder)((BlockModelBuilder)this.models().getBuilder("block/tubes/template_filter_core_base"))
               .parent(this.models().getExistingFile(this.mcLoc("block/block"))))
            .ao(false)
      );
      filterBase.element()
         .from(5.5F, 5.5F, 5.5F)
         .to(10.5F, 10.5F, 10.5F)
         .face(Direction.NORTH)
         .uvs(5.5F, 5.5F, 10.5F, 10.5F)
         .texture("#texture")
         .end()
         .face(Direction.SOUTH)
         .uvs(5.5F, 5.5F, 10.5F, 10.5F)
         .texture("#texture")
         .end()
         .face(Direction.WEST)
         .uvs(5.5F, 5.5F, 10.5F, 10.5F)
         .texture("#texture")
         .end()
         .face(Direction.EAST)
         .uvs(5.5F, 5.5F, 10.5F, 10.5F)
         .texture("#texture")
         .end()
         .face(Direction.UP)
         .uvs(5.5F, 5.5F, 10.5F, 10.5F)
         .texture("#texture")
         .end()
         .face(Direction.DOWN)
         .uvs(5.5F, 5.5F, 10.5F, 10.5F)
         .texture("#texture")
         .end()
         .end();
      BlockModelBuilder filterOverlay = this.cutout(
         (BlockModelBuilder)((BlockModelBuilder)((BlockModelBuilder)this.models().getBuilder("block/tubes/template_filter_core_overlay"))
               .parent(this.models().getExistingFile(this.mcLoc("block/block"))))
            .ao(false)
      );
      filterOverlay.element()
         .from(5.5F, 5.5F, 5.5F)
         .to(10.5F, 10.5F, 10.5F)
         .face(Direction.NORTH)
         .uvs(5.5F, 5.5F, 10.5F, 10.5F)
         .texture("#texture")
         .tintindex(0)
         .end()
         .face(Direction.SOUTH)
         .uvs(5.5F, 5.5F, 10.5F, 10.5F)
         .texture("#texture")
         .tintindex(0)
         .end()
         .face(Direction.WEST)
         .uvs(5.5F, 5.5F, 10.5F, 10.5F)
         .texture("#texture")
         .tintindex(0)
         .end()
         .face(Direction.EAST)
         .uvs(5.5F, 5.5F, 10.5F, 10.5F)
         .texture("#texture")
         .tintindex(0)
         .end()
         .face(Direction.UP)
         .uvs(5.5F, 5.5F, 10.5F, 10.5F)
         .texture("#texture")
         .tintindex(0)
         .end()
         .face(Direction.DOWN)
         .uvs(5.5F, 5.5F, 10.5F, 10.5F)
         .texture("#texture")
         .tintindex(0)
         .end()
         .end();
      BlockModelBuilder bufferCore = this.cutout(
         (BlockModelBuilder)((BlockModelBuilder)((BlockModelBuilder)this.models().getBuilder("block/tubes/template_buffer_core"))
               .parent(this.models().getExistingFile(this.mcLoc("block/block"))))
            .ao(false)
      );
      bufferCore.element()
         .from(4.0F, 4.0F, 4.0F)
         .to(12.0F, 12.0F, 12.0F)
         .face(Direction.NORTH)
         .uvs(4.0F, 4.0F, 12.0F, 12.0F)
         .texture("#texture")
         .end()
         .face(Direction.SOUTH)
         .uvs(4.0F, 4.0F, 12.0F, 12.0F)
         .texture("#texture")
         .end()
         .face(Direction.WEST)
         .uvs(4.0F, 4.0F, 12.0F, 12.0F)
         .texture("#texture")
         .end()
         .face(Direction.EAST)
         .uvs(4.0F, 4.0F, 12.0F, 12.0F)
         .texture("#texture")
         .end()
         .face(Direction.UP)
         .uvs(4.0F, 4.0F, 12.0F, 12.0F)
         .texture("#texture")
         .end()
         .face(Direction.DOWN)
         .uvs(4.0F, 4.0F, 12.0F, 12.0F)
         .texture("#texture")
         .end()
         .end();
   }

   @NotNull
   private ResourceLocation tpl(@NotNull String templateName) {
      return this.modLoc("block/tubes/" + templateName);
   }

   @NotNull
   private BlockModelBuilder cutout(@NotNull BlockModelBuilder model) {
      return (BlockModelBuilder)model.renderType("cutout");
   }

   @NotNull
   private static ResourceLocation tex(@NotNull String texName) {
      return ResourceLocation.fromNamespaceAndPath("thaumcraft", "block/" + texName);
   }

   public static final class TubeDef {
      final String name;
      final ResourceLocation texArm;
      final ResourceLocation texCoreSingle;
      final ResourceLocation texCoreConnected;

      TubeDef(String name, ResourceLocation texArm, ResourceLocation texCoreSingle, ResourceLocation texCoreConnected) {
         this.name = name;
         this.texArm = texArm;
         this.texCoreSingle = texCoreSingle;
         this.texCoreConnected = texCoreConnected;
      }
   }
}
