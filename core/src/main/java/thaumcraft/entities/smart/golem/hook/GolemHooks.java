package thaumcraft.entities.smart.golem.hook;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashMap;
import java.util.HashSet;
import javax.annotation.Nonnull;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import thaumcraft.api.entities.IGolemHookHandler;
import thaumcraft.api.entities.IGolemHookSyncRegistry;
import thaumcraft.entities.smart.golem.EntityGolemBase;
import thaumcraft.util.ItemStackData;

public class GolemHooks {
   private static final Logger LOGGER = LogManager.getLogger("GolemHooks");
   public static final EntityDataAccessor<String> DATA_HOOK_SYNC = SynchedEntityData.defineId(EntityGolemBase.class, EntityDataSerializers.STRING);
   private static final GolemHooks.DummyHookHandler internalHandler = new GolemHooks.DummyHookHandler();
   private static final GolemSyncRegistry defaultSyncRegistry = new GolemSyncRegistry();
   private static final HashSet<IGolemHookHandler> registeredHandlers = new HashSet<>();
   private static final HashSet<IGolemHookHandler> dynamicHandlers = new HashSet<>();
   private static final HashSet<IGolemHookHandler> renderHandlers = new HashSet<>();
   private static boolean hasHandlers = false;

   private static void logCaughtException(String hook, IGolemHookHandler handler, Exception e) {
      String handlerName = handler != null ? handler.getClass().getCanonicalName() : "null";
      if (e != null) {
         LOGGER.warn("Caught exception during call to '{}' for handler '{}': {}", hook, handlerName, e.toString());
      } else {
         LOGGER.warn("Caught unknown exception during call to '{}' for handler '{}'", hook, handlerName);
      }
   }

   public static boolean hasHandlers() {
      return hasHandlers;
   }

   public static void hook_EntityInit(EntityGolemBase golem, HashMap<IGolemHookHandler, Object> golemHandlerData) {
      if (hasHandlers) {
         GolemSyncRegistry localRegistry = new GolemSyncRegistry();
         localRegistry.copyDefaults(defaultSyncRegistry);
         golemHandlerData.put(internalHandler, localRegistry);
      }
   }

   public static void hook_DefineSynchedData(Builder builder) {
      builder.define(DATA_HOOK_SYNC, "");
   }

   public static void hook_SetupGolem(EntityGolemBase golem, HashMap<IGolemHookHandler, Object> golemHandlerData) {
      if (hasHandlers) {
         GolemSyncRegistry localRegistry = (GolemSyncRegistry)golemHandlerData.get(internalHandler);
         if (localRegistry != null) {
            boolean isClientSide = golem.level().isClientSide;

            for (IGolemHookHandler handler : registeredHandlers) {
               try {
                  Object handlerData = golemHandlerData.getOrDefault(handler, null);
                  boolean hadData = handlerData != null;
                  handlerData = handler.setupGolem(golem, handlerData, localRegistry, isClientSide);
                  if (handlerData == null) {
                     if (hadData) {
                        golemHandlerData.remove(handler);
                     }
                  } else {
                     golemHandlerData.put(handler, handlerData);
                  }
               } catch (Exception var9) {
                  logCaughtException("setupGolem", handler, var9);
               }
            }

            if (localRegistry.hasChanged() && !isClientSide) {
               try {
                  golem.getEntityData().set(DATA_HOOK_SYNC, localRegistry.mappingsToString());
               } catch (Exception var8) {
                  localRegistry.markDirty();
               }
            }
         }
      }
   }

