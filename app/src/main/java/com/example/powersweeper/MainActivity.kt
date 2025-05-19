package com.example.powersweeper

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge


//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.tooling.preview.Preview
//import com.example.powersweeper.ui.theme.PowerSweeperTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.menu)

        val buttonSettings = findViewById<Button>(R.id.settingsButton)

        buttonSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

//        button_settings.setOnClickListener {
//            val intent = Intent(
//                this@MainActivity,
//                SettingsActivity::class.java
//            )
//            startActivity(intent)
//        }

        val difficultySpinner = findViewById<Spinner>(R.id.difficultySelect)
        // Create an ArrayAdapter using the string array and a default spinner layout
        val adapter = ArrayAdapter.createFromResource(
            this, R.array.difficulties, R.layout.difficultyspinnerlayout
        )
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(R.layout.difficultyitemlayout)
        // Apply the adapter to the spinner
        difficultySpinner.adapter = adapter
        // Set a listener to handle item selection
        difficultySpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                // Get the selected item
                val selectedItem = parent.getItemAtPosition(position).toString()
                // Show a toast message or handle the selection
                Toast.makeText(parent.context, "Selected: $selectedItem", Toast.LENGTH_SHORT).show()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        val playButton = findViewById<Button>(R.id.playButton)  // Your play button ID

        playButton.setOnClickListener {
            // Get selected difficulty from spinner as string, e.g. "easy", "medium", "hard"
            val difficulty = when (difficultySpinner.selectedItem.toString().lowercase()) {
                "medium" -> GameActivity.DIFFICULTY_MEDIUM
                "hard" -> GameActivity.DIFFICULTY_HARD
                else -> GameActivity.DIFFICULTY_EASY
            }

            // Create intent and pass difficulty extra
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra(GameActivity.EXTRA_DIFFICULTY, difficulty)
            startActivity(intent)
        }

    }
}

//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hello $name!",
//        modifier = modifier
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    PowerSweeperTheme {
//        Greeting("Android")
//    }
//}