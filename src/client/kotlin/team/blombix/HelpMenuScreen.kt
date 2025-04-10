package team.blombix

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.text.Text
import net.minecraft.util.Util

class HelpMenuScreen : Screen(Text.translatable("menu.minez_help.title")) {

    private var textField: TextFieldWidget? = null
    private val dynamicButtons = mutableListOf<ButtonWidget>()
    private val buttonTexts = listOf(
        Text.translatable("menu.minez_help.button1"),
        Text.translatable("menu.minez_help.button2"),
        Text.translatable("menu.minez_help.button3"),
        Text.translatable("menu.minez_help.button4"),
        Text.translatable("menu.minez_help.button5"),
        Text.translatable("menu.minez_help.button6"),
        Text.translatable("menu.minez_help.button7"),
        Text.translatable("menu.minez_help.button8"),
        Text.translatable("menu.minez_help.button9"),
        Text.translatable("menu.minez_help.button10"),
        Text.translatable("menu.minez_help.button11"),
        Text.translatable("menu.minez_help.button12")
    )

    override fun init() {
        val leftPanelWidth = (width * 0.3).toInt()

        val labelY = 20
        val textFieldY = labelY + 15

        textField = TextFieldWidget(
            textRenderer,
            20,
            textFieldY,
            leftPanelWidth - 40,
            20,
            Text.translatable("menu.minez_help.input")
        )
        textField?.setMaxLength(100)
        textField?.setEditable(true)
        addSelectableChild(textField)

        val startY = textFieldY + 30
        val buttonWidth = (leftPanelWidth - 60) / 3
        val buttonHeight = 20
        var index = 0
        for (row in 0 until 6) {
            for (col in 0 until 2) {
                val x = 20 + col * (buttonWidth + 10)
                val y = startY + row * (buttonHeight + 5)
                val button = ButtonWidget.builder(buttonTexts[index]) {
                    // logika po kliknięciu
                }.dimensions(x, y, buttonWidth, buttonHeight).build()
                dynamicButtons.add(button)
                addDrawableChild(button)
                index++
            }
        }

        addDrawableChild(ButtonWidget.builder(Text.translatable("menu.minez_help.webmap")) {
            Util.getOperatingSystem().open("https://maps.shotbow.net/minez")
        }.dimensions(width - 290, height - 30, 80, 20).build())

        addDrawableChild(ButtonWidget.builder(Text.translatable("menu.minez_help.wiki")) {
            Util.getOperatingSystem().open("https://wiki.shotbow.net/MineZ")
        }.dimensions(width - 190, height - 30, 80, 20).build())

        addDrawableChild(ButtonWidget.builder(Text.translatable("menu.minez_help.close")) {
            client?.setScreen(null)
        }.dimensions(width - 90, height - 30, 80, 20).build())
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground()

        val leftPanelWidth = (width * 0.3).toInt()

        context.fill(10, 10, leftPanelWidth - 10, height - 50, 0x80000000.toInt())
        context.drawTextWithShadow(textRenderer, Text.translatable("menu.minez_help.input_label"), 20, 20, 0xFFFFFF)

        context.fill(leftPanelWidth + 10, 10, width - 10, height - 50, 0x80202020.toInt())
        context.drawTextWithShadow(
            textRenderer,
            Text.translatable("menu.minez_help.description"),
            leftPanelWidth + 20,
            20,
            0xFFFFFF
        )

        context.fill(0, height - 40, width, height, 0x80303030.toInt())

        textField?.render(context, mouseX, mouseY, delta)
        super.render(context, mouseX, mouseY, delta)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (textField?.keyPressed(keyCode, scanCode, modifiers) == true) return true
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    private fun renderBackground() {}

    override fun shouldPause(): Boolean = false
}