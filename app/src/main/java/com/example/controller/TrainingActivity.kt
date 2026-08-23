package com.example.controller
import androidx.compose.ui.focus.onFocusChanged
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.zIndex

import androidx.core.graphics.ColorUtils.colorToHSL


fun adjustColour (colour: Color, hue: Float = 0f, saturation: Float = 0f, lightness: Float = 0f): Color {
    val hsl = FloatArray(3)
    colorToHSL(colour.toArgb(), hsl)

    val newHue = (hsl[0] + hue).coerceIn(0.0f, 360.0f)
    val newSaturation = (hsl[1] + saturation).coerceIn(0.0f, 1.0f)
    val newLightness = (hsl[2] + lightness).coerceIn(0.0f, 1.0f)

    return Color.hsl(newHue,newSaturation, newLightness)
}

class SegmentDefinitions (val colour: Color, val height: Dp, val text: (Segment) -> String, val moveable: Boolean = true, val editable: Boolean = true, val segment: Boolean = true)

val standardSegmentText: (Segment) -> String = { segment ->
    "${segment.name}\n" +
            (if (segment.time[0] != 0) "${segment.time[0]}m " else "") +
            "${segment.time[1]}s @ ${segment.start}" +
            (if (segment.ramp) "-${segment.end}%" else "%")
}

const val nestSizeReduction: Int = 20

val segmentTypes: Map<String, SegmentDefinitions> = mapOf(
    "Warm Up" to SegmentDefinitions(ColourMiddle, 50.dp, standardSegmentText),
    "Interval" to SegmentDefinitions(ColourPlus10, 50.dp, standardSegmentText),
    "Cool Down" to SegmentDefinitions(ColourMiddle, 50.dp, standardSegmentText),
    "Rest" to SegmentDefinitions(ColourMinus10, 50.dp, standardSegmentText),
    "RepeatStart" to SegmentDefinitions(adjustColour(ColourButtons, lightness = -0.1f), 50.dp, { segment -> "Repeat x${segment.start}" }, segment = false),
    "RepeatEnd" to SegmentDefinitions(adjustColour(ColourButtons, lightness = -0.1f), 25.dp, { "" }, editable = false, segment = false)
)



var firstLoad = true
class Segment(var name: String, var ID: Int, var time: Array<Int>, var ramp: Boolean, var start: Int, var end: Int = start, var nest: Int = 0)

class TrainingPlan (val name: String, val segments: MutableList<Segment>, var maxID: Int = 0)
{
    fun addSegment(name: String, time: Array<Int>, ramp: Boolean, start: Int, end: Int = start, position: Int = segments.size)
    {
        segments.add(position, Segment(name, maxID, time, ramp, start, end))
        maxID++
    }

