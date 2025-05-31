package team.blombix.navigation

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
                                .executes { context ->
                                    val name = StringArgumentType.getString(context, "name")
                                    val loc = LocationManager.getLocationByName(name)
                                    val player = MinecraftClient.getInstance().player

                                    if (loc != null && player != null) {
                                        val start = player.pos
                                        val end = Vec3d(loc.x + 0.5, loc.y.toDouble(), loc.z + 0.5)
                                        NavigationModClient.setNavigationTarget(start, end)
                                        player.sendMessage(Text.literal("Nawigacja do: ${loc.name}"), false)
                                    } else {
                                        player?.sendMessage(
                                            Text.literal("Nie znaleziono lokalizacji: $name"),
                                            false
                                        )
                                    }
                                    1
                                }
                        )
                    )
                    .then(
                        ClientCommandManager.literal("end")
                            .executes {
                                NavigationModClient.clearNavigation()
                                MinecraftClient.getInstance().player?.sendMessage(
                                    Text.literal("Nawigacja zakończona."), false
                                )
                                1
                            }
                            .then(
                                ClientCommandManager.literal("info")
                                .then(
                                    ClientCommandManager.argument("name", StringArgumentType.string())
                                        .executes { context ->
                                            val name = StringArgumentType.getString(context, "name")
                                            val loc = LocationManager.getLocationByName(name)
                                            if (loc != null) {
                                                context.source.sendFeedback(Text.literal("Info: ${loc.name}, X:${loc.x}, Y:${loc.y}, Z:${loc.z}"))
                                            } else {
                                                context.source.sendFeedback(Text.literal("Nie znaleziono lokalizacji: $name"))
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
                                                ClientCommandManager.argument(
                                                "z",
                                                IntegerArgumentType.integer()
                                            )
                                                .then(
                                                    ClientCommandManager.argument(
                                                        "name",
                                                        StringArgumentType.string()
                                                    )
                                                        .executes { context ->
                                                            val x = IntegerArgumentType.getInteger(context, "x")
                                                            val y = IntegerArgumentType.getInteger(context, "y")
                                                            val z = IntegerArgumentType.getInteger(context, "z")
                                                            val name = StringArgumentType.getString(
                                                                context,
                                                                "name"
                                                            )
                                                            LocationManager.addLocation(
                                                                Location(
                                                                    name,
                                                                    x,
                                                                    y,
                                                                    z
                                                                )
                                                            )
                                                            MinecraftClient.getInstance().player?.sendMessage(
                                                                Text.literal("Dodano lokalizację: $name"),
                                                                false
                                                            )
                                                            1
                                                        }
                                                )
                                            )
                                        )
                                    )
                            )
                    )
            )
        }
    }
}
