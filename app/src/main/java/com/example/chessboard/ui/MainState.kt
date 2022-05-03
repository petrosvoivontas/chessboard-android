package com.example.chessboard.ui

import com.example.chessboard.utils.Position

class MainState {

	sealed class StartPosition(val position: Position?) {
		object Empty : StartPosition(null)
		class PositionSelected(position: Position) : StartPosition(position)
	}

	sealed class EndPosition(val position: Position?) {
		object Empty : EndPosition(null)
		class PositionSelected(position: Position) : EndPosition(position)
	}

	sealed class GridSize(val size: Int) {
		object Default : GridSize(8)
		class SizeSelected(size: Int) : GridSize(size)
	}

	sealed class MaxMoves(val moves: Int) {
		object Default : MaxMoves(3)
		object Min : MaxMoves(1)
		class MovesSelected(moves: Int) : MaxMoves(moves)
	}
}