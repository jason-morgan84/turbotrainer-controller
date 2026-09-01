package com.example.controller

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
import com.example.controller.ui.theme.adjustColour
import com.example.controller.ui.WorkoutList

import com.example.controller.ui.segmentTypes
import com.example.controller.ui.slopedRoundedBackground
import kotlinx.coroutines.delay

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
fun WorkoutsScreen(workoutName: String,
                   onBack: () -> Unit,
                   onFinish: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val workoutList = remember { WorkoutList(mutableStateListOf()) }
    // val currentSegment by remember { mutableIntStateOf(0) }

    var runningWorkout by remember {mutableStateOf(false)}

    LaunchedEffect(workoutName) {
        workoutList.loadWorkoutList(context)

    }
    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    LaunchedEffect(runningWorkout) {
        if (runningWorkout) {
            val start = System.currentTimeMillis()
            while (true) {
                // Update the state every 10ms for smooth math, or 1000ms for simple counters
                elapsedSeconds = (System.currentTimeMillis() - start) / 1000
                delay(500) // Ticks twice a second
            }
        } else {
            elapsedSeconds = 0
        }
    }

    val flattenedWorkout = remember(workoutName, workoutList.workouts.size) {
        val workout = workoutList.workouts.find { it.name == workoutName }
        workout?.flattenWorkout() ?: mutableListOf()
    }
    var startTime by remember {mutableLongStateOf(0)}
    val lengthofWorkout = remember(flattenedWorkout) {
        flattenedWorkout.sumOf { it.time }
    }

    var currentSegment by remember { mutableIntStateOf(0)}
    if (elapsedSeconds > flattenedWorkout.take(currentSegment + 1).sumOf {it.time})
    {
        currentSegment ++//TODO code on current segment change
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ColourBackground,
        bottomBar = {            // Buttons at the bottom
            Row(
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            )   {
                MyButton(
                    onClick = onBack,
                    label = "Back",
                    backgroundColor = ColourButtons,
                    textColor = Color.Black,
                    width = 120.dp,
                    roundCorners = 12.dp
                )
                //TODO add are you sure you want to end workout buttons
                MyButton(
                    onClick = {if (runningWorkout)
                    {runningWorkout = false }
                    else {
                        runningWorkout = true
                        startTime = System.currentTimeMillis()
                    }},
                    label = if (runningWorkout) "End" else "Start",
                    backgroundColor = ColourButtons,
                    textColor = Color.Black,
                    width = 120.dp,
                    roundCorners = 12.dp
                )
            }}


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
                            value = "%02d:%02d".format(elapsedSeconds / 60, elapsedSeconds % 60),

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
                            value = actualCadence.toString().plus(" rpm"),
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


            // Next three cards
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
            {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp))
                {

                    if (flattenedWorkout.isNotEmpty()) {
                        val displayEnd = minOf(currentSegment + 3, flattenedWorkout.size)
                        for (i in currentSegment until displayEnd)
                        {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = (40 + (i - currentSegment) * 10).dp)
                                    .height((70 - (i - currentSegment) * 5).dp),//changed from 50
                                //TODO get proper text
                                colors = CardDefaults.cardColors(
                                    containerColor = adjustColour( segmentTypes[flattenedWorkout[i].type]!!.colour, lightness = ((i - currentSegment) * 0.1f))),
                                shape = RoundedCornerShape(8.dp),
                                //TODO leave final card up on time 0 to show resistance if continuing
                            )
                            {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                        text = flattenedWorkout[i].type,
                                        fontSize = (20 - (i - currentSegment) * 2).sp,
                                        color = adjustColour(
                                            Color.Black,
                                            lightness = ((i - currentSegment) * 0.2f)
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }
            }
            //Central buttons
            Box(
                modifier = Modifier
                    //.weight(1f)
                    .fillMaxWidth()
                    .fillMaxHeight(.2f)
                    .padding(horizontal = 20.dp)
            )

            //Segment Graph
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            )
            {
                Row(modifier = Modifier
                    .fillMaxSize(),
                    verticalAlignment = Alignment.Bottom)
                {
                    val rowHeightModifier = 350/100 //TODO make sure the height of the graph below is limited to the available space

                    for (item in flattenedWorkout)
                    {
                        val leftSideHeight = item.start.toFloat() * 3
                        val rightSideHeight = item.end.toFloat() * 3
                        val cardMaxHeight = maxOf(leftSideHeight, rightSideHeight)


                        Box(
                            modifier = Modifier
                                .height(cardMaxHeight.dp)
                                .weight(item.time.toFloat())
                                .slopedRoundedBackground(
                                    leftHeight = leftSideHeight.dp,
                                    rightHeight = rightSideHeight.dp,
                                    cornerRadius = 2.dp,
                                    backgroundColor = adjustColour(segmentTypes[item.type]!!.colour)
                                )
                        )
                    }
                }
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val progress = if (lengthofWorkout > 0) elapsedSeconds.toFloat() / lengthofWorkout else 0f
                    val x = size.width * progress
                    drawLine(
                        color = adjustColour(Color.Red, lightness = -0.1f),
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 4.dp.toPx())}


        }
    }
}}
