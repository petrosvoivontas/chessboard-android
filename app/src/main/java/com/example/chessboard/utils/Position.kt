package com.example.chessboard.utils

data class Position(val row: Int, val col: Int) {

	fun knightMoves(gridSize: Int): List<Position> {
		val xOffsets = intArrayOf(2, 1, -1, -2, -2, -1, 1, 2)
		val yOffsets = intArrayOf(1, 2, 2, 1, -1, -2 ,-2, -1)

		val res = arrayListOf<Position>()

		for (i in xOffsets.indices) {
			val dx = xOffsets[i]
			val dy = yOffsets[i]

			val newCol = col + dx
			val newRow = row + dy

			if (newCol in 0 until gridSize && newRow in 0 until gridSize) {
				res.add(Position(newRow, newCol))
			}
		}

		return res
	}
}
