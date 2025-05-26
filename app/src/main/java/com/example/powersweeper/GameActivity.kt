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
import android.widget.FrameLayout
import android.widget.ToggleButton
import android.widget.Button
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

        // Create a container to hold gameView and toggle button
        val container = FrameLayout(this)

        // Create MinesweeperView with parameters
        gameView = MinesweeperView(this, boardSize, mineCount)

        // Create home button
        val homeButton = Button(this).apply {
            text = "Home"
            setBackgroundColor(Color.LTGRAY)
            setTextColor(Color.BLACK)
            textSize = 18f
            // Positioning: put at top left corner with padding
            setPadding(20, 20, 20, 20)
        }

        // Layout params for home button (position top-left)
        val homeParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            topMargin = 20
            marginStart = 20
        }


        // Create toggle button for flag/reveal mode
        val toggleButton = ToggleButton(this).apply {
            textOn = "Flag Mode"
            textOff = "Reveal Mode"
            isChecked = false // Default to reveal mode
            setBackgroundColor(Color.LTGRAY)
            setTextColor(Color.BLACK)
            textSize = 18f
            // Positioning: put at top right corner with padding
            setPadding(20, 20, 20, 20)
        }

        // Layout params for toggle button (position bottom-middle)
        val toggleParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            bottomMargin = 20
        }

        // Add views to container
        container.addView(gameView)
        container.addView(homeButton, homeParams)
        container.addView(toggleButton, toggleParams)
        setContentView(container)

        // Set home button listener to return to menu
        homeButton.setOnClickListener {
            finish()
        }

        // Set toggle listener to update flag mode in gameView
        toggleButton.setOnCheckedChangeListener { _, isChecked ->
            gameView.isFlagMode = isChecked
        }
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

    fun showGameWonDialog() {
        val builder = AlertDialog.Builder(this)
            .setTitle("Success")
            .setMessage("You revealed all of the cells!\n \nBoard size: ${gameView.boardSize}x${gameView.boardSize}")
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                // Return to menu by finishing this activity
                finish()
            }
        builder.show()
    }
}