   public static boolean hook_CustomInteraction(EntityGolemBase golem, Player player, HashMap<IGolemHookHandler, Object> golemHandlerData) {
      if (!hasHandlers) {
         return false;
      } else {
         GolemSyncRegistry syncRegistry = (GolemSyncRegistry)golemHandlerData.get(internalHandler);
         if (syncRegistry == null) {
            return false;
         } else {
            boolean needsSync = false;
            boolean needsSetup = false;
            boolean skipGUI = false;
            boolean isClientSide = golem.level().isClientSide;

            for (IGolemHookHandler handler : registeredHandlers) {
               Object handlerData = golemHandlerData.getOrDefault(handler, null);

               IGolemHookHandler.InteractionLevel handlerLevel;
               try {
                  handlerLevel = handler.canHandleInteraction(golem, handlerData, player, isClientSide);
               } catch (Exception var15) {
                  logCaughtException("canHandleInteraction", handler, var15);
                  continue;
               }

               switch (handlerLevel) {
                  case NoInteraction:
                     continue;
                  case BasicInteraction:
                  default:
                     break;
                  case SyncInteraction:
                     needsSync = true;
                     break;
                  case FullInteraction:
                     needsSync = true;
                     needsSetup = true;
               }

               skipGUI = true;
               boolean hadData = handlerData != null;

               try {
                  handlerData = handler.customInteraction(golem, handlerData, syncRegistry, player, isClientSide);
               } catch (Exception var14) {
                  logCaughtException("customInteraction", handler, var14);
                  continue;
               }

               if (handlerData == null) {
                  if (hadData) {
                     golemHandlerData.remove(handler);
                  }
               } else {
                  golemHandlerData.put(handler, handlerData);
               }
            }

            if (!isClientSide) {
               if (needsSetup) {
                  golem.setupGolem();
               }

               if (needsSync) {
                  syncRegistry.markDirty();
               }
            }

            return skipGUI;
         }
      }
   }

   public static void hook_onEntityUpdate(EntityGolemBase golem, HashMap<IGolemHookHandler, Object> golemHandlerData) {
      // Client-side sync reader: syncDataChanged is client-only and is stripped from a dedicated server.
      if (hasHandlers && golem.level().isClientSide) {
         GolemSyncRegistry syncRegistry = (GolemSyncRegistry)golemHandlerData.get(internalHandler);
         if (syncRegistry != null) {
            syncRegistry.clientSyncTicks++;
            if (syncRegistry.clientSyncTicks >= 30.0F) {
               syncRegistry.clientSyncTicks = 0.0F;

               String unused;
               try {
                  unused = (String)golem.getEntityData().get(DATA_HOOK_SYNC);
                  if (!syncRegistry.hasChanged() && unused.equals(syncRegistry.lastUpdatedFrom)) {
                     return;
                  }
               } catch (Exception var11) {
                  return;
               }

               HashSet<IGolemHookHandler> _x = syncRegistry.readFromString(unused);
               if (_x != null) {
                  for (IGolemHookHandler _xx : _x) {
                     Object _xxx = golemHandlerData.getOrDefault(_xx, null);
                     boolean _xxxx = _xxx != null;

                     try {
                        _xxx = _xx.syncDataChanged(syncRegistry, _xxx);
                     } catch (Exception var10) {
                        logCaughtException("onSyncDataChanged", _xx, var10);
                        continue;
                     }

                     if (_xxx == null) {
                        if (_xxxx) {
                           golemHandlerData.remove(_xx);
                        }
                     } else {
                        golemHandlerData.put(_xx, _xxx);
                     }
                  }
               }
            }
         }
      }
   }

   public static void hook_Bell_OnLeftClickGolem(EntityGolemBase golem, ItemStack dropped, Player player, HashMap<IGolemHookHandler, Object> golemHandlerData) {
      if (hasHandlers) {
         boolean dismantled = player.isShiftKeyDown();
         boolean isClientSide = golem.level().isClientSide;

         for (IGolemHookHandler handler : registeredHandlers) {
            try {
               Object handlerData = golemHandlerData.getOrDefault(handler, null);
               handler.bellLeftClicked(golem, handlerData, dropped, player, dismantled, isClientSide);
            } catch (Exception var9) {
               logCaughtException("onBellLeftClick", handler, var9);
            }
         }
      }
   }

   public static void hook_Placer_SpawnGolem(EntityGolemBase golem, ItemStack itemGolemPlacer, HashMap<IGolemHookHandler, Object> golemHandlerData) {
      if (hasHandlers) {
         if (ItemStackData.hasTag(itemGolemPlacer)) {
            boolean isClientSide = golem.level().isClientSide;

            for (IGolemHookHandler handler : registeredHandlers) {
               try {
                  Object handlerData = handler.spawnGolemFromItemStack(golem, itemGolemPlacer, isClientSide);
                  if (handlerData != null) {
                     golemHandlerData.put(handler, handlerData);
                  }
               } catch (Exception var7) {
                  logCaughtException("spawnGolemFromItemStack", handler, var7);
               }
            }
         }
      }
   }

