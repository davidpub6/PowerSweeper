package com.example.powersweeper

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.AttributeSet
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * Custom View that draws the Minesweeper board and handles user interaction
 * to zoom, pan, and reveal tiles.
 */

class MinesweeperView @JvmOverloads constructor(
    context: Context,
    val boardSize: Int = 8,
    val mineCount: Int = 10,
    attrs: AttributeSet? = null
) : View(context, attrs), ScaleGestureDetector.OnScaleGestureListener {

    // Paint objects for drawing
    private val paintGrid = Paint().apply {
        color = Color.GRAY
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    private val paintHidden = Paint().apply {
        color = Color.LTGRAY
        style = Paint.Style.FILL
    }

    private val paintRevealed = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val paintMine = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
    }

    private val paintText = Paint().apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        textSize = 40f
    }


    // Board data representation
    data class Cell(
        var isMine: Boolean = false,
        var isRevealed: Boolean = false,
        var adjacentMines: Int = 0
    )


    private val board: Array<Array<Cell>> = Array(boardSize) { Array(boardSize) { Cell() } }

    // Zoom and pan variables
    private var scaleFactor = 1f
    private val scaleGestureDetector = ScaleGestureDetector(context, this)

    private var offsetX = 0f
    private var offsetY = 0f
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var isDragging = false

    // Cell drawing size at scale factor 1
    private val baseCellSize = 100f

    init {
        // Place mines and calculate adjacent mine counts when view is created
        placeMines()
        calculateAdjacentMineCounts()
    }

    /**
     * Randomly places mines on the board.
     */

    private fun placeMines() {
        var placedMines = 0
        val totalCells = boardSize * boardSize
        val randomPositions = (0 until totalCells).shuffled()

        for (pos in randomPositions) {
            val row = pos / boardSize
            val col = pos % boardSize
            board[row][col].isMine = true
            placedMines++
            if (placedMines >= mineCount) break
        }
    }

    /**
     * Calculates the number of adjacent mines for each cell.
     */

    private fun calculateAdjacentMineCounts() {
        for (row in 0 until boardSize) {
            for (col in 0 until boardSize) {
                if (!board[row][col].isMine) {
                    var count = 0
                    for (r in max(0, row - 1)..min(boardSize - 1, row + 1)) {
                        for (c in max(0, col - 1)..min(boardSize - 1, col + 1)) {
                            if (board[r][c].isMine) count++
                        }
                    }
                    board[row][col].adjacentMines = count
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Apply translation and scaling for zoom and pan
        canvas.save()
        canvas.translate(offsetX, offsetY)
        canvas.scale(scaleFactor, scaleFactor)

        // Draw cells
        for (row in 0 until boardSize) {
            for (col in 0 until boardSize) {
                val left = col * baseCellSize
                val top = row * baseCellSize
                val right = left + baseCellSize
                val bottom = top + baseCellSize

                val cell = board[row][col]

                // Draw cell background
                if (cell.isRevealed) {
                    canvas.drawRect(left, top, right, bottom, paintRevealed)
                    if (cell.isMine) {
                        // Draw mine as a red circle
                        val cx = left + baseCellSize / 2
                        val cy = top + baseCellSize / 2
                        val radius = baseCellSize / 3
                        canvas.drawCircle(cx, cy, radius, paintMine)
                    } else if (cell.adjacentMines > 0) {
                        // Draw number of adjacent mines
                        val cx = left + baseCellSize / 2
                        val cy = top + baseCellSize / 2 - (paintText.ascent() + paintText.descent()) / 2
                        canvas.drawText(cell.adjacentMines.toString(), cx, cy, paintText)
                    }

                } else {
                    // Hidden cell
                    canvas.drawRect(left, top, right, bottom, paintHidden)
                }

                // Draw cell border
                canvas.drawRect(left, top, right, bottom, paintGrid)
            }
        }
        canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Let ScaleGestureDetector handle the event first (for pinch zoom)
        scaleGestureDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
                isDragging = false
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - lastTouchX
                val dy = event.y - lastTouchY

                if (!scaleGestureDetector.isInProgress) {
                    // If moved enough, start dragging
                    if (!isDragging) {
                        if (dx * dx + dy * dy > 25) { // threshold
                            isDragging = true
                        }
                    }

                    if (isDragging) {
                        offsetX += dx
                        offsetY += dy
                        invalidate()
                    }
                }

                lastTouchX = event.x
                lastTouchY = event.y
            }
            MotionEvent.ACTION_UP -> {
                if (!isDragging && !scaleGestureDetector.isInProgress) {
                    // Interpret as tap - convert touch point to board cell
                    val boardX = ((event.x - offsetX) / scaleFactor) / baseCellSize
                    val boardY = ((event.y - offsetY) / scaleFactor) / baseCellSize
                    val col = floor(boardX).toInt()
                    val row = floor(boardY).toInt()

                    if (row in 0 until boardSize && col in 0 until boardSize) {
                        revealCell(row, col)
                    }
                }
            }
        }

        return true
    }


    /**
     * Reveal the cell at the given row and column.
     * If no adjacent mines, recursively reveals neighbors.
     */

    private fun revealCell(row: Int, col: Int) {
        val cell = board[row][col]
        if (cell.isRevealed) return // Already revealed

        cell.isRevealed = true
        invalidate()

        if (cell.isMine) {

            // Show game over dialog from the activity

            (context as? GameActivity)?.showGameOverDialog(revealedCellsCount)

        } else if (cell.adjacentMines == 0) {
            // Reveal neighbors recursively
            for (r in max(0, row - 1)..min(boardSize - 1, row + 1)) {
                for (c in max(0, col - 1)..min(boardSize - 1, col + 1)) {
                    if (!(r == row && c == col)) {
                        revealCell(r, c)
                    }
                }
            }
        }
    }

    // ScaleGestureDetector.OnScaleGestureListener methods

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        // Update scale factor, limit between 0.5x and 3x
        scaleFactor *= detector.scaleFactor
        scaleFactor = max(0.5f, min(scaleFactor, 3.0f))
        invalidate()
        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        // Beginning of a scale gesture
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
        // Scale gesture ended
    }
}