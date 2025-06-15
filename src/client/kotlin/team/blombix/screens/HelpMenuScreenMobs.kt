package team.blombix.screens

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.toast.SystemToast
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

    private val screenList = listOf(
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

    private val leftPanelWidth get() = (width * 0.25f).toInt()
    private val rightPanelWidth get() = (width * 0.25f).toInt()
    private val centerPanelWidth get() = width - leftPanelWidth - rightPanelWidth - 40

    private val leftPanelX = 10
    private val centerPanelX get() = leftPanelX + leftPanelWidth + 10
    private val rightPanelX get() = centerPanelX + centerPanelWidth + 10

    private val leftScrollTop get() = 60 + textRenderer.fontHeight + 10
    private val leftScrollBottom get() = height - 90
    private val leftScrollHeight get() = leftScrollBottom - leftScrollTop

    override fun init() {
        val textFieldY = 20 + textRenderer.fontHeight + 5

        textField = TextFieldWidget(
            textRenderer,
            leftPanelX + 10,
            textFieldY,
            leftPanelWidth - 20,
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
            }.dimensions(0, 0, leftPanelWidth - 20, buttonHeight).build()
            dynamicButtons.add(button)
            addDrawableChild(button)
        }

        totalLeftButtonsHeight = dynamicButtons.size * (buttonHeight + spacing)

        val bottomButtonY = height - 60
        val buttonHalfWidth = (leftPanelWidth - 30) / 2
        val smallButtonWidth = 80

        addDrawableChild(ButtonWidget.builder(Text.translatable("menu.minez_help.pageback")) {
            client?.setScreen(HelpMenuScreenSpawnKits())
        }.dimensions(leftPanelX + 10, bottomButtonY, buttonHalfWidth, 20).build())

        addDrawableChild(ButtonWidget.builder(Text.translatable("menu.minez_help.next")) {
            client?.setScreen(HelpMenuScreenGraveRobbingFishing())
        }.dimensions(leftPanelX + 20 + buttonHalfWidth, bottomButtonY, buttonHalfWidth, 20).build())

        addDrawableChild(ButtonWidget.builder(Text.translatable("menu.minez_help.wiki")) {
            Util.getOperatingSystem().open("https://wiki.shotbow.net/Category:MineZ_Mobs")
        }.dimensions(width - 200, height - 40, smallButtonWidth, 20).build())

        addDrawableChild(ButtonWidget.builder(Text.translatable("menu.minez_help.close")) {
            client?.setScreen(null)
        }.dimensions(width - 100, height - 40, smallButtonWidth, 20).build())

        val sectionButtonWidth = rightPanelWidth - 20
        val sectionButtonHeight = 20
        var sectionY = 60

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
                    //TMP toast alert
                    this.client?.toastManager?.add(
                        SystemToast.create(
                            this.client,
                            SystemToast.Type.NARRATOR_TOGGLE,
                            Text.translatable("menu.minez_toast.title"),
                            Text.translatable("menu.minez_toast.workinprogres.text")
                        )
                    )
                    client?.setScreen(factory())
                }.dimensions(rightPanelX + 10, sectionY, sectionButtonWidth, sectionButtonHeight).build()
            )
            sectionY += sectionButtonHeight + 5
        }
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground()

        context.fill(leftPanelX, 10, leftPanelX + leftPanelWidth, height - 10, 0x80000000.toInt())
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
            button.width = leftPanelWidth - 20
            button.visible = visible
            if (visible) button.render(context, mouseX, mouseY, delta)
            y += button.height + 5
        }

        context.fill(centerPanelX, 10, centerPanelX + centerPanelWidth, height - 10, 0x80202020.toInt())
        context.matrices.push()
        context.matrices.translate((centerPanelX + 10).toFloat(), 15f, 0f)
        context.matrices.scale(1.5f, 1.5f, 1f)
        context.drawTextWithShadow(textRenderer, Text.translatable("menu.minez_help.menu6.title"), 0, 0, 0xFFFFFF)
        context.matrices.pop()

        val lines = textRenderer.wrapLines(
            Text.translatable("menu.minez_help.description.mobs"),
            centerPanelWidth - 20
        )

        totalTextHeight = lines.size * 12
        y = scrollAreaTop - scrollOffset
        for (line in lines) {
            if (y + 12 > scrollAreaTop && y < scrollAreaBottom) {
                context.drawTextWithShadow(textRenderer, line, centerPanelX + 10, y, 0xFFFFFF)
            }
            y += 12
        }

        context.fill(rightPanelX, 10, rightPanelX + rightPanelWidth, height - 10, 0x80303030.toInt())

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

        if (mouseX in centerPanelX.toDouble()..(centerPanelX + centerPanelWidth).toDouble() &&
            mouseY in scrollAreaTop.toDouble()..scrollAreaBottom.toDouble()
        ) {
            scrollOffset = (scrollOffset - (verticalAmount * scrollStep).toInt()).coerceIn(0, maxScroll)
            return true
        }

        if (mouseX in leftPanelX.toDouble()..(leftPanelX + leftPanelWidth).toDouble() &&
            mouseY in leftScrollTop.toDouble()..leftScrollBottom.toDouble()
        ) {
            leftScrollOffset = (leftScrollOffset - (verticalAmount * scrollStep).toInt()).coerceIn(0, leftMaxScroll)
            return true
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    private fun drawScrollbar(context: DrawContext) {
        if (totalTextHeight <= scrollAreaHeight) return
        val scrollbarX = centerPanelX + centerPanelWidth - 4
        val thumbHeight = (scrollAreaHeight * (scrollAreaHeight.toFloat() / totalTextHeight)).toInt().coerceAtLeast(20)
        val maxScroll = (totalTextHeight - scrollAreaHeight).coerceAtLeast(1)
        val thumbY = scrollAreaTop + ((scrollOffset.toFloat() / maxScroll) * (scrollAreaHeight - thumbHeight)).toInt()

        context.fill(scrollbarX, scrollAreaTop, scrollbarX + 4, scrollAreaBottom, 0x80000000.toInt())
        context.fill(scrollbarX, thumbY, scrollbarX + 4, thumbY + thumbHeight, 0xFFAAAAAA.toInt())
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (textField?.keyPressed(keyCode, scanCode, modifiers) == true) return true
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    private fun renderBackground() {}

    override fun shouldPause(): Boolean = false
}
