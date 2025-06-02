package team.blombix.navigation

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.particle.DustParticleEffect
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import org.joml.Vector3f
import kotlin.math.atan2

object NavigationModClient : ClientModInitializer {
    private var currentPath: List<Vec3d>? = null
    private var fallbackPath: List<Vec3d>? = null
    private var targetPosition: Vec3d? = null
    private const val PARTICLE_RANGE_SQ = 45.0 * 45.0
    private const val MAX_ASTAR_RANGE = 300.0

    override fun onInitializeClient() {
        register()
    }

    fun register() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            val world = client.world ?: return@register
            val player = client.player ?: return@register
            val target = targetPosition ?: return@register

            val playerPos = player.pos
            val directionVec = target.subtract(playerPos).normalize()
            val yaw = Math.toDegrees(atan2(directionVec.z, directionVec.x))
            val distance = playerPos.distanceTo(target)

            val arrow = when (yaw) {
                in -22.5..22.5 -> "→"
                in 22.5..67.5 -> "↘"
                in 67.5..112.5 -> "↓"
                in 112.5..157.5 -> "↙"
                in 157.5..180.0, in -180.0..-157.5 -> "←"
                in -157.5..-112.5 -> "↖"
                in -112.5..-67.5 -> "↑"
                in -67.5..-22.5 -> "↗"
                else -> "?"
            }

            val compassText = Text.literal("§6$arrow §7${"%.0f".format(distance)}m")
            client.inGameHud.setOverlayMessage(compassText, false)

            if (playerPos.squaredDistanceTo(target) <= 1.5) {
                player.sendMessage(Text.literal("§7[§a✔§7] §7You reached your destination."), false)
                clearNavigation()
                return@register
            }

            val path = currentPath ?: fallbackPath ?: return@register
            if (path.isEmpty()) return@register

            for (point in path) {
                val distanceSq = point.squaredDistanceTo(player.pos)
                if (distanceSq <= PARTICLE_RANGE_SQ) {
                    world.addParticle(
                        DustParticleEffect(Vector3f(0.6f, 0.0f, 1.0f), 1.0f),
                        point.x, point.y, point.z,
                        0.0, 0.0, 0.0
                    )
                }
            }
        }
    }

    fun navigateTo(target: Vec3d) {
        targetPosition = target
        val playerPos = MinecraftClient.getInstance().player?.pos ?: return
        println("Navigating to $target from $playerPos")
        computePath(playerPos, target)
    }

    private fun computePath(start: Vec3d, end: Vec3d) {
        val world = MinecraftClient.getInstance().world ?: return
        val startBlock = BlockPos.ofFloored(start)
        val endBlock = BlockPos.ofFloored(end)

        val distanceSq = startBlock.getSquaredDistance(endBlock)
        println("Computing path... Distance²: $distanceSq")

        if (distanceSq > MAX_ASTAR_RANGE * MAX_ASTAR_RANGE) {
            println("Using fallback path (too far)")
            fallbackPath = interpolatePath(start, end)
            currentPath = null
            return
        }

        val astarPath = AStarPathfinder.findPath(world, startBlock, endBlock, 2048)
        println("A* path size: ${astarPath.size}")
        if (astarPath.isEmpty()) {
            println("A* failed, switching to fallback path")
            fallbackPath = interpolatePath(start, end)
            currentPath = null
            return
        }

        currentPath = astarPath.map { Vec3d(it.x + 0.5, it.y + 0.2, it.z + 0.5) }
        fallbackPath = null
    }

    private fun interpolatePath(start: Vec3d, end: Vec3d): List<Vec3d> {
        val steps = 100
        val path = mutableListOf<Vec3d>()
        for (i in 0..steps) {
            val t = i.toDouble() / steps
            val x = start.x + (end.x - start.x) * t
            val y = start.y + (end.y - start.y) * t
            val z = start.z + (end.z - start.z) * t
            path.add(Vec3d(x, y + 0.2, z))
        }
        return path
    }

    fun clearNavigation() {
        currentPath = null
        fallbackPath = null
        targetPosition = null
    }

    @Suppress("unused")
    fun getPath(): List<Vec3d> = currentPath ?: fallbackPath ?: emptyList()
}
