package thaumcraft.particles.fx.bolt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import thaumcraft.particles.fx.WRVector3;

public class FXLightningBoltCommon {
   ArrayList<FXLightningBoltCommon.Segment> segments;
   WRVector3 start;
   WRVector3 end;
   HashMap<Integer, Integer> splitparents;
   public float multiplier;
   public float length;
   public int numsegments0;
   public int increment;
   public int type = 0;
   public boolean nonLethal = false;
   private int numsplits;
   private boolean finalized;
   private final Random rand;
   public long seed;
   public int particleAge;
   public int particleMaxAge;
   private final ClientLevel level;
   public static final float speed = 3.0F;
   public static final int fadetime = 20;

   public FXLightningBoltCommon(ClientLevel level, WRVector3 start, WRVector3 end, long seed) {
      this.segments = new ArrayList<>();
      this.splitparents = new HashMap<>();
      this.level = level;
      this.start = start;
      this.end = end;
      this.seed = seed;
      this.rand = new Random(seed);
      this.numsegments0 = 1;
      this.increment = 1;
      this.length = this.end.copy().sub(this.start).length();
      this.particleMaxAge = 3 + this.rand.nextInt(3) - 1;
      this.multiplier = 1.0F;
      this.particleAge = -((int)(this.length * 3.0F));
      this.segments.add(new FXLightningBoltCommon.Segment(this.start, this.end));
   }

   public FXLightningBoltCommon(ClientLevel level, Entity entity, Entity entity2, long n) {
      this(level, new WRVector3(entity), new WRVector3(entity2), n);
   }

   public FXLightningBoltCommon(ClientLevel level, Entity entity, Entity entity2, long n, int increment) {
      this(level, new WRVector3(entity), new WRVector3(entity2.getX(), entity2.getY() + entity2.getEyeHeight() - 0.7, entity2.getZ()), n);
      this.increment = increment;
      this.multiplier = 0.4F;
   }

   public FXLightningBoltCommon(ClientLevel level, BlockEntity blockEntity, Entity entity, long n) {
      this(level, new WRVector3(blockEntity), new WRVector3(entity), n);
   }

   public FXLightningBoltCommon(ClientLevel level, BlockEntity blockEntity, double n, double n2, double n3, long n4) {
      this(level, new WRVector3(blockEntity), new WRVector3(n, n2, n3), n4);
   }

   public FXLightningBoltCommon(ClientLevel level, double n, double n2, double n3, double n4, double n5, double n6, long n7, int n8, float multiplier) {
      this(level, new WRVector3(n, n2, n3), new WRVector3(n4, n5, n6), n7);
      this.particleMaxAge = n8 + this.rand.nextInt(n8) - n8 / 2;
      this.multiplier = multiplier;
   }

   public FXLightningBoltCommon(
      ClientLevel level, double n, double n2, double n3, double n4, double n5, double n6, long n7, int n8, float multiplier, int increment
   ) {
      this(level, new WRVector3(n, n2, n3), new WRVector3(n4, n5, n6), n7);
      this.particleMaxAge = n8 + this.rand.nextInt(n8) - n8 / 2;
      this.multiplier = multiplier;
      this.increment = increment;
   }

   public void setMultiplier(float multiplier) {
      this.multiplier = multiplier;
   }

