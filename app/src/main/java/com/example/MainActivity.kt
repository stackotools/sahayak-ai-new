package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.SahayakApp
import com.example.ui.theme.SahayakTheme
import com.example.ui.viewmodel.SahayakViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: SahayakViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SahayakTheme {
                SahayakApp(viewModel = viewModel)
            }
        }
    }
}
