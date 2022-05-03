package com.example.chessboard.ui

import com.example.chessboard.utils.Position

sealed class MainIntent {

	data class SetStartPosition(val position: Position) : MainIntent()
	data class SetEndPosition(val position: Position) : MainIntent()

	data class ChangeGridSize(val size: Int) : MainIntent()

	object DecreaseMaxMoves : MainIntent()
	object IncreaseMaxMoves : MainIntent()

	object Reset : MainIntent()

	object ViewNextPath : MainIntent()
}