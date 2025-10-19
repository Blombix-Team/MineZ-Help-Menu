package team.blombix.bankviewer

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

object BankKeybinds {
    lateinit var openViewer: KeyBinding

    fun init() {
        openViewer = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.bankviewer.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "category.minez_help_menu"
            )
        )

        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client ->
            if (openViewer.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(BankViewerScreen())
                } else {
                    client.setScreen(null)
                }
            }
        })
    }
}