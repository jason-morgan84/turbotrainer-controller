package com.example.controller

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.Dialog
import com.example.controller.ui.theme.ColourButtons
import com.example.controller.ui.theme.ColourMinus10
import kotlin.collections.emptyList
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.toSize

import androidx.compose.material3.*
import androidx.compose.runtime.mutableIntStateOf


val SegmentType = listOf("Warm Up", "Interval", "Rest", "Repeat", "Cool Down")

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
    val coloursMap = mapOf(
        "Warm Up" to ColourMiddle,
        "Cool Down" to ColourMiddle,
        "Interval" to ColourPlus10,
        "Rest" to ColourMinus10,
        "Repeat" to ColourButtons)

    Box(
        modifier = Modifier.fillMaxWidth(fraction = 0.6f)
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
    var mExpanded by remember { mutableStateOf(false) }
    var mSelectedText by remember { mutableStateOf("") }
    var mTextFieldSize by remember { mutableStateOf(Size.Zero)}
    val icon = if (mExpanded)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown
    var currentRamp by remember { mutableStateOf(false) }

    val currentStartResistance = rememberTextFieldState()
    val currentEndResistance = rememberTextFieldState()

    fun updateSegments() {
        if (newSegment) {
            Log.d("DEBUG",mSelectedText)
            segmentList.add(
                Segment(
                    name = mSelectedText,

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
                //modifier = Modifier
                    //.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Text(
                    text = if (newSegment) "New Segment" else "Edit Segment",
                    fontSize = 20.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    modifier = Modifier.padding(16.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                )
                {
                    Label("Type:")
                    OutlinedTextField(
                        value = mSelectedText,
                        onValueChange = { mSelectedText = it },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(4.dp)
                            .onGloballyPositioned { coordinates ->
                                // This value is used to assign to
                                // the DropDown the same width
                                mTextFieldSize = coordinates.size.toSize()
                            },
                        trailingIcon = {
                            Icon(
                                icon, "contentDescription",
                                Modifier.clickable { mExpanded = !mExpanded })
                        }
                    )

                    DropdownMenu(
                        expanded = mExpanded,
                        onDismissRequest = { mExpanded = false },
                        modifier = Modifier
                            .width(with(receiver = LocalDensity.current) { mTextFieldSize.width.toDp() })

                    ) {

                        SegmentType.forEach { label ->
                            DropdownMenuItem(
                                text = { Text(text = label) },
                                onClick = {
                                    mSelectedText = label
                                    mExpanded = false
                                }
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    )
                {
                    Label("Ramp:")
                    Switch( modifier = Modifier
                        .padding(4.dp),
                        checked = currentRamp,
                        onCheckedChange = {currentRamp = !currentRamp},
                    )

                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),

                    verticalAlignment = Alignment.CenterVertically,
                )
                {
                    Label("Resistance: ")
                    OutlinedTextField(
                        state = currentStartResistance,
                        lineLimits = TextFieldLineLimits.SingleLine,
                        modifier = Modifier
                        .width(60.dp)
                        .padding(4.dp))
                        //TODO: ADD TESTING VALUE CHANGE AND UPDATING OF SEGMENT LIST


                    if (currentRamp) {
                        Text("% - ")
                        OutlinedTextField(
                            state = currentEndResistance,
                            lineLimits = TextFieldLineLimits.SingleLine,
                            modifier = Modifier
                                .width(60.dp)
                                .padding(4.dp))
                        Text("%")
                    }
                    else
                        Text("%")

                }

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