package thaumcraft.particles.fx;

public class WRMat4 {
   float[] mat;

   public WRMat4() {
      this.loadIdentity();
   }

   public WRMat4 loadIdentity() {
      this.mat = new float[16];
      this.mat[0] = 1.0F;
      this.mat[5] = 1.0F;
      this.mat[10] = 1.0F;
      this.mat[15] = 1.0F;
      return this;
   }

   public WRVector3 translate(WRVector3 v) {
      float x = v.x * this.mat[0] + v.y * this.mat[1] + v.z * this.mat[2] + this.mat[3];
      float y = v.x * this.mat[4] + v.y * this.mat[5] + v.z * this.mat[6] + this.mat[7];
      float z = v.x * this.mat[8] + v.y * this.mat[9] + v.z * this.mat[10] + this.mat[11];
      v.x = x;
      v.y = y;
      v.z = z;
      return v;
   }

   public static WRMat4 rotationMat(double angleDegrees, WRVector3 axis) {
      axis = axis.copy().normalize();
      float x = axis.x;
      float y = axis.y;
      float z = axis.z;
      angleDegrees *= 0.0174532925;
      float cos = (float)Math.cos(angleDegrees);
      float oneMinusCos = 1.0F - cos;
      float sin = (float)Math.sin(angleDegrees);
      WRMat4 result = new WRMat4();
      result.mat[0] = x * x * oneMinusCos + cos;
      result.mat[1] = y * x * oneMinusCos + z * sin;
      result.mat[2] = x * z * oneMinusCos - y * sin;
      result.mat[4] = x * y * oneMinusCos - z * sin;
      result.mat[5] = y * y * oneMinusCos + cos;
      result.mat[6] = y * z * oneMinusCos + x * sin;
      result.mat[8] = x * z * oneMinusCos + y * sin;
      result.mat[9] = y * z * oneMinusCos - x * sin;
      result.mat[10] = z * z * oneMinusCos + cos;
      result.mat[15] = 1.0F;
      return result;
   }
}
