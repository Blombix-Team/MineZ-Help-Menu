package team.blombix.navigation

data class Location(
    val name: String,
    val x: Int,
    val y: Int,
    val z: Int,
    val anvil: Boolean = false,
    val furnace: Boolean = false,
    val crafting: Boolean = false,
    val cauldron: Boolean = false,
    val water: Boolean = false,
    val brewingstand: Boolean = false,
    val ironore: Boolean = false,
    val coalore: Boolean = false,
    val carrots: Boolean = false,
    val wheat: Boolean = false,
    val beetroots: Boolean = false,
    val potatoes: Boolean = false,
    val pumpkin: Boolean = false,
    val melon: Boolean = false,
    val buttonroom: Boolean = false,
    val buttonentry: Boolean = false,
    val iscave: Boolean = false,
    val iscaveentrance: Boolean = false,
    val iscaveexit: Boolean = false,
    val biome: String,
    val hiden: Boolean = false
)
