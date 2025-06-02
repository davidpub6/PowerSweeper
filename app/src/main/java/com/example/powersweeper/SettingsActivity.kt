package com.example.powersweeper

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ToggleButton
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.content.edit


class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.settings)

        val button_back = findViewById<Button>(R.id.close_button)

        button_back.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Optional: Call finish() if you want to remove the current activity from the back stack
        }

        val themeToggleButton = findViewById<ToggleButton>(R.id.theme_toggle_button)
        val sharedPreferences = getSharedPreferences("theme_preferences", MODE_PRIVATE)
        val isDarkTheme = sharedPreferences.getBoolean("is_dark_theme", true)
        themeToggleButton.isChecked = isDarkTheme
        themeToggleButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                setTheme(R.style.DarkTheme)
                sharedPreferences.edit() { putBoolean("is_dark_theme", true) }
            } else {
                setTheme(R.style.LightTheme)
                sharedPreferences.edit() { putBoolean("is_dark_theme", false) }
            }
        }
    }
}