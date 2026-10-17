package thaumcraft.block.tube.render.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.core.Direction;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public final class TubeRenderUtil {
   private TubeRenderUtil() {
   }

   @OnlyIn(Dist.CLIENT)
   public static void applyFacingOpposite(PoseStack pose, Direction dir) {
      if (dir.getStepY() == 0) {
         pose.mulPose(Axis.YP.rotationDegrees(90.0F));
      } else {
         pose.mulPose(Axis.XN.rotationDegrees(90.0F));
         pose.mulPose(Axis.XP.rotationDegrees(90.0F * dir.getStepY()));
      }

      Vector3f axis = new Vector3f(dir.getStepX(), dir.getStepY(), dir.getStepZ());
      if (axis.lengthSquared() > 0.0F) {
         axis.normalize();
         pose.mulPose(new Quaternionf().fromAxisAngleDeg(axis, 90.0F));
      }
   }

   public static int idx(Direction dir) {
      return dir.get3DDataValue();
   }
}
