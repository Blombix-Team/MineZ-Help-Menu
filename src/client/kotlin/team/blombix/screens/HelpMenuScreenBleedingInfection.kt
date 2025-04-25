package team.blombix.screens

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.text.Text
import net.minecraft.util.Util

class HelpMenuScreenBleedingInfection : Screen(Text.translatable("menu.minez_help.button4")) {

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
        Text.translatable("menu.minez_help.button12"),
        Text.translatable("menu.minez_help.button13"),
        Text.translatable("menu.minez_help.button14"),
        Text.translatable("menu.minez_help.button15")
    )
    private val screenList: List<() -> Screen> = listOf(
        { HelpMenuScreenGettingStarted() },
        { HelpMenuScreenThirstVisibility() },
        { HelpMenuScreenHealing() },
        { HelpMenuScreenBleedingInfection() },
        { HelpMenuScreenSpawnKits() },
        { HelpMenuScreenMobs() },
        { HelpMenuScreenGraveRobbingFishing() },
        { HelpMenuScreenMiningComponentBags() },
        { HelpMenuScreenRecipes() },
        { HelpMenuScreenItems() },
        { HelpMenuScreenLocations() },
        { HelpMenuScreenDungeons() },
        { HelpMenuScreenLootChests() },
        { HelpMenuScreenAchievements() },
        { HelpMenuScreenMineZLoreQuestlines() }
    )

    private var scrollOffset = 0
    private var totalTextHeight = 0
    private val scrollAreaTop = 38
    private val scrollAreaBottom get() = height - 65
    private val scrollAreaHeight get() = scrollAreaBottom - scrollAreaTop

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
        val availableHeight = height - startY - 60
        val buttonCount = 15
        val buttonHeight = 22
        val spacing = (availableHeight - buttonCount * buttonHeight) / (buttonCount - 1).coerceAtLeast(1)
        val buttonWidth = ((leftPanelWidth - 20) * 2) / 3

        for (i in 0 until buttonCount) {
            val y = startY + i * (buttonHeight + spacing)
            val button = ButtonWidget.builder(buttonTexts[i]) {
                client?.setScreen(screenList[i]())
            }.dimensions(20, y, buttonWidth, buttonHeight).build()
            dynamicButtons.add(button)
            addDrawableChild(button)
        }

        val buttonY = height - 40
        val smallButtonWidth = 80

        addDrawableChild(ButtonWidget.builder(Text.translatable("menu.minez_help.pageback")) {
            client?.setScreen(HelpMenuScreenHealing())
        }.dimensions(width - 650, buttonY, smallButtonWidth, 20).build())

        addDrawableChild(ButtonWidget.builder(Text.translatable("menu.minez_help.next")) {
            client?.setScreen(HelpMenuScreenSpawnKits())
        }.dimensions(width - 550, buttonY, smallButtonWidth, 20).build())

        addDrawableChild(ButtonWidget.builder(Text.translatable("menu.minez_help.webmap")) {
            Util.getOperatingSystem().open("https://maps.shotbow.net/minez")
        }.dimensions(width - 300, buttonY, smallButtonWidth, 20).build())

        addDrawableChild(ButtonWidget.builder(Text.translatable("menu.minez_help.wiki")) {
            Util.getOperatingSystem().open("https://wiki.shotbow.net/MineZ_Getting_Started")
        }.dimensions(width - 200, buttonY, smallButtonWidth, 20).build())

        addDrawableChild(ButtonWidget.builder(Text.translatable("menu.minez_help.close")) {
            client?.setScreen(null)
        }.dimensions(width - 100, buttonY, smallButtonWidth, 20).build())
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground()

        val leftPanelWidth = (width * 0.3).toInt()

        context.fill(10, 10, leftPanelWidth - 10, height - 10, 0x80000000.toInt())
        context.drawTextWithShadow(textRenderer, Text.translatable("menu.minez_help.input_label"), 20, 20, 0xFFFFFF)

        context.fill(leftPanelWidth + 10, 10, width - 10, height - 10, 0x80202020.toInt())

        context.matrices.push()
        context.matrices.translate((leftPanelWidth + 20).toFloat(), 15f, 0f)
        context.matrices.scale(1.5f, 1.5f, 1f)
        context.drawTextWithShadow(
            textRenderer,
            Text.translatable("menu.minez_help.menu4.title"),
            0,
            0,
            0xFFFFFF
        )
        context.matrices.pop()

        val lines = textRenderer.wrapLines(
            Text.translatable("menu.minez_help.description.bleedinginfection"),
            width - leftPanelWidth - 40
        )

        totalTextHeight = lines.size * 12
        var y = scrollAreaTop - scrollOffset

        for (line in lines) {
            if (y + 12 > scrollAreaTop && y < scrollAreaBottom) {
                context.drawTextWithShadow(textRenderer, line, leftPanelWidth + 20, y, 0xFFFFFF)
            }
            y += 12
        }

        drawScrollbar(context)

        textField?.render(context, mouseX, mouseY, delta)
        super.render(context, mouseX, mouseY, delta)
    }

    override fun mouseScrolled(
        mouseX: Double,
        mouseY: Double,
        horizontalAmount: Double,
        verticalAmount: Double
    ): Boolean {
        val scrollStep = 12
        val maxScroll = (totalTextHeight - scrollAreaHeight).coerceAtLeast(0)

        if (mouseY in scrollAreaTop.toDouble()..scrollAreaBottom.toDouble()) {
            scrollOffset = (scrollOffset - (verticalAmount * scrollStep).toInt()).coerceIn(0, maxScroll)
            return true
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    private fun drawScrollbar(context: DrawContext) {
        if (totalTextHeight <= scrollAreaHeight) return

        val scrollbarX = width - 8
        val scrollbarY = scrollAreaTop
        val scrollbarHeight = scrollAreaHeight
        val thumbHeight = (scrollbarHeight * (scrollAreaHeight.toFloat() / totalTextHeight)).toInt().coerceAtLeast(20)
        val maxScroll = (totalTextHeight - scrollAreaHeight).coerceAtLeast(1)
        val thumbY = scrollbarY + ((scrollOffset.toFloat() / maxScroll) * (scrollbarHeight - thumbHeight)).toInt()

        context.fill(scrollbarX, scrollbarY, scrollbarX + 4, scrollbarY + scrollbarHeight, 0x80000000.toInt())
        context.fill(scrollbarX, thumbY, scrollbarX + 4, thumbY + thumbHeight, 0xFFAAAAAA.toInt())
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (textField?.keyPressed(keyCode, scanCode, modifiers) == true) return true
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    private fun renderBackground() {}

    override fun shouldPause(): Boolean = false
}
