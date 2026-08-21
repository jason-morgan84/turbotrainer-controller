package com.example.controller
import androidx.compose.ui.focus.onFocusChanged
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.delete
import androidx.compose.foundation.text.input.insert
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh

import androidx.compose.material3.Card

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.window.Dialog
import com.example.controller.ui.theme.ColourMinus10

import androidx.compose.material3.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.zIndex

import androidx.core.graphics.ColorUtils.colorToHSL

//TODO NEXT: ADD REPEAT BUTTON
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
var firstLoad = true
class Segment(var name: String, var ID: Int, var time: Array<Int>, var ramp: Boolean, var start: Int, var end: Int = start)

class TrainingPlan (val name: String, val segments: MutableList<Segment>, var maxID: Int = 0)
{
    fun addSegment(name: String, time: Array<Int>, ramp: Boolean, start: Int, end: Int = start, position: Int = segments.size)
    {
        segments.add(position, Segment(name, maxID, time, ramp, start, end))
        maxID++
    }
    fun removeSegmentWithIndex(index: Int)
    {
        segments.removeAt(index)
    }

    fun moveSegmentByIndex(id: Int, direction: String, index: Int = getIndexFromID(id)) {
        if (index != -1) {
            if (direction == "up" && index > 0) {
                val temp = segments[index]
                segments[index] = segments[index - 1]
                segments[index - 1] = temp
            } else if (direction == "down" && index < segments.size - 1) {
                val temp = segments[index]
                segments[index] = segments[index + 1]
                segments[index + 1] = temp
            }
        }
    }

    fun removeSegmentWithID (id: Int)
    {
        val index = getIndexFromID(id)
        if (index != -1) segments.removeAt(index)
    }
    fun getSegmentFromIndex(index: Int): Segment
    {
        return segments[index]
    }
    fun getSegmentFromID(id: Int): Segment {
        for (n in segments.indices) {
            if (segments[n].ID == id)
                return segments[n]
        }
        return Segment("Error", id, arrayOf(0, 0), false, 0)
    }
    fun getIndexFromID(id: Int): Int {
        for (n in segments.indices) {
            if (segments[n].ID == id)
                return n
        }
        return -1
    }
}
class TrainingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ControllerTheme {
                val defaultTrainingPlan = remember {
                    TrainingPlan("Default", mutableStateListOf()).apply {
                        addSegment("Warm Up", arrayOf(3, 0), true, 10, 30)
                        addSegment("Interval", arrayOf(5, 0), false, 50)
                        addSegment("Cool Down", arrayOf(3, 0), true, 30, 10)
                    }
                }

                val openTrainingPlan = defaultTrainingPlan
                var segmentEdit by remember { mutableStateOf(true) }
                var segmentEditID by remember { mutableIntStateOf(0) }
                var showDialog by remember { mutableStateOf(false) }
                if (showDialog){
                    DialogUpdateSegment(
                        onDismissRequest = { showDialog = false },
                        onConfirmation = { showDialog = false },
                        editSegment = segmentEdit,
                        segmentID = segmentEditID,
                        trainingPlan = openTrainingPlan
                    )
                }

                var selectedSegment by remember { mutableIntStateOf(0) }
                //TODO update openTraining to whatever's chosen in previous screen



                @Composable
                fun drawSegmentCard(trainingPlan: TrainingPlan, segmentIndex: Int)
                //TODO ID or INDEX?
                {
                    val newSegment = trainingPlan.getSegmentFromIndex(segmentIndex)
                    val cardColor = coloursMap[newSegment.name] ?: ColourBackground
                    Box(
                        modifier = Modifier
                            .layoutId(newSegment.ID.toString())
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(ColourBackground))
                    {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 80.dp)
                                .fillMaxHeight()
                                .combinedClickable(
                                    onClick = { selectedSegment = segmentIndex },
                                    onLongClick = {
                                        segmentEdit = true; segmentEditID =
                                        newSegment.ID; showDialog = true;
                                    }),
                            colors = CardDefaults.cardColors(
                                adjustColour( cardColor, lightness = if (selectedSegment == segmentIndex) 0.05f else 0.1f)
                            ),
                            border = BorderStroke(if (selectedSegment == segmentIndex) 3.dp else 0.dp, adjustColour(cardColor,saturation = 1f,lightness = -0.05f)),
                            shape = RoundedCornerShape(8.dp),
                        )
                        {
                            Row(modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically)
                            {
                                Text(
                                    maxLines = 2,
                                    text = if (newSegment.ramp)
                                        if (newSegment.time[0] != 0)
                                            "${newSegment.name}\n ${newSegment.time[0]}m ${newSegment.time[1]}s @ ${newSegment.start}-${newSegment.end}%"
                                        else
                                            "${newSegment.name}\n ${newSegment.time[1]}s @ ${newSegment.start}-${newSegment.end}%"
                                    else
                                        if (newSegment.time[0] != 0)
                                            "${newSegment.name}\n ${newSegment.time[0]}m ${newSegment.time[1]}s @ ${newSegment.start}%"
                                        else
                                            "${newSegment.name}\n ${newSegment.time[1]}s @ ${newSegment.start}%"
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Button(modifier = Modifier
                                    .width(30.dp)
                                    .padding(vertical = 4.dp)
                                    .height(30.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = adjustColour(ColourButtons,lightness = -0.5f).copy(alpha=0.5f)),
                                    contentPadding = PaddingValues(0.dp),
                                    shape = CircleShape,
                                    onClick = {trainingPlan.removeSegmentWithIndex(segmentIndex)})
                                {
                                    Icon(
                                        modifier = Modifier.size(32.dp),
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete"
                                    )
                                }
                            }
                        }
                    }
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


                            Box(
                                modifier = Modifier
                                    .fillMaxHeight(0.9f)
                                    .fillMaxWidth()
                                    .background(color = ColourBackground)
                                    .padding(top=16.dp),

                            ) {
                                Column(modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement
                                        .spacedBy(5.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally)
                                {
                                    for (item in openTrainingPlan.segments.withIndex())
                                        {
                                            Box(modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(50.dp)
                                                    .zIndex(if (item.index == selectedSegment) 5f else 0f),
                                                contentAlignment = Alignment.CenterStart)
                                            {
                                                drawSegmentCard(openTrainingPlan, item.index)
                                                if (item.index == selectedSegment) {
                                                    Box(
                                                        modifier = Modifier
                                                            .width(60.dp)
                                                            .wrapContentHeight(align = Alignment.CenterVertically, unbounded = true)
                                                            .padding(horizontal = 8.dp)
                                                            ,
                                                    )
                                                    {
                                                        Column(
                                                            modifier = Modifier.fillMaxSize(),
                                                            verticalArrangement = Arrangement.Center,
                                                            horizontalAlignment = Alignment.CenterHorizontally
                                                        )
                                                        {
                                                            Button(
                                                                modifier = Modifier.size(40.dp).padding(bottom = 4.dp),
                                                                shape = CircleShape,
                                                                contentPadding = PaddingValues(0.dp),
                                                                onClick =
                                                                    {
                                                                        if (selectedSegment > 0) {
                                                                            openTrainingPlan.moveSegmentByIndex(
                                                                                item.value.ID,
                                                                                "up"
                                                                            );
                                                                            selectedSegment -= 1
                                                                        }
                                                                    },
                                                                colors = ButtonDefaults.buttonColors(
                                                                    containerColor = ColourButtons,
                                                                    contentColor = Color.Black
                                                                )
                                                            )
                                                            {
                                                                Icon(
                                                                    imageVector = Icons.Default.ArrowUpward,
                                                                    contentDescription = "Move up"
                                                                )
                                                            }
                                                            Button(
                                                                modifier = Modifier.size(40.dp).padding(top = 4.dp),
                                                                shape = CircleShape,
                                                                contentPadding = PaddingValues(0.dp),
                                                                onClick =
                                                                    {
                                                                        if (selectedSegment < openTrainingPlan.segments.size - 1) {
                                                                            openTrainingPlan.moveSegmentByIndex(
                                                                                item.value.ID,
                                                                                "down"
                                                                            );
                                                                            selectedSegment += 1
                                                                        }
                                                                    },
                                                                colors = ButtonDefaults.buttonColors(
                                                                    containerColor = ColourButtons,
                                                                    contentColor = Color.Black
                                                                )
                                                            )
                                                            {
                                                                Icon(
                                                                    imageVector = Icons.Default.ArrowDownward,
                                                                    contentDescription = "Move down"
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
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
                                            Button(modifier = Modifier
                                                .width(50.dp)
                                                .padding(vertical = 4.dp)
                                                .height(50.dp),
                                                shape = CircleShape,
                                                contentPadding = PaddingValues(0.dp),
                                                onClick = {showDialog=true;segmentEdit = false},
                                                colors = ButtonDefaults.buttonColors(containerColor = ColourButtons, contentColor = Color.Black)
                                            )
                                            {
                                                Icon(
                                                    modifier = Modifier.size(32.dp),
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "Add Segment"
                                                )
                                            }
                                            Button(modifier = Modifier
                                                .width(50.dp)
                                                .padding(vertical = 4.dp)
                                                .height(50.dp),
                                                contentPadding = PaddingValues(0.dp),
                                                shape = CircleShape,
                                                onClick = {showDialog = true; segmentEdit = false},
                                                colors = ButtonDefaults.buttonColors(containerColor = ColourButtons, contentColor = Color.Black)
                                            )
                                            {
                                                Icon(
                                                    modifier = Modifier.size(32.dp),
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = "Add Repeat"
                                                )
                                            }
                                        }
                                    }


                                }


                                }
                            Row(
                                modifier = Modifier
                                    .padding(bottom = 32.dp)
                                    .background(color = ColourBackground)
                                    .fillMaxHeight(1f)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            )
                            {
                                MyButton(
                                    onClick = { finish(); firstLoad = true },
                                    label = "Back",
                                    backgroundColor = ColourButtons,
                                    textColor = Color.Black,
                                    width = 120.dp,
                                    roundCorners = 12.dp
                                )
                                MyButton(
                                    onClick = { finish(); firstLoad = true },
                                    label = "Save",
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
fun DialogUpdateSegment(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    editSegment: Boolean,
    segmentID: Int,
    trainingPlan: TrainingPlan
) {
    val segmentIndex = trainingPlan.getIndexFromID(segmentID)
    val workingSegment = trainingPlan.getSegmentFromIndex(segmentIndex)
    var currentRamp by remember { mutableStateOf(value = if(editSegment) workingSegment.ramp else false) }
    val currentStartResistance = rememberTextFieldState(initialText = if (editSegment) workingSegment.start.toString() else "")
    val currentEndResistance = rememberTextFieldState(initialText = if(editSegment) workingSegment.end.toString() else "")
    val currentTime = rememberTextFieldState(initialText =
        if (editSegment)
        {
            var minutes = workingSegment.time[0].toString().filter { it.isDigit() }
            var seconds = workingSegment.time[1].toString().filter { it.isDigit() }
            while (minutes.length <2)
            {
                minutes = "0$minutes"
            }
            while (seconds.length < 2)
            {
                seconds = "0$seconds"
            }
            minutes + seconds
        }
        else { "0000" }
    )
    var currentSegmentType by remember { mutableStateOf(value = if (editSegment) workingSegment.name else "Interval") }
    val timeInputTransformation = InputTransformation {
        //val digits = asCharSequence().filter { it.isDigit() }
    }

    val timeOutputTransformation = OutputTransformation {

        while (length < 4) {
            insert(0, "0")
        }

        while (this.toString()[0] == '0' && length > 4) {
                delete(0, 1)

        }

        insert(length - 2, "m ")
        append("s")
    }

    fun checkTime(): CharSequence {

        var stringTime = currentTime.text.toString().ifEmpty { "0000" }
        stringTime = stringTime.filter { it.isDigit() }

        while (stringTime.length < 4) {
            stringTime = "0$stringTime"
        }

        var minutes = stringTime.substring(0, stringTime.length - 2)
        var seconds = stringTime.substring(stringTime.length - 2,stringTime.length)

        if (seconds.toInt() >= 60){
            seconds = (seconds.toInt() - 60).toString()
            minutes = (minutes.toInt() + 1).toString()
        }
        while (seconds.length < 2) {
            seconds = "0$seconds"
        }
        return minutes + seconds

    }

    fun updateSegments() {
        if (editSegment) {
            val newTime = checkTime()
            trainingPlan.segments[segmentIndex].name = currentSegmentType
            trainingPlan.segments[segmentIndex].time[0] = newTime.ifEmpty { "0000" }.substring(0, newTime.length - 2).toInt()
            trainingPlan.segments[segmentIndex].time[1] = newTime.ifEmpty { "0000" }.substring(newTime.length - 2, newTime.length).toInt()
            trainingPlan.segments[segmentIndex].ramp = currentRamp
            trainingPlan.segments[segmentIndex].start = currentStartResistance.text.toString().toInt()
            trainingPlan.segments[segmentIndex].end = if (currentRamp) currentEndResistance.text.toString().toInt() else currentStartResistance.text.toString().toInt()
            //TODO: ADD TYPE TESTING HERE AND WHAT TO DO IF RESISTANCES NOT ENTERED
            onConfirmation()
        }
        else{

            trainingPlan.addSegment(
                name = currentSegmentType,
                time = arrayOf(
                    currentTime.text.toString().ifEmpty { "0000" }.substring(0, currentTime.text.toString().length - 2).toInt(),
                    currentTime.text.toString().ifEmpty { "0000" }.substring(currentTime.text.toString().length - 2, currentTime.text.toString().length).toInt()),
                ramp = currentRamp,
                start = currentStartResistance.text.toString().toInt(),
                end = if (currentRamp) currentEndResistance.text.toString().toInt() else currentStartResistance.text.toString().toInt()
                //TODO: ADD TYPE TESTING HERE AND WHAT TO DO IF RESISTANCES NOT ENTERED
            )
            onConfirmation()
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
                    text = if (editSegment) "Edit Segment" else "New Segment",
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
                        onClick = { currentSegmentType = item }
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
                        .onFocusChanged {
                            if (!it.isFocused) {
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    MyButton(
                        onClick = { onDismissRequest() },
                        label = "Back",
                        backgroundColor = ColourButtons,
                        textColor = Color.Black,
                        width = 120.dp,
                        roundCorners = 12.dp,
                        modifier = Modifier.padding(all = 8.dp),
                    )
                    MyButton(
                        onClick = { updateSegments()},
                        label = if (editSegment) "Update" else "Add",
                        backgroundColor = ColourButtons,
                        textColor = Color.Black,
                        width = 120.dp,
                        roundCorners = 12.dp,
                        modifier = Modifier.padding(all = 8.dp),
                    )
                }
            }
        }
    }
}