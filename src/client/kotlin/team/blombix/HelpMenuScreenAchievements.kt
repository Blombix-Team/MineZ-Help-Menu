package team.blombix

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text

class HelpMenuScreenAchievements : Screen(Text.translatable("menu.minez_help.button14")) {

    override fun init() {
        addDrawableChild(ButtonWidget.builder(Text.translatable("menu.minez_help.back")) {
            client?.setScreen(HelpMenuScreenGettingStarted())
        }.dimensions(width / 2 - 40, height - 40, 80, 20).build())
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground()
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 20, 0xFFFFFF)
        super.render(context, mouseX, mouseY, delta)
    }

    private fun renderBackground() {

    }

    override fun shouldPause(): Boolean = false
}
