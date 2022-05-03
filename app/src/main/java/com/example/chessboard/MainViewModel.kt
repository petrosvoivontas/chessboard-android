package com.example.chessboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chessboard.ui.MainIntent
import com.example.chessboard.ui.MainState
import com.example.chessboard.utils.PathsFinder
import com.example.chessboard.utils.Position
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {

	val intent = Channel<MainIntent>(Channel.UNLIMITED)

	private val _startPosition =
		MutableStateFlow<MainState.StartPosition>(MainState.StartPosition.Empty)
	val startPosition: StateFlow<MainState.StartPosition>
		get() = _startPosition

	private val _endPosition =
		MutableStateFlow<MainState.EndPosition>(MainState.EndPosition.Empty)
	val endPosition: StateFlow<MainState.EndPosition>
		get() = _endPosition

	private val _chessBoardClickIntent =
		MutableStateFlow<(Position) -> MainIntent>(MainIntent::SetStartPosition)
	val chessBoardClickIntent: StateFlow<(Position) -> MainIntent>
		get() = _chessBoardClickIntent

	private val _gridSize = MutableStateFlow<MainState.GridSize>(MainState.GridSize.Default)
	val gridSize: StateFlow<MainState.GridSize>
		get() = _gridSize

	private val _maxMoves = MutableStateFlow<MainState.MaxMoves>(MainState.MaxMoves.Default)
	val maxMoves: StateFlow<MainState.MaxMoves>
		get() = _maxMoves

	private val _canDecreaseMaxMoves = MutableStateFlow(_maxMoves.value.moves > 1)
	val canDecreaseMaxMoves: StateFlow<Boolean>
		get() = _canDecreaseMaxMoves

	private val _pathsIterator = MutableStateFlow<Iterator<List<Position>>?>(null)

	private val _loadingPath = MutableStateFlow(false)
	val loadingPath: StateFlow<Boolean>
		get() = _loadingPath

	private val _hasNextPath = MutableStateFlow(false)
	val hasNextPath: StateFlow<Boolean>
		get() = _hasNextPath

	private val _currentPath = MutableStateFlow<List<Position>?>(listOf())
	val currentPath: StateFlow<List<Position>?>
		get() = _currentPath

	init {
		viewModelScope.launch {
			intent.consumeAsFlow().collect {
				handleIntent(it)
			}
		}
	}

	private fun handleIntent(intent: MainIntent) {
		when (intent) {
			is MainIntent.SetStartPosition -> setStartPosition(intent.position)
			is MainIntent.SetEndPosition -> {
				setEndPosition(intent.position)
				findPaths()
			}
			is MainIntent.ChangeGridSize -> changeGridSize(intent.size)
			MainIntent.DecreaseMaxMoves -> decreaseMaxMoves()
			MainIntent.IncreaseMaxMoves -> increaseMaxMoves()
			MainIntent.Reset -> reset()
			MainIntent.ViewNextPath -> viewNextPath()
		}
	}

	private fun setStartPosition(position: Position) {
		_startPosition.value = MainState.StartPosition.PositionSelected(position)
		_chessBoardClickIntent.value = MainIntent::SetEndPosition
	}

	private fun setEndPosition(position: Position) {
		_endPosition.value = MainState.EndPosition.PositionSelected(position)
		_chessBoardClickIntent.value = MainIntent::SetStartPosition
	}

	private fun findPaths() {
		val startPosition = _startPosition.value.position ?: return
		val endPosition = _endPosition.value.position ?: return
		val gridSize = _gridSize.value.size
		val maxMoves = _maxMoves.value.moves

		val pathFinder = PathsFinder()
		val iterator = pathFinder.findPaths(startPosition, endPosition, gridSize, maxMoves)
		_pathsIterator.value = iterator
		viewModelScope.launch {
			_loadingPath.value = true
			withContext(Dispatchers.Default) {
				if (iterator.hasNext()) {
					val path = iterator.next()
					_currentPath.value = path
					_hasNextPath.value = iterator.hasNext()
				}
				_loadingPath.value = false
			}
		}
	}

	private fun changeGridSize(newSize: Int) {
		_gridSize.value = MainState.GridSize.SizeSelected(newSize)
	}

	private fun decreaseMaxMoves() {
		_maxMoves.value = if (_maxMoves.value.moves > 2) {
			MainState.MaxMoves.MovesSelected(_maxMoves.value.moves - 1)
		} else {
			_canDecreaseMaxMoves.value = false
			MainState.MaxMoves.Min
		}
	}

	private fun increaseMaxMoves() {
		_canDecreaseMaxMoves.value = true
		_maxMoves.value = MainState.MaxMoves.MovesSelected(_maxMoves.value.moves + 1)
	}

	private fun viewNextPath() {
		viewModelScope.launch {
			_loadingPath.value = true
			withContext(Dispatchers.Default) {
				val iterator = _pathsIterator.value
				if (iterator == null) {
					_loadingPath.value = false
					return@withContext
				}
				val path = iterator.next()
				_currentPath.value = path
				_hasNextPath.value = iterator.hasNext()
				_loadingPath.value = false
			}
		}
	}

	private fun reset() {
		_startPosition.value = MainState.StartPosition.Empty
		_gridSize.value = MainState.GridSize.Default
		_maxMoves.value = MainState.MaxMoves.Default
		_chessBoardClickIntent.value = MainIntent::SetStartPosition
		_loadingPath.value = false
		_hasNextPath.value = false
		_currentPath.value = null
	}
}