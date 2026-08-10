package com.example.controller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.controller.ui.Label
import com.example.controller.ui.MyButton
import com.example.controller.ui.theme.ControllerTheme
import com.example.controller.ui.theme.ColourBackground
import com.example.controller.ui.theme.ColourButtons
import com.example.controller.ui.theme.ColourMiddle

class TrainingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ControllerTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = ColourBackground
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Label(
                                value = "Training Window",
                                fontSize = 32.sp
                            )
                            
                            MyButton(
                                onClick = { finish() },
                                label = "Back to Main",
                                backgroundColor = ColourButtons,
                                width = 200.dp
                            )
                        }
                    }
                }
            }
        }
    }
}
