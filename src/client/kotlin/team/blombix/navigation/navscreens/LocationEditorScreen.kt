package team.blombix.navigation.navscreens


import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.CyclingButtonWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import team.blombix.navigation.Location
import team.blombix.navigation.LocationManager


class LocationEditorScreen : Screen(Text.literal("Location Editor")) {

    private lateinit var nameField: TextFieldWidget
    private lateinit var xField: TextFieldWidget
    private lateinit var yField: TextFieldWidget
    private lateinit var zField: TextFieldWidget
    private lateinit var biomeDropdown: CyclingButtonWidget<String>

    private val biomeOptions = listOf(
        "swamp", "gravel", "forest", "dark_forest", "jungle forest", "jungle", "savana",
        "winter", "lava", "desert", "islands", "swamp_caves", "winter_caves"
    )

    private val boolFlags = mutableMapOf<String, Boolean>()
    private val boolKeys = listOf(
        "anvil", "furnace", "crafting", "cauldron", "water", "brewingstand",
        "ironore", "coalore", "carrots", "wheat", "beetroots", "potatoes",
        "pumpkin", "melon", "buttonroom", "buttonentry", "iscave",
        "iscaveentrance", "iscaveexit", "hiden"
    )

    override fun init() {
        val centerX = width / 2
        var y = 30

        val player = client?.player
        val pos = player?.blockPos ?: BlockPos.ORIGIN

        nameField = TextFieldWidget(textRenderer, centerX - 100, y, 200, 20, Text.literal("Location Name"))
        addSelectableChild(nameField)
        y += 30

        xField =
            TextFieldWidget(textRenderer, centerX - 100, y, 60, 20, Text.literal("X")).apply { text = pos.x.toString() }
        yField =
            TextFieldWidget(textRenderer, centerX - 30, y, 60, 20, Text.literal("Y")).apply { text = pos.y.toString() }
        zField =
            TextFieldWidget(textRenderer, centerX + 40, y, 60, 20, Text.literal("Z")).apply { text = pos.z.toString() }
        addSelectableChild(xField)
        addSelectableChild(yField)
        addSelectableChild(zField)
        y += 40

        biomeDropdown = CyclingButtonWidget.builder<String> { Text.literal(it) }
            .values(biomeOptions)
            .initially(biomeOptions.first())
            .build(centerX - 100, y, 200, 20, Text.literal("Biome")) { _, _ -> }
        addDrawableChild(biomeDropdown)
        y += 30

        boolKeys.chunked(3).forEach { row ->
            var x = centerX - 100
            row.forEach { key ->
                val button = PressableToggleWidget(Text.literal(key), x, y)
                button.onToggle = { boolFlags[key] = it }
                addDrawableChild(button)
                boolFlags[key] = false
                x += 70
            }
            y += 25
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Save")) {
            val name = nameField.text
            val x = xField.text.toIntOrNull() ?: 0
            val yCoord = yField.text.toIntOrNull() ?: 0
            val z = zField.text.toIntOrNull() ?: 0
            val biomeId = biomeDropdown.value

            val loc = Location(
                name = name,
                x = x,
                y = yCoord,
                z = z,
                biome = biomeId,
                anvil = boolFlags["anvil"] == true,
                furnace = boolFlags["furnace"] == true,
                crafting = boolFlags["crafting"] == true,
                cauldron = boolFlags["cauldron"] == true,
                water = boolFlags["water"] == true,
                brewingstand = boolFlags["brewingstand"] == true,
                ironore = boolFlags["ironore"] == true,
                coalore = boolFlags["coalore"] == true,
                carrots = boolFlags["carrots"] == true,
                wheat = boolFlags["wheat"] == true,
                beetroots = boolFlags["beetroots"] == true,
                potatoes = boolFlags["potatoes"] == true,
                pumpkin = boolFlags["pumpkin"] == true,
                melon = boolFlags["melon"] == true,
                buttonroom = boolFlags["buttonroom"] == true,
                buttonentry = boolFlags["buttonentry"] == true,
                iscave = boolFlags["iscave"] == true,
                iscaveentrance = boolFlags["iscaveentrance"] == true,
                iscaveexit = boolFlags["iscaveexit"] == true,
                hiden = boolFlags["hiden"] == false
            )

            LocationManager.addLocation(loc)
            client?.setScreen(null)
        }.dimensions(centerX - 40, height - 30, 80, 20).build())
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground()
        context.drawCenteredText(textRenderer, title, width / 2, 10, 0xFFFFFF)
        super.render(context, mouseX, mouseY, delta)
    }

    private fun renderBackground() {}

    override fun shouldPause() = false

    class PressableToggleWidget(
        text: Text,
        x: Int,
        y: Int
    ) : ButtonWidget(
        x, y, 65, 20, text,
        { },
        DEFAULT_NARRATION_SUPPLIER
    ) {
        private var toggled = false
        var onToggle: ((Boolean) -> Unit)? = null

        init {
            this.onPress = {
                toggled = !toggled
                updateMessage()
                onToggle?.invoke(toggled)
            } as PressAction?
            updateMessage()
        }

        private fun updateMessage() {
            this.message = if (toggled)
                Text.literal("✔ ${message.string.removePrefix("✔ ")}")
            else
                Text.literal(message.string.removePrefix("✔ "))
        }
    }

}

private fun DrawContext.drawCenteredText(
    textRenderer: TextRenderer,
    title: Text,
    i: Int,
    i2: Int,
    i3: Int
) {
}
