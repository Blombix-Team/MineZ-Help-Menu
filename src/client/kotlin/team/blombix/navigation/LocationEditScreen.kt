package team.blombix.navigation

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.text.Text
import kotlin.math.max
import kotlin.math.min

class LocationEditScreen(
    private val location: Location,
    private val parent: Screen? = null
) : Screen(Text.literal("Location Editor: ${location.name}")) {

    private lateinit var nameField: TextFieldWidget
    private lateinit var xField: TextFieldWidget
    private lateinit var yField: TextFieldWidget
    private lateinit var zField: TextFieldWidget

    private val biomes = listOf(
        "Swamp",
        "Gravel",
        "Forest",
        "Plains",
        "Ocean",
        "Redwood-Forest",
        "Jungle-Forest",
        "Savana",
        "Snow",
        "Lava",
        "Desert",
        "Islands",
        "Swamp-Caves",
        "Snow-Caves"
    )

    private var biomeIndex = 0
    private var biomeDropdownOpen = false
    private var biomeScroll = 0

    private val biomeItemHeight = 20
    private val biomeVisibleItems = 6

    private val featureNames = listOf(
        "Anvil",
        "Furnace",
        "Crafting",
        "Cauldron",
        "Water",
        "Brewingstand",
        "Iron Ore",
        "Coal Ore",
        "Carrots",
        "Wheat",
        "Beetroots",
        "Potatoes",
        "Pumpkin",
        "Melon",
        "Button room",
        "Button entry",
        "Cave",
        "Cave entrance",
        "Cave exit",
        "Hidden"
    )

    private val featureValues = mutableMapOf(
        "Anvil" to location.anvil,
        "Furnace" to location.furnace,
        "Crafting" to location.crafting,
        "Cauldron" to location.cauldron,
        "Water" to location.water,
        "Brewingstand" to location.brewingstand,
        "Iron Ore" to location.ironore,
        "Coal Ore" to location.coalore,
        "Carrots" to location.carrots,
        "Wheat" to location.wheat,
        "Beetroots" to location.beetroots,
        "Potatoes" to location.potatoes,
        "Pumpkin" to location.pumpkin,
        "Melon" to location.melon,
        "Button room" to location.buttonroom,
        "Button entry" to location.buttonentry,
        "Cave" to location.iscave,
        "Cave entrance" to location.iscaveentrance,
        "Cave exit" to location.iscaveexit,
        "Hidden" to location.hiden
    )

    private val featureButtons = mutableMapOf<String, ButtonWidget>()

    private var panelLeft = 0
    private var panelTop = 0
    private var panelWidth = 0
    private var panelHeight = 0

    private var contentLeft = 0
    private var contentWidth = 0

    private var featureScroll = 0

    private var featureTop = 0
    private var featureBottom = 0

    override fun init() {
        super.init()
        calculateLayout()

        createNameField()
        createCoordinateFields()
        createBiomeDropdown()
        createFeatureButtons()
        createBottomButtons()

        setInitialFocus(nameField)
    }

    private fun calculateLayout() {
        panelWidth = min(560, width - 20)

        panelHeight = min(540, height - 20)

        panelLeft = (width - panelWidth) / 2
        panelTop = (height - panelHeight) / 2

        contentLeft = panelLeft + 24
        contentWidth = panelWidth - 50

        featureTop = panelTop + 202

        featureBottom = panelTop + panelHeight - 64
    }

    private fun createNameField() {
        nameField = TextFieldWidget(
            textRenderer,
            contentLeft,
            panelTop + 60,
            contentWidth,
            22,
            Text.literal("Name")
        )

        nameField.text = location.name
        nameField.setMaxLength(64)

        addDrawableChild(nameField)
    }

    private fun createCoordinateFields() {
        val gap = 10
        val coordWidth = (contentWidth - gap * 2) / 3

        xField = createNumberField(
            contentLeft,
            panelTop + 90,
            coordWidth,
            location.x.toString(),
            "X"
        )

        yField = createNumberField(
            contentLeft + coordWidth + gap,
            panelTop + 90,
            coordWidth,
            location.y.toString(),
            "Y"
        )

        zField = createNumberField(
            contentLeft + (coordWidth + gap) * 2,
            panelTop + 90,
            coordWidth,
            location.z.toString(),
            "Z"
        )

        addDrawableChild(xField)
        addDrawableChild(yField)
        addDrawableChild(zField)
    }

    private fun createNumberField(
        x: Int,
        y: Int,
        width: Int,
        value: String,
        name: String
    ): TextFieldWidget {
        return TextFieldWidget(
            textRenderer,
            x,
            y,
            width,
            22,
            Text.literal(name)
        ).also {
            it.text = value
            it.setMaxLength(12)

            it.setTextPredicate { text ->
                text.matches(Regex("-?\\d*"))
            }
        }
    }


    private fun createBiomeDropdown() {
        biomeIndex = biomes
            .indexOf(location.biome)
            .coerceAtLeast(0)
    }

    private fun biomeButtonTop(): Int {
        return panelTop + 139
    }

    private fun biomeButtonBottom(): Int {
        return biomeButtonTop() + 21
    }

    private fun biomeDropdownTop(): Int {
        return biomeButtonBottom() + 3
    }

    private fun biomeDropdownHeight(): Int {
        return min(
            biomeVisibleItems,
            biomes.size
        ) * biomeItemHeight
    }

    private fun biomeDropdownBottom(): Int {
        return biomeDropdownTop() +
                biomeDropdownHeight()
    }

    // --------------------------------------------------
    // FEATURES
    // --------------------------------------------------

    private fun createFeatureButtons() {
        featureButtons.clear()

        val gap = 3
        val buttonWidth = (contentWidth - gap) / 3
        val rowHeight = 30

        featureNames.forEachIndexed { index, feature ->
            val column = index % 3
            val row = index / 3

            val x =
                contentLeft +
                        column * (buttonWidth + gap)

            val y =
                featureTop +
                        row * rowHeight

            val button = ButtonWidget.builder(
                featureText(feature)
            ) {
                featureValues[feature] =
                    !(featureValues[feature] ?: false)

                it.message = featureText(feature)
            }.dimensions(x, y, buttonWidth, 10).build()

            featureButtons[feature] = button

            addDrawableChild(button)
        }
    }

    private fun updateFeatureButtonPositions() {
        val gap = 3
        val buttonWidth = (contentWidth - gap) / 3
        val rowHeight = 25

        featureNames.forEachIndexed { index, feature ->

            val button =
                featureButtons[feature]
                    ?: return@forEachIndexed

            val column = index % 3
            val row = index / 3

            val x =
                contentLeft +
                        column * (buttonWidth + gap)

            val y =
                featureTop +
                        row * rowHeight -
                        featureScroll

            button.x = x
            button.y = y
            button.width = buttonWidth
            button.height = 22

            val insideViewport =
                y + button.height > featureTop &&
                        y < featureBottom

            button.visible =
                !biomeDropdownOpen &&
                        insideViewport

            button.active =
                !biomeDropdownOpen &&
                        insideViewport
        }
    }

    private fun updateFeatureVisibility() {
        updateFeatureButtonPositions()
    }

    private fun featureText(feature: String): Text {
        val enabled =
            featureValues[feature] ?: false

        return if (enabled) {
            Text.literal("§a☑  $feature")
        } else {
            Text.literal("§7☐  $feature")
        }
    }

    // --------------------------------------------------
    // BOTTOM BUTTONS
    // --------------------------------------------------

    private fun createBottomButtons() {
        val buttonGap = 12

        val bottomButtonWidth =
            (contentWidth - buttonGap) / 2

        val buttonY =
            panelTop + panelHeight - 40

        addDrawableChild(
            ButtonWidget.builder(
                Text.literal("§a✔  SAVE")
            ) {
                saveLocation()
            }.dimensions(
                contentLeft,
                buttonY,
                bottomButtonWidth,
                26
            ).build()
        )

        addDrawableChild(
            ButtonWidget.builder(
                Text.literal("§c✖  EXIT")
            ) {
                close()
            }.dimensions(
                contentLeft +
                        bottomButtonWidth +
                        buttonGap,
                buttonY,
                bottomButtonWidth,
                26
            ).build()
        )
    }

    // --------------------------------------------------
    // SCROLLING
    // --------------------------------------------------

    override fun mouseScrolled(
        mouseX: Double,
        mouseY: Double,
        horizontalAmount: Double,
        verticalAmount: Double
    ): Boolean {

        // --------------------------------------------------
        // BIOME DROPDOWN
        // --------------------------------------------------

        if (
            biomeDropdownOpen &&
            mouseX >= contentLeft &&
            mouseX <= contentLeft + contentWidth &&
            mouseY >= biomeDropdownTop() &&
            mouseY <= biomeDropdownBottom()
        ) {

            val maxScroll =
                max(
                    0,
                    biomes.size -
                            biomeVisibleItems
                )

            biomeScroll +=
                if (verticalAmount < 0) 1 else -1

            biomeScroll =
                biomeScroll.coerceIn(
                    0,
                    maxScroll
                )

            return true
        }

        // --------------------------------------------------
        // FEATURES
        // --------------------------------------------------

        if (
            !biomeDropdownOpen &&
            mouseX >= contentLeft &&
            mouseX <= contentLeft + contentWidth &&
            mouseY >= featureTop &&
            mouseY <= featureBottom
        ) {

            scrollFeatures(verticalAmount)

            return true
        }

        return super.mouseScrolled(
            mouseX,
            mouseY,
            horizontalAmount,
            verticalAmount
        )
    }

    private fun scrollFeatures(
        verticalAmount: Double
    ) {
        val rowHeight = 25

        val rows =
            (featureNames.size + 1) / 2

        val contentHeight =
            rows * rowHeight

        val visibleHeight =
            featureBottom - featureTop

        val maxScroll =
            max(
                0,
                contentHeight - visibleHeight
            )

        featureScroll -=
            (verticalAmount * rowHeight).toInt()

        featureScroll =
            featureScroll.coerceIn(
                0,
                maxScroll
            )

        updateFeatureButtonPositions()
    }

    // --------------------------------------------------
    // MOUSE
    // --------------------------------------------------

    override fun mouseClicked(
        mouseX: Double,
        mouseY: Double,
        button: Int
    ): Boolean {

        // --------------------------------------------------
        // BIOME SELECTOR
        // --------------------------------------------------

        val biomeLeft = contentLeft

        val biomeRight =
            contentLeft + contentWidth

        val biomeTop =
            biomeButtonTop()

        val biomeBottom =
            biomeButtonBottom()

        if (
            mouseX >= biomeLeft &&
            mouseX <= biomeRight &&
            mouseY >= biomeTop &&
            mouseY <= biomeBottom
        ) {

            biomeDropdownOpen =
                !biomeDropdownOpen

            if (biomeDropdownOpen) {
                biomeScroll = 0
            }

            updateFeatureVisibility()

            return true
        }

        // --------------------------------------------------
        // BIOME DROPDOWN
        // --------------------------------------------------

        if (biomeDropdownOpen) {

            val dropdownTop =
                biomeDropdownTop()

            val dropdownBottom =
                biomeDropdownBottom()

            if (
                mouseX >= biomeLeft &&
                mouseX <= biomeRight &&
                mouseY >= dropdownTop &&
                mouseY <= dropdownBottom
            ) {

                val row =
                    (
                            (mouseY - dropdownTop) /
                                    biomeItemHeight
                            ).toInt()

                val index =
                    biomeScroll + row

                if (
                    index >= 0 &&
                    index < biomes.size
                ) {

                    biomeIndex = index

                    biomeDropdownOpen = false
                    biomeScroll = 0

                    updateFeatureVisibility()

                    return true
                }
            }

            // Click outside dropdown
            biomeDropdownOpen = false
            biomeScroll = 0

            updateFeatureVisibility()

            return true
        }

        return super.mouseClicked(
            mouseX,
            mouseY,
            button
        )
    }

    // --------------------------------------------------
    // CUSTOM BIOME RENDERING
    // --------------------------------------------------

    private fun drawBiomeSelector(
        context: DrawContext,
        mouseX: Int,
        mouseY: Int
    ) {

        val left = contentLeft

        val right =
            contentLeft + contentWidth

        val top =
            biomeButtonTop()

        val bottom =
            biomeButtonBottom()

        // --------------------------------------------------
        // MAIN SELECTOR
        // --------------------------------------------------

        context.fill(
            left,
            top,
            right,
            bottom,
            0xFF171B22.toInt()
        )

        context.drawBorder(
            left,
            top,
            contentWidth,
            21,
            0xFF6E528F.toInt()
        )

        val hovered =
            mouseX >= left &&
                    mouseX <= right &&
                    mouseY >= top &&
                    mouseY <= bottom

        if (hovered) {

            context.fill(
                left + 1,
                top + 1,
                right - 1,
                bottom - 1,
                0xFF211B29.toInt()
            )
        }

        val selectedText =
            Text.literal(
                "§d${biomes[biomeIndex]}"
            )

        context.drawCenteredTextWithShadow(
            textRenderer,
            selectedText,
            (left + right) / 2 - 8,
            top + 6,
            0xFFFFFF
        )

        // Arrow
        val arrow =
            if (biomeDropdownOpen) {
                "▲"
            } else {
                "▼"
            }

        context.drawTextWithShadow(
            textRenderer,
            Text.literal("§d$arrow"),
            right - 18,
            top + 6,
            0xFFFFFF
        )

        // --------------------------------------------------
        // DROPDOWN
        // --------------------------------------------------

        if (!biomeDropdownOpen) {
            return
        }

        val dropdownTop =
            biomeDropdownTop()

        val dropdownHeight =
            biomeDropdownHeight()

        val dropdownBottom =
            dropdownTop + dropdownHeight

        // Shadow
        context.fill(
            left + 3,
            dropdownTop + 3,
            right + 3,
            dropdownBottom + 3,
            0x99000000.toInt()
        )

        // Background
        context.fill(
            left,
            dropdownTop,
            right,
            dropdownBottom,
            0xFF11151C.toInt()
        )

        // Border
        context.drawBorder(
            left,
            dropdownTop,
            contentWidth,
            dropdownHeight,
            0xFF6E528F.toInt()
        )

        // --------------------------------------------------
        // ITEMS
        // --------------------------------------------------

        val first =
            biomeScroll

        val last =
            min(
                biomes.size,
                first + biomeVisibleItems
            )

        for (index in first until last) {

            val row =
                index - first

            val itemTop =
                dropdownTop +
                        row * biomeItemHeight

            val itemBottom =
                itemTop +
                        biomeItemHeight

            val isHovered =
                mouseX >= left &&
                        mouseX <= right &&
                        mouseY >= itemTop &&
                        mouseY < itemBottom

            val isSelected =
                index == biomeIndex

            // Hover
            if (isHovered) {

                context.fill(
                    left + 1,
                    itemTop,
                    right - 1,
                    itemBottom,
                    0xFF30233A.toInt()
                )
            }

            // Selected
            if (isSelected) {

                context.fill(
                    left + 1,
                    itemTop,
                    left + 3,
                    itemBottom,
                    0xFFD76BFF.toInt()
                )
            }

            val text =
                if (isSelected) {
                    Text.literal(
                        "§d${biomes[index]}"
                    )
                } else {
                    Text.literal(
                        "§f${biomes[index]}"
                    )
                }

            context.drawTextWithShadow(
                textRenderer,
                text,
                left + 10,
                itemTop + 5,
                0xFFFFFF
            )
        }

        // --------------------------------------------------
        // SCROLLBAR
        // --------------------------------------------------

        if (biomes.size > biomeVisibleItems) {

            val scrollbarX =
                right - 5

            val trackTop =
                dropdownTop + 2

            val trackBottom =
                dropdownBottom - 2

            val trackHeight =
                trackBottom - trackTop

            val maxScroll =
                biomes.size -
                        biomeVisibleItems

            val thumbHeight =
                max(
                    18,
                    (
                            trackHeight.toFloat() *
                                    biomeVisibleItems /
                                    biomes.size
                            ).toInt()
                )

            val scrollRange =
                trackHeight -
                        thumbHeight

            val thumbOffset =
                if (maxScroll > 0) {
                    (
                            scrollRange.toFloat() *
                                    biomeScroll /
                                    maxScroll
                            ).toInt()
                } else {
                    0
                }

            context.fill(
                scrollbarX,
                trackTop,
                scrollbarX + 3,
                trackBottom,
                0xFF25202B.toInt()
            )

            context.fill(
                scrollbarX,
                trackTop + thumbOffset,
                scrollbarX + 3,
                trackTop +
                        thumbOffset +
                        thumbHeight,
                0xFF8B64A8.toInt()
            )
        }
    }

    // --------------------------------------------------
    // SAVE
    // --------------------------------------------------

    private fun saveLocation() {
        val name = nameField.text.trim()

        if (name.isBlank()) {
            nameField.setEditableColor(
                0xFFFF5555.toInt()
            )
            return
        }

        nameField.setEditableColor(
            0xFFE0E0E0.toInt()
        )

        val x =
            xField.text.toIntOrNull()
                ?: location.x

        val y =
            yField.text.toIntOrNull()
                ?: location.y

        val z =
            zField.text.toIntOrNull()
                ?: location.z

        val biome =
            biomes[biomeIndex]

        val updatedLocation = Location(
            name = name,
            x = x, y = y, z = z,
            anvil = featureValues["Anvil"] ?: false,
            furnace = featureValues["Furnace"] ?: false,
            crafting = featureValues["Crafting"] ?: false,
            cauldron = featureValues["Cauldron"] ?: false,
            water = featureValues["Water"] ?: false,
            brewingstand = featureValues["Brewingstand"] ?: false,
            ironore = featureValues["Iron Ore"] ?: false,
            coalore = featureValues["Coal Ore"] ?: false,
            carrots = featureValues["Carrots"] ?: false,
            wheat = featureValues["Wheat"] ?: false,
            beetroots = featureValues["Beetroots"] ?: false,
            potatoes = featureValues["Potatoes"] ?: false,
            pumpkin = featureValues["Pumpkin"] ?: false,
            melon = featureValues["Melon"] ?: false,
            buttonroom = featureValues["Button room"] ?: false,
            buttonentry = featureValues["Button entry"] ?: false,
            iscave = featureValues["Cave"] ?: false,
            iscaveentrance = featureValues["Cave entrance"] ?: false,
            iscaveexit = featureValues["Cave exit"] ?: false,
            biome = biome,
            hiden = featureValues["Hidden"] ?: false
        )

        val index = LocationManager.getAll().indexOfFirst { it.name.equals(location.name, ignoreCase = true) }

        if (index != -1) {
            LocationManager.updateLocation(index, updatedLocation)
        }

        client?.player?.sendMessage(
            Text.literal("§7[§a✔§7] §7Location updated: §6$name"), false
        )
        close()
    }

    override fun close() {
        client?.setScreen(parent)
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        context.fill(0, 0, width, height, 0xFF0B0E13.toInt())
        val panelRight = panelLeft + panelWidth
        val panelBottom = panelTop + panelHeight

        context.fill(panelLeft + 4, panelTop + 4, panelRight + 4, panelBottom + 4, 0x66000000.toInt())
        context.fill(panelLeft, panelTop, panelRight, panelBottom, 0xFF151A21.toInt())
        context.drawBorder(panelLeft, panelTop, panelWidth, panelHeight, 0xFF555A66.toInt())
        context.fill(panelLeft + 20, panelTop + 40, panelRight - 20, panelTop + 41, 0xFF5E4A78.toInt())
        context.drawCenteredTextWithShadow(
            textRenderer,
            Text.literal("§d§lLOCATION EDITOR"),
            width / 2, panelTop + 15, 0xFFFFFF
        )
        context.drawTextWithShadow(
            textRenderer,
            Text.literal("§d§lLOCATION"),
            contentLeft, panelTop + 47, 0xFFFFFF
        )
        context.drawTextWithShadow(
            textRenderer,
            Text.literal("§d§lBIOME"),
            contentLeft, panelTop + 127, 0xFFFFFF
        )
        if (!biomeDropdownOpen) {
            val featuresText = Text.literal("FEATURES")

            context.drawTextWithShadow(
                textRenderer,
                Text.literal("§d§lFEATURES"),
                width / 2 -
                        textRenderer.getWidth(featuresText) / 2,
                panelTop + 180,
                0xFFFFFF
            )
        }
        context.fill(
            contentLeft - 4,
            featureTop - 3,
            contentLeft + contentWidth + 4,
            featureBottom + 3,
            0xFF10141A.toInt()
        )
        context.drawBorder(
            contentLeft - 4,
            featureTop - 3,
            contentWidth + 8,
            featureBottom - featureTop + 6,
            0xFF343A45.toInt()
        )
        val bottomSeparatorY = panelTop + panelHeight - 52

        context.fill(
            panelLeft + 20,
            bottomSeparatorY,
            panelRight - 20,
            bottomSeparatorY + 1,
            0xFF343A45.toInt()
        )
        updateFeatureButtonPositions()
        super.render(context, mouseX, mouseY, delta)
        drawBiomeSelector(context, mouseX, mouseY)
    }

}