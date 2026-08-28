package com.example.controller
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.runtime.DisposableEffect
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
import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import kotlinx.serialization.json.Json
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.RectangleShape
import com.example.controller.ui.AlertDefinitions
import com.example.controller.ui.theme.ColourPlus10
class WorkoutsList : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ControllerTheme {
                WorkoutsListScreen(
                    onBack = { finish() },
                    onEdit = { workoutName ->
                        val intent = Intent(this, EditWorkout::class.java)
                        intent.putExtra("WORKOUT_NAME", workoutName)
                        startActivity(intent)
                    },
                    onStart = { workoutName ->
                        val intent = Intent(this, Workouts::class.java)
                        intent.putExtra("WORKOUT_NAME", workoutName)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

fun deleteWorkout(name: String, context: android.content.Context)
{
    val file = File(context.filesDir, "all_workouts.json")
    if (file.exists()) {
        try {
            val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
            val workouts = json.decodeFromString<List<Workout>>(file.readText()).toMutableList()
            
            if (workouts.removeIf { it.name == name }) {
                file.writeText(json.encodeToString(workouts))
                Log.i("Delete", "Workout '$name' deleted.")
            }
        } catch (e: Exception) {
            Log.e("Delete", "Error deleting workout", e)
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
fun WorkoutsListScreen(onBack: () -> Unit, onEdit: (String) -> Unit, onStart: (String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val workoutList = remember {mutableStateListOf<String>()}
    var selectedWorkout by remember { mutableStateOf("") }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

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
        var activeAlert by remember { mutableStateOf<AlertDefinitions?>(null) }

        activeAlert?.AlertPopup(onClose = { activeAlert = null })
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text = "Workouts",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(16.dp)
                                )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),

            )
            Column(modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .verticalScroll(rememberScrollState()),

                //verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start)

            {
                for (item in workoutList)
                {
                    TextButton(modifier = Modifier
                        .fillMaxWidth(),
                        shape = RectangleShape,
                        onClick = { selectedWorkout = if (selectedWorkout == item) "" else item })
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
                Box(modifier = Modifier
                    .width(80.dp)
                    .height(120.dp)
                    .align(Alignment.End)
                    .padding(horizontal = 8.dp)
                )
                {
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight())
                    {
                        if (selectedWorkout!="")
                        {
                            Button(modifier = Modifier
                                .width(50.dp)
                                .padding(vertical = 4.dp)
                                .height(50.dp),
                                contentPadding = PaddingValues(0.dp),
                                shape = CircleShape,
                                onClick = { onEdit(selectedWorkout) },
                                colors = ButtonDefaults.buttonColors(containerColor = ColourButtons, contentColor = Color.Black)
                            )
                            {
                                Icon(
                                    modifier = Modifier.size(32.dp),
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Workout"
                                )
                            }
                            Button(modifier = Modifier
                                .width(50.dp)
                                .padding(vertical = 4.dp)
                                .height(50.dp),
                                contentPadding = PaddingValues(0.dp),
                                shape = CircleShape,
                                onClick = {activeAlert = AlertDefinitions(
                                            title = "Confirm delete",
                                            text = "Are you sure you want to delete workout $selectedWorkout?",
                                            confirmText = "Yes",
                                            dismissText = "No",
                                            onConfirm = {
                                                deleteWorkout(selectedWorkout, context)
                                                loadWorkouts(context, workoutList)
                                                selectedWorkout = ""
                                            },
                                            onDismiss = {

                                            }
                                        )


                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ColourButtons, contentColor = Color.Black)
                            )
                            {
                                Icon(
                                    modifier = Modifier.size(32.dp),
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Workout"
                                )
                            }
                        }

                        Button(modifier = Modifier
                            .width(50.dp)
                            .padding(vertical = 4.dp)
                            .height(50.dp),
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            onClick = {onEdit("New Workout")},
                            colors = ButtonDefaults.buttonColors(containerColor = ColourButtons, contentColor = Color.Black)
                        )
                        {
                            Icon(
                                modifier = Modifier.size(32.dp),
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Workout"
                            )
                        }

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
                    onClick = { if (selectedWorkout != "") onStart(selectedWorkout) },
                    label = "Start",
                    backgroundColor = ColourButtons,
                    textColor = Color.Black,
                    width = 120.dp,
                    roundCorners = 12.dp
                )
            }
        }
    }
}
