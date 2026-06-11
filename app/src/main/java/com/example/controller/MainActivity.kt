package com.example.controller

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.controller.ui.theme.ControllerTheme


var resistance by mutableIntStateOf(50)
var hue by mutableFloatStateOf(60f)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ControllerTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color(0x22b5b5b0) // Pale grey
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            MyButton(
                                onClick = { updateResistance(10)},
                                label = "+10",
                                backgroundColor = Color.hsl(0f,0.55f,0.55f),
                                width = 150.dp
                            )
                            MyButton(
                                onClick = { updateResistance(5)},
                                label = "+5",
                                backgroundColor = Color.hsl(0f,0.55f,0.65f),
                                width = 150.dp
                            )
                            MyButton(
                                onClick = { updateResistance(1)},
                                label = "+1",
                                backgroundColor = Color.hsl(0f,0.55f,0.75f),
                                width = 150.dp
                            )
                            Box(
                                modifier = Modifier
                                    .padding(vertical = 5.dp)
                                    .size(200.dp)
                                    .drawBehind {
                                        val radius = size.minDimension / 2
                                        drawCircle(
                                            brush = Brush.radialGradient(
                                                0.0f to Color.hsl(hue, 0.90f, 0.90f),
                                                0.65f to Color.hsl(hue, 0.90f, 0.90f),
                                                0.70f to Color.hsl(hue, 0.90f, 0.50f),
                                                1.0f to Color.Transparent,
                                                radius = radius
                                            ),
                                            radius = radius
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Label(
                                    value = resistance,
                                    fontSize = 48.sp
                                )
                            }

                            MyButton(
                                onClick = { updateResistance(-1)},
                                label = "-1",
                                backgroundColor = Color.hsl(115f,0.55f,0.75f),
                                width = 150.dp
                            )
                            MyButton(
                                onClick = { updateResistance(-5)},
                                label = "-5",
                                backgroundColor = Color.hsl(115f,0.55f,0.65f),
                                width = 150.dp
                            )
                            MyButton(
                                onClick = { updateResistance(-10)},
                                label = "-10",
                                backgroundColor = Color.hsl(115f,0.55f,0.55f),
                                width = 150.dp
                            )
                        }

                        MyButton(
                            onClick = { /* Handle click */ },
                            label = "Connect",
                            backgroundColor = Color(red = 200, green = 200, blue = 200),
                            textColor = Color.Black,
                            width = 150.dp,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 32.dp)

                        )
                    }
                }
            }
        }
    }
}

fun updateResistance(value: Int) {
    resistance += value
    if (resistance>100) { resistance = 100 }
    if (resistance < 0) { resistance = 0 }
    hue = (120 - resistance * 1.2).toFloat()
    //Log.d("hue updated", hue.toString())
}

@Composable
fun Label(
    value: Int,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified
) {
    Text(
        text = value.toString().plus("%"),
        modifier = modifier,
        fontSize = fontSize
    )
}

@Composable
fun MyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Click Me",
    width: Dp? = null,
    backgroundColor: Color? = null,
    textColor: Color? = null
) {
    Button(
        onClick = onClick,
        modifier = if (width != null) modifier.width(width) else modifier,
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor ?: ButtonDefaults.buttonColors().containerColor,
            contentColor = textColor ?: ButtonDefaults.buttonColors().contentColor
        )
    ) {
        Text(text = label)
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ControllerTheme {
        Column {
            Label(resistance)
            MyButton(onClick = {},label="+")
        }
    }
}