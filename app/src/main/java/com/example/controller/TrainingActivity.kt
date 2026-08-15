package com.example.controller

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.controller.ui.theme.ColourPlus10
import com.example.controller.ui.theme.ColourPlus5
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import com.example.controller.ui.theme.ColourButtons
import com.example.controller.ui.theme.ColourMinus10
import kotlin.collections.emptyList

class Segment(val name: String, val ramp: Boolean, val start: Int, val end: Int = start)
class TrainingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ControllerTheme {
                var showDialog by remember { mutableStateOf(false) }
                if (showDialog){
                    DialogNewSegment(
                        onDismissRequest = { showDialog = false },
                        onConfirmation = { showDialog = false },
                        imageDescription = "Add segment",
                    )
                }
                val trainingSegments = remember {mutableStateListOf<Segment>()}
                fun updateTrainingSegments()
                {
                    //trainingSegments.add("Segment " + (trainingSegments.size + 2).toString())
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = ColourBackground
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth()
                            .padding(innerPadding),


                    ) {
                        Column {


                            Row(
                                modifier = Modifier
                                    .fillMaxHeight(0.9f)
                                    .fillMaxWidth()
                                    .background(color = ColourMinus10)

                            ) {
                                Column(modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement
                                        .spacedBy(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally)
                                {
                                    for (item in trainingSegments)
                                        {
                                        TrainingSegment(
                                            name = "Rest",
                                            ramp = true,
                                            start = 0,
                                            end = 10,
                                            //backgroundColor = ColourPlus5
                                        )
                                    }

                                }
                            }
                            Row(
                                //verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    //.align(alignment = Alignment.BottomCenter)
                                    .padding(bottom = 32.dp)
                                    .background(color = ColourPlus10)
                                    .fillMaxHeight(1f)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically

                            )

                            {


                                MyButton(
                                    onClick = { finish() },
                                    label = "Back",
                                    backgroundColor = ColourButtons,
                                    textColor = Color.Black,
                                    width = 120.dp,
                                    roundCorners = 12.dp
                                )
                                MyButton(
                                    onClick = { showDialog = true },
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
            }
        }
    }
}

@Composable
fun TrainingSegment(
    //backgroundColor: Color? = null,
    name: String,
    ramp: Boolean,
    start: Int,
    end: Int = start)
{
    val coloursMap = mapOf(
        "Warm Up" to ColourMiddle,
        "Cool Down" to ColourMiddle,
        "Interval" to ColourPlus10,
        "Rest" to ColourMinus10)

    Box(
        modifier = Modifier.fillMaxWidth(fraction = 0.6f)
            .height(40.dp)
            .background(color = coloursMap[name]?: ColourMinus10)
        //shape = RoundedCornerShape(4.dp),



    )
    {
        Text(
            text = name,
        )
    }
}

@Composable
fun DialogNewSegment(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    imageDescription: String,
) {

    Dialog(onDismissRequest = { onDismissRequest() }) {
        // Draw a rectangle shape with rounded corners inside the dialog
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(375.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "This is a dialog with buttons and an image.",
                    modifier = Modifier.padding(16.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = { onDismissRequest() },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Back")
                    }
                    TextButton(
                        onClick = { onConfirmation() },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}