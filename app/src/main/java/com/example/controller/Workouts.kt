package com.example.controller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.controller.ui.Label
import com.example.controller.ui.MyButton
import com.example.controller.ui.theme.ColourBackground
import com.example.controller.ui.theme.ColourButtons
import com.example.controller.ui.theme.ColourPlus10
import com.example.controller.ui.theme.ControllerTheme
import kotlin.math.round

class Workouts : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ControllerTheme {
                val workoutName = intent.getStringExtra("WORKOUT_NAME") ?: "Unknown Workout"
                WorkoutsScreen(
                    workoutName = workoutName,
                    onBack = { finish() },
                    onFinish = { finish() }
                )
            }
        }
    }
}

@Composable
fun WorkoutsScreen(workoutName: String, onBack: () -> Unit, onFinish: () -> Unit) {

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ColourBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    //.align(Alignment.TopCenter)
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                        .border(
                            width = 8.dp,
                            color = ColourPlus10,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .background(
                            color = ColourBackground,
                            shape = RoundedCornerShape(24.dp)
                        ),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                )
                {
                    Row(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth(0.6f),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Label(
                            value = actualPower.toString().plus(" W"),
                            fontSize = 32.sp
                        )
                        Label(
                            value = actualCadence.toString().plus(" rpm"),
                            fontSize = 32.sp
                        )

                    }
                    Row(
                        modifier = Modifier
                            .padding(bottom = 12.dp, top = 4.dp)
                            .fillMaxWidth(0.6f),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Label(
                            value = averagePower.toString().plus(" W"),
                            fontSize = 18.sp
                        )
                        Label(
                            value = (round(actualDistance / 10) / 100).toString()
                                .plus(" km"),
                            fontSize = 18.sp
                        )
                        Label(
                            value = actualEnergy.toString().plus(" kcal"),
                            fontSize = 18.sp
                        )

                    }
                }
            }
            Text(
                text = workoutName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            )

            // Blank space in the middle
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            // Buttons at the bottom
            Row(
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MyButton(
                    onClick = onBack,
                    label = "Back",
                    backgroundColor = ColourButtons,
                    textColor = Color.Black,
                    width = 120.dp,
                    roundCorners = 12.dp
                )

                MyButton(
                    onClick = onFinish,
                    label = "Finish",
                    backgroundColor = ColourButtons,
                    textColor = Color.Black,
                    width = 120.dp,
                    roundCorners = 12.dp
                )
            }
        }
    }
}
