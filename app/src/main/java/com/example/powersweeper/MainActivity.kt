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

        val button_settings = findViewById<Button>(R.id.settingsButton)

        button_settings.setOnClickListener {
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

        val mySpinner = findViewById<Spinner>(R.id.difficultySelect)
        // Create an ArrayAdapter using the string array and a default spinner layout
        val adapter = ArrayAdapter.createFromResource(
            this, R.array.difficulties, R.layout.difficultyspinnerlayout
        )
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(R.layout.difficultyitemlayout)
        // Apply the adapter to the spinner
        mySpinner.adapter = adapter
        // Set a listener to handle item selection
        mySpinner.onItemSelectedListener = object : OnItemSelectedListener {
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