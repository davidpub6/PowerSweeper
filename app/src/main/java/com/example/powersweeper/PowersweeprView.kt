package com.example.powersweeper

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.*
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

import com.example.powersweeper.cellClasses.Cell
import com.example.powersweeper.cellClasses.SpecialType
import androidx.core.graphics.withTranslation

/**
 * Custom View that draws the Minesweeper board and handles user interaction
 * to zoom, pan, and reveal tiles.
 */
class MinesweeperView @JvmOverloads constructor(
    context: Context,
    val boardSize: Int = 8,
    val mineCount: Int = 10,
    val specialCount: Int = mineCount/2,
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
    private val paintFlag = Paint().apply {
        color = Color.RED
        textAlign = Paint.Align.CENTER
        textSize = 40f
    }
    private val paintText = Paint().apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        textSize = 40f
    }
    private val paintRevOne = Paint().apply {
        color = Color.YELLOW
        style = Paint.Style.STROKE
        strokeWidth = 15f
    }
    private val paintRevArea = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.STROKE
        strokeWidth = 15f
    }
    private val paintCoverOne = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 15f
    }
    private val paintFlagBomb = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 15f
    }

    var isFlagMode = false

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
        placeSpecial()
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
     * Special functions
     */
    private fun onSpecial(row: Int, col: Int){
        invalidate()
        val cell = board[row][col]
        if (cell.isSpecial){
            when (cell.special){
                SpecialType.REVEAL_ONE -> revealOne()
                SpecialType.REVEAL_AREA -> revealArea(row, col)
                SpecialType.COVER_ONE -> coverCells()
                SpecialType.FLAG_BOMB -> flagRandomBombs()
                else -> return
            }
        }
    }

    /**
     * Reveal adjacent tiles
     */
    private fun revealArea (row: Int, col: Int){
        invalidate()

        // Reveal neighbors
        for (r in max(0, row - 1)..min(boardSize - 1, row + 1)) {
            for (c in max(0, col - 1)..min(boardSize - 1, col + 1)) {
                if (!(r == row && c == col)) { // Don't reveal the center cell itself again
                    val neighborCell = board[r][c]
                    if (neighborCell.isMine) {
                        neighborCell.isFlagged = true // Flag if it's a bomb
                    } else {
                        revealCell(r, c) // Reveal non-bomb neighbors
                    }
                }
            }
        }
    }

    /**
     * Reveal one random tile
     */
    private fun revealOne() {
        val potentialCells = mutableListOf<Pair<Int, Int>>()

        // Find all cells that are not mines and not already revealed
        for (r in 0 until boardSize) {
            for (c in 0 until boardSize) {
                val cell = board[r][c]
                if (!cell.isMine && !cell.isRevealed) {
                    potentialCells.add(Pair(r, c))
                }
            }
        }

        if (potentialCells.isNotEmpty()) {
            // Pick a random cell from the list of potential cells
            val (randomRow, randomCol) = potentialCells.random(Random) // Use Kotlin's Random

            // If the chosen cell is flagged, unflag it first
            if (board[randomRow][randomCol].isFlagged) {
                board[randomRow][randomCol].isFlagged = false
            }

            // Now reveal the cell
            revealCell(randomRow, randomCol) // Assuming you have a revealCell function
            invalidate() // Redraw the board to show the change
        }
    }

    /**
     * Cover random tiles (that are not mines nor special)
     */
    fun coverCells(count: Int = 5) {
        if (count <= 0) return // Nothing to do

        val potentialCellsToCover = mutableListOf<Pair<Int, Int>>()

        // 1. Find all eligible cells: revealed, not a mine, not special
        for (r in 0 until boardSize) {
            for (c in 0 until boardSize) {
                val cell = board[r][c]
                if (cell.isRevealed && !cell.isMine && cell.special == SpecialType.NONE) {
                    // We only want to cover cells that don't have an inherent special property
                    // and are not mines.
                    potentialCellsToCover.add(Pair(r, c))
                }
            }
        }
        // 2. Shuffle and select cells to cover
        potentialCellsToCover.shuffle(Random) // Shuffle for randomness

        var cellsCovered = 0
        for (i in 0 until min(count, potentialCellsToCover.size)) {
            val (row, col) = potentialCellsToCover[i]
            val cell = board[row][col]

            // 3. "Cover" the cell
            cell.isRevealed = false
            revealedCellsCount--
            unrevealedCellsCount++
            cellsCovered++
        }
        if (cellsCovered > 0) {
            invalidate()
        }
    }

    /**
     * Flag random bombs (that are not flagged) (the amount can be changed)
     */
    fun flagRandomBombs(count: Int = 1): Int {
        if (count <= 0) return 0
        val potentialBombsToFlag = mutableListOf<Pair<Int, Int>>()

        // 1. Find all eligible bombs: isMine, not revealed, and not already flagged
        for (r in 0 until boardSize) {
            for (c in 0 until boardSize) {
                val cell = board[r][c]
                if (cell.isMine && !cell.isRevealed && !cell.isFlagged) {
                    potentialBombsToFlag.add(Pair(r, c))
                }
            }
        }
        // 2. Shuffle and select bombs to flag
        potentialBombsToFlag.shuffle(Random)

        var bombsFlaggedCount = 0
        for (i in 0 until min(count, potentialBombsToFlag.size)) {
            val (row, col) = potentialBombsToFlag[i]
            val cell = board[row][col]

            // 3. Flag the bomb
            cell.isFlagged = true
            bombsFlaggedCount++
        }

        if (bombsFlaggedCount > 0) {
            invalidate()
        }
        return bombsFlaggedCount
    }

    /**
     * Picks the type of Special
     */
    private fun chooseSpecial (): SpecialType {
        val random = (1..4).random()
        when (random){
            1 -> return SpecialType.REVEAL_ONE
            2 -> return SpecialType.REVEAL_AREA
            3 -> return SpecialType.COVER_ONE
            4 -> return SpecialType.FLAG_BOMB
            else -> return SpecialType.NONE
        }
    }

    /**
     * Randomly places special cells while avoiding mines.
     */
    private fun placeSpecial() {
        val nonMineCells = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until boardSize) {
            for (c in 0 until boardSize) {
                if (!board[r][c].isMine) {
                    nonMineCells.add(Pair(r, c))
                }
            }
        }

        nonMineCells.shuffle()

        var placedSpecial = 0
        for (i in 0 until min(specialCount, nonMineCells.size)) {
            val (row, col) = nonMineCells[i]
            board[row][col].special = chooseSpecial()
            board[row][col].isSpecial = true
            placedSpecial++
        }
        // You can add a check here if placedSpecial < specialCount
        // to see if you couldn't place all due to lack of non-mine cells.
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
        canvas.withTranslation(offsetX, offsetY) {
            scale(scaleFactor, scaleFactor)
            //Was canvas.save()

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
                        drawRect(left, top, right, bottom, paintRevealed)
                        if (cell.isMine) {
                            // Draw mine as a red circle
                            val cx = left + baseCellSize / 2
                            val cy = top + baseCellSize / 2
                            val radius = baseCellSize / 3
                            drawCircle(cx, cy, radius, paintMine)
                        } else if (cell.adjacentMines > 0) {
                            // Draw number of adjacent mines
                            val cx = left + baseCellSize / 2
                            val cy =
                                top + baseCellSize / 2 - (paintText.ascent() + paintText.descent()) / 2
                            drawText(cell.adjacentMines.toString(), cx, cy, paintText)
                        }
                        if (cell.isSpecial) {
                            val borderOffset = 15f / 2
                            val innerLeft = left + borderOffset
                            val innerTop = top + borderOffset
                            val innerRight = right - borderOffset
                            val innerBottom = bottom - borderOffset

                            when (cell.special) {
                                SpecialType.REVEAL_ONE -> drawRect(
                                    innerLeft,
                                    innerTop,
                                    innerRight,
                                    innerBottom,
                                    paintRevOne
                                )

                                SpecialType.REVEAL_AREA -> drawRect(
                                    innerLeft,
                                    innerTop,
                                    innerRight,
                                    innerBottom,
                                    paintRevArea
                                )

                                SpecialType.COVER_ONE -> drawRect(
                                    innerLeft,
                                    innerTop,
                                    innerRight,
                                    innerBottom,
                                    paintCoverOne
                                )

                                SpecialType.FLAG_BOMB -> drawRect(
                                    innerLeft,
                                    innerTop,
                                    innerRight,
                                    innerBottom,
                                    paintFlagBomb
                                )

                                SpecialType.NONE -> break
                            }
                        }

                    } else if (cell.isFlagged) {
                        val cx = left + baseCellSize / 2
                        val cy =
                            top + baseCellSize / 2 - (paintText.ascent() + paintText.descent()) / 2
                        drawRect(left, top, right, bottom, paintHidden)
                        drawText("F", cx, cy, paintFlag)
                    } else {
                        // Hidden cell
                        drawRect(left, top, right, bottom, paintHidden)
                        /*
                    //For test purpose
                    if (cell.isMine) {
                        // Draw mine as a red circle
                        val cx = left + baseCellSize / 2
                        val cy = top + baseCellSize / 2
                        val radius = baseCellSize / 3
                        canvas.drawCircle(cx, cy, radius, paintMine)
                    }
                    */
                    }

                    // Draw cell border
                    drawRect(left, top, right, bottom, paintGrid)
                }
            }
        }
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
                        if (isFlagMode){
                            flagCell(row, col)
                        } else {
                            revealCell(row, col)
                        }
                    }
                }
            }
        }

        return true
    }

    private fun flagCell(row: Int, col: Int) {
        val cell = board[row][col]
        if (cell.isRevealed) return // Already revealed

        invalidate()

        if (!cell.isFlagged){
            cell.isFlagged = true
        } else {
            cell.isFlagged = false
        }

    }

    /**
     * Reveal the cell at the given row and column.
     * If no adjacent mines, recursively reveals neighbors.
     */
    private var revealedCellsCount = 0
    private var unrevealedCellsCount = boardSize*boardSize

    private fun revealCell(row: Int, col: Int) {
        val cell = board[row][col]
        if (cell.isRevealed) return // Already revealed
        if (cell.isFlagged) return // Flagged cell

        cell.isRevealed = true
        revealedCellsCount++
        unrevealedCellsCount--
        invalidate()

        if (cell.isMine) {

            // Show game over dialog from the activity

            (context as? GameActivity)?.showGameOverDialog(revealedCellsCount)

        } else if(cell.isSpecial){
            onSpecial(row, col)
        } else if (unrevealedCellsCount == mineCount) {
            //Game won
            (context as? GameActivity)?.showGameWonDialog()

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