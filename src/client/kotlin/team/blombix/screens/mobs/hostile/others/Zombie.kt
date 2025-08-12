package team.blombix.screens.mobs.hostile.others

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.entity.EntityType
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.mob.ZombieEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.Text
import net.minecraft.util.Util
import org.joml.Quaternionf
import org.joml.Vector3f
import team.blombix.screens.*

class Zombie : Screen(Text.translatable("menu.minez_help.title.mobs.hostile.others.zombie")) {

    private var searchField: TextFieldWidget? = null
    private val dynamicButtons = mutableListOf<ButtonWidget>()
    private lateinit var zombieEntity: ZombieEntity

    private var centerScrollOffset = 0
    private var leftScrollOffset = 0
    private var totalTextHeight = 0
    private var totalLeftButtonsHeight = 0

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

    private val leftPanelWidth get() = (width * 0.25f).toInt()
    private val rightPanelWidth get() = (width * 0.25f).toInt()
    private val centerPanelWidth get() = width - leftPanelWidth - rightPanelWidth - 40

    private val leftPanelX = 10
    private val centerPanelX get() = leftPanelX + leftPanelWidth + 10
    private val rightPanelX get() = width - rightPanelWidth - 10

    private val leftScrollTop get() = 60 + textRenderer.fontHeight + 10
    private val leftScrollBottom get() = height - 90
    private val leftScrollHeight get() = leftScrollBottom - leftScrollTop

