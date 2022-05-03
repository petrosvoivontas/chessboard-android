package com.example.chessboard.utils

import java.util.*

class PathsFinder {
	private val visited = hashSetOf<Position>()
	private val previousPosition = hashMapOf<Position, Position>()
	private val stack = Stack<Position>()
	private val numOfMoves = hashMapOf<Pair<Position, Position>, Int>()

	private var hasNext = false

	fun findPaths(
		startPosition: Position,
		endPosition: Position,
		gridSize: Int,
		maxMoves: Int
	): Iterator<List<Position>> {
		stack.add(startPosition)
		numOfMoves[startPosition to startPosition] = 0
		return object : Iterator<List<Position>> {
			override fun hasNext(): Boolean {
				hasNext = false
				outer@ while (stack.isNotEmpty()) {
					val u = stack.pop()
					val moves = numOfMoves[previousPosition[u] to u] ?: 0
					visited.add(u)

					if (moves >= maxMoves) continue

					for (nextPosition in u.knightMoves(gridSize)) {
						if (nextPosition in visited) continue
						previousPosition[nextPosition] = u
						stack.push(nextPosition)
						numOfMoves[u to nextPosition] = moves + 1
						if (nextPosition == endPosition) {
							visited.remove(nextPosition)
							stack.pop()
							hasNext = true
							break@outer
						}
					}
				}
				return hasNext
			}

			override fun next(): List<Position> {
				if (!hasNext) throw NoSuchElementException()
				val res = arrayListOf<Position>()
				var curr: Position? = endPosition
				res.add(endPosition)
				while (curr != null && curr != startPosition) {
					val prev = previousPosition[curr]!!
					res.add(prev)
					curr = prev
				}
				return res
			}
		}
	}
}