package team.blombix.screens

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.text.Text
import team.blombix.navigation.Location
import team.blombix.navigation.LocationManager

class LocationEditorScreen : Screen(Text.literal("Location Editor")) {
    private val fields = mutableMapOf<String, TextFieldWidget>()
    private val toggles = mutableMapOf<String, Boolean>()
    private val toggleButtons = mutableMapOf<String, ButtonWidget>()
    private var selectedIndex = -1
    private var status = ""

    private val toggleDefinitions = listOf(
        "anvil" to "Anvil", "furnace" to "Furnace", "crafting" to "Crafting", "cauldron" to "Cauldron",
        "water" to "Water", "brewingstand" to "Brewing Stand", "ironore" to "Iron Ore", "coalore" to "Coal Ore",
        "carrots" to "Carrots", "wheat" to "Wheat", "beetroots" to "Beetroots", "potatoes" to "Potatoes",
        "pumpkin" to "Pumpkin", "melon" to "Melon", "buttonroom" to "Button Room", "buttonentry" to "Button Entry",
        "iscave" to "Cave", "iscaveentrance" to "Cave Entrance", "iscaveexit" to "Cave Exit", "hiden" to "Hidden"
    )

    override fun init() {
        clearChildren(); fields.clear(); toggleButtons.clear()
        val listX = 10; val listWidth = 180; val editorX = listX + listWidth + 10

        addDrawableChild(ButtonWidget.builder(Text.literal("+ New")) {
            selectedIndex = -1; status = "New location"; loadLocation(null)
        }.dimensions(listX, 20, listWidth, 20).build())

        LocationManager.getAll().forEachIndexed { index, location ->
            addDrawableChild(ButtonWidget.builder(Text.literal(location.name)) {
                selectedIndex = index; status = "Editing ${location.name}"; loadLocation(location)
            }.dimensions(listX, 45 + index * 24, listWidth, 20).build())
        }

        addTextField("name", editorX, 20); addTextField("x", editorX, 48); addTextField("y", editorX, 76)
        addTextField("z", editorX, 104); addTextField("biome", editorX, 132)

        toggleDefinitions.forEachIndexed { index, (key, label) ->
            val x = editorX + (index / 10) * 185; val y = 165 + (index % 10) * 23
            val button = ButtonWidget.builder(Text.literal(label)) {
                toggles[key] = !(toggles[key] ?: false); updateToggleButton(key)
            }.dimensions(x, y, 175, 20).build()
            toggleButtons[key] = button; addDrawableChild(button)
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Save")) { saveLocation() }.dimensions(editorX, height - 30, 80, 20).build())
        addDrawableChild(ButtonWidget.builder(Text.literal("Delete")) {
            if (selectedIndex >= 0) { LocationManager.removeLocation(selectedIndex); selectedIndex = -1; status = "Deleted"; init() }
        }.dimensions(editorX + 85, height - 30, 80, 20).build())
        addDrawableChild(ButtonWidget.builder(Text.literal("Back")) {
            client?.setScreen(HelpMenuScreenLocations())
        }.dimensions(width - 90, height - 30, 80, 20).build())

        loadLocation(LocationManager.getAll().getOrNull(selectedIndex))
    }

    private fun addTextField(key: String, x: Int, y: Int) {
        val field = TextFieldWidget(textRenderer, x, y, 170, 20, Text.literal(key))
        field.setMaxLength(100); fields[key] = field; addDrawableChild(field)
    }

    private fun loadLocation(location: Location?) {
        fields["name"]?.text = location?.name ?: ""
        fields["x"]?.text = location?.x?.toString() ?: "0"
        fields["y"]?.text = location?.y?.toString() ?: "0"
        fields["z"]?.text = location?.z?.toString() ?: "0"
        fields["biome"]?.text = location?.biome ?: ""
        toggleDefinitions.forEach { (key, _) ->
            toggles[key] = when (key) {
                "anvil" -> location?.anvil ?: false; "furnace" -> location?.furnace ?: false; "crafting" -> location?.crafting ?: false
                "cauldron" -> location?.cauldron ?: false; "water" -> location?.water ?: false; "brewingstand" -> location?.brewingstand ?: false
                "ironore" -> location?.ironore ?: false; "coalore" -> location?.coalore ?: false; "carrots" -> location?.carrots ?: false
                "wheat" -> location?.wheat ?: false; "beetroots" -> location?.beetroots ?: false; "potatoes" -> location?.potatoes ?: false
                "pumpkin" -> location?.pumpkin ?: false; "melon" -> location?.melon ?: false; "buttonroom" -> location?.buttonroom ?: false
                "buttonentry" -> location?.buttonentry ?: false; "iscave" -> location?.iscave ?: false
                "iscaveentrance" -> location?.iscaveentrance ?: false; "iscaveexit" -> location?.iscaveexit ?: false; "hiden" -> location?.hiden ?: false
                else -> false
            }; updateToggleButton(key)
        }
    }

    private fun updateToggleButton(key: String) {
        toggleButtons[key]?.message = Text.literal("${if (toggles[key] == true) "[x]" else "[ ]"} ${toggleDefinitions.first { it.first == key }.second}")
    }

    private fun bool(key: String) = toggles[key] == true

    private fun saveLocation() {
        val name = fields["name"]?.text?.trim().orEmpty(); val biome = fields["biome"]?.text?.trim().orEmpty()
        if (name.isBlank() || biome.isBlank()) { status = "Name and biome are required"; return }
        val location = Location(
            name, fields["x"]?.text?.toIntOrNull() ?: 0, fields["y"]?.text?.toIntOrNull() ?: 0, fields["z"]?.text?.toIntOrNull() ?: 0,
            bool("anvil"), bool("furnace"), bool("crafting"), bool("cauldron"), bool("water"), bool("brewingstand"),
            bool("ironore"), bool("coalore"), bool("carrots"), bool("wheat"), bool("beetroots"), bool("potatoes"),
            bool("pumpkin"), bool("melon"), bool("buttonroom"), bool("buttonentry"), bool("iscave"), bool("iscaveentrance"), bool("iscaveexit"), biome, bool("hiden")
        )
        if (selectedIndex >= 0) { LocationManager.updateLocation(selectedIndex, location); status = "Saved" }
        else { LocationManager.addLocation(location); selectedIndex = LocationManager.getAll().lastIndex; status = "Created"; init() }
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context); context.drawTextWithShadow(textRenderer, title, 10, 5, 0xFFFFFF)
        context.drawTextWithShadow(textRenderer, status, 220, height - 55, 0xFFFFFF); super.render(context, mouseX, mouseY, delta)
    }

    override fun shouldPause(): Boolean = false
}
