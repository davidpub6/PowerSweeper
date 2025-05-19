package com.example.powersweeper

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.AttributeSet
import android.view.*
import androidx.activity.ComponentActivity
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * This Activity hosts the Minesweeper game.
 * The game board size is determined by difficulty level passed as an intent extra.
 */

class GameActivity : ComponentActivity() {

    companion object {
        // Difficulty levels and their board settings
        const val DIFFICULTY_EASY = "easy"
        const val DIFFICULTY_MEDIUM = "medium"
        const val DIFFICULTY_HARD = "hard"

        const val EXTRA_DIFFICULTY = "difficulty"
    }

    private lateinit var gameView: MinesweeperView
    private var difficulty = DIFFICULTY_EASY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get difficulty from intent extras, default to easy
        val difficulty = intent.getStringExtra(EXTRA_DIFFICULTY) ?: DIFFICULTY_EASY

        // Define board size and mine count based on difficulty
        val boardSize: Int
        val mineCount: Int

        when (difficulty) {
            DIFFICULTY_MEDIUM -> {
                boardSize = 16
                mineCount = 40
            }
            DIFFICULTY_HARD -> {
                boardSize = 24
                mineCount = 99
            }
            else -> {
                boardSize = 8
                mineCount = 10
            }
        }

        // Create MinesweeperView with parameters
        val gameView = MinesweeperView(this, boardSize, mineCount)
        setContentView(gameView)
    }

    /**

     * Call to show game over dialog with stats, then return to menu on OK.

     */

    fun showGameOverDialog(revealedCells: Int) {

        val totalCells = gameView.boardSize * gameView.boardSize

        val builder = AlertDialog.Builder(this)

            .setTitle("Game Over")

            .setMessage("You revealed a mine!\n\nCells revealed: $revealedCells\nBoard size: ${gameView.boardSize}x${gameView.boardSize}")

            .setCancelable(false)

            .setPositiveButton("OK") { _, _ ->

                // Return to menu by finishing this activity

                finish()

            }

        builder.show()

    }
}
