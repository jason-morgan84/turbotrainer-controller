package com.example.controller
import androidx.compose.ui.focus.onFocusChanged
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.delete
import androidx.compose.foundation.text.input.insert
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh

import androidx.compose.material3.Card

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.controller.ui.MyButton
import com.example.controller.ui.theme.ControllerTheme
import com.example.controller.ui.theme.ColourBackground
import com.example.controller.ui.theme.ColourButtons

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.window.Dialog

import androidx.compose.material3.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.zIndex
import com.example.controller.ui.AlertDefinitions
import com.example.controller.ui.Segment
import com.example.controller.ui.SegmentDefinitions
import com.example.controller.ui.Workout
import com.example.controller.ui.WorkoutList
import com.example.controller.ui.theme.ColourMiddle
import com.example.controller.ui.theme.ColourMinus10
import com.example.controller.ui.theme.ColourPlus10
import com.example.controller.ui.theme.adjustColour
import com.example.controller.ui.segmentTypes
import com.example.controller.ui.nestSizeReduction



class WorkoutEdit : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ControllerTheme {
                val context = androidx.compose.ui.platform.LocalContext.current
                val workoutName = intent.getStringExtra("WORKOUT_NAME") ?: ""
                var segmentEdit by remember { mutableStateOf(true) }
                var segmentEditID by remember { mutableIntStateOf(0) }
                var selectedSegment by remember { mutableIntStateOf(-1) }
                var showSegmentDialog by remember { mutableStateOf(false) }
                var showRepeatDialog by remember { mutableStateOf(false) }
                var editNameDialog by remember { mutableStateOf(false) }
                var activeAlert by remember { mutableStateOf<AlertDefinitions?>(null) }
                val workouts by remember {
                    mutableStateOf(WorkoutList(mutableStateListOf<Workout>()))
                }

                activeAlert?.AlertPopup(onClose = { activeAlert = null })

                @Composable
                fun drawSegmentCard(workout: Workout, segmentIndex: Int)
                {
                    val newSegment = workout.getSegmentFromIndex(segmentIndex)
                    val cardColor = segmentTypes[newSegment.type]?.colour ?: ColourBackground
                    val cardHeight = segmentTypes[newSegment.type]?.height ?: 50.dp
                    val cardText = segmentTypes[newSegment.type]?.text?.invoke(newSegment) ?: "Unknown"
                    val cardEditable = segmentTypes[newSegment.type]?.editable ?: true
                    val cardSegment = segmentTypes[newSegment.type]?.segment ?: true
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
                                    onClick = {
                                        selectedSegment =
                                            if (selectedSegment == segmentIndex) -1 else segmentIndex; Log.d(
                                        "Position",
                                        "On click $selectedSegment"
                                    )
                                    },
                                    onLongClick = {
                                        if (cardEditable) {
                                            segmentEdit = true
                                            segmentEditID = newSegment.ID
                                            if (cardSegment) showSegmentDialog =
                                                true else showRepeatDialog = true
                                        }
                                    }),
                            colors = CardDefaults.cardColors(
                                containerColor = if (newSegment.type.contains("Repeat")) adjustColour( cardColor, lightness = 0.1f - (newSegment.nest * 0.1f))
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
                                        onClick = { workout.removeSegmentWithIndex(segmentIndex,segmentTypes[workout.segments[segmentIndex].type]?.segment?: true) })
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
                val openWorkout = remember {
                    workouts.loadWorkoutList(context)
                    if(workoutName == "")
                {
                    Workout("New Workout", mutableStateListOf()).apply {
                        addSegment("Warm Up", 180, true, 10, 30)
                        addSegment("Interval", 300, false, 50)
                        addSegment("Cool Down", 180, true, 30, 10)
                        edited = false}
                }
                    else
                {

                    val loaded = workouts.workouts[workouts.getIndex(workoutName)]
                    Workout(
                        name = loaded.name,
                        segments = mutableStateListOf<Segment>().apply { addAll(loaded.segments) },
                        maxID = loaded.maxID,
                        edited = loaded.edited)
                }
                }

                if (showSegmentDialog){
                    DialogUpdateSegment(
                        onDismissRequest = { showSegmentDialog = false },
                        onConfirmation = { showSegmentDialog = false },
                        onShowAlert = { activeAlert = it },
                        editSegment = segmentEdit,
                        editSegmentID = segmentEditID,
                        selectedSegmentID = selectedSegment,
                        workout = openWorkout
                    )
                }

