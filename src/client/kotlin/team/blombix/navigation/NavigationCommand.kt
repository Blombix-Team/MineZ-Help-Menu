package team.blombix.navigation

import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d

object NavigationCommand {
    fun register() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                ClientCommandManager.literal("navigation")
                .then(
                    ClientCommandManager.literal("start")
                        .then(
                            ClientCommandManager.argument("name", StringArgumentType.string())
                                .suggests { _, builder ->
                                    LocationManager.getAll().forEach { builder.suggest(it.name) }
                                    builder.buildFuture()
                                }
                                .executes { context ->
                                    val name = StringArgumentType.getString(context, "name")
                                    val loc = LocationManager.getLocationByName(name)
                                    val player = MinecraftClient.getInstance().player

                                    if (loc != null && player != null) {
                                        val start = player.pos
                                        val end = Vec3d(loc.x + 0.5, loc.y.toDouble(), loc.z + 0.5)
                                        NavigationModClient.setNavigationTarget(start, end)
                                        val (distance, walk, run) = NavigationModClient.estimateTime(NavigationModClient.getPath())
                                        player.sendMessage(
                                            Text.literal(
                                                "§7[§b⏱§7] §7Distance: §f${"%.1f".format(distance)}m §8|§7 Walk: §f${
                                                    "%.1f".format(
                                                        walk
                                                    )
                                                }s §8|§7 Sprint: §f${"%.1f".format(run)}s"
                                            ),
                                            false
                                        )
                                        player.sendMessage(
                                            Text.literal("§7[§a➤§7] §7Navigation to:§6 ${loc.name}"),
                                            false
                                        )
                                    } else {
                                        player?.sendMessage(
                                            Text.literal("§7[§c✘§7] §7No location was found:§6 $name"),
                                            false
                                        )
                                    }
                                    1
                                }
                        )
                    )
                .then(
                    ClientCommandManager.literal("info")
                        .then(
                            ClientCommandManager.argument("name", StringArgumentType.string())
                                .suggests { _, builder ->
                                    LocationManager.getAll().forEach { builder.suggest(it.name) }
                                    builder.buildFuture()
                                }
                                .executes { context ->
                                    val name = StringArgumentType.getString(context, "name")
                                    val loc = LocationManager.getLocationByName(name)
                                    if (loc != null) {
                                        context.source.sendFeedback(Text.literal("§7§lInformation about:§6 ${loc.name}"))
                                        context.source.sendFeedback(Text.literal("§cX:${loc.x} §aY:${loc.y} §bZ:${loc.z}"))
                                        context.source.sendFeedback(Text.literal("§7Crafting:§6 ${loc.crafting}"))
                                        context.source.sendFeedback(Text.literal("§7Furnace:§6 ${loc.furnace}"))
                                        context.source.sendFeedback(Text.literal("§7Anvil:§6 ${loc.anvil}"))
                                        context.source.sendFeedback(Text.literal("§7Cauldron:§6 ${loc.cauldron}"))
                                        context.source.sendFeedback(Text.literal("§7Brewing Stand:§6 ${loc.brewingstand}"))
                                        context.source.sendFeedback(Text.literal("§7Iron Ore:§6 ${loc.ironore}"))
                                        context.source.sendFeedback(Text.literal("§7Coal Ore:§6 ${loc.coalore}"))
                                        context.source.sendFeedback(Text.literal("§7Carrots:§6 ${loc.carrots}"))
                                        context.source.sendFeedback(Text.literal("§7Wheat:§6 ${loc.wheat}"))
                                        context.source.sendFeedback(Text.literal("§7Beetroots:§6 ${loc.beetroots}"))
                                        context.source.sendFeedback(Text.literal("§7Potatoes:§6 ${loc.potatoes}"))
                                        context.source.sendFeedback(Text.literal("§7Pumpkin:§6 ${loc.pumpkin}"))
                                        context.source.sendFeedback(Text.literal("§7Melon:§6 ${loc.melon}"))
                                    } else {
                                        context.source.sendFeedback(Text.literal("§7[§c✘§7] §7No location was found:§6 $name"))
                                    }
                                    1
                                }
                        )
                    )
                .then(
                    ClientCommandManager.literal("add")
                        .then(
                            ClientCommandManager.argument("x", IntegerArgumentType.integer())
                                .then(
                                    ClientCommandManager.argument("y", IntegerArgumentType.integer())
                                        .then(
                                            ClientCommandManager.argument("z", IntegerArgumentType.integer())
                                                .then(
                                                    ClientCommandManager.argument(
                                                        "name",
                                                        StringArgumentType.string()
                                                    )
                                                        .then(
                                                            ClientCommandManager.argument(
                                                                "anvil",
                                                                BoolArgumentType.bool()
                                                            )
                                                                .then(
                                                                    ClientCommandManager.argument(
                                                                        "furnace",
                                                                        BoolArgumentType.bool()
                                                                    )
                                                                        .then(
                                                                            ClientCommandManager.argument(
                                                                                "crafting",
                                                                                BoolArgumentType.bool()
                                                                            )
                                                                                .then(
                                                                                    ClientCommandManager.argument(
                                                                                        "cauldron",
                                                                                        BoolArgumentType.bool()
                                                                                    )
                                                                                        .then(
                                                                                            ClientCommandManager.argument(
                                                                                                "brewing_stand",
                                                                                                BoolArgumentType.bool()
                                                                                            )
                                                                                                .then(
                                                                                                    ClientCommandManager.argument(
                                                                                                        "iron_ore",
                                                                                                        BoolArgumentType.bool()
                                                                                                    )
                                                                                                        .then(
                                                                                                            ClientCommandManager.argument(
                                                                                                                "coal_ore",
                                                                                                                BoolArgumentType.bool()
                                                                                                            )
                                                                                                                .then(
                                                                                                                    ClientCommandManager.argument(
                                                                                                                        "carrots",
                                                                                                                        BoolArgumentType.bool()
                                                                                                                    )
                                                                                                                        .then(
                                                                                                                            ClientCommandManager.argument(
                                                                                                                                "wheat",
                                                                                                                                BoolArgumentType.bool()
                                                                                                                            )
                                                                                                                                .then(
                                                                                                                                    ClientCommandManager.argument(
                                                                                                                                        "beetroots",
                                                                                                                                        BoolArgumentType.bool()
                                                                                                                                    )
                                                                                                                                        .then(
                                                                                                                                            ClientCommandManager.argument(
                                                                                                                                                "potatoes",
                                                                                                                                                BoolArgumentType.bool()
                                                                                                                                            )
                                                                                                                                                .then(
                                                                                                                                                    ClientCommandManager.argument(
                                                                                                                                                        "pumpkin",
                                                                                                                                                        BoolArgumentType.bool()
                                                                                                                                                    )
                                                                                                                                                        .then(
                                                                                                                                                            ClientCommandManager.argument(
                                                                                                                                                                "melon",
                                                                                                                                                                BoolArgumentType.bool()
                                                                                                                                                            )
                                                                                                .executes { context ->
                                                                                                    val x =
                                                                                                        IntegerArgumentType.getInteger(
                                                                                                            context,
                                                                                                            "x"
                                                                                                        )
                                                                                                    val y =
                                                                                                        IntegerArgumentType.getInteger(
                                                                                                            context,
                                                                                                            "y"
                                                                                                        )
                                                                                                    val z =
                                                                                                        IntegerArgumentType.getInteger(
                                                                                                            context,
                                                                                                            "z"
                                                                                                        )
                                                                                                    val name =
                                                                                                        StringArgumentType.getString(
                                                                                                            context,
                                                                                                            "name"
                                                                                                        )
                                                                                                    val loc = Location(
                                                                                                        name = name,
                                                                                                        x = x,
                                                                                                        y = y,
                                                                                                        z = z,
                                                                                                        anvil = BoolArgumentType.getBool(
                                                                                                            context,
                                                                                                            "anvil"
                                                                                                        ),
                                                                                                        furnace = BoolArgumentType.getBool(
                                                                                                            context,
                                                                                                            "furnace"
                                                                                                        ),
                                                                                                        crafting = BoolArgumentType.getBool(
                                                                                                            context,
                                                                                                            "crafting"
                                                                                                        ),
                                                                                                        cauldron = BoolArgumentType.getBool(
                                                                                                            context,
                                                                                                            "cauldron"
                                                                                                        ),
                                                                                                        brewingstand = BoolArgumentType.getBool(
                                                                                                            context,
                                                                                                            "brewing_stand"
                                                                                                        ),
                                                                                                        ironore = BoolArgumentType.getBool(
                                                                                                            context,
                                                                                                            "iron_ore"
                                                                                                        ),
                                                                                                        coalore = BoolArgumentType.getBool(
                                                                                                            context,
                                                                                                            "coal_ore"
                                                                                                        ),
                                                                                                        carrots = BoolArgumentType.getBool(
                                                                                                            context,
                                                                                                            "carrots"
                                                                                                        ),
                                                                                                        wheat = BoolArgumentType.getBool(
                                                                                                            context,
                                                                                                            "wheat"
                                                                                                        ),
                                                                                                        beetroots = BoolArgumentType.getBool(
                                                                                                            context,
                                                                                                            "beetroots"
                                                                                                        ),
                                                                                                        potatoes = BoolArgumentType.getBool(
                                                                                                            context,
                                                                                                            "potatoes"
                                                                                                        ),
                                                                                                        pumpkin = BoolArgumentType.getBool(
                                                                                                            context,
                                                                                                            "pumpkin"
                                                                                                        ),
                                                                                                        melon = BoolArgumentType.getBool(
                                                                                                            context,
                                                                                                            "melon"
                                                                                                        )
                                                                                                    )
                                                                                                    LocationManager.addLocation(
                                                                                                        loc
                                                                                                    )
                                                                                                    MinecraftClient.getInstance().player?.sendMessage(
                                                                                                        Text.literal(
                                                                                                            "§7[§a✚§7] §7Location added:§6 $name"
                                                                                                        ),
                                                                                                        false
                                                                                                    )
                                                                                                    1
                                                                                                }
                                                                                        )))))))))))))))))
                    )
                    .then(
                        ClientCommandManager.literal("end")
                            .executes {
                                NavigationModClient.clearNavigation()
                                MinecraftClient.getInstance().player?.sendMessage(
                                    Text.literal("§7[§a✔§7] §7Navigation completed."), false
                                )
                                1
                            }
                    )
                    .then(
                        ClientCommandManager.literal("find")
                        .then(
                            ClientCommandManager.argument("feature", StringArgumentType.string())
                                .suggests { _, builder ->
                                    listOf(
                                        "anvil", "crafting", "cauldron", "furnace", "brewingstand",
                                        "ironore", "coalore", "carrots", "wheat", "beetroots",
                                        "potatoes", "pumpkin", "melon"
                                    ).forEach { builder.suggest(it) }
                                    builder.buildFuture()
                                }
                                .executes { context ->
                                    val feature = StringArgumentType.getString(context, "feature").lowercase()
                                    val player = MinecraftClient.getInstance().player ?: return@executes 0
                                    val locations = LocationManager.getAll()

                                    val matching = locations.filter {
                                        val field = Location::class.java.getDeclaredField(feature)
                                        field.isAccessible = true
                                        (field.get(it) as? Boolean) == true
                                    }

                                    if (matching.isEmpty()) {
                                        player.sendMessage(
                                            Text.literal("§7[§c✘§7] §7No location found with feature: §6$feature"),
                                            false
                                        )
                                        return@executes 1
                                    }

                                    val closest = matching.minByOrNull {
                                        Vec3d(it.x + 0.5, it.y.toDouble(), it.z + 0.5).squaredDistanceTo(player.pos)
                                    } ?: return@executes 1

                                    val target = Vec3d(closest.x + 0.5, closest.y.toDouble(), closest.z + 0.5)
                                    NavigationModClient.setNavigationTarget(player.pos, target)

                                    player.sendMessage(
                                        Text.literal("§7[§b⏱§7] §7Navigating to closest: §6$feature §8→ §e${closest.name}"),
                                        false
                                    )
                                    1
                                }
                        )
                    )
            )
        }
    }
}
