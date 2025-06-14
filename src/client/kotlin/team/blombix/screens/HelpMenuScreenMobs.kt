package team.blombix.screens

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextFieldWidget
//import net.minecraft.client.toast.SystemToast
import net.minecraft.text.Text
import net.minecraft.util.Util
//import team.blombix.screens.mobs.HMSMobsHostile
//import team.blombix.screens.mobs.HMSMobsHostileBosses
//import team.blombix.screens.mobs.HMSMobsHostileDungeons
//import team.blombix.screens.mobs.HMSMobsPassive
import team.blombix.screens.mobs.hostile.others.Zombie

class HelpMenuScreenMobs : Screen(Text.translatable("menu.minez_help.button6")) {

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
        val buttonHalfWidth = (leftPanelWidth - 40) / 2
        val smallButtonWidth = 80

        addDrawableChild(ButtonWidget.builder(Text.translatable("menu.minez_help.pageback")) {
            client?.setScreen(HelpMenuScreenSpawnKits())
        }.dimensions(leftPanelX + 10, bottomButtonY, buttonHalfWidth, 20).build())

        addDrawableChild(ButtonWidget.builder(Text.translatable("menu.minez_help.next")) {
            client?.setScreen(HelpMenuScreenGraveRobbingFishing())
        }.dimensions(leftPanelX + 20 + buttonHalfWidth, bottomButtonY, buttonHalfWidth, 20).build())

        val rightY = height - 40

        addDrawableChild(ButtonWidget.builder(Text.translatable("menu.minez_help.wiki")) {
            Util.getOperatingSystem().open("https://wiki.shotbow.net/MineZ_Mobs")
        }.dimensions(width - 200, rightY, smallButtonWidth, 20).build())

        addDrawableChild(ButtonWidget.builder(Text.translatable("menu.minez_help.close")) {
            client?.setScreen(null)
        }.dimensions(width - 100, rightY, smallButtonWidth, 20).build())

        // Kategorie mobów – sekcje po prawej
        val sectionButtonWidth = 150
        val sectionButtonHeight = 20
        var sectionY = 80

        val categories = listOf(
            "Test Page" to { Zombie() },
            //"Hostile" to { HMSMobsHostile() },
            //"Hostile - Bosses" to { HMSMobsHostileBosses() },
            //"Hostile - Dungeons" to { HMSMobsHostileDungeons() },
            //"Passive" to { HMSMobsPassive() }
        )

        for ((label, factory) in categories) {
            addDrawableChild(
                ButtonWidget.builder(Text.literal(label)) {
                    client?.setScreen(factory())
                }.dimensions(width - sectionButtonWidth - 20, sectionY, sectionButtonWidth, sectionButtonHeight).build()
            )
            sectionY += sectionButtonHeight + 5
        }

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
            if (visible) button.render(context, mouseX, mouseY, delta)
            y += button.height + 5
        }

        val rightPanelX = leftPanelRight + 30
        context.fill(rightPanelX, 10, width - 10, height - 10, 0x80202020.toInt())

        context.matrices.push()
        context.matrices.translate((rightPanelX + 10).toFloat(), 15f, 0f)
        context.matrices.scale(1.5f, 1.5f, 1f)
        context.drawTextWithShadow(textRenderer, Text.translatable("menu.minez_help.menu6.title"), 0, 0, 0xFFFFFF)
        context.matrices.pop()

        val lines = textRenderer.wrapLines(
            Text.translatable("menu.minez_help.description.mobs"),
            width - rightPanelX - 20
        )

        totalTextHeight = lines.size * 12
        y = scrollAreaTop - scrollOffset

        for (line in lines) {
            if (y + 12 > scrollAreaTop && y < scrollAreaBottom) {
                context.drawTextWithShadow(textRenderer, line, rightPanelX + 10, y, 0xFFFFFF)
            }
            y += 12
        }

        drawScrollbar(context)
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
        val leftMaxScroll = (totalLeftButtonsHeight - leftScrollHeight).coerceAtLeast(0)

        if (mouseX > width * 0.3 && mouseY in scrollAreaTop.toDouble()..scrollAreaBottom.toDouble()) {
            scrollOffset = (scrollOffset - (verticalAmount * scrollStep).toInt()).coerceIn(0, maxScroll)
            return true
        }

        if (mouseX <= width * 0.3 && mouseY in leftScrollTop.toDouble()..leftScrollBottom.toDouble()) {
            leftScrollOffset = (leftScrollOffset - (verticalAmount * scrollStep).toInt()).coerceIn(0, leftMaxScroll)
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
