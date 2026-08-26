package com.example.controller

import android.R
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.controller.ui.MyButton
import com.example.controller.ui.theme.ColourBackground
import com.example.controller.ui.theme.ColourButtons
import com.example.controller.ui.theme.ControllerTheme
import java.io.File
import androidx.compose.runtime.remember
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.io.path.listDirectoryEntries

class Workouts : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ControllerTheme {
                WorkoutsScreen(
                    onBack = { finish() },
                    onEdit = { 
                        startActivity(Intent(this, EditWorkout::class.java))
                    }
                )
            }
        }
    }
}


fun loadWorkouts (context: android.content.Context,workoutList: MutableList<String>)
{
    workoutList.clear()
    val workoutDir = File(context.filesDir, "workouts")
    if (!workoutDir.exists()) workoutDir.mkdirs()
    val files = workoutDir.listFiles()
    files?.forEach { file ->
        if (file.extension == "json") {
            workoutList.add(file.nameWithoutExtension)
        }
    }
}
@Composable
fun WorkoutsScreen(onBack: () -> Unit, onEdit: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var workoutList = remember {mutableStateListOf<String>()}
    LaunchedEffect(Unit) {


        println("The screen has loaded! I will only run once.")

        loadWorkouts(context,workoutList)
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ColourBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Text at the top
            Text(
                text = "Workouts",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(16.dp)
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
                    onClick = onEdit,
                    label = "Edit",
                    backgroundColor = ColourButtons,
                    textColor = Color.Black,
                    width = 120.dp,
                    roundCorners = 12.dp
                )
            }
        }
    }
}
