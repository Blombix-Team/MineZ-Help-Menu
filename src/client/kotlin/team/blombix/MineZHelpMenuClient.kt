package team.blombix

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.server.command.CommandManager
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW
import team.blombix.navigation.Location
import team.blombix.navigation.LocationManager
import team.blombix.screens.HelpMenuScreenGettingStarted
import team.blombix.tooltips.TooltipInjector

class MineZHelpMenuClient : ClientModInitializer {
    private lateinit var openGuiKey: KeyBinding

    override fun onInitializeClient() {


        LocationManager.loadLocations()

        CommandRegistrationCallback.EVENT.register { dispatcher, registryAccess: CommandRegistryAccess, _ ->
            dispatcher.register(
                CommandManager.literal("navigation")
                    .then(
                        CommandManager.literal("start")
                            .then(
                                CommandManager.argument("name", StringArgumentType.string())
                                    .executes { context ->
                                        val name = StringArgumentType.getString(context, "name")
                                        val loc = LocationManager.getLocationByName(name)
                                        if (loc != null) {
                                            // NavigationModClient.setNavigationTarget(loc.toVec3d())
                                            //TODO: Podłącznie do Systemu Nawigacji
                                            context.source.player?.sendMessage(
                                                Text.literal("Nawigacja do: ${loc.name}"),
                                                false
                                            )
                                        } else {
                                            context.source.player?.sendMessage(
                                                Text.literal("Nie znaleziono lokalizacji: $name"),
                                                false
                                            )
                                        }
                                        1
                                    }
                            )
                    )
                    .then(
                        CommandManager.literal("add")
                            .then(
                                CommandManager.argument("x", IntegerArgumentType.integer())
                                    .then(
                                        CommandManager.argument("y", IntegerArgumentType.integer())
                                            .then(
                                                CommandManager.argument("z", IntegerArgumentType.integer())
                                                    .then(
                                                        CommandManager.argument("name", StringArgumentType.string())
                                                            .executes { context ->
                                                                val x = IntegerArgumentType.getInteger(context, "x")
                                                                val y = IntegerArgumentType.getInteger(context, "y")
                                                                val z = IntegerArgumentType.getInteger(context, "z")
                                                                val name = StringArgumentType.getString(context, "name")

                                                                val newLoc = Location(name, x, y, z)
                                                                LocationManager.addLocation(newLoc)
                                                                context.source.player?.sendMessage(
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
        }




        TooltipInjector.register()
        openGuiKey = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.minez_help_menu.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "category.minez_help_menu"
            )
        )

        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client ->
            if (openGuiKey.wasPressed()) {
                client.setScreen(HelpMenuScreenGettingStarted())
            }
        })
    }
}