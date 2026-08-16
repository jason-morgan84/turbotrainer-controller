package com.example.controller
import androidx.compose.ui.focus.onFocusChanged
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.delete
import androidx.compose.foundation.text.input.insert
import androidx.compose.foundation.text.input.rememberTextFieldState

import androidx.compose.material3.Card

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.window.Dialog
import com.example.controller.ui.theme.ColourButtons
import com.example.controller.ui.theme.ColourMinus10

import androidx.compose.material3.*
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.input.KeyboardType

import androidx.core.graphics.ColorUtils.colorToHSL
import kotlin.text.append
import kotlin.text.filter
import kotlin.text.takeLast

fun adjustColour (colour: Color, hue: Float = 0f, saturation: Float = 0f, lightness: Float = 0f): Color {
    val hsl = FloatArray(3)
    colorToHSL(colour.toArgb(), hsl)

    val newHue = (hsl[0] + hue).coerceIn(0.0f, 360.0f)
    val newSaturation = (hsl[1] + saturation).coerceIn(0.0f, 1.0f)
    val newLightness = (hsl[2] + lightness).coerceIn(0.0f, 1.0f)

    return Color.hsl(newHue,newSaturation, newLightness)
}


val SegmentType = listOf("Warm Up", "Cool Down", "Interval", "Rest")
val coloursMap = mapOf(
    "Warm Up" to ColourMiddle,
    "Cool Down" to ColourMiddle,
    "Interval" to ColourPlus10,
    "Rest" to ColourMinus10,
    "Repeat" to ColourButtons)

