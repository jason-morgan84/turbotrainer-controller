package com.example.controller
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.text.style.TextAlign
import kotlinx.serialization.Serializable
import android.util.Log
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.RectangleShape
import com.example.controller.ui.theme.ColourPlus10
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


fun loadWorkouts (context: android.content.Context, workoutList: MutableList<String>)
{
    workoutList.clear()
    val file = File(context.filesDir, "all_workouts.json")
    
    if (file.exists()) {
        try {
            val json = Json { ignoreUnknownKeys = true }
            val workouts = json.decodeFromString<List<Workout>>(file.readText())
            workoutList.addAll(workouts.map { it.name })
        } catch (e: Exception) {
            Log.e("Load", "Error loading workouts from single file", e)
        }
    }
}
@Composable
fun WorkoutsScreen(onBack: () -> Unit, onEdit: () -> Unit) {
    var firstLoad by remember {mutableStateOf(true)}
    val context = androidx.compose.ui.platform.LocalContext.current
    var workoutList = remember {mutableStateListOf<String>()}
    var selectedWorkout by remember { mutableStateOf("") }
    val lifecycleOwner = LocalLifecycleOwner.current

// This effect listens for when the user RETURNS to this screen
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                loadWorkouts(context, workoutList)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
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
                    .fillMaxWidth(),

            )
            Column(modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),

                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start)

            {
                for (item in workoutList)
                {
                    TextButton(modifier = Modifier
                        .fillMaxWidth(),
                        shape = RectangleShape,
                        onClick = {if (selectedWorkout == item) selectedWorkout = "None" else selectedWorkout = item})
                    {
                        Text(modifier = Modifier
                            .fillMaxWidth()
                            .padding (horizontal = 40.dp),
                            fontSize = 16.sp,
                            textAlign = TextAlign.Start,
                            color = if (selectedWorkout == item) adjustColour(ColourPlus10,lightness = -0.1f,saturation = -0.2f) else adjustColour(Color.DarkGray, lightness = -0.1f),
                            text = item)

                    }
                }
            }

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
                    onClick = { firstLoad = true;onEdit() },
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
