package com.example.chessboard

import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.chessboard.databinding.ActivityMainBinding
import com.example.chessboard.ui.ChessPiece
import com.example.chessboard.ui.MainIntent
import kotlinx.coroutines.flow.collect

class MainActivity : AppCompatActivity() {

	private lateinit var binding: ActivityMainBinding
	private val viewModel: MainViewModel by viewModels()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)

		binding.chessboardSizeSlider.addOnChangeListener { _, value, _ ->
			lifecycleScope.launchWhenCreated {
				viewModel.intent.send(MainIntent.ChangeGridSize(value.toInt()))
			}
		}

		binding.chessboard.setOnChessPositionClickedListener { _, position ->
			lifecycleScope.launchWhenCreated {
				val intent = viewModel.chessBoardClickIntent.value(position)
				viewModel.intent.send(intent)
			}
		}

		binding.maxMovesDecrease.setOnClickListener {
			lifecycleScope.launchWhenCreated {
				viewModel.intent.send(MainIntent.DecreaseMaxMoves)
			}
		}

		binding.maxMovesIncrease.setOnClickListener {
			lifecycleScope.launchWhenCreated {
				viewModel.intent.send(MainIntent.IncreaseMaxMoves)
			}
		}

		binding.resetButton.setOnClickListener {
			lifecycleScope.launchWhenCreated {
				viewModel.intent.send(MainIntent.Reset)
			}
		}

		binding.nextPathButton.setOnClickListener {
			lifecycleScope.launchWhenCreated {
				viewModel.intent.send(MainIntent.ViewNextPath)
			}
		}

		observeViewModel()
	}

	private fun observeViewModel() {
		lifecycleScope.launchWhenCreated {
			viewModel.startPosition.collect {
				if (it.position != null) {
					binding.chessboard.clearBoard()
					binding.chessboard.placePiece(it.position, ChessPiece.KNIGHT)
				} else binding.chessboard.clearBoard()
			}
		}

		lifecycleScope.launchWhenCreated {
			viewModel.endPosition.collect {
				if (it.position != null) {
					binding.chessboard.markPosition(it.position, Color.GREEN)
				}
			}
		}

		lifecycleScope.launchWhenCreated {
			viewModel.gridSize.collect {
				binding.chessboardSizeSlider.value = it.size.toFloat()
				binding.chessboard.gridSize = it.size
				binding.chessboardSizeValue.text = it.size.toString()
			}
		}

		lifecycleScope.launchWhenCreated {
			viewModel.maxMoves.collect {
				binding.maxMovesValue.text = it.moves.toString()
			}
		}

		lifecycleScope.launchWhenCreated {
			viewModel.canDecreaseMaxMoves.collect {
				binding.maxMovesDecrease.isEnabled = it
			}
		}

		lifecycleScope.launchWhenCreated {
			viewModel.loadingPath.collect {
				binding.pathProgressBar.isVisible = it
			}
		}

		lifecycleScope.launchWhenCreated {
			viewModel.hasNextPath.collect {
				binding.nextPathButton.isEnabled = it
			}
		}

		lifecycleScope.launchWhenCreated {
			viewModel.currentPath.collect {
				val path = it ?: return@collect
				val startPosition = viewModel.startPosition.value.position ?: return@collect
				val endPosition = viewModel.endPosition.value.position ?: return@collect
				binding.chessboard.clearMarkedPositions()
				binding.chessboard.markPosition(endPosition, Color.GREEN)
				for (position in path) {
					if (position != startPosition && position != endPosition) {
						binding.chessboard.markPosition(position, Color.BLUE)
					}
				}
			}
		}
	}
}