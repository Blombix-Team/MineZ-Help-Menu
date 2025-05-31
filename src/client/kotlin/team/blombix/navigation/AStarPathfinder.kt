package team.blombix.navigation

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.*
import kotlin.math.abs

object AStarPathfinder {
    data class Node(
        val pos: BlockPos,
        val gCost: Int,
        val hCost: Int,
        val parent: Node?
    ) {
        val fCost: Int
            get() = gCost + hCost
    }

    fun findPath(world: World, start: BlockPos, end: BlockPos, maxSteps: Int = 6000): List<Vec3d> {
        val openSet = PriorityQueue<Node>(compareBy { it.fCost })
        val closedSet = mutableSetOf<BlockPos>()
        val startNode = Node(start, 0, heuristic(start, end), null)
        openSet.add(startNode)

        while (openSet.isNotEmpty() && closedSet.size < maxSteps) {
            val current = openSet.poll() ?: break
            if (current.pos == end) {
                return buildPath(current)
            }

            closedSet.add(current.pos)
            for (neighbor in getNeighbors(world, current.pos)) {
                if (neighbor in closedSet) continue

                val gCost = current.gCost + 1
                val hCost = heuristic(neighbor, end)
                val node = Node(neighbor, gCost, hCost, current)

                // Jeśli w openSet już istnieje z mniejszym kosztem — pomiń
                if (openSet.any { it.pos == neighbor && it.fCost <= node.fCost }) continue
                openSet.add(node)
            }
        }

        return emptyList() // nie znaleziono ścieżki
    }

    private fun heuristic(a: BlockPos, b: BlockPos): Int {
        return abs(a.x - b.x) + abs(a.y - b.y) + abs(a.z - b.z)
    }

    private fun buildPath(endNode: Node): List<Vec3d> {
        val path = mutableListOf<Vec3d>()
        var node: Node? = endNode
        while (node != null) {
            path.add(Vec3d(node.pos.x + 0.5, node.pos.y + 0.2, node.pos.z + 0.5))
            node = node.parent
        }
        return path.reversed()
    }

    private fun getNeighbors(world: World, pos: BlockPos): List<BlockPos> {
        val directions = listOf(
            BlockPos(1, 0, 0), BlockPos(-1, 0, 0),
            BlockPos(0, 0, 1), BlockPos(0, 0, -1)
        )

        val result = mutableListOf<BlockPos>()

        for (offset in directions) {
            val forward = pos.add(offset)

            val candidates = listOf(
                forward,
                forward.up(),
                forward.down()
            )

            for (candidate in candidates) {
                val ground = candidate.down()
                val stateAtFeet = world.getBlockState(candidate)
                val stateAtHead = world.getBlockState(candidate.up())
                val stateBelow = world.getBlockState(ground)

                val isFeetPassable = stateAtFeet.isAir ||
                        stateAtFeet.getCollisionShape(world, candidate).isEmpty ||
                        stateAtFeet.block.translationKey.contains("door")

                val isHeadClear = stateAtHead.isAir || stateAtHead.getCollisionShape(world, candidate.up()).isEmpty

                val isGroundSolid = stateBelow.isOpaque || stateBelow.block.translationKey.contains("water")

                if (isFeetPassable && isHeadClear && isGroundSolid) {
                    result.add(candidate)
                }
            }
        }

        return result
    }

}