    override fun init() {
        initZombieEntity()

        val textFieldY = 20 + textRenderer.fontHeight + 5

        searchField = TextFieldWidget(
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
        addSelectableChild(searchField)

        dynamicButtons.clear()
        val buttonHeight = 20
        val spacing = 5

        for (i in screenList.indices) {
            val button = ButtonWidget.builder(Text.translatable("menu.minez_help.button${i + 1}")) {
                client?.setScreen(screenList[i]())
            }.dimensions(0, 0, leftPanelWidth - 20, buttonHeight).build()
            dynamicButtons.add(button)
            addDrawableChild(button)
        }

        totalLeftButtonsHeight = dynamicButtons.size * (buttonHeight + spacing)

        val bottomButtonY = height - 60
        val buttonHalfWidth = (leftPanelWidth - 30) / 2
        val smallButtonWidth = 80

        addDrawableChild(ButtonWidget.builder(Text.translatable("menu.minez_help.back")) {
            client?.setScreen(HelpMenuScreenMobs())
        }.dimensions(leftPanelX + 10, bottomButtonY, buttonHalfWidth, 20).build())

        addDrawableChild(ButtonWidget.builder(Text.translatable("menu.minez_help.wiki")) {
            Util.getOperatingSystem().open("https://wiki.shotbow.net/MineZ_Mobs#Zombies")
        }.dimensions(width - 190, height - 40, smallButtonWidth, 20).build())

        addDrawableChild(ButtonWidget.builder(Text.translatable("menu.minez_help.close")) {
            client?.setScreen(null)
        }.dimensions(width - 100, height - 40, smallButtonWidth, 20).build())
    }

    private fun initZombieEntity() {
        zombieEntity = ZombieEntity(EntityType.ZOMBIE, client?.world).apply {
            setPos(0.0, 0.0, 0.0)
            setCustomName(Text.literal("Player Zombie"))
            isCustomNameVisible = true

            fun named(item: Item?, name: String) = ItemStack(item).apply {
                setCustomName(Text.literal(name))
            }

            equipStack(EquipmentSlot.HEAD, named(Items.IRON_HELMET, "Cursed Helmet"))
            equipStack(EquipmentSlot.CHEST, named(Items.IRON_CHESTPLATE, "Undead Chestguard"))
            equipStack(EquipmentSlot.LEGS, named(Items.IRON_LEGGINGS, "Rotten Leggings"))
            equipStack(EquipmentSlot.FEET, named(Items.IRON_BOOTS, "Gravewalkers"))
            equipStack(EquipmentSlot.MAINHAND, named(Items.IRON_SWORD, "Soul Blade"))
        }
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground()

        // Left panel
        context.fill(leftPanelX, 10, leftPanelX + leftPanelWidth, height - 10, 0x80000000.toInt())
        context.drawTextWithShadow(
            textRenderer,
            Text.translatable("menu.minez_help.input_label"),
            leftPanelX + 10,
            20,
            0xFFFFFF
        )
        searchField?.render(context, mouseX, mouseY, delta)

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

        // Center panel (text)
        context.fill(centerPanelX, 10, centerPanelX + centerPanelWidth, height - 10, 0x80202020.toInt())

        context.matrices.push()
        context.matrices.translate((centerPanelX + 10).toFloat(), 15f, 0f)
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
        y = scrollAreaTop - centerScrollOffset

        for (line in lines) {
            if (y + 12 > scrollAreaTop && y < scrollAreaBottom) {
                context.drawTextWithShadow(textRenderer, line, centerPanelX + 10, y, 0xFFFFFF)
            }
            y += 12
        }

        drawScrollbar(context)

        // Right panel
        renderRightPanel(context)
        super.render(context, mouseX, mouseY, delta)
    }

    private fun renderRightPanel(context: DrawContext) {
        val x = rightPanelX
        val w = rightPanelWidth
        context.fill(x, 10, x + w, height - 10, 0x80303030.toInt())

        // Render 3D model
        val yaw = Quaternionf().rotateY(Math.toRadians(180.0).toFloat())       // Obrót w stronę gracza
        val flipZ = Quaternionf().rotateZ(Math.toRadians(180.0).toFloat())     // Obrót do góry nogami
        val combined = yaw.mul(flipZ)

        InventoryScreen.drawEntity(
            context,
            (x + w / 2).toFloat(),
            80f,
            30f,
            Vector3f(0f, 0f, 0f),
            combined,
            null,
            zombieEntity
        )


        var ry = 120
        val spacing = 18

        context.drawTextWithShadow(textRenderer, Text.literal("Drops:"), x + 10, ry, 0xFFFFFF)
        ry += spacing
        context.drawTextWithShadow(textRenderer, Text.literal("• 100x Rotten Flesh"), x + 10, ry, 0xAAAAAA)
        ry += spacing

        context.drawTextWithShadow(textRenderer, Text.literal("Health:"), x + 10, ry, 0xFFFFFF)
        ry += spacing
        context.drawTextWithShadow(textRenderer, Text.literal("♥ 200 (standard)"), x + 10, ry, 0x55FF55)
        ry += spacing

        context.drawTextWithShadow(textRenderer, Text.literal("Damage:"), x + 10, ry, 0xFFFFFF)
        ry += spacing
        context.drawTextWithShadow(textRenderer, Text.literal("♥ 200"), x + 10, ry, 0xFF5555)
    }

    private fun drawScrollbar(context: DrawContext) {
        if (totalTextHeight <= scrollAreaHeight) return

        val scrollbarX = centerPanelX + centerPanelWidth - 4
        val scrollbarY = scrollAreaTop
        val scrollbarHeight = scrollAreaHeight
        val thumbHeight = (scrollbarHeight * (scrollAreaHeight.toFloat() / totalTextHeight)).toInt().coerceAtLeast(20)
        val maxScroll = (totalTextHeight - scrollAreaHeight).coerceAtLeast(1)
        val thumbY = scrollbarY + ((centerScrollOffset.toFloat() / maxScroll) * (scrollbarHeight - thumbHeight)).toInt()

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
        val centerMaxScroll = (totalTextHeight - scrollAreaHeight).coerceAtLeast(0)
        val leftMaxScroll = (totalLeftButtonsHeight - leftScrollHeight).coerceAtLeast(0)

        if (mouseX in centerPanelX.toDouble()..(centerPanelX + centerPanelWidth).toDouble() && mouseY in scrollAreaTop.toDouble()..scrollAreaBottom.toDouble()) {
            centerScrollOffset =
                (centerScrollOffset - (verticalAmount * scrollStep).toInt()).coerceIn(0, centerMaxScroll)
            return true
        }

        if (mouseX in leftPanelX.toDouble()..(leftPanelX + leftPanelWidth).toDouble() && mouseY in leftScrollTop.toDouble()..leftScrollBottom.toDouble()) {
            leftScrollOffset = (leftScrollOffset - (verticalAmount * scrollStep).toInt()).coerceIn(0, leftMaxScroll)
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
