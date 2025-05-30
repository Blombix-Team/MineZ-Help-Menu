package team.blombix.navigation

import net.minecraft.util.math.Vec3d

data class Location(
    val name: String,
    val x: Int,
    val y: Int,
    val z: Int
) {
    fun toVec3d(): Vec3d = Vec3d(x + 0.5, y.toDouble(), z + 0.5)
}
