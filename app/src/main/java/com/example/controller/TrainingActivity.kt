package com.example.controller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import com.example.controller.ui.theme.ColourButtons
import com.example.controller.ui.theme.ColourMinus10

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
                            .fillMaxHeight()
                            .fillMaxWidth()
                            .padding(innerPadding),


                    ) {
                        Column {


                            Row(
                                //verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    //.height(500.dp)
                                    .fillMaxHeight(0.9f)
                                    .fillMaxWidth()
                                    .background(color = ColourMinus10)

                            ) {
                                TrainingSegment(
                                    name = "Segment 1",
                                    ramp = true,
                                    start = 0,
                                    end = 10,
                                    backgroundColor = ColourPlus5
                                )
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
                                    onClick = { finish() },
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
    backgroundColor: Color? = null,
    name: String,
    ramp: Boolean,
    start: Int,
    end: Int = start)
{
    Box(
        modifier = Modifier.fillMaxWidth(fraction = 0.6f)
            .height(40.dp)
            .background(color = backgroundColor ?: ColourMiddle),

    )
    {
        Text(
            text = "Hello",
        )
    }
}

