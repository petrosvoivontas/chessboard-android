package com.example.chessboard.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.example.chessboard.utils.Position
import java.util.*
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.min

fun interface OnChessPositionClickedListener {
	fun onChessPositionClicked(chessView: ChessView, position: Position)
}

class ChessView(context: Context, attrs: AttributeSet) : View(context, attrs) {

	private val whiteCellPaint = Paint().apply { color = Color.LTGRAY }
	private val blackCellPaint = Paint().apply { color = Color.GRAY }
	private val positionMarkerPaint = Paint()
	private var startX = -1f
	private var startY = -1f
	private var onChessPositionClickedListener: OnChessPositionClickedListener? = null
	private val placedPieces: MutableMap<Position, ChessPiece> = hashMapOf()
	private val markedPositions: MutableMap<Position, Int> = hashMapOf()
	private val bitmaps: MutableMap<ChessPiece, Bitmap> = EnumMap(ChessPiece::class.java)
	private val viewConfiguration = ViewConfiguration.get(context)

	var gridSize = 8
		set(value) {
			field = value
			invalidate()
		}

	init {
		loadBitmaps()
	}

	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec)
		val width = MeasureSpec.getSize(widthMeasureSpec)
		val height = MeasureSpec.getSize(heightMeasureSpec)
		val size = min(width, height)
		setMeasuredDimension(size, size)

		ViewConfiguration.get(context).scaledTouchSlop
	}

	override fun onDraw(canvas: Canvas) {
		drawChessboard(canvas)
	}

	@SuppressLint("ClickableViewAccessibility")
	override fun onTouchEvent(event: MotionEvent): Boolean {
		when (event.action) {
			MotionEvent.ACTION_DOWN -> {
				startX = event.x
				startY = event.y
			}
			MotionEvent.ACTION_UP -> {
				if (abs(startX - event.x) > viewConfiguration.scaledTouchSlop || abs(startY - event.y) > viewConfiguration.scaledTouchSlop) return true
				val cellSize = width.toDouble() / gridSize
				val row = floor(event.y / cellSize.coerceAtLeast(0.1)).toInt()
				val col = floor(event.x / cellSize.coerceAtLeast(0.1)).toInt()
				onChessPositionClickedListener?.onChessPositionClicked(this, Position(row, col))
			}
		}
		return true
	}

	fun setOnChessPositionClickedListener(listener: OnChessPositionClickedListener?) {
		onChessPositionClickedListener = listener
	}

	fun placePiece(position: Position, piece: ChessPiece) {
		placedPieces[position] = piece
		invalidate()
	}

	fun clearMarkedPositions() {
		markedPositions.clear()
		invalidate()
	}

	fun markPosition(position: Position, @ColorInt colorResId: Int) {
		markedPositions[position] = colorResId
		invalidate()
	}

	fun clearBoard() {
		placedPieces.clear()
		markedPositions.clear()
		invalidate()
	}

	private fun drawChessboard(canvas: Canvas) {
		val cellSize = width.toFloat() / gridSize
		for (row in 0 until gridSize)
			for (col in 0 until gridSize) {
				val position = Position(row, col)
				val positionColor = markedPositions[position]
				val paint = when {
					positionColor != null -> {
						positionMarkerPaint.also { it.color = positionColor }
					}
					(row + col) % 2 == 0 -> whiteCellPaint
					else -> blackCellPaint
				}
				drawCell(canvas, cellSize, row, col, paint)

				placedPieces[position]?.let {
					drawPiece(canvas, cellSize, row, col, it, paint)
				}
			}
	}

	private fun drawCell(canvas: Canvas, cellSize: Float, row: Int, col: Int, paint: Paint) {
		canvas.drawRect(
			col * cellSize,
			row * cellSize,
			(col + 1) * cellSize,
			(row + 1) * cellSize,
			paint
		)
	}

	private fun drawPiece(
		canvas: Canvas,
		cellSize: Float,
		row: Int,
		col: Int,
		piece: ChessPiece,
		paint: Paint
	) {
		bitmaps[piece]?.let {
			val newWidth: Float
			val newHeight: Float
			if (it.width >= it.height) {
				val ratio = it.height.toFloat() / it.width
				newWidth = it.width.toFloat().coerceAtMost(cellSize)
				newHeight = newWidth * ratio
			} else {
				val ratio = it.width.toFloat() / it.height
				newHeight = it.height.toFloat().coerceAtMost(cellSize)
				newWidth = newHeight * ratio
			}
			val paddingLeft = (cellSize - newWidth) / 2
			val paddingTop = (cellSize - newHeight) / 2
			canvas.drawBitmap(
				it.scale(floor(newWidth).toInt(), floor(newHeight).toInt()),
				col * newWidth + paddingLeft * (2 * col + 1),
				row * newHeight + paddingTop * (2 * row + 1),
				paint
			)
		}
	}

	private fun loadBitmaps() {
		enumValues<ChessPiece>().map { piece ->
			VectorDrawableCompat.create(resources, piece.resId, context.theme)?.let {
				bitmaps[piece] = it.toBitmap()
			}
		}
	}
}