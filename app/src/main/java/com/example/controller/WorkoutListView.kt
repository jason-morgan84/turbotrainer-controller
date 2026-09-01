package com.example.controller
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.runtime.DisposableEffect
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.remember
import androidx.compose.ui.text.style.TextAlign
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.RectangleShape
import com.example.controller.ui.AlertDefinitions
import com.example.controller.ui.WorkoutList
import com.example.controller.ui.Workout
import com.example.controller.ui.theme.ColourPlus10
import com.example.controller.ui.theme.adjustColour


class WorkoutListView : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ControllerTheme {
                WorkoutListScreen(
                    onBack = { finish() },
                    onEdit = { workoutName ->
                        val intent = Intent(this, WorkoutEdit::class.java)
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


@Composable
fun WorkoutListScreen(onBack: () -> Unit, onEdit: (String) -> Unit, onStart: (String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val workouts by remember {
        mutableStateOf(WorkoutList(mutableStateListOf<Workout>()))
    }
    var selectedWorkout by remember { mutableStateOf("") }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current


// This effect listens for when the user RETURNS to this screen
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                workouts.loadWorkoutList(context = context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ColourBackground,
        bottomBar ={Row(
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
        }}
    ) { innerPadding ->
        var activeAlert by remember { mutableStateOf<AlertDefinitions?>(null) }

        activeAlert?.AlertPopup(onClose = { activeAlert = null })
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { selectedWorkout = "" }
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
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),

                //verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start)

            {
                for (item in workouts.workouts)
                {
                    TextButton(modifier = Modifier
                        .fillMaxWidth(),
                        shape = RectangleShape,
                        onClick = { selectedWorkout = if (selectedWorkout == item.name) "" else item.name })
                    {
                        Text(modifier = Modifier
                            .fillMaxWidth()
                            .padding (horizontal = 40.dp),
                            fontSize = 16.sp,
                            textAlign = TextAlign.Start,
                            color = if (selectedWorkout == item.name) adjustColour(ColourPlus10,lightness = -0.1f,saturation = -0.2f) else adjustColour(Color.DarkGray, lightness = -0.1f),
                            text = item.name)

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
                                                workouts.deleteWorkout(selectedWorkout)
                                                workouts.saveWorkoutList(context)
                                                //workouts.loadWorkoutList(context = context)
                                                //loadWorkouts(context, workoutList)
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
                            onClick = {onEdit("")},
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

        }
    }
}
