package team.blombix.screens.mobs.hostile.others

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.text.Text
import net.minecraft.util.Util
import team.blombix.screens.*

class HMSMobsHostileWolf : Screen(Text.translatable("menu.minez_help.button1")) {

    private var searchField: TextFieldWidget? = null
    private val dynamicButtons = mutableListOf<ButtonWidget>()
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
        val rightPanelWidth = (width * 0.2).toInt()
        width - leftPanelWidth - rightPanelWidth - 40

        initLeftPanel(leftPanelWidth)

        // Przyciski na dole
        val buttonY = height - 40
        val smallButtonWidth = 80

        addDrawableChild(ButtonWidget.builder(Text.translatable("menu.minez_help.pageback")) {
            client?.setScreen(HelpMenuScreenMineZLoreQuestlines())
        }.dimensions(width - 650, buttonY, smallButtonWidth, 20).build())

        addDrawableChild(ButtonWidget.builder(Text.translatable("menu.minez_help.next")) {
            client?.setScreen(HelpMenuScreenThirstVisibility())
        }.dimensions(width - 550, buttonY, smallButtonWidth, 20).build())

        addDrawableChild(ButtonWidget.builder(Text.translatable("menu.minez_help.back")) {
            client?.setScreen(HelpMenuScreenMobs())
        }.dimensions(width - 450, buttonY, smallButtonWidth, 20).build())

        addDrawableChild(ButtonWidget.builder(Text.translatable("menu.minez_help.wiki")) {
            Util.getOperatingSystem().open("https://wiki.shotbow.net/MineZ_Mobs")
        }.dimensions(width - 200, buttonY, smallButtonWidth, 20).build())

        addDrawableChild(ButtonWidget.builder(Text.translatable("menu.minez_help.close")) {
            client?.setScreen(null)
        }.dimensions(width - 100, buttonY, smallButtonWidth, 20).build())
    }

    private fun initLeftPanel(leftPanelWidth: Int) {
        val labelY = 20
        val textFieldY = labelY + 15

        searchField = TextFieldWidget(
            textRenderer,
            20,
            textFieldY,
            leftPanelWidth - 40,
            20,
            Text.translatable("menu.minez_help.input")
        ).apply {
            setMaxLength(100)
            setEditable(true)
        }
        addSelectableChild(searchField)

        val startY = textFieldY + 30
        val availableHeight = height - startY - 60
        val buttonCount = 15
        val buttonHeight = 20
        val spacing = (availableHeight - buttonCount * buttonHeight) / (buttonCount - 1).coerceAtLeast(1)
        val buttonWidth = ((leftPanelWidth - 20) * 2) / 3


        for (i in 0 until buttonCount) {
            val y = startY + i * (buttonHeight + spacing)
            val button = ButtonWidget.builder(Text.translatable("menu.minez_help.button${i + 1}")) {
                client?.setScreen(screenList[i]())
            }.dimensions(20, y, buttonWidth, buttonHeight).build()
            dynamicButtons.add(button)
            addDrawableChild(button)
        }
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground()

        val leftPanelWidth = (width * 0.3).toInt()
        val rightPanelWidth = (width * 0.2).toInt()
        val centerPanelWidth = width - leftPanelWidth - rightPanelWidth - 40

        // Lewy panel
        context.fill(10, 10, leftPanelWidth - 10, height - 10, 0x80000000.toInt())
        context.drawTextWithShadow(textRenderer, Text.translatable("menu.minez_help.input_label"), 20, 20, 0xFFFFFF)

        // Środkowy panel
        val centerPanelX = leftPanelWidth + 10
        context.fill(centerPanelX, 10, centerPanelX + centerPanelWidth, height - 10, 0x80202020.toInt())

        context.matrices.push()
        context.matrices.translate(centerPanelX + 10f, 15f, 0f)
        context.matrices.scale(1.5f, 1.5f, 1f)
        context.drawTextWithShadow(
            textRenderer,
            Text.translatable("menu.minez_help.title.mobs.hostile.others.zombie"),
            0,
            0,
            0xFFFFFF
        )
        context.matrices.pop()

        val lines = textRenderer.wrapLines(
            Text.translatable("menu.minez_help.description.mobs.hostile.others.zombie"),
            centerPanelWidth - 20
        )

        totalTextHeight = lines.size * 12
        var y = scrollAreaTop - scrollOffset

        for (line in lines) {
            if (y + 12 > scrollAreaTop && y < scrollAreaBottom) {
                context.drawTextWithShadow(textRenderer, line, centerPanelX + 10, y, 0xFFFFFF)
            }
            y += 12
        }

        drawScrollbar(context, centerPanelX + centerPanelWidth)

        // Prawy panel
        val rightPanelX = centerPanelX + centerPanelWidth + 10
        context.fill(rightPanelX, 10, rightPanelX + rightPanelWidth - 10, height - 10, 0x80303030.toInt())

        context.drawTextWithShadow(textRenderer, Text.literal("Wygląd:"), rightPanelX + 10, 20, 0xFFFFFF)
        context.drawTextWithShadow(textRenderer, Text.literal("zombie.png"), rightPanelX + 10, 40, 0x00BFFF)

        context.drawTextWithShadow(textRenderer, Text.literal("Drops:"), rightPanelX + 10, 180, 0xFFFFFF)
        context.drawTextWithShadow(textRenderer, Text.literal("1 Rotten Flesh 25%"), rightPanelX + 10, 190, 0xFFFFFF)

        context.drawTextWithShadow(textRenderer, Text.literal("Health:"), rightPanelX + 10, 230, 0xFFFFFF)
        context.drawTextWithShadow(textRenderer, Text.literal("♥ ♥ ♥ ♥ ♥ ♥ ♥ ♥ ♥ ♥"), rightPanelX + 10, 250, 0xFF5555)

        context.drawTextWithShadow(textRenderer, Text.literal("Damage:"), rightPanelX + 10, 280, 0xFFFFFF)
        context.drawTextWithShadow(textRenderer, Text.literal("♥ ♥ ♥ ♥ ♥"), rightPanelX + 10, 290, 0xFF5555)

        searchField?.render(context, mouseX, mouseY, delta)
        super.render(context, mouseX, mouseY, delta)
    }

    private fun drawScrollbar(context: DrawContext, xPosition: Int) {
        if (totalTextHeight <= scrollAreaHeight) return

        val scrollbarX = xPosition - 8
        val scrollbarY = scrollAreaTop
        val scrollbarHeight = scrollAreaHeight
        val thumbHeight = (scrollbarHeight * (scrollAreaHeight.toFloat() / totalTextHeight)).toInt().coerceAtLeast(20)
        val maxScroll = (totalTextHeight - scrollAreaHeight).coerceAtLeast(1)
        val thumbY = scrollbarY + ((scrollOffset.toFloat() / maxScroll) * (scrollbarHeight - thumbHeight)).toInt()

        context.fill(scrollbarX, scrollbarY, scrollbarX + 4, scrollbarY + scrollbarHeight, 0x80000000.toInt())
        context.fill(scrollbarX, thumbY, scrollbarX + 4, thumbY + thumbHeight, 0xFFAAAAAA.toInt())
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

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (searchField?.keyPressed(keyCode, scanCode, modifiers) == true) return true
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    private fun renderBackground() {}

    override fun shouldPause(): Boolean = false
}