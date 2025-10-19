package team.blombix.dungshelper.mazeofthetenth

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.particle.DustParticleEffect
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import org.joml.Vector3f
import kotlin.math.sqrt

class MazeOfTheTenth {

    private val mc = MinecraftClient.getInstance()
    private var activeRoute: List<Vec3i>? = null

    private val checkPositions = listOf(
        1 to BlockPos(-21, 35, -1),
        2 to BlockPos(-8, 33, -16),
        3 to BlockPos(0, 33, -16),
        4 to BlockPos(11, 33, -15),
        5 to BlockPos(16, 33, -8),
        6 to BlockPos(16, 33, 8),
        7 to BlockPos(10, 33, 16),
        8 to BlockPos(0, 34, 18),
        9 to BlockPos(-8, 33, 18),
        10 to BlockPos(-15, 33, 8)
    )

    private val routes = mapOf(
        9 to listOf(
            Vec3i(-8, 33, 18),
            Vec3i(-29, 33, 48),
            Vec3i(-57, 33, 18),
            Vec3i(-65, 33, 21),
            Vec3i(-36, 32, 59),
            Vec3i(-43, 32, 69),
            Vec3i(0, 32, 81),
            Vec3i(0, 33, 70),
            Vec3i(69, 33, 16),
            Vec3i(79, 32, 20),
            Vec3i(57, 33, 60),
            Vec3i(62, 32, 69),
            Vec3i(0, 32, 93),
            Vec3i(0, 33, 107)
        )
    )

    fun register() {
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client ->
            val player = client.player ?: return@EndTick
            val world = client.world ?: return@EndTick

            // 🔹 jeśli nie ma aktywnej trasy, sprawdź pozycje startowe
            if (activeRoute == null) {
                val pos = player.blockPos
                for ((num, check) in checkPositions) {
                    if (isClose(pos, check, 1.5)) {
                        val route = routes[num]
                        if (route != null) {
                            activeRoute = route
                            player.sendMessage(Text.literal("✨ Rozpoczęto trasę $num"), false)
                        } else {
                            player.sendMessage(Text.literal("⚠️ Brak danych trasy $num"), false)
                        }
                        break
                    }
                }
            }

            // 🔹 rysowanie particle
            val route = activeRoute ?: return@EndTick
            val particle = DustParticleEffect(Vector3f(0.6f, 0.0f, 1.0f), 1.0f)
            val playerPos = player.pos

            val finish = Vec3d(route.last().x + 0.5, route.last().y + 0.5, route.last().z + 0.5)
            if (playerPos.distanceTo(finish) <= 2.0) {
                player.sendMessage(Text.literal("🏁 Ukończyłeś trasę!"), false)
                activeRoute = null
                return@EndTick
            }

            for (i in 0 until route.size - 1) {
                val start = Vec3d(route[i].x + 0.5, route[i].y + 0.5, route[i].z + 0.5)
                val end = Vec3d(route[i + 1].x + 0.5, route[i + 1].y + 0.5, route[i + 1].z + 0.5)
                if (playerPos.distanceTo(start) > 40 && playerPos.distanceTo(end) > 40) continue

                val diff = end.subtract(start)
                val length = diff.length()
                val stepSize = 1.5
                val steps = (length / stepSize).toInt().coerceAtLeast(1)
                val step = diff.multiply(1.0 / steps)

                var point = start
                repeat(steps) {
                    if (playerPos.distanceTo(point) <= 20) {
                        world.addParticle(particle, point.x, point.y, point.z, 0.0, 0.0, 0.0)
                    }
                    point = point.add(step)
                }
            }
        })
    }

    private fun isClose(a: BlockPos, b: BlockPos, radius: Double): Boolean {
        val dx = (a.x - b.x).toDouble()
        val dy = (a.y - b.y).toDouble()
        val dz = (a.z - b.z).toDouble()
        return sqrt(dx * dx + dy * dy + dz * dz) <= radius
    }
}