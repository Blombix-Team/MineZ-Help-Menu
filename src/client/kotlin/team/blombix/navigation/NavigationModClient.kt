package team.blombix.navigation

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.particle.DustParticleEffect
import net.minecraft.util.math.Vec3d
import org.joml.Vector3f

object NavigationModClient {
    private var currentPath: List<Vec3d>? = null

    fun setNavigationTarget(start: Vec3d, end: Vec3d) {
        val steps = 80
        val path = mutableListOf<Vec3d>()
        for (i in 0..steps) {
            val t = i.toDouble() / steps
            val x = lerp(start.x, end.x, t)
            val y = lerp(start.y, end.y, t)
            val z = lerp(start.z, end.z, t)
            path.add(Vec3d(x, y + 0.2, z))
        }
        currentPath = path
    }

    private fun lerp(a: Double, b: Double, t: Double): Double = a + (b - a) * t

    fun register() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            val path = currentPath ?: return@register
            val world = client.world ?: return@register

            for (point in path) {
                world.addParticle(
                    DustParticleEffect(Vector3f(1f, 0f, 1f), 1.0f),
                    point.x, point.y, point.z,
                    0.0, 0.0, 0.0
                )
            }
        }
    }

    fun clearNavigation() {
        currentPath = null
        //animationTick = 0
    }

}
