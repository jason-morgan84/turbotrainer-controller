package com.example.controller.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils.colorToHSL

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Controller App Colors
val ColourBackground = Color(0xfff5f9f8)
val ColourPlus1 = Color(0xff715fff)
val ColourPlus5 = Color(0xff835fff)
val ColourPlus10 = Color(0xff965fff)
val ColourMiddle = Color(0xff5f5fff)
val ColourMinus1 = Color(0xff7777e7)
val ColourMinus5 = Color(0xff8787d7)
val ColourMinus10 = Color(0xff9797c7)
val ColourButtons = Color(red = 200, green = 200, blue = 200)

fun adjustColour (colour: Color, hue: Float = 0f, saturation: Float = 0f, lightness: Float = 0f): Color {
    val hsl = FloatArray(3)
    colorToHSL(colour.toArgb(), hsl)

    val newHue = (hsl[0] + hue).coerceIn(0.0f, 360.0f)
    val newSaturation = (hsl[1] + saturation).coerceIn(0.0f, 1.0f)
    val newLightness = (hsl[2] + lightness).coerceIn(0.0f, 1.0f)

    return Color.hsl(newHue,newSaturation, newLightness)
}