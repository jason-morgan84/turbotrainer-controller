package com.example.controller.ui

import android.util.Log

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.controller.ui.theme.ColourBackground
import com.example.controller.ui.theme.ColourButtons
import com.example.controller.ui.theme.ColourMiddle
import com.example.controller.ui.theme.ColourMinus10
import com.example.controller.ui.theme.ColourPlus10
import com.example.controller.ui.theme.adjustColour
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File



@Serializable
@Suppress("PropertyName")
class Segment(var type: String, var ID: Int, var time: Int, var ramp: Boolean, var start: Int, var end: Int = start, var repeat: Int = 0, var nest: Int = 0)

class SegmentDefinitions (val colour: Color, val height: Dp, val text: (Segment) -> String, val editable: Boolean = true, val segment: Boolean = true)

const val nestSizeReduction: Int = 20

val standardSegmentText: (Segment) -> String = { segment ->
    "${segment.type}\n" +
            (if (segment.time >= 60) "${segment.time/60}m " else "") +
            "${segment.time%60}s @ ${segment.start}" +
            (if (segment.ramp) "-${segment.end}%" else "%")
}

val segmentTypes: Map<String, SegmentDefinitions> = mapOf(
    "Warm Up" to SegmentDefinitions(ColourMiddle, 50.dp, standardSegmentText),
    "Interval" to SegmentDefinitions(ColourPlus10, 50.dp, standardSegmentText),
    "Cool Down" to SegmentDefinitions(ColourMiddle, 50.dp, standardSegmentText),
    "Rest" to SegmentDefinitions(ColourMinus10, 50.dp, standardSegmentText),
    "RepeatStart" to SegmentDefinitions(adjustColour(ColourButtons, lightness = -0.1f), 50.dp, { segment -> "Repeat x${segment.repeat}" }, segment = false),
    "RepeatEnd" to SegmentDefinitions(adjustColour(ColourButtons, lightness = -0.1f), 25.dp, { "" }, editable = false, segment = false),
    "Padding" to SegmentDefinitions(ColourBackground, 25.dp, { "" }, editable = false, segment = false))




@Serializable
class Workout (var name: String, val segments: MutableList<Segment>, var maxID: Int = 0, var edited: Boolean = false)
{
    fun addSegment(type: String, time: Int, ramp: Boolean, start: Int, end: Int = start, position: Int = -1)
    {
        if (position == -1)
            segments.add(Segment(type, maxID, time, ramp, start, end))
        else {
            segments.add(position+1, Segment(type, maxID, time, ramp, start, end,nest=segments[position].nest))
        }
        maxID++
        edited = true
    }

    fun addRepeat(repeats: Int, position: Int = segments.size) {

        var added = false

        var repeatStartIndex = segments.size + 1
        var repeatEndIndex = segments.size + 1
        var maxNest = 0


        if (position == -1)
        {
            repeatStartIndex = segments.size
            repeatEndIndex = segments.size + 1
            added = true
        }
        else if (segments[position].type.contains("Repeat"))
        {

            for (i in getIndexFromID(segments[position].ID)..getIndexFromID(segments[position].ID,position+1))
            {
                if (segments[i].nest > maxNest) maxNest = segments[i].nest
            }
            if (maxNest < 2){
                repeatStartIndex =
                    if (segments[position].type == "RepeatStart") position else
                        getIndexFromID(segments[position].ID)

                repeatEndIndex = if (segments[position].type == "RepeatStart")
                    getIndexFromID(segments[position].ID,position+1) else
                    position


                for (i in repeatStartIndex .. repeatEndIndex)
                    segments[i].nest ++

                repeatEndIndex += 2
                added = true
            }
            else{
                added = true
            }
        }
        else {
            if (segments[position].nest < 2) {
                segments[position].nest++
                repeatStartIndex = position
                repeatEndIndex = position + 2

                added = true
            }

        }

        if (added)
        {
            segments.add(
                repeatStartIndex,
                Segment("RepeatStart", maxID, 0, false, 0, repeat = repeats)
            )
            segments.add(
                repeatEndIndex,
                Segment("RepeatEnd", maxID, 0, false, 0, repeat = repeats)
            )
            maxID++
        }
        edited = true
    }
    fun removeSegmentWithIndex(index: Int, segment: Boolean = true)
    {
        Log.d("DELETING", "$index from segments of length ${segments.size}")
        if (segment) {
            segments.removeAt(index)
            edited = true
            Log.d("DELETING", "DELETING SEGMENT LEAVING ${segments.size}")
        }
        else {
            val repeatEndIndex = getIndexFromID(segments[index].ID, index + 1)

            for (i in index..repeatEndIndex)
            {
                segments[i].nest--
            }
            segments.removeAt(index)
            segments.removeAt(repeatEndIndex - 1)
            edited = true
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
                if (segments[index].type.contains("Repeat"))
                    moveRepeatbyIndex(increment, index) else moveSegmentByIndex(increment, index)
            edited = true
            return movement
        }
        else {
            return 0
        }

    }
    fun moveSegmentByIndex(increment: Int = 0, index: Int): Int
    {
        val nextElement = segments[index + increment]

        if (nextElement.type == "RepeatEnd") {

            segments[index].nest -= increment
        }
        else if (nextElement.type == "RepeatStart"){
            segments[index].nest += increment
        }

        val temp = segments[index]
        segments[index] = segments[index + increment]
        segments[index + increment] = temp
        edited = true

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

        if ((currentElement.type == "RepeatEnd" && nextElement.type == "RepeatStart" && currentElement.ID == nextElement.ID && movement == -1) ||
            (currentElement.type == "RepeatStart" && nextElement.type == "RepeatEnd" && currentElement.ID == nextElement.ID && movement == 1) ||
            (currentElement.type == "RepeatStart" && nextElement.type == "RepeatStart" && movement == -1) ||
            (currentElement.type == "RepeatEnd" && nextElement.type == "RepeatStart" && movement == -1) ||
            (currentElement.type == "RepeatEnd" && nextElement.type == "RepeatEnd")){
            // A RepeatStart can't go past its own RepeatEnd
            // A RepeatEnd can't got past its own RepeatStart
            // A RepeatStart can't go up past another RepeatStart - swapping of repeats like this seems like it could get messy, just edit them
            // A RepeatEnd can't go up or down past another RepeatEnd
            // Do nothing
            allowMovement = false
        }
        else if (currentElement.type == "RepeatStart" && nextElement.type == "RepeatEnd" && currentElement.ID != nextElement.ID){
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
        else if (currentElement.type == "RepeatStart" && nextElement.type == "RepeatStart" && currentElement.ID != nextElement.ID){
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
            if (currentElement.type == "RepeatStart") {
                segments[index + increment].nest -= increment
            } else if (currentElement.type == "RepeatEnd") {
                segments[index + increment].nest += increment
            }
            addIndex = if (movement > 0) index + movement + 1 else index + movement
            removeIndex = if (movement<0) index  + 1 else index
        }
        if (allowMovement) {

            segments.add(addIndex, segments[index])
            segments.removeAt(removeIndex)
            edited = true
            return movement

        }
        else
            return 0

    }
    fun getSegmentFromIndex(index: Int): Segment
    {
        return segments.getOrNull(index) ?: Segment("Empty", -1, 0, false, 0)
    }

    fun getIndexFromID(id: Int, start: Int = 0): Int {
        if (segments.isEmpty()) return -1
        for (n in start until segments.size) {
            if (segments[n].ID == id)
                return n
        }
        return -1
    }

    fun flattenWorkout(start: Int = 0, end:Int = segments.size): MutableList<Segment>
    {
        val flattenedWorkout = mutableListOf<Segment>()
        var currentIndex = start
        while (currentIndex < end)
        {
            if(segments[currentIndex].type == "RepeatStart")
            {
                val start = currentIndex + 1
                val end = getIndexFromID(segments[currentIndex].ID,currentIndex+1)
                val repeats = segments[currentIndex].repeat

                for (i in 0 until repeats) {
                    flattenedWorkout.addAll(flattenWorkout(start,end))
                }


                currentIndex = end + 1

            }
            else
            {
                flattenedWorkout.add(segments[currentIndex])
                currentIndex++
            }
        }

        return flattenedWorkout
    }

}

class WorkoutList (val workouts: MutableList<Workout>)
{

    fun getIndex(name: String): Int
    {
       // workouts.forEachIndexed { index, workout -> if (workout.name == name) return index }
        return workouts.indexOfFirst { it.name == name }

    }

    fun getAllNames(): MutableList<String>
    {
        val nameList = mutableListOf<String>()
        for (workout in workouts)
        {
            nameList.add(workout.name)
        }
        return nameList
    }

//TODO - deal with name changes
    fun loadWorkoutList( context: android.content.Context, fileName: String = "all_workouts.json",)
    {
        workouts.clear()
        val file = File(context.filesDir, fileName)
        if (file.exists()) {
            try {
                val json = Json { ignoreUnknownKeys = true }
                val loadedWorkouts = json.decodeFromString<List<Workout>>(file.readText())
                workouts.addAll(loadedWorkouts)
            } catch (e: Exception) {
            }
        }
    }
    fun updateWorkout (updatedWorkout: Workout) {

        val index = getIndex(updatedWorkout.name)
        updatedWorkout.edited = false
        Log.d("TESTINGHERE", "returned index is $index")
        if (index != -1)

        {
            Log.d("TESTINGHERE", "Updating Workout ${updatedWorkout.name}")
            workouts[index] = updatedWorkout}
        else
        {
            Log.d("TESTINGHERE","New workout: ${updatedWorkout.name}")
            workouts.add(updatedWorkout)
        }

        Log.d("TESTINGHERE", "Workouts is ${workouts.size} big")

    }

    fun saveWorkoutList(context: android.content.Context,fileName: String = "all_workouts.json")
    {
        Log.d("TESTINGHERE","Saving ${workouts.size} workouts")
        val json = Json { prettyPrint = true }
        val file = File(context.filesDir, fileName)
        file.writeText(json.encodeToString(workouts))
    }
    fun deleteWorkout(name: String)
    {
        workouts.removeAt(getIndex(name))
    }


}