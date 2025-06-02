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
                                        val target = Vec3d(loc.x + 0.5, loc.y.toDouble(), loc.z + 0.5)
                                        NavigationModClient.navigateTo(target)
                                        player.sendMessage(
                                            Text.literal("§7[§a➤§7] §7Navigation to: §6${loc.name}"),
                                            false
                                        )
                                    } else {
                                        player?.sendMessage(
                                            Text.literal("§7[§c✘§7] §7No location was found: §6$name"),
                                            false
                                        )
                                    }
                                    1
                                }
                            )
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
                                            Text.literal("§7[§c✘§7] §7No location with a feature: §6$feature"),
                                            false
                                        )
                                        return@executes 1
                                    }

                                    val closest = matching.minByOrNull {
                                        Vec3d(it.x + 0.5, it.y.toDouble(), it.z + 0.5).squaredDistanceTo(player.pos)
                                    } ?: return@executes 1

                                    val target = Vec3d(closest.x + 0.5, closest.y.toDouble(), closest.z + 0.5)
                                    NavigationModClient.navigateTo(target)
                                    player.sendMessage(
                                        Text.literal("§7[§b⏱§7] §7Navigation to: §e${closest.name}"),
                                        false
                                    )
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

                                                                                                        val loc =
                                                                                                            Location(
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
                                                                                                                "§7[§a✚§7] §7Location added: §6$name"
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
                                        val player = MinecraftClient.getInstance().player

                                        if (loc != null && player != null) {
                                            player.sendMessage(
                                                Text.literal("§7§lLocation information: §6${loc.name}"),
                                                false
                                            )
                                            player.sendMessage(
                                                Text.literal("§ePosition: §fX:${loc.x} Y:${loc.y} Z:${loc.z}"),
                                                false
                                            )
                                            val resources = listOf(
                                                "anvil" to loc.anvil,
                                                "crafting" to loc.crafting,
                                                "cauldron" to loc.cauldron,
                                                "furnace" to loc.furnace,
                                                "brewingstand" to loc.brewingstand,
                                                "ironore" to loc.ironore,
                                                "coalore" to loc.coalore,
                                                "carrots" to loc.carrots,
                                                "wheat" to loc.wheat,
                                                "beetroots" to loc.beetroots,
                                                "potatoes" to loc.potatoes,
                                                "pumpkin" to loc.pumpkin,
                                                "melon" to loc.melon
                                            ).filter { it.second }
                                                .joinToString(" ") { "§a${it.first}" }

                                            val resourceText = if (resources.isBlank()) "§cBrak" else resources
                                            player.sendMessage(Text.literal("§eResources: $resourceText"), false)

                                        } else {
                                            player?.sendMessage(
                                                Text.literal("§7[§c✘§7] §7No location was found: §6$name"),
                                                false
                                            )
                                        }

                                        1
                                    }
                            )
                    )

            )
        }
        // Alias /nav
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                ClientCommandManager.literal("nav")
                    .redirect(dispatcher.root.getChild("navigation"))
            )
        }

    }
}