                if (showRepeatDialog){
                    DialogUpdateRepeat(
                        onDismissRequest = { showRepeatDialog = false },
                        onConfirmation = { showRepeatDialog = false },
                        editRepeat = segmentEdit,
                        editRepeatID = segmentEditID,
                        selectedSegmentID = selectedSegment,
                        workout = openWorkout
                    )
                }
                if (editNameDialog){
                    DialogUpdateName(
                        onDismissRequest = { editNameDialog = false },
                        onConfirmation = { editNameDialog = false },
                        workout = openWorkout
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
                            .padding(innerPadding)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { selectedSegment = -1 },


                    ) {
                        Column {


                            Box(
                                modifier = Modifier
                                    .fillMaxHeight(0.9f)
                                    .fillMaxWidth()
                                    .background(color = ColourBackground)
                                    .padding(),

                            ) {

                                Column(modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement
                                        .spacedBy(5.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                )

                                {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.Bottom
                                    )
                                    {
                                        TextButton(

                                            modifier = Modifier.padding(start=16.dp),
                                            onClick = {editNameDialog = true}
                                        )
                                        {
                                            Text(
                                                fontSize = 24.sp,
                                                color = adjustColour(Color.Gray, lightness = -0.1f),
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                                text = openWorkout.name)
                                            Icon(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .padding(start = 4.dp),
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Name",
                                                tint = Color.Gray
                                            )
                                        }

                                    }
                                    for (item in openWorkout.segments.withIndex())
                                        {
                                            Box(modifier = Modifier
                                                .fillMaxWidth()
                                                .height(if (item.value.type == "RepeatEnd") 25.dp else 50.dp)
                                                .zIndex(if (item.index == selectedSegment) 5f else 0f),
                                                contentAlignment = Alignment.CenterStart)
                                            {
                                                drawSegmentCard(openWorkout, item.index)
                                                if (item.index == selectedSegment) {
                                                    Box(
                                                        modifier = Modifier
                                                            .width(60.dp)
                                                            .wrapContentHeight(
                                                                align = Alignment.CenterVertically,
                                                                unbounded = true
                                                            )
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
                                                                modifier = Modifier
                                                                    .size(40.dp)
                                                                    .padding(bottom = 4.dp),
                                                                shape = CircleShape,
                                                                contentPadding = PaddingValues(0.dp),
                                                                onClick =
                                                                    {
                                                                        if (selectedSegment > 0) {
                                                                            selectedSegment += openWorkout.move(index =
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
                                                                modifier = Modifier
                                                                    .size(40.dp)
                                                                    .padding(top = 4.dp),
                                                                shape = CircleShape,
                                                                contentPadding = PaddingValues(0.dp),
                                                                onClick =
                                                                    {
                                                                        if (selectedSegment < openWorkout.segments.size - 1) {
                                                                            selectedSegment += openWorkout.move(
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
                                    onClick = {
                                        if (openWorkout.edited) {
                                            activeAlert = AlertDefinitions(
                                                title = "Save Changes",
                                                text = "Do you want to save your changes before leaving?",
                                                confirmText = "Yes",
                                                dismissText = "No",
                                                onConfirm = {
                                                    workouts.updateWorkout(openWorkout)
                                                    workouts.saveWorkoutList(context)
                                                    finish()
                                                },
                                                onDismiss = {
                                                    finish()
                                                }
                                            )
                                        } else {
                                            finish()
                                        }
                                    },

                                    label = "Back",
                                    backgroundColor = ColourButtons,
                                    textColor = Color.Black,
                                    width = 120.dp,
                                    roundCorners = 12.dp
                                )
                                MyButton(
                                    onClick = {

                                        if (openWorkout.name in workouts.getAllNames())
                                        {
                                            activeAlert = AlertDefinitions(
                                                title = "Overwrite workout?",
                                                text = "A workout with that name already exists. Do you want to overwrite?",
                                                confirmText = "Yes",
                                                dismissText = "No",
                                                onConfirm = {
                                                    workouts.updateWorkout(openWorkout)
                                                    workouts.saveWorkoutList(context)
                                                    finish()
                                                },
                                                onDismiss = {
                                                }
                                            )
                                        }
                                        else{
                                        workouts.updateWorkout(openWorkout);
                                        workouts.saveWorkoutList(context);
                                        finish()}},
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
    workout: Workout,
    onShowAlert: (AlertDefinitions) -> Unit
) {

    val segmentIndex = workout.getIndexFromID(editSegmentID)
    val workingSegment = workout.getSegmentFromIndex(segmentIndex)
    var currentRamp by remember { mutableStateOf(value = if(editSegment) workingSegment.ramp else false) }
    val currentStartResistance = rememberTextFieldState(initialText = if (editSegment) workingSegment.start.toString() else "")
    val currentEndResistance = rememberTextFieldState(initialText = if(editSegment) workingSegment.end.toString() else "")
    val currentTime = rememberTextFieldState(initialText =
        if (editSegment)
        {
            var minutes = (workingSegment.time/60).toString().filter { it.isDigit() }
            var seconds = (workingSegment.time%60).toString().filter { it.isDigit() }
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
    var currentSegmentType by remember { mutableStateOf(value = if (editSegment) workingSegment.type else "Interval") }
    val timeInputTransformation = InputTransformation {
        if (asCharSequence().any { !it.isDigit() }) {
            revertAllChanges()
        }
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

    fun updateSegments() {
        // Inside updateSegments
        val start = currentStartResistance.text.toString().filter { it.isDigit() }.toIntOrNull()
        val end = currentEndResistance.text.toString().filter { it.isDigit() }.toIntOrNull()
        val time = currentTime.text.toString().filter { it.isDigit() }.takeIf { it.isNotEmpty() }!!

        if (start != null && time != "0000" && (!currentRamp || end != null)) {
            val minutes = time.substring(0, time.length - 2).toInt()
            val seconds = time.substring(time.length - 2, time.length).toInt()
            val finalEnd = if (currentRamp) end!! else start

            if (editSegment) {
                workout.segments[segmentIndex].type = currentSegmentType
                workout.segments[segmentIndex].time = minutes*60 + seconds
                workout.segments[segmentIndex].ramp = currentRamp
                workout.segments[segmentIndex].start = start
                workout.segments[segmentIndex].end = finalEnd
                onConfirmation()
            } else {

                workout.addSegment(
                    position = if (selectedSegmentID != -1) selectedSegmentID else -1,
                    type = currentSegmentType,
                    time = minutes*60 + seconds,
                    ramp = currentRamp,
                    start = start,
                    end = finalEnd
                )
                onConfirmation()
            }
        } else {
            onShowAlert(
                    AlertDefinitions(
                        title = "Missing Data",
                        text = "You haven't entered " +
                                (if (start == null || (currentRamp && end == null)) {
                                    "resistance " + (if (time == "0000") "or time " else "")
                                } else "time ") + "for the segment.",
                        confirmText = "Ok",
                        dismissText = "",
                        onConfirm = {},
                        onDismiss = {}
                    )
                )
        }
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
                    text = if (editSegment) "Edit Segment" else "New Segment",
                    fontSize = 20.sp,
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
                    onKeyboardAction = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .onFocusChanged {},
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
    workout: Workout
) {
    val repeatIndex = workout.getIndexFromID(editRepeatID)
    val workingSegment = workout.getSegmentFromIndex(repeatIndex)
    val currentRepeats = rememberTextFieldState(initialText = if (editRepeat) workingSegment.repeat.toString() else "")

    fun updateRepeats(){
        if (currentRepeats.text.toString() != "") {
            if (editRepeat)
                workout.segments[repeatIndex].repeat = currentRepeats.text.toString().filter { it.isDigit() }.toInt()
            else
                workout.addRepeat(
                    repeats = currentRepeats.text.toString().filter { it.isDigit() }.toInt(),
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
@Composable
fun DialogUpdateName(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    workout: Workout
)
{
    val currentName = rememberTextFieldState(initialText = workout.name)

    fun updateName(){
        if (currentName.text.toString() != "") {
            workout.name = currentName.text.toString()

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
                text = "Edit Name",
                fontSize = 20.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                modifier = Modifier.padding(16.dp),
            )
            OutlinedTextField(
                state = currentName,
                lineLimits = TextFieldLineLimits.SingleLine,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                label = { Text("Name") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
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
                    onClick = { updateName()},
                    label = "Update",
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