class Segment(val name: String, val ramp: Boolean, val start: Int, val end: Int = start)
class TrainingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ControllerTheme {
                val trainingSegments = remember {mutableStateListOf<Segment>()}
                var showDialog by remember { mutableStateOf(false) }
                if (showDialog){
                    DialogUpdateSegment(
                        onDismissRequest = { showDialog = false },
                        onConfirmation = { showDialog = false },
                        newSegment = true,
                        segmentList = trainingSegments
                    )
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
                                    .background(color = ColourBackground)

                            ) {
                                Column(modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement
                                        .spacedBy(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally)
                                {
                                    for (item in trainingSegments)
                                        {
                                        TrainingSegment(
                                            name = item.name,
                                            ramp = item.ramp,
                                            start = item.start,
                                            end = item.end,
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
                                    .background(color = ColourBackground)
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


    Box(
        modifier = Modifier
            .fillMaxWidth(fraction = 0.6f)
            .height(50.dp)
            .background(ColourBackground))
    {
        Card(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
            colors = CardDefaults.cardColors(coloursMap[name] ?: ColourBackground),
            shape = RoundedCornerShape(8.dp))
        {
            Text(
                maxLines = 2,
                modifier = Modifier.padding(start = 4.dp),
                text = if (ramp) "$name\n xS from $start% to $end" else "$name:\nxS at $start%"
            )
        }
    }
}

@Composable
fun DialogUpdateSegment(
    segmentList: MutableList<Segment>,
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    newSegment: Boolean
) {

    var currentRamp by remember { mutableStateOf(false) }
    val currentStartResistance = rememberTextFieldState()
    val currentEndResistance = rememberTextFieldState()
    val currentTime = rememberTextFieldState()
    var currentSegmentType by remember { mutableStateOf("Interval") }

    val timeInputTransformation = InputTransformation {
        val digits = asCharSequence().filter { it.isDigit() }
        //val result = if (digits.length > 4) digits.takeLast(4).toString() else digits.toString()
        //replace(0, length, result)
    }

    val timeOutputTransformation = OutputTransformation {
        // 1. Pad with leading zeros until we have 4 digits
        while (length < 4) {
            insert(0, "0")
        }
        Log.d("TIME",this.toString())

        while (this.toString()[0] == '0' && length > 4) {
                delete(0, 1)
                Log.d("TIME",this.toString())

        }
        // 2. Insert "m " after the first two digits and "s" at the end
        // Buffer is "MMSS" -> "MMm SSs"
        insert(length - 2, "m ")
        append("s")
    }

    fun checkTime(): CharSequence {
        val stringTime = currentTime.text.toString().ifEmpty { "0000" }
        var minutes = stringTime.substring(0, stringTime.length - 2)
        var seconds = stringTime.substring(stringTime.length - 2,stringTime.length)
        Log.d("TIME", "stringTime: $stringTime Minutes: $minutes Seconds: $seconds")
        if (seconds.toInt() >= 60){
            seconds = (seconds.toInt() - 60).toString()
            minutes = (minutes.toInt() + 1).toString()
        }
        while (seconds.length < 2) {
            seconds = "0$seconds"
        }

        Log.d("TIME", "New Minutes: $minutes New Seconds: $seconds Returns: ${minutes + seconds}")
        return minutes + seconds

    }

    fun updateSegments() {
        if (newSegment) {
            segmentList.add(
                Segment(
                    name = currentSegmentType,

                    ramp = currentRamp,
                    start = currentStartResistance.text.toString().toInt(),
                    end = if (currentRamp) currentEndResistance.text.toString().toInt() else currentStartResistance.text.toString().toInt()
                    //TODO: ADD TYPE TESTING HERE

                )
            )
            onConfirmation()
        }
        else{
            //TODO: add method to edit existing segment, including identification of selected segment
        }
    }


    Dialog(onDismissRequest = { onDismissRequest() }) {
        // Draw a rectangle shape with rounded corners inside the dialog
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Text(
                    text = if (newSegment) "New Segment" else "Edit Segment",
                    fontSize = 20.sp,
                    //color = Color.Black,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    modifier = Modifier.padding(16.dp),
                )
                for (item in SegmentType)
                {
                    val colour = coloursMap[item] ?: ColourButtons
                        ElevatedButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 0.dp)
                                .padding(horizontal = 48.dp),
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor =
                                    if (item == currentSegmentType) adjustColour(
                                        colour,
                                        saturation = -0.6f,
                                        lightness = 0.05f
                                    ) else adjustColour(colour, lightness = 0.15f)
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = if (item == currentSegmentType) 0.dp else 6.dp),
                            onClick = { currentSegmentType = item },

                            )

                        {
                            Text(
                                text = item,
                                color = if (item == currentSegmentType) Color.Black else Color.DarkGray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            )
                        }

                }
                OutlinedTextField(
                    state = currentTime,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    inputTransformation = timeInputTransformation,
                    outputTransformation = timeOutputTransformation,
                    onKeyboardAction = {
                        val newText = checkTime().toString()
                        currentTime.edit {replace(0, length, newText) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .onFocusChanged { if (!it.isFocused) {
                                // This code runs when focus is lost
                                val newText = checkTime().toString()
                                currentTime.edit { replace(0, length, newText) }
                            }
                        }
                            ,
                    label = { Text("Time") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    )


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    )
                {
                    Text("Ramp:")
                    Switch( modifier = Modifier
                        .padding(4.dp),
                        checked = currentRamp,
                        onCheckedChange = {currentRamp = !currentRamp}
                    )

                }

                    OutlinedTextField(
                        state = currentStartResistance,
                        lineLimits = TextFieldLineLimits.SingleLine,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        label = {if (currentRamp) Text("Start Resistance") else Text("Resistance") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        suffix = { Text ("%") })

                        //TODO: ADD TESTING VALUE CHANGE AND UPDATING OF SEGMENT LIST


                    if (currentRamp) {
                        OutlinedTextField(
                            state = currentEndResistance,
                            lineLimits = TextFieldLineLimits.SingleLine,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            label = {Text ("End Resistance") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            suffix = { Text ("%") })

                    }




//TODO Update to normal button styles
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = { onDismissRequest() },
                        modifier = Modifier.padding(horizontal = 8.dp),
                    ) {
                        Text("Back")
                    }
                    TextButton(
                        onClick = { updateSegments() },
                        //onConfirmation()
                        modifier = Modifier.padding(horizontal = 8.dp),
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}