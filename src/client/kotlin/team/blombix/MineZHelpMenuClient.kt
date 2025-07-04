package team.blombix

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW
import team.blombix.navigation.LocationManager
import team.blombix.navigation.NavigationCommand
import team.blombix.navigation.NavigationModClient
import team.blombix.screens.HelpMenuScreenGettingStarted
import team.blombix.tooltips.TooltipInjector

class MineZHelpMenuClient : ClientModInitializer {
    private lateinit var openGuiKey: KeyBinding

    override fun onInitializeClient() {

        LocationManager.loadLocations()
        NavigationCommand.register()
        NavigationModClient.register()

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
                val screen = NavigationState.lastScreenFactory?.invoke() ?: HelpMenuScreenGettingStarted()
                client.setScreen(screen)
            }
        })
    }

    object NavigationState {
        var lastScreenFactory: (() -> Screen)? = null
    }

}