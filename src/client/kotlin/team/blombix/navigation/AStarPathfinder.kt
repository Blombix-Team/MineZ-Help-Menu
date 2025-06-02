package team.blombix.navigation

import net.minecraft.util.math.BlockPos
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

    fun findPath(world: World, start: BlockPos, end: BlockPos, maxSteps: Int = 2048): List<BlockPos> {
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

                if (openSet.any { it.pos == neighbor && it.fCost <= node.fCost }) continue
                openSet.add(node)
            }
        }

        return emptyList()
    }

    private fun heuristic(a: BlockPos, b: BlockPos): Int {
        return abs(a.x - b.x) + abs(a.y - b.y) + abs(a.z - b.z)
    }

    private fun buildPath(endNode: Node): List<BlockPos> {
        val path = mutableListOf<BlockPos>()
        var node: Node? = endNode
        while (node != null) {
            path.add(node.pos)
            node = node.parent
        }
        return path.reversed()
    }

    private fun getNeighbors(world: World, pos: BlockPos): List<BlockPos> {
        val directions = listOf(
            BlockPos(1, 0, 0), BlockPos(-1, 0, 0),
            BlockPos(0, 0, 1), BlockPos(0, 0, -1),
            BlockPos(0, 1, 0), BlockPos(0, -1, 0)
        )

        val result = mutableListOf<BlockPos>()

        for (offset in directions) {
            val candidate = pos.add(offset)
            val ground = candidate.down()

            val feetState = world.getBlockState(candidate)
            val headState = world.getBlockState(candidate.up())
            val groundState = world.getBlockState(ground)

            val feetPassable = feetState.isAir || feetState.getCollisionShape(world, candidate).isEmpty
            val headPassable = headState.isAir || headState.getCollisionShape(world, candidate.up()).isEmpty
            val groundSolid = groundState.isOpaque || groundState.block.translationKey.contains("water")

            if (feetPassable && headPassable && groundSolid) {
                result.add(candidate)
            }
        }

        return result
    }

}
