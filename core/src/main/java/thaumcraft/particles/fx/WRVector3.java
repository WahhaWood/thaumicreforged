package thaumcraft.particles.fx;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public class WRVector3 {
   public float x;
   public float y;
   public float z;

   public WRVector3(double x, double y, double z) {
      this.x = (float)x;
      this.y = (float)y;
      this.z = (float)z;
   }

   public WRVector3(BlockEntity blockEntity) {
      BlockPos pos = blockEntity.getBlockPos();
      this.x = pos.getX() + 0.5F;
      this.y = pos.getY() + 0.5F;
      this.z = pos.getZ() + 0.5F;
   }

   public WRVector3(Entity entity) {
      this(entity.getX(), entity.getY(), entity.getZ());
   }

   public WRVector3 add(WRVector3 other) {
      this.x = this.x + other.x;
      this.y = this.y + other.y;
      this.z = this.z + other.z;
      return this;
   }

   public WRVector3 sub(WRVector3 other) {
      this.x = this.x - other.x;
      this.y = this.y - other.y;
      this.z = this.z - other.z;
      return this;
   }

   public WRVector3 scale(float factor) {
      this.x *= factor;
      this.y *= factor;
      this.z *= factor;
      return this;
   }

   public WRVector3 scale(float fx, float fy, float fz) {
      this.x *= fx;
      this.y *= fy;
      this.z *= fz;
      return this;
   }

   public WRVector3 normalize() {
      float len = this.length();
      this.x /= len;
      this.y /= len;
      this.z /= len;
      return this;
   }

   public float length() {
      return (float)Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
   }

   public float lengthPow2() {
      return this.x * this.x + this.y * this.y + this.z * this.z;
   }

   public WRVector3 copy() {
      return new WRVector3(this.x, this.y, this.z);
   }

   public static WRVector3 crossProduct(WRVector3 a, WRVector3 b) {
      return new WRVector3(a.y * b.z - a.z * b.y, a.z * b.x - a.x * b.z, a.x * b.y - a.y * b.x);
   }

   public static WRVector3 xCrossProduct(WRVector3 v) {
      return new WRVector3(0.0, v.z, -v.y);
   }

   public static WRVector3 zCrossProduct(WRVector3 v) {
      return new WRVector3(-v.y, v.x, 0.0);
   }

   public static float dotProduct(WRVector3 a, WRVector3 b) {
      return a.x * b.x + a.y * b.y + a.z * b.z;
   }

   public static float angle(WRVector3 a, WRVector3 b) {
      return anglePreNorm(a.copy().normalize(), b.copy().normalize());
   }

   public static float anglePreNorm(WRVector3 a, WRVector3 b) {
      return (float)Math.acos(dotProduct(a, b));
   }

   public WRVector3 rotate(float angle, WRVector3 axis) {
      return WRMat4.rotationMat(angle, axis).translate(this);
   }

   @Override
   public String toString() {
      return "[" + this.x + "," + this.y + "," + this.z + "]";
   }

   public Vec3 toVec3() {
      return new Vec3(this.x, this.y, this.z);
   }

   public static WRVector3 getPerpendicular(WRVector3 v) {
      return v.z == 0.0F ? zCrossProduct(v) : xCrossProduct(v);
   }

   public boolean isZero() {
      return this.x == 0.0F && this.y == 0.0F && this.z == 0.0F;
   }
}
