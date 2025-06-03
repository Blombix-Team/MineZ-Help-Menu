package team.blombix.screens

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.text.Text
import net.minecraft.util.Util

class HelpMenuScreenThirstVisibility : Screen(Text.translatable("menu.minez_help.button2")) {

    private var textField: TextFieldWidget? = null
    private val dynamicButtons = mutableListOf<ButtonWidget>()
    private var scrollOffset = 0
    private var leftScrollOffset = 0
    private var totalTextHeight = 0
    private var totalLeftButtonsHeight = 0

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

    private val screenList = listOf<() -> Screen>(
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

    private val scrollAreaTop = 38
    private val scrollAreaBottom get() = height - 65
    private val scrollAreaHeight get() = scrollAreaBottom - scrollAreaTop

    private val leftPanelWidth get() = (width * 0.3f).toInt()
    private val leftPanelX = 10
    private val leftPanelRight get() = leftPanelX + leftPanelWidth - 20
    private val leftScrollTop get() = 60 + textRenderer.fontHeight + 10
    private val leftScrollBottom get() = height - 90
    private val leftScrollHeight get() = leftScrollBottom - leftScrollTop

    override fun init() {
        val textFieldY = 20 + textRenderer.fontHeight + 5

        textField = TextFieldWidget(
            textRenderer,
            leftPanelX + 10,
            textFieldY,
            leftPanelWidth - 40,
            20,
            Text.translatable("menu.minez_help.input")
        ).apply {
            setMaxLength(100)
            setEditable(true)
        }
        addSelectableChild(textField)

        dynamicButtons.clear()
        val buttonHeight = 20
        val spacing = 5

        for (i in buttonTexts.indices) {
            val button = ButtonWidget.builder(buttonTexts[i]) {
                client?.setScreen(screenList[i]())
            }.dimensions(0, 0, leftPanelWidth - 40, buttonHeight).build()
            dynamicButtons.add(button)
            addDrawableChild(button)
        }

        totalLeftButtonsHeight = dynamicButtons.size * (buttonHeight + spacing)

        val bottomButtonY = height - 60
        val buttonWidth = (leftPanelWidth - 40) / 2
        val buttonY = height - 40
        val smallButtonWidth = 80

        addDrawableChild(ButtonWidget.builder(Text.translatable("menu.minez_help.pageback")) {
            client?.setScreen(HelpMenuScreenGettingStarted())
        }.dimensions(leftPanelX + 10, bottomButtonY, buttonWidth, 20).build())

        addDrawableChild(ButtonWidget.builder(Text.translatable("menu.minez_help.next")) {
            client?.setScreen(HelpMenuScreenHealing())
        }.dimensions(leftPanelX + 20 + buttonWidth, bottomButtonY, buttonWidth, 20).build())


        addDrawableChild(ButtonWidget.builder(Text.translatable("menu.minez_help.webmap")) {
            Util.getOperatingSystem().open("https://maps.shotbow.net/minez")
        }.dimensions(width - 300, buttonY, smallButtonWidth, 20).build())

        addDrawableChild(ButtonWidget.builder(Text.translatable("menu.minez_help.wiki")) {
            Util.getOperatingSystem().open("https://wiki.shotbow.net/Thirst_%26_Visibility")
        }.dimensions(width - 200, buttonY, smallButtonWidth, 20).build())

        addDrawableChild(ButtonWidget.builder(Text.translatable("menu.minez_help.close")) {
            client?.setScreen(null)
        }.dimensions(width - 100, buttonY, smallButtonWidth, 20).build())
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground()

        context.fill(leftPanelX, 10, leftPanelRight + 20, height - 10, 0x80000000.toInt())
        context.drawTextWithShadow(
            textRenderer,
            Text.translatable("menu.minez_help.input_label"),
            leftPanelX + 10,
            20,
            0xFFFFFF
        )
        textField?.render(context, mouseX, mouseY, delta)

        var y = leftScrollTop - leftScrollOffset
        for (button in dynamicButtons) {
            val visible = y + button.height > leftScrollTop && y < leftScrollBottom
            button.x = leftPanelX + 10
            button.y = y
            button.width = leftPanelWidth - 40
            button.visible = visible
            if (visible) {
                button.render(context, mouseX, mouseY, delta)
            }
            y += button.height + 5
        }

        val rightPanelX = leftPanelRight + 30
        context.fill(rightPanelX, 10, width - 10, height - 10, 0x80202020.toInt())

        context.matrices.push()
        context.matrices.translate((rightPanelX + 10).toFloat(), 15f, 0f)
        context.matrices.scale(1.5f, 1.5f, 1f)
        context.drawTextWithShadow(textRenderer, Text.translatable("menu.minez_help.menu2.title"), 0, 0, 0xFFFFFF)
        context.matrices.pop()

        val lines = textRenderer.wrapLines(
            Text.translatable("menu.minez_help.description.thirstvisibility"),
            width - rightPanelX - 20
        )

        totalTextHeight = lines.size * 12 + 28 * 12 + 150
        y = scrollAreaTop - scrollOffset

        for (line in lines) {
            if (y + 12 > scrollAreaTop && y < scrollAreaBottom) {
                context.drawTextWithShadow(textRenderer, line, rightPanelX + 10, y, 0xFFFFFF)
            }
            y += 12
        }

        val (headers, rows) = getLangTableData()
        val columnWidths = listOf(95, 55, 70, 55, 55, 150)
        val cellHeight = 15
        val tableStartY = y + 10

        var headerX = rightPanelX + 10
        headers.forEachIndexed { i, header ->
            context.drawTextWithShadow(textRenderer, header, headerX, tableStartY, 0xFFDD55)
            headerX += columnWidths[i]
        }

        for ((rowIndex, rowData) in rows.withIndex()) {
            var cellX = rightPanelX + 10
            val cellY = tableStartY + (rowIndex + 1) * cellHeight
            if (cellY in scrollAreaTop until scrollAreaBottom) {
                rowData.forEachIndexed { colIndex, cellText ->
                    context.drawTextWithShadow(textRenderer, cellText, cellX, cellY, 0xAAAAAA)
                    cellX += columnWidths[colIndex]
                }
            }
        }

        drawScrollbar(context)
        super.render(context, mouseX, mouseY, delta)
    }

    private fun getLangTableData(): Pair<List<String>, List<List<String>>> {
        val headers = (0..5).map { i ->
            Text.translatable("menu.minez_help.thirst_table.headers[$i]").string
        }
        val rows = (0 until 27).map { row ->
            (0 until 6).map { col ->
                Text.translatable("menu.minez_help.thirst_table.rows[$row][$col]").string
            }
        }
        return Pair(headers, rows)
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