    fun addRepeat(repeats: Int, position: Int = segments.size) {
        //TODO- deal with adding repeats around repeats
        //TODO - when a repeat is selected, add repeatstart above, repeat endbelow and nest everything between
        // TODO - when a segment is selected, add repeat start above, repeat end below and nest segment
        //TODO - but check for too much nesting, else add at end.
        //
        Log.d("POSITION",position.toString())
        if (position == -1) {

            segments.add(Segment("RepeatStart", maxID, arrayOf(0, 0), false, repeats))
            segments.add(Segment("RepeatEnd", maxID, arrayOf(0, 0), false, repeats))
        }
        else {
            segments[position].nest ++
            segments.add(position, Segment("RepeatStart", maxID, arrayOf(0, 0), false, repeats))
            segments.add(position + 2, Segment("RepeatEnd", maxID, arrayOf(0, 0), false, repeats))
        }
        maxID++
    }
    fun removeSegmentWithIndex(index: Int)
    {
        if (segmentTypes[segments[index].name]?.segment == true)
            segments.removeAt(index)
        else {
            val repeatEndIndex = getIndexFromID(segments[index].ID, index + 1)

            for (i in index..repeatEndIndex)
            {
                segments[i].nest--
            }
            segments.removeAt(index)
            segments.removeAt(repeatEndIndex - 1)
        }

    }
    fun move(id: Int = 0, direction: String, index: Int = getIndexFromID(id)): Int
    {
        if (index != -1) {
            val increments: Map<String, Int> = mapOf("up" to -1, "down" to 1).withDefault { 0 }
            val increment =
                if ((index + increments.getValue(direction)) in segments.indices)
                    increments.getValue(direction) else 0
            val movement =
                if (segments[index].name.contains("Repeat"))
                    moveRepeatbyIndex(increment, index) else moveSegmentByIndex(increment, index)
            return movement
        }
        else {
            return 0
        }
    }
    fun moveSegmentByIndex(increment: Int = 0, index: Int): Int
    {
        val nextElement = segments[index + increment]

        if (nextElement.name == "RepeatEnd") {

            segments[index].nest -= increment
        }
        else if (nextElement.name == "RepeatStart"){
            segments[index].nest += increment
        }

        val temp = segments[index]
        segments[index] = segments[index + increment]
        segments[index + increment] = temp

        return increment
    }

    fun moveRepeatbyIndex (increment: Int = 0, index: Int): Int

    {
        var movement = increment
        var allowMovement = true
        val currentElement = segments[index]
        val nextElement = segments[index + increment]
        var addIndex = index + increment
        var removeIndex = index

        if ((currentElement.name == "RepeatEnd" && nextElement.name == "RepeatStart" && currentElement.ID == nextElement.ID && movement == -1) ||
            (currentElement.name == "RepeatStart" && nextElement.name == "RepeatEnd" && currentElement.ID == nextElement.ID && movement == 1) ||
            (currentElement.name == "RepeatStart" && nextElement.name == "RepeatStart" && movement == -1) ||
            (currentElement.name == "RepeatEnd" && nextElement.name == "RepeatStart" && movement == -1) ||
            (currentElement.name == "RepeatEnd" && nextElement.name == "RepeatEnd")){
            // A RepeatStart can't go past its own RepeatEnd
            // A RepeatEnd can't got past its own RepeatStart
            // A RepeatStart can't go up past another RepeatStart - swapping of repeats like this seems like it could get messy, just edit them
            // A RepeatEnd can't go up or down past another RepeatEnd
            // Do nothing
            allowMovement = false
        }
        else if (currentElement.name == "RepeatStart" && nextElement.name == "RepeatEnd" && currentElement.ID != nextElement.ID){
            // movement must be up (movement down dealt with above, results in no move)
            val aboveRepeatEndIndex = index - 1
            val aboveRepeatStartIndex = getIndexFromID(segments[aboveRepeatEndIndex].ID)

            for (i in aboveRepeatStartIndex..aboveRepeatEndIndex){
                if (segments[i].nest == 2) allowMovement = false
            }

            if (allowMovement) {
                for (i in aboveRepeatStartIndex..aboveRepeatEndIndex) {
                    segments[i].nest++
                }
                addIndex = aboveRepeatStartIndex
                removeIndex = index + 1
                movement = -(index - aboveRepeatStartIndex)
            }
        }
        else if (currentElement.name == "RepeatStart" && nextElement.name == "RepeatStart" && currentElement.ID != nextElement.ID){
            // movement must be down (movement up dealt with above, results in no move)
            val belowRepeatStartIndex = index + 1
            val belowRepeatEndIndex = getIndexFromID(segments[belowRepeatStartIndex].ID,index + 2)
            // don't need to test for nesting, this will be moving items out of nested repeat
            for (i in belowRepeatStartIndex..belowRepeatEndIndex){
                segments[i].nest --
            }
            addIndex = belowRepeatEndIndex + 1
            removeIndex = index
            movement = belowRepeatEndIndex - belowRepeatStartIndex + 1
        }
        else {
            if (currentElement.name == "RepeatStart") {
                segments[index + increment].nest -= increment
            } else if (currentElement.name == "RepeatEnd") {
                segments[index + increment].nest += increment
            }
            addIndex = if (movement > 0) index + movement + 1 else index + movement
            removeIndex = if (movement<0) index  + 1 else index
        }
        if (allowMovement) {

            segments.add(addIndex, segments[index])
            segments.removeAt(removeIndex)
            return movement
        }
        else
            return 0

    }


    fun removeSegmentWithID (id: Int)
    {
        val index = getIndexFromID(id)
        if (index != -1) segments.removeAt(index)
    }
    fun getSegmentFromIndex(index: Int): Segment
    {
        return segments.getOrNull(index) ?: Segment("Empty", -1, arrayOf(0, 0), false, 0)
    }
    fun getSegmentFromID(id: Int): Segment {
        for (n in segments.indices) {
            if (segments[n].ID == id)
                return segments[n]
        }
        return Segment("Error", id, arrayOf(0, 0), false, 0)
    }
    fun getIndexFromID(id: Int, start: Int = 0): Int {
        if (segments.isEmpty()) return -1
        for (n in start until segments.size) {
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
                var selectedSegment by remember { mutableIntStateOf(-1) }
                var showSegmentDialog by remember { mutableStateOf(false) }
                var showRepeatDialog by remember { mutableStateOf(false) }
                if (showSegmentDialog){
                    DialogUpdateSegment(
                        onDismissRequest = { showSegmentDialog = false },
                        onConfirmation = { showSegmentDialog = false },
                        editSegment = segmentEdit,
                        editSegmentID = segmentEditID,
                        selectedSegmentID = selectedSegment,
                        trainingPlan = openTrainingPlan
                    )
                }

                if (showRepeatDialog){
                    Log.d("Position","selectedSegment $selectedSegment")
                    DialogUpdateRepeat(
                        onDismissRequest = { showRepeatDialog = false },
                        onConfirmation = { showRepeatDialog = false },
                        editRepeat = segmentEdit,
                        editRepeatID = segmentEditID,
                        selectedSegmentID = selectedSegment,
                        trainingPlan = openTrainingPlan
                    )
                }


                //TODO update openTraining to whatever's chosen in previous screen



                @Composable
                fun drawSegmentCard(trainingPlan: TrainingPlan, segmentIndex: Int)
                {
                    val newSegment = trainingPlan.getSegmentFromIndex(segmentIndex)
                    val cardColor = segmentTypes[newSegment.name]?.colour ?: ColourBackground
                    val cardHeight = segmentTypes[newSegment.name]?.height ?: 50.dp
                    val cardText = segmentTypes[newSegment.name]?.text?.invoke(newSegment) ?: "Unknown"
                    val cardEditable = segmentTypes[newSegment.name]?.editable ?: true
                    val cardSegment = segmentTypes[newSegment.name]?.segment ?: true
                    Box(
                        modifier = Modifier
                            .layoutId(newSegment.ID.toString())
                            .fillMaxWidth()
                            .height(cardHeight)
                            .background(ColourBackground))
                    {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = (80 + newSegment.nest * nestSizeReduction).dp)
                                .padding(end = 80.dp)
                                .fillMaxHeight()
                                .combinedClickable(
                                    onClick = { selectedSegment = if (selectedSegment == segmentIndex) -1 else segmentIndex; Log.d("Position","On click $selectedSegment")},
                                    onLongClick = { if (cardEditable) {
                                        segmentEdit = true;
                                        segmentEditID = newSegment.ID;
                                        if (cardSegment) showSegmentDialog = true else showRepeatDialog = true}
                                    }),
                            colors = CardDefaults.cardColors(
                                containerColor = if (newSegment.name.contains("Repeat")) adjustColour( cardColor, lightness = 0.1f - (newSegment.nest * 0.1f))
                                else adjustColour( cardColor, lightness = if (selectedSegment == segmentIndex) 0.05f else 0.1f)
                            ),
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
                                    text = cardText
                                    )
                                if (cardEditable) {
                                    Spacer(modifier = Modifier.weight(1f))

                                    Button(
                                        modifier = Modifier
                                            .width(30.dp)
                                            .padding(vertical = 4.dp)
                                            .height(30.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = adjustColour(
                                                ColourButtons,
                                                lightness = -0.5f
                                            ).copy(alpha = 0.5f)
                                        ),
                                        contentPadding = PaddingValues(0.dp),
                                        shape = CircleShape,
                                        onClick = { trainingPlan.removeSegmentWithIndex(segmentIndex) })
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
                                                    .height(if (item.value.name=="RepeatEnd") 25.dp else 50.dp)
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
                                                                            selectedSegment += openTrainingPlan.move(index =
                                                                                item.index,
                                                                                direction = "up"
                                                                            )
                                                                            //selectedSegment += 1
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
                                                                            selectedSegment += openTrainingPlan.move(
                                                                                index = item.index,
                                                                                direction = "down"
                                                                            )
                                                                            //selectedSegment += 1
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
                                                onClick = {showSegmentDialog=true; segmentEdit = false},
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
                                                onClick = {showRepeatDialog = true; segmentEdit = false},
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
    editSegmentID: Int,
    selectedSegmentID: Int = -1,
    trainingPlan: TrainingPlan
) {

    val segmentIndex = trainingPlan.getIndexFromID(editSegmentID)
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
                position = if (selectedSegmentID!=-1) selectedSegmentID else -1,
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
                segmentTypes.forEach { entry ->
                    if (entry.value.segment) {
                        val colour = entry.value.colour
                        ElevatedButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 0.dp)
                                .padding(horizontal = 48.dp),
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor =
                                    if (entry.key == currentSegmentType) adjustColour(
                                        colour,
                                        saturation = -0.6f,
                                        lightness = 0.05f
                                    ) else adjustColour(colour, lightness = 0.15f)
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = if (entry.key == currentSegmentType) 0.dp else 6.dp),
                            onClick = { currentSegmentType = entry.key }
                        )

                        {
                            Text(
                                text = entry.key,
                                color = if (entry.key == currentSegmentType) Color.Black else Color.DarkGray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            )
                        }
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
@Composable
fun DialogUpdateRepeat(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    editRepeat: Boolean,
    editRepeatID: Int,
    selectedSegmentID: Int = -1,
    trainingPlan: TrainingPlan
) {
    val repeatIndex = trainingPlan.getIndexFromID(editRepeatID)
    val workingSegment = trainingPlan.getSegmentFromIndex(repeatIndex)
    val currentRepeats = rememberTextFieldState(initialText = if (editRepeat) workingSegment.start.toString() else "")

    fun updateRepeats(){
        //TODO: consider how type testing/what to do if resistances not entered here compare to segment edit dialogue
        if (currentRepeats.text.toString() != "") {
            if (editRepeat)
                trainingPlan.segments[repeatIndex].start = currentRepeats.text.toString().toInt()
            else
                trainingPlan.addRepeat(
                    repeats = currentRepeats.text.toString().toInt(),
                    position = if (selectedSegmentID != -1) selectedSegmentID else -1
                )
            onConfirmation()
        }
        else
            onDismissRequest()
    }

    Dialog(onDismissRequest = { onDismissRequest() }) {
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
                    text = if (editRepeat) "Edit Repeat" else "New Repeat",
                    fontSize = 20.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    modifier = Modifier.padding(16.dp),
                )
                OutlinedTextField(
                    state = currentRepeats,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    label = { Text("Repeats") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                )
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
                        onClick = { updateRepeats()},
                        label = if (editRepeat) "Update" else "Add",
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