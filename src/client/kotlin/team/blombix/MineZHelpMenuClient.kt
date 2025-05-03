package team.blombix

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.ResourcePackActivationType
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.util.Identifier
import org.lwjgl.glfw.GLFW

import team.blombix.hud.CustomHudOverlay
import team.blombix.screens.HelpMenuScreenGettingStarted
import team.blombix.tooltips.TooltipInjector

class MineZHelpMenuClient : ClientModInitializer {
    private lateinit var openGuiKey: KeyBinding

    override fun onInitializeClient() {


        FabricLoader.getInstance().getModContainer("minez-help-menu").ifPresent { container ->
            ResourceManagerHelper.registerBuiltinResourcePack(
                Identifier.of("minez-help-menu", "minez_mod_resourcespack"),
                container,
                ResourcePackActivationType.ALWAYS_ENABLED
            )
        }




        TooltipInjector.register()

        openGuiKey = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.minez_help_menu.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_I,
                "category.minez_help_menu"
            )
        )

        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client ->
            if (openGuiKey.wasPressed()) {
                client.setScreen(HelpMenuScreenGettingStarted())
            }
        })


        HudRenderCallback.EVENT.register { _, _ ->
            CustomHudOverlay.render()
        }

    }

}
