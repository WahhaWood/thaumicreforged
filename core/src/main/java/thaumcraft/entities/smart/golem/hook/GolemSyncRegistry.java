package thaumcraft.entities.smart.golem.hook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import thaumcraft.api.entities.IGolemHookHandler;
import thaumcraft.api.entities.IGolemHookSyncRegistry;

public class GolemSyncRegistry implements IGolemHookSyncRegistry {
   private final ArrayList<Character> mappings = new ArrayList<>();
   private final ArrayList<IGolemHookHandler> handlerMappings = new ArrayList<>();
   boolean canRegister = false;
   private boolean dirty = false;
   String lastUpdatedFrom = null;
   float clientSyncTicks = 0.0F;

   @Override
   public char getSyncCharOrDefault(int id, char defaultChar) {
      return id >= 0 && id < this.mappings.size() ? this.mappings.get(id) : defaultChar;
   }

   @Override
   public int registerSyncChar(IGolemHookHandler handler, char c) {
      if (!this.canRegister) {
         throw new IllegalStateException("Cannot register sync entries outside of registration phase");
      } else {
         int id = this.mappings.size();
         this.mappings.add(c);
         this.handlerMappings.add(handler);
         return id;
      }
   }

   @Override
   public void updateSyncChar(IGolemHookHandler handler, int id, char c) {
      if (id >= 0 && id < this.mappings.size()) {
         char old = this.mappings.get(id);
         if (old != c) {
            this.mappings.set(id, c);
            this.dirty = true;
         }
      }
   }

   public String mappingsToString() {
      StringBuilder sb = new StringBuilder(this.mappings.size());

      for (char c : this.mappings) {
         sb.append(c);
      }

      String result = sb.toString();
      this.lastUpdatedFrom = result;
      this.dirty = false;
      return result;
   }

   public boolean hasChanged() {
      return this.dirty;
   }

   public void markDirty() {
      this.dirty = true;
   }

   public void copyDefaults(GolemSyncRegistry defaults) {
      this.mappings.clear();
      this.handlerMappings.clear();
      this.mappings.addAll(defaults.mappings);
      this.handlerMappings.addAll(defaults.handlerMappings);
      this.dirty = true;
   }

   public HashSet<IGolemHookHandler> readFromString(String data) {
      if (data != null && !data.isEmpty()) {
         this.lastUpdatedFrom = data;
         this.dirty = false;
         Map<IGolemHookHandler, Boolean> changedHandlers = new HashMap<>();
         int len = Math.min(data.length(), this.mappings.size());

         for (int i = 0; i < len; i++) {
            char newChar = data.charAt(i);
            char oldChar = this.mappings.get(i);
            if (newChar != oldChar) {
               this.mappings.set(i, newChar);
               IGolemHookHandler handler = this.handlerMappings.get(i);
               if (handler != null) {
                  changedHandlers.put(handler, true);
               }
            }
         }

         return changedHandlers.isEmpty() ? null : new HashSet<>(changedHandlers.keySet());
      } else {
         return null;
      }
   }
}