   public void fractal(int n, float n2, float n3, float n4, float n5) {
      if (!this.finalized) {
         ArrayList<FXLightningBoltCommon.Segment> segments = this.segments;
         this.segments = new ArrayList<>();

         for (FXLightningBoltCommon.Segment segment : segments) {
            FXLightningBoltCommon.Segment prev = segment.prev;
            WRVector3 scale = segment.diff.copy().scale(1.0F / n);
            FXLightningBoltCommon.BoltPoint[] array = new FXLightningBoltCommon.BoltPoint[n + 1];
            WRVector3 point = segment.startpoint.point;
            array[0] = segment.startpoint;
            array[n] = segment.endpoint;

            for (int i = 1; i < n; i++) {
               WRVector3 rotate = WRVector3.getPerpendicular(segment.diff).rotate(this.rand.nextFloat() * 360.0F, segment.diff);
               rotate.scale((this.rand.nextFloat() - 0.5F) * n2);
               array[i] = new FXLightningBoltCommon.BoltPoint(point.copy().add(scale.copy().scale(i)), rotate);
            }

            for (int j = 0; j < n; j++) {
               FXLightningBoltCommon.Segment next = new FXLightningBoltCommon.Segment(
                  array[j], array[j + 1], segment.light, segment.segmentno * n + j, segment.splitno
               );
               if ((next.prev = prev) != null) {
                  prev.next = next;
               }

               if (j != 0 && this.rand.nextFloat() < n3) {
                  WRVector3 scale2 = next.diff
                     .copy()
                     .rotate((this.rand.nextFloat() * 0.66F + 0.33F) * n5, WRVector3.xCrossProduct(next.diff).rotate(this.rand.nextFloat() * 360.0F, next.diff))
                     .scale(n4);
                  this.numsplits++;
                  this.splitparents.put(this.numsplits, next.splitno);
                  FXLightningBoltCommon.Segment segment2 = new FXLightningBoltCommon.Segment(
                     array[j],
                     new FXLightningBoltCommon.BoltPoint(array[j + 1].basepoint, array[j + 1].offsetvec.copy().add(scale2)),
                     segment.light / 2.0F,
                     next.segmentno,
                     this.numsplits
                  );
                  segment2.prev = prev;
                  this.segments.add(segment2);
               }

               prev = next;
               this.segments.add(next);
            }

            if (segment.next != null) {
               segment.next.prev = prev;
            }
         }

         this.numsegments0 *= n;
      }
   }

   public void defaultFractal() {
      this.fractal(2, this.length * this.multiplier / 8.0F, 0.7F, 0.1F, 45.0F);
      this.fractal(2, this.length * this.multiplier / 12.0F, 0.5F, 0.1F, 50.0F);
      this.fractal(2, this.length * this.multiplier / 17.0F, 0.5F, 0.1F, 55.0F);
      this.fractal(2, this.length * this.multiplier / 23.0F, 0.5F, 0.1F, 60.0F);
      this.fractal(2, this.length * this.multiplier / 30.0F, 0.0F, 0.0F, 0.0F);
      this.fractal(2, this.length * this.multiplier / 34.0F, 0.0F, 0.0F, 0.0F);
      this.fractal(2, this.length * this.multiplier / 40.0F, 0.0F, 0.0F, 0.0F);
   }

   private void calculateCollisionAndDiffs() {
      HashMap<Integer, Integer> hashMap = new HashMap<>();
      Collections.sort(this.segments, new FXLightningBoltCommon.SegmentSorter());
      int splitno = 0;
      int segmentno = 0;

      for (FXLightningBoltCommon.Segment segment : this.segments) {
         if (segment.splitno > splitno) {
            hashMap.put(splitno, segmentno);
            splitno = segment.splitno;
            hashMap.get(this.splitparents.get(segment.splitno));
         }

         segmentno = segment.segmentno;
      }

      hashMap.put(splitno, segmentno);
      int splitno2 = 0;
      Integer n = hashMap.get(0);
      Iterator<FXLightningBoltCommon.Segment> iterator2 = this.segments.iterator();

      while (iterator2.hasNext()) {
         FXLightningBoltCommon.Segment segment2 = iterator2.next();
         if (splitno2 != segment2.splitno) {
            splitno2 = segment2.splitno;
            n = hashMap.get(segment2.splitno);
         }

         if (n != null && segment2.segmentno > n) {
            iterator2.remove();
         }

         segment2.calcEndDiffs();
      }
   }

