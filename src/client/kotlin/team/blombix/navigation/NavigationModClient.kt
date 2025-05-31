package team.blombix.navigation

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.particle.DustParticleEffect
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import org.joml.Vector3f

object NavigationModClient {
    private var currentPath: List<Vec3d>? = null
    private var animationTick: Int = 0

    fun setNavigationTarget(start: Vec3d, end: Vec3d) {
        val world = MinecraftClient.getInstance().world ?: return
        val startBlock = BlockPos.ofFloored(start)
        val endBlock = BlockPos.ofFloored(end)

        val path = AStarPathfinder.findPath(world, startBlock, endBlock)
        currentPath = path
        animationTick = 0
    }


    fun clearNavigation() {
        currentPath = null
        animationTick = 0
    }

    fun register() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            val path = currentPath ?: return@register
            val world = client.world ?: return@register
            if (path.isEmpty()) return@register

            val stepsToShow = 5
            for (i in 0 until stepsToShow) {
                val step = (animationTick + i) % path.size
                val point = path[step]

                world.addParticle(
                    DustParticleEffect(Vector3f(0f, 1f, 0f), 1.0f),
                    point.x, point.y, point.z,
                    0.0, 0.0, 0.0
                )
            }

            animationTick++
        }
    }

}
