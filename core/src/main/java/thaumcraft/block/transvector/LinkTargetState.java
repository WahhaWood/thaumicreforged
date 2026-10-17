package thaumcraft.block.transvector;


public enum LinkTargetState {
   NO_TARGET(false, false),
   UNLOADED(false, false),
   LOADED_TARGET(true, false),
   RESOLVED(true, false),
   INVALID_SELF(false, true),
   INVALID_OUT_OF_RANGE(false, true),
   INVALID_MISSING_BLOCK_ENTITY(false, true);

   private final boolean loadedTarget;
   private final boolean clearTarget;

   private LinkTargetState(boolean loadedTarget, boolean clearTarget) {
      this.loadedTarget = loadedTarget;
      this.clearTarget = clearTarget;
   }

   public boolean isLoadedTarget() {
      return this.loadedTarget;
   }

   public boolean shouldClearTarget() {
      return this.clearTarget;
   }
}
