package thaumcraft.event.common;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.research.ResearchApi;
import thaumcraft.api.research.ResearchCategoryList;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.network.PacketHandler;
import thaumcraft.network.playerdata.PacketSyncAspects;
import thaumcraft.network.playerdata.PacketSyncResearch;
import thaumcraft.network.playerdata.PacketSyncScannedEntities;
import thaumcraft.network.playerdata.PacketSyncScannedItems;
import thaumcraft.network.playerdata.PacketSyncScannedPhenomena;
import thaumcraft.network.playerdata.PacketSyncWarp;
import thaumcraft.network.playerdata.PacketWarpMessage;
import thaumcraft.research.PlayerKnowledge;
import thaumcraft.research.ResearchManager;

@net.neoforged.fml.common.EventBusSubscriber(modid = "thaumcraft")
public class CommandThaumcraft {
   @net.neoforged.bus.api.SubscribeEvent
   public static void onRegisterCommands(net.neoforged.neoforge.event.RegisterCommandsEvent event) {
      register(event.getDispatcher());
   }

   public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
      LiteralCommandNode<CommandSourceStack> commandNode = dispatcher.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                                 "thaumcraft"
                              )
                              .executes(
                                 context -> {
                                    send(
                                       (CommandSourceStack)context.getSource(),
                                       Component.translatable("command.tc.invalid").append("\n").append(Component.translatable("command.tc.invalid.use"))
                                    );
                                    return 1;
                                 }
                              ))
                           .then(Commands.literal("help").executes(context -> {
                              MutableComponent component = Component.translatable("command.tc.help.0");
                              appendHelp(context, component, 4, 1, 2);
                              appendHelp(context, component, 4, 3, 4);
                              appendHelp(context, component, 4, 5, 6, 7);
                              appendHelp(context, component, 0, 8, 9);
                              send((CommandSourceStack)context.getSource(), component);
                              return 1;
                           })))
                        .then(
                           ((LiteralArgumentBuilder)Commands.literal("iskamiunlocked").requires(CommandSourceStack::isPlayer))
                              .executes(context -> isKamiUnlocked((CommandSourceStack)context.getSource()))
                        ))
                     .then(
                        ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("research").requires(source -> source.hasPermission(4)))
                              .then(Commands.literal("list").executes(context -> {
                                 for (ResearchCategoryList researchCategoryList : ResearchApi.getResearchLists().values()) {
                                    for (ResearchItem researchItem : researchCategoryList.research.values()) {
                                       send((CommandSourceStack)context.getSource(), "В§5" + researchItem.key);
                                    }
                                 }

                                 return 1;
                              })))
                           .then(
                              ((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("player", EntityArgument.player())
                                       .then(Commands.literal("all").executes(context -> {
                                          giveAllResearch((CommandSourceStack)context.getSource(), EntityArgument.getPlayer(context, "player"));
                                          return 1;
                                       })))
                                    .then(Commands.literal("reset").executes(context -> {
                                       resetResearch((CommandSourceStack)context.getSource(), EntityArgument.getPlayer(context, "player"));
                                       return 1;
                                    })))
                                 .then(
                                    Commands.argument("research", StringArgumentType.string())
                                       .suggests((context, builder) -> {
                                          List<String> research = new ArrayList<>();

                                          for (ResearchCategoryList value : ResearchApi.getResearchLists().values()) {
                                             research.addAll(value.research.values().stream().map(researchItem -> researchItem.key).toList());
                                          }

                                          return SharedSuggestionProvider.suggest(research, builder);
                                       })
                                       .executes(
                                          context -> {
                                             giveResearch(
                                                (CommandSourceStack)context.getSource(),
                                                EntityArgument.getPlayer(context, "player"),
                                                StringArgumentType.getString(context, "research")
                                             );
                                             return 1;
                                          }
                                       )
                                 )
                           )
                     ))
                  .then(
                     ((LiteralArgumentBuilder)Commands.literal("aspect").requires(source -> source.hasPermission(4)))
                        .then(
                           Commands.argument("player", EntityArgument.player())
                              .then(
                                 Commands.argument("aspect", StringArgumentType.string())
                                    .suggests((context, builder) -> SharedSuggestionProvider.suggest(Aspect.aspects.keySet(), builder))
                                    .then(
                                       Commands.argument("amount", IntegerArgumentType.integer(1))
                                          .executes(
                                             context -> {
                                                giveAspect(
                                                   (CommandSourceStack)context.getSource(),
                                                   EntityArgument.getPlayer(context, "player"),
                                                   StringArgumentType.getString(context, "aspect"),
                                                   IntegerArgumentType.getInteger(context, "amount")
                                                );
                                                return 1;
                                             }
                                          )
                                    )
                              )
                        )
                  ))
               .then(
                  ((LiteralArgumentBuilder)Commands.literal("warp").requires(source -> source.hasPermission(4)))
                     .then(
                        ((RequiredArgumentBuilder)Commands.argument("player", EntityArgument.player())
                              .then(
                                 Commands.literal("add")
                                    .then(
                                       ((RequiredArgumentBuilder)Commands.argument("amount", IntegerArgumentType.integer(-100, 100))
                                             .executes(
                                                context -> {
                                                   addWarp(
                                                      (CommandSourceStack)context.getSource(),
                                                      EntityArgument.getPlayer(context, "player"),
                                                      IntegerArgumentType.getInteger(context, "amount"),
                                                      "STICKY"
                                                   );
                                                   return 1;
                                                }
                                             ))
                                          .then(
                                             Commands.argument("type", StringArgumentType.string())
                                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(List.of("PERM", "WARP", "STICKY"), builder))
                                                .executes(
                                                   context -> {
                                                      addWarp(
                                                         (CommandSourceStack)context.getSource(),
                                                         EntityArgument.getPlayer(context, "player"),
                                                         IntegerArgumentType.getInteger(context, "amount"),
                                                         StringArgumentType.getString(context, "type")
                                                      );
                                                      return 1;
                                                   }
                                                )
                                          )
                                    )
                              ))
                           .then(
                              Commands.literal("set")
                                 .then(
                                    ((RequiredArgumentBuilder)Commands.argument("value", IntegerArgumentType.integer(0))
                                          .executes(
                                             context -> {
                                                setWarp(
                                                   (CommandSourceStack)context.getSource(),
                                                   EntityArgument.getPlayer(context, "player"),
                                                   IntegerArgumentType.getInteger(context, "value"),
                                                   "STICKY"
                                                );
                                                return 1;
                                             }
                                          ))
                                       .then(
                                          Commands.argument("type", StringArgumentType.string())
                                             .suggests((context, builder) -> SharedSuggestionProvider.suggest(List.of("PERM", "WARP", "STICKY"), builder))
                                             .executes(
                                                context -> {
                                                   setWarp(
                                                      (CommandSourceStack)context.getSource(),
                                                      EntityArgument.getPlayer(context, "player"),
                                                      IntegerArgumentType.getInteger(context, "value"),
                                                      StringArgumentType.getString(context, "type")
                                                   );
                                                   return 1;
                                                }
                                             )
                                       )
                                 )
                           )
                     )
               ))
            .then(
               ((LiteralArgumentBuilder)Commands.literal("debug").requires(source -> source.hasPermission(4)))
                  .then(
                     ((LiteralArgumentBuilder)Commands.literal("aspects")
                           .then(((LiteralArgumentBuilder)Commands.literal("held").requires(CommandSourceStack::isPlayer)).executes(context -> {
                              debugHeldAspects((CommandSourceStack)context.getSource());
                              return 1;
                           })))
                        .then(Commands.literal("entity").then(Commands.argument("target", EntityArgument.entity()).executes(context -> {
                           debugEntityAspects((CommandSourceStack)context.getSource(), EntityArgument.getEntity(context, "target"));
                           return 1;
                        })))
                  )
            )
      );
      commandNode.addChild(buildReset().build());
      dispatcher.register((LiteralArgumentBuilder)Commands.literal("tc").redirect(commandNode));
      dispatcher.register((LiteralArgumentBuilder)Commands.literal("thaum").redirect(commandNode));
   }

   private static LiteralArgumentBuilder<CommandSourceStack> buildReset() {
      RequiredArgumentBuilder<CommandSourceStack, ?> player = Commands.argument("player", EntityArgument.player())
         .executes(context -> resetScope(context, "all"));
      for (String scope : List.of("all", "research", "aspects", "scans", "warp")) {
         player.then(Commands.literal(scope).executes(context -> resetScope(context, scope)));
      }

      return Commands.literal("reset").requires(source -> source.hasPermission(4)).then(player);
   }

   private static int resetScope(CommandContext<CommandSourceStack> context, String scope) throws CommandSyntaxException {
      CommandSourceStack source = context.getSource();
      ServerPlayer target = EntityArgument.getPlayer(context, "player");
      PlayerKnowledge knowledge = PlayerKnowledge.of(target);
      boolean all = scope.equals("all");
      if (all || scope.equals("research")) {
         knowledge.getResearchCompleted().clear();
      }

      if (all || scope.equals("aspects")) {
         knowledge.getAspectsDiscovered().aspects.clear();
      }

      if (all || scope.equals("scans")) {
         knowledge.getObjectsScanned().clear();
         knowledge.getEntitiesScanned().clear();
         knowledge.getPhenomenaScanned().clear();
      }

      if (all || scope.equals("warp")) {
         knowledge.setWarpPerm(0);
         knowledge.setWarpTemp(0);
         knowledge.setWarpSticky(0);
         knowledge.setWarpCounter(0);
      }

      // re-grant auto-unlocked research, primal aspects and hooks, as on a fresh login
      knowledge.refreshProgressionState();
      PacketHandler.sendTo(new PacketSyncResearch(target), target);
      PacketHandler.sendTo(new PacketSyncAspects(target), target);
      PacketHandler.sendTo(new PacketSyncScannedItems(target), target);
      PacketHandler.sendTo(new PacketSyncScannedEntities(target), target);
      PacketHandler.sendTo(new PacketSyncScannedPhenomena(target), target);
      PacketHandler.sendTo(new PacketSyncWarp(target, (byte)0), target);
      PacketHandler.sendTo(new PacketSyncWarp(target, (byte)1), target);
      PacketHandler.sendTo(new PacketSyncWarp(target, (byte)2), target);
      send(source, "Thaumcraft progress reset (" + scope + ") for " + target.getGameProfile().getName());
      return 1;
   }

   private static int isKamiUnlocked(CommandSourceStack source) throws CommandSyntaxException {
      ResearchItem kamiResearch = ResearchApi.getResearch("ichor");
      if (kamiResearch == null) {
         send(source, "command.tc.kami.critical");
         return 0;
      } else {
         String[] parentsHidden = kamiResearch.parentsHidden;
         if (parentsHidden != null && parentsHidden.length != 0) {
            ServerPlayer player = source.getPlayerOrException();
            List<String> missing = Arrays.stream(parentsHidden).filter(s -> !ResearchManager.isResearchComplete(player, s)).toList();
            if (missing.isEmpty()) {
               send(player, "command.tc.kami.opened");
               return 1;
            } else {
               send(player, "command.tc.kami.requires");

               for (String key : missing) {
                  ResearchItem researchItem = ResearchApi.getResearch(key);
                  Component name = researchItem != null ? researchItem.getName() : Component.translatable("command.tc.kami.research.unknown");
                  send(player, Component.translatable("command.tc.kami.research", new Object[]{name, key}));
               }

               return 1;
            }
         } else {
            send(source, "command.tc.kami.opened");
            return 1;
         }
      }
   }

   private static void giveAspect(CommandSourceStack source, ServerPlayer player, String s, int n) {
      PlayerKnowledge knowledge = PlayerKnowledge.of(player);
      if (s.equalsIgnoreCase("all")) {
         for (Aspect aspect : Aspect.aspects.values()) {
            knowledge.addAspectPool(aspect, (short)n);
         }

         ResearchManager.scheduleSave(player);
         send(player, Component.translatable("command.tc.aspect.given", new Object[]{getSenderName(source), n}));
         send(source, "command.tc.success");
         PacketHandler.sendTo(new PacketSyncAspects(player), player);
      } else {
         Aspect aspect = Aspect.getAspect(s);
         if (aspect == null) {
            for (Aspect aspect2 : Aspect.aspects.values()) {
               if (s.equalsIgnoreCase(aspect2.getTag())) {
                  aspect = aspect2;
                  break;
               }
            }
         }

         if (aspect != null) {
            knowledge.addAspectPool(aspect, (short)n);
            ResearchManager.scheduleSave(player);
            PacketHandler.sendTo(new PacketSyncAspects(player), player);
            send(player, Component.translatable("command.tc.aspect.given.one", new Object[]{getSenderName(source), n, aspect.getName()}));
            send(source, "command.tc.success");
         } else {
            send(source, "command.tc.aspect.unknown");
         }
      }
   }

   private static void setWarp(CommandSourceStack source, ServerPlayer player, int n, String s) {
      PlayerKnowledge knowledge = PlayerKnowledge.of(player);
      if (s.equalsIgnoreCase("PERM")) {
         knowledge.setWarpPerm(n);
         ResearchManager.scheduleSave(player);
         PacketHandler.sendTo(new PacketSyncWarp(player, (byte)0), player);
      } else if (s.equalsIgnoreCase("TEMP")) {
         knowledge.setWarpTemp(n);
         ResearchManager.scheduleSave(player);
         PacketHandler.sendTo(new PacketSyncWarp(player, (byte)2), player);
      } else {
         knowledge.setWarpSticky(n);
         ResearchManager.scheduleSave(player);
         PacketHandler.sendTo(new PacketSyncWarp(player, (byte)1), player);
      }

      send(player, Component.translatable("command.tc.warp.set", new Object[]{getSenderName(source), s.toLowerCase(), n}));
      send(source, "command.tc.success");
   }

   private static void addWarp(CommandSourceStack source, ServerPlayer player, int n, String s) {
      PlayerKnowledge knowledge = PlayerKnowledge.of(player);
      if (s.equalsIgnoreCase("PERM")) {
         knowledge.addWarpPerm(n);
         ResearchManager.scheduleSave(player);
         PacketHandler.sendTo(new PacketSyncWarp(player, (byte)0), player);
         PacketHandler.sendTo(new PacketWarpMessage((byte)0, n), player);
      } else if (s.equalsIgnoreCase("TEMP")) {
         knowledge.addWarpTemp(n);
         ResearchManager.scheduleSave(player);
         PacketHandler.sendTo(new PacketSyncWarp(player, (byte)2), player);
         PacketHandler.sendTo(new PacketWarpMessage((byte)2, n), player);
      } else {
         knowledge.addWarpSticky(n);
         ResearchManager.scheduleSave(player);
         PacketHandler.sendTo(new PacketSyncWarp(player, (byte)1), player);
         PacketHandler.sendTo(new PacketWarpMessage((byte)1, n), player);
      }

      send(player, Component.translatable("command.tc.warp.add", new Object[]{getSenderName(source), n, s.toLowerCase()}));
      send(source, "command.tc.success");
   }

   private static void giveResearch(CommandSourceStack source, ServerPlayer player, String s) {
      if (ResearchApi.getResearch(s) != null) {
         giveRecursiveResearch(player, s);
         PacketHandler.sendTo(new PacketSyncResearch(player), player);
         send(player, Component.translatable("command.tc.research.given.one", new Object[]{getSenderName(source), s}));
         send(source, "command.tc.success");
      } else {
         send(source, "command.tc.research.unknown");
      }
   }

   private static void giveAllResearch(CommandSourceStack source, ServerPlayer player) {
      for (ResearchCategoryList value : ResearchApi.getResearchLists().values()) {
         for (ResearchItem researchItem : value.research.values()) {
            if (!ResearchManager.isResearchComplete(player, researchItem.key)) {
               ResearchApi.completeResearch(player, researchItem.key);
            }
         }
      }

      send(player, Component.translatable("command.tc.research.given", new Object[]{getSenderName(source)}));
      send(source, "command.tc.success");
      PacketHandler.sendTo(new PacketSyncResearch(player), player);
   }

   private static void resetResearch(CommandSourceStack source, ServerPlayer player) {
      PlayerKnowledge.of(player).getResearchCompleted().clear();

      for (ResearchCategoryList value : ResearchApi.getResearchLists().values()) {
         for (ResearchItem researchItem : value.research.values()) {
            if (researchItem.isAutoUnlock()) {
               ResearchApi.completeResearch(player, researchItem.key);
            }
         }
      }

      send(player, Component.translatable("command.tc.research.reset", new Object[]{getSenderName(source)}));
      send(source, "command.tc.success");
      PacketHandler.sendTo(new PacketSyncResearch(player), player);
   }

   private static void send(CommandSourceStack stack, String message) {
      send(stack.source, message);
   }

   private static void send(CommandSource source, String message) {
      source.sendSystemMessage(Component.translatable(message));
   }

   private static void send(CommandSourceStack stack, Component message) {
      send(stack.source, message);
   }

   private static void send(CommandSource source, Component message) {
      source.sendSystemMessage(message);
   }

   private static void debugHeldAspects(CommandSourceStack source) throws CommandSyntaxException {
      ServerPlayer player = source.getPlayerOrException();
      ItemStack stack = player.getMainHandItem();
      if (stack.isEmpty()) {
         send(source, Component.literal("Main hand is empty"));
      } else {
         for (Component line : AspectDebugFormatter.describeHeldItem(stack)) {
            send(source, line);
         }
      }
   }

   private static void debugEntityAspects(CommandSourceStack source, Entity entity) {
      for (Component line : AspectDebugFormatter.describeEntity(entity)) {
         send(source, line);
      }
   }

   private static void appendHelp(CommandContext<CommandSourceStack> context, MutableComponent component, int permissionLevel, int... values) {
      if (((CommandSourceStack)context.getSource()).hasPermission(permissionLevel)) {
         for (int value : values) {
            component.append("\n").append(Component.translatable("command.tc.help." + value));
         }
      }
   }

   private static void giveRecursiveResearch(ServerPlayer player, String s) {
      if (!ResearchManager.isResearchComplete(player, s)) {
         ResearchApi.completeResearch(player, s);
         ResearchItem researchItem = ResearchApi.getResearch(s);
         if (researchItem == null) {
            return;
         }

         if (researchItem.parents != null) {
            String[] parents = researchItem.parents;
            int length = parents.length;

            for (int i = 0; i < length; i++) {
               giveRecursiveResearch(player, parents[i]);
            }
         }

         if (researchItem.parentsHidden != null) {
            String[] parentsHidden = researchItem.parentsHidden;
            int length2 = parentsHidden.length;

            for (int j = 0; j < length2; j++) {
               giveRecursiveResearch(player, parentsHidden[j]);
            }
         }

         if (researchItem.siblings != null) {
            String[] siblings = researchItem.siblings;
            int length3 = siblings.length;

            for (int k = 0; k < length3; k++) {
               giveRecursiveResearch(player, siblings[k]);
            }
         }
      }
   }

   private static String getSenderName(CommandSourceStack stack) {
      Player player = stack.getPlayer();
      return player != null ? player.getScoreboardName() : "Console";
   }
}
