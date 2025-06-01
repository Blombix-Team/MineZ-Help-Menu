package team.blombix.navigation

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.particle.DustParticleEffect
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import org.joml.Vector3f

object NavigationModClient {
    private var currentPath: List<Vec3d>? = null
    private var animationTick: Int = 0
    private var tickCounter: Int = 0
    private var targetPosition: Vec3d? = null
    private var lastPlayerPos: Vec3d? = null

    fun setNavigationTarget(start: Vec3d, end: Vec3d) {
        targetPosition = end
        computePath(start, end)
    }

    private fun computePath(start: Vec3d, end: Vec3d) {
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
        targetPosition = null
        lastPlayerPos = null
        tickCounter = 0
    }

    fun getPath(): List<Vec3d> = currentPath ?: emptyList()

    fun estimateTime(path: List<Vec3d>): Triple<Double, Double, Double> {
        if (path.size < 2) return Triple(0.0, 0.0, 0.0)
        var distance = 0.0

        for (i in 1 until path.size) {
            distance += path[i].distanceTo(path[i - 1])
        }

        val walkSpeed = 4.3
        val runSpeed = 5.6

        val walkTime = distance / walkSpeed
        val runTime = distance / runSpeed

        return Triple(distance, walkTime, runTime)
    }

    fun register() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            val world = client.world ?: return@register
            val player = client.player ?: return@register
            val path = currentPath ?: return@register
            if (path.isEmpty()) return@register
            if (targetPosition == null) return@register

            // AUTO-END when near destination
            val target = targetPosition
            if (target != null && player.pos.squaredDistanceTo(target) <= 1.5) {
                player.sendMessage(
                    Text.literal("§7[§a✔§7] §7You have reached your destination: §6${target.x.toInt()}, ${target.y.toInt()}, ${target.z.toInt()}"),
                    false
                )
                clearNavigation()
                return@register
            }

            // Show entire static path within 25 block range
            for (point in path) {
                val distanceSq = point.squaredDistanceTo(player.pos)
                if (distanceSq <= 25.0 * 25.0) {
                    world.addParticle(
                        DustParticleEffect(Vector3f(0.6f, 0.0f, 1.0f), 1.0f),
                        point.x, point.y, point.z,
                        0.1, 0.0, 0.1
                    )
                }
            }
        }
    }

}
