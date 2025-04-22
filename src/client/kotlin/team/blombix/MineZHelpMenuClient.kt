package team.blombix

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

class MineZHelpMenuClient : ClientModInitializer {
    private lateinit var openGuiKey: KeyBinding
    private lateinit var openSettingsGuiKey: KeyBinding

    override fun onInitializeClient() {
        openGuiKey = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.minez_help_menu.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_I,
                "category.minez_help_menu"
            )
        )

        openSettingsGuiKey = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.minez_help_menu.open_settings_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_U,
                "category.minez_help_menu"
            )
        )

        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client ->
            if (openGuiKey.wasPressed()) {
                client.setScreen(HelpMenuScreenGettingStarted())
            }
            if (openSettingsGuiKey.wasPressed()) {
                client.setScreen(ModSettingsScreen())
            }
        })

        ModConfig.load()

        HudRenderCallback.EVENT.register { context, _ ->
            CustomHudOverlay.render()
        }

    }

}