   public void finalizeBolt() {
      if (!this.finalized) {
         this.finalized = true;
         this.calculateCollisionAndDiffs();
         Collections.sort(this.segments, new FXLightningBoltCommon.SegmentLightSorter());
      }
   }

   public void onUpdate() {
      this.particleAge = this.particleAge + this.increment;
      if (this.particleAge > this.particleMaxAge) {
         this.particleAge = this.particleMaxAge;
      }
   }

   public class BoltPoint {
      WRVector3 point;
      WRVector3 basepoint;
      WRVector3 offsetvec;

      public BoltPoint(final WRVector3 basepoint, final WRVector3 offsetvec) {
         this.point = basepoint.copy().add(offsetvec);
         this.basepoint = basepoint;
         this.offsetvec = offsetvec;
      }
   }

   public class Segment {
      public FXLightningBoltCommon.BoltPoint startpoint;
      public FXLightningBoltCommon.BoltPoint endpoint;
      public WRVector3 diff;
      public FXLightningBoltCommon.Segment prev;
      public FXLightningBoltCommon.Segment next;
      public WRVector3 nextdiff;
      public WRVector3 prevdiff;
      public float sinprev;
      public float sinnext;
      public float light;
      public int segmentno;
      public int splitno;

      public void calcDiff() {
         this.diff = this.endpoint.point.copy().sub(this.startpoint.point);
      }

      public void calcEndDiffs() {
         if (this.prev != null) {
            WRVector3 normalize = this.prev.diff.copy().normalize();
            WRVector3 normalize2 = this.diff.copy().normalize();
            this.prevdiff = normalize2.add(normalize).normalize();
            this.sinprev = (float)Math.sin(WRVector3.anglePreNorm(normalize2, normalize.scale(-1.0F)) / 2.0F);
         } else {
            this.prevdiff = this.diff.copy().normalize();
            this.sinprev = 1.0F;
         }

         if (this.next != null) {
            WRVector3 normalize3 = this.next.diff.copy().normalize();
            WRVector3 normalize4 = this.diff.copy().normalize();
            this.nextdiff = normalize4.add(normalize3).normalize();
            this.sinnext = (float)Math.sin(WRVector3.anglePreNorm(normalize4, normalize3.scale(-1.0F)) / 2.0F);
         } else {
            this.nextdiff = this.diff.copy().normalize();
            this.sinnext = 1.0F;
         }
      }

      @Override
      public String toString() {
         return this.startpoint.point.toString() + " " + this.endpoint.point.toString();
      }

      public Segment(
         final FXLightningBoltCommon.BoltPoint startpoint,
         final FXLightningBoltCommon.BoltPoint endpoint,
         final float light,
         final int segmentno,
         final int splitno
      ) {
         this.startpoint = startpoint;
         this.endpoint = endpoint;
         this.light = light;
         this.segmentno = segmentno;
         this.splitno = splitno;
         this.calcDiff();
      }

      public Segment(WRVector3 start, WRVector3 end) {
         this(
            FXLightningBoltCommon.this.new BoltPoint(start, new WRVector3(0.0, 0.0, 0.0)),
            FXLightningBoltCommon.this.new BoltPoint(end, new WRVector3(0.0, 0.0, 0.0)),
            1.0F,
            0,
            0
         );
      }
   }

   public class SegmentLightSorter implements Comparator<FXLightningBoltCommon.Segment> {
      public int compare(FXLightningBoltCommon.Segment segment, FXLightningBoltCommon.Segment segment2) {
         return Float.compare(segment2.light, segment.light);
      }
   }

   public class SegmentSorter implements Comparator<FXLightningBoltCommon.Segment> {
      public int compare(FXLightningBoltCommon.Segment segment, FXLightningBoltCommon.Segment segment2) {
         int compareTo = Integer.valueOf(segment.splitno).compareTo(segment2.splitno);
         return compareTo == 0 ? Integer.valueOf(segment.segmentno).compareTo(segment2.segmentno) : compareTo;
      }
   }
}
