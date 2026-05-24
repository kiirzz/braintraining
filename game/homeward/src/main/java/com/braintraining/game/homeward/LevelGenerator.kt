package com.braintraining.game.homeward

import kotlin.random.Random

internal object LevelGenerator {

    private data class Params(
        val rows: Int,
        val cols: Int,
        val agentCount: Int,
        val obstacleCount: Int,
        val buffer: Int,
    )

    private fun params(level: Int): Params = when (level) {
        in 1..3  -> Params(5, 5, 2, 0, 4)
        in 4..6  -> Params(5, 5, 3, 2, 3)
        in 7..9  -> Params(6, 6, 3, 4, 2)
        in 10..12 -> Params(6, 6, 4, 6, 1)
        else     -> Params(7, 7, 5, 8, 0)
    }

    fun generate(level: Int): LevelData {
        val rng = Random(level.toLong() * 31337L)
        val p = params(level)
        repeat(200) {
            val result = tryGenerate(p, rng, level)
            if (result != null) return result
        }
        return fallback(p, level)
    }

    private fun tryGenerate(p: Params, rng: Random, level: Int): LevelData? {
        val occupied = mutableSetOf<Cell>()

        fun place(): Cell? {
            val free = (0 until p.rows).flatMap { r ->
                (0 until p.cols).map { c -> Cell(r, c) }
            }.filter { it !in occupied }
            return free.randomOrNull(rng)?.also { occupied.add(it) }
        }

        val agents = (0 until p.agentCount).map { i ->
            AgentState(i, AgentColor.entries[i], place() ?: return null)
        }
        val houses = (0 until p.agentCount).map { i ->
            HouseState(i, AgentColor.entries[i], place() ?: return null)
        }
        repeat(p.obstacleCount) { place() }

        val agentCells = agents.map { it.position }.toSet()
        val houseCells = houses.map { it.position }.toSet()
        val obstacles = occupied - agentCells - houseCells

        val distances = agents.map { agent ->
            val house = houses.first { it.agentId == agent.id }
            bfs(agent.position, house.position, p.rows, p.cols, obstacles) ?: return null
        }

        val minSteps = distances.sum()
        return LevelData(
            boardState = BoardState(p.rows, p.cols, agents, houses, obstacles),
            stepBudget = minSteps + p.buffer,
            minSteps = minSteps,
            level = level,
        )
    }

    // BFS shortest path on a grid avoiding obstacle cells.
    // Returns null if `to` is unreachable from `from`.
    fun bfs(from: Cell, to: Cell, rows: Int, cols: Int, obstacles: Set<Cell>): Int? {
        if (from == to) return 0
        val visited = mutableSetOf(from)
        val queue = ArrayDeque<Pair<Cell, Int>>()
        queue.addLast(from to 0)
        val dirs = arrayOf(Cell(-1, 0), Cell(1, 0), Cell(0, -1), Cell(0, 1))

        while (queue.isNotEmpty()) {
            val (cell, dist) = queue.removeFirst()
            for (d in dirs) {
                val next = Cell(cell.row + d.row, cell.col + d.col)
                if (next.row !in 0 until rows || next.col !in 0 until cols) continue
                if (next in obstacles || next in visited) continue
                if (next == to) return dist + 1
                visited.add(next)
                queue.addLast(next to dist + 1)
            }
        }
        return null
    }

    private fun fallback(p: Params, level: Int): LevelData {
        val n = p.agentCount.coerceAtMost(p.cols)
        val agents = (0 until n).map { i ->
            AgentState(i, AgentColor.entries[i], Cell(0, i))
        }
        val houses = (0 until n).map { i ->
            HouseState(i, AgentColor.entries[i], Cell(p.rows - 1, i))
        }
        val minSteps = n * (p.rows - 1)
        return LevelData(
            boardState = BoardState(p.rows, p.cols, agents, houses, emptySet()),
            stepBudget = minSteps + p.buffer,
            minSteps = minSteps,
            level = level,
        )
    }
}