   public static void hook_ReadEntityFromNBT(EntityGolemBase golem, HashMap<IGolemHookHandler, Object> golemHandlerData, CompoundTag nbt) {
      if (hasHandlers) {
         for (IGolemHookHandler handler : registeredHandlers) {
            try {
               Object handlerData = handler.readEntityFromNBT(golem, nbt);
               if (handlerData == null) {
                  golemHandlerData.remove(handler);
               } else {
                  golemHandlerData.put(handler, handlerData);
               }
            } catch (Exception var6) {
               logCaughtException("readEntityFromNBT", handler, var6);
            }
         }
      }
   }

   public static void hook_WriteEntityToNBT(EntityGolemBase golem, HashMap<IGolemHookHandler, Object> golemHandlerData, CompoundTag nbt) {
      if (hasHandlers) {
         for (IGolemHookHandler handler : registeredHandlers) {
            try {
               Object handlerData = golemHandlerData.getOrDefault(handler, null);
               handler.writeEntityNBT(golem, handlerData, nbt);
            } catch (Exception var6) {
               logCaughtException("writeEntityNBT", handler, var6);
            }
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static void hook_RenderGolem(
      EntityGolemBase golem,
      HashMap<IGolemHookHandler, Object> golemHandlerData,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight,
      float partialTick
   ) {
      if (hasHandlers) {
         for (IGolemHookHandler handler : renderHandlers) {
            try {
               handler.renderGolem(golem, golemHandlerData.get(handler), poseStack, bufferSource, packedLight, partialTick);
            } catch (Exception var9) {
               logCaughtException("renderGolem", handler, var9);
            }
         }
      }
   }

   public static boolean hasRenderHandlers() {
      return !renderHandlers.isEmpty();
   }

   public static void registerHandler(@Nonnull IGolemHookHandler handler) {
      try {
         registeredHandlers.add(handler);
         hasHandlers = true;
         if (handler.needsRenderer()) {
            renderHandlers.add(handler);
         }

         if (handler.needsDynamicUpdates()) {
            dynamicHandlers.add(handler);
         }

         defaultSyncRegistry.canRegister = true;
         handler.addDefaultSyncEntries(defaultSyncRegistry);
         defaultSyncRegistry.canRegister = false;
         LOGGER.info("Registered golem hook handler: {}", handler.getClass().getCanonicalName());
      } catch (Exception var2) {
         logCaughtException("registerHandler", handler, var2);
      }
   }

   public static class DummyHookHandler implements IGolemHookHandler {
      @Override
      public void addDefaultSyncEntries(@Nonnull IGolemHookSyncRegistry syncRegistry) {
      }

      @Override
      public void bellLeftClicked(@Nonnull Entity g, Object d, @Nonnull ItemStack i, @Nonnull Player p, boolean dis, boolean c) {
      }

      @Nonnull
      @Override
      public IGolemHookHandler.InteractionLevel canHandleInteraction(@Nonnull Entity g, Object d, @Nonnull Player p, boolean c) {
         return IGolemHookHandler.InteractionLevel.NoInteraction;
      }

      @Override
      public Object customInteraction(@Nonnull Entity g, Object d, @Nonnull IGolemHookSyncRegistry s, @Nonnull Player p, boolean c) {
         return null;
      }

      @Override
      public void golemTick(@Nonnull Entity g, Object d, @Nonnull IGolemHookSyncRegistry s) {
      }

      @Override
      public boolean needsDynamicUpdates() {
         return false;
      }

      @Override
      public boolean needsRenderer() {
         return false;
      }

      @Override
      public Object readEntityFromNBT(@Nonnull Entity g, @Nonnull CompoundTag t) {
         return null;
      }

      @OnlyIn(Dist.CLIENT)
      @Override
      public void renderGolem(@Nonnull Entity g, Object d, @Nonnull PoseStack ps, @Nonnull MultiBufferSource b, int l, float p) {
      }

      @Override
      public Object setupGolem(@Nonnull Entity g, Object d, @Nonnull IGolemHookSyncRegistry s, boolean c) {
         return null;
      }

      @Override
      public Object spawnGolemFromItemStack(@Nonnull Entity g, @Nonnull ItemStack i, boolean c) {
         return null;
      }

      @OnlyIn(Dist.CLIENT)
      @Override
      public Object syncDataChanged(@Nonnull IGolemHookSyncRegistry s, Object d) {
         return null;
      }

      @Override
      public void writeEntityNBT(@Nonnull Entity g, Object d, @Nonnull CompoundTag t) {
      }
   }
}
