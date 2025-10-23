package team.blombix

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW

object DebugConfig {
    @Volatile
    var debug: Boolean = false

    fun log(message: String, vararg args: Any?) {
        val text = if (args.isNotEmpty()) String.format(message, *args) else message
        println("[BankViewer] $text")
        if (debug) {
            try {
                val client = MinecraftClient.getInstance()
                val player = client.player
                player?.sendMessage(Text.literal("[BankViewer] $text"), false)
            } catch (_: Throwable) {
            }
        }
    }

    fun toggleAndNotify() {
        debug = !debug
        log("Debug mode %s", if (debug) "ON" else "OFF")
    }
}


object DebugKeybinds {
    private lateinit var toggleDebugKey: KeyBinding

    fun init() {
        toggleDebugKey = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.minez_help_menu.toggle_debugmode",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_BACKSLASH, // 🔁 zmień tu, jeśli chcesz inny klawisz
                "category.minez_help_menu"
            )
        )

        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client ->
            while (toggleDebugKey.wasPressed()) {
                DebugConfig.toggleAndNotify()
            }
        })
    }
}