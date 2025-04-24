package team.blombix.configs

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text

class ModSettingsScreen : Screen(Text.literal("Mod Settings")) {
    private var showDropButton: ButtonWidget? = null
    private var gradientBarButton: ButtonWidget? = null

    override fun init() {
        val centerX = width / 2
        val startY = height / 2 - 40

        showDropButton = ButtonWidget.builder(
            Text.literal("Water Icon: ${if (ModConfig.showWaterDropIcon) "ON" else "OFF"}")
        ) {
            ModConfig.showWaterDropIcon = !ModConfig.showWaterDropIcon
            ModConfig.save()
            showDropButton?.message = Text.literal("Water Icon: ${if (ModConfig.showWaterDropIcon) "ON" else "OFF"}")
        }.dimensions(centerX - 75, startY, 150, 20).build()

        gradientBarButton = ButtonWidget.builder(
            Text.literal("EXP Gradient: ${if (ModConfig.enableExpGradient) "ON" else "OFF"}")
        ) {
            ModConfig.enableExpGradient = !ModConfig.enableExpGradient
            ModConfig.save()
            gradientBarButton?.message =
                Text.literal("EXP Gradient: ${if (ModConfig.enableExpGradient) "ON" else "OFF"}")
        }.dimensions(centerX - 75, startY + 30, 150, 20).build()

        val backButton = ButtonWidget.builder(Text.literal("Back")) {
            client?.setScreen(null)
        }.dimensions(centerX - 75, startY + 70, 150, 20).build()

        addDrawableChild(showDropButton)
        addDrawableChild(gradientBarButton)
        addDrawableChild(backButton)
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground()
        context.drawTextWithShadow(
            textRenderer,
            title,
            width / 2 - textRenderer.getWidth(title) / 2,
            height / 2 - 60,
            0xFFFFFF
        )
        super.render(context, mouseX, mouseY, delta)
    }

    private fun renderBackground() {

    }

    override fun shouldPause(): Boolean = false
}
