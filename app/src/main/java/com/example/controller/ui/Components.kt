package com.example.controller.ui

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
//import com.example.controller.ui.theme.NunitoFontFamily
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

import androidx.compose.ui.draw.drawWithCache


import android.graphics.CornerPathEffect

import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb


@Composable
fun Label(
    value: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: androidx.compose.ui.text.font.FontWeight? = null,
    color: Color = Color.Unspecified
) {
    Text(
        text = value,
        modifier = modifier,
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = color
    )
}

@Composable
fun MyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Click Me",
    roundCorners: Dp = 4.dp,
    width: Dp? = null,
    backgroundColor: Color? = null,
    textColor: Color? = null
) {
    Button(
        onClick = onClick,
        modifier = if (width != null) modifier.width(width) else modifier,
        shape = RoundedCornerShape(roundCorners),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor ?: ButtonDefaults.buttonColors().containerColor,
            contentColor = textColor ?: ButtonDefaults.buttonColors().contentColor
        )
    ) {
        Text(text = label)
    }
}

data class AlertDefinitions (val title: String = "Alert",
                        val text: String = "Be Alert",
                        val confirmText: String = "OK",
                        val dismissText: String = "cancel",
                        val onConfirm: () -> Unit = {},
                        val onDismiss: () -> Unit = {}) {
    @Composable
    fun AlertPopup(onClose: () -> Unit) {
        AlertDialog(
            onDismissRequest = { onClose() },
            title = { Text(title) },
            text = { Text(text)},
            confirmButton = {
                TextButton(onClick = {
                    onConfirm()
                    onClose()
                })
                { Text(confirmText) }
            },

            dismissButton = {
                if (dismissText != "") {
                    TextButton(onClick = {
                        onDismiss()
                        onClose()
                    })
                    { Text(dismissText) }
                }
            }
        )
    }
}

class DynamicSlopedShape(
    private val leftHeight: Dp,
    private val rightHeight: Dp,
    private val cornerRadius: Dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        with(density) {
            val leftPx = leftHeight.toPx()
            val rightPx = rightHeight.toPx()
            val radius = cornerRadius.toPx()

            val path = Path().apply {
                // Top-left starting point (adjusted for radius offset)
                moveTo(x = 0f, y = size.height - leftPx + radius)

                // Line toward top-right, stopping just before the corner radius
                lineTo(x = size.width - radius, y = size.height - rightPx)
                // Round top-right corner
                quadraticTo(
                    x1 = size.width, y1 = size.height - rightPx,
                    x2 = size.width, y2 = size.height - rightPx + radius
                )

                // Line down toward bottom-right
                lineTo(x = size.width, y = size.height - radius)
                // Round bottom-right corner
                quadraticTo(
                    x1 = size.width, y1 = size.height,
                    x2 = size.width - radius, y2 = size.height
                )

                // Line across the flat bottom toward bottom-left
                lineTo(x = radius, y = size.height)
                // Round bottom-left corner
                quadraticTo(
                    x1 = 0f, y1 = size.height,
                    x2 = 0f, y2 = size.height - radius
                )

                // Line up toward top-left
                lineTo(x = 0f, y = size.height - leftPx + radius)

                close()
            }
            return Outline.Generic(path)
        }
    }
}

fun Modifier.slopedRoundedBackground(
    leftHeight: Dp,
    rightHeight: Dp,
    cornerRadius: Dp,
    backgroundColor: Color
) = this.drawWithCache {
    val leftHeightPx = leftHeight.toPx()
    val rightHeightPx = rightHeight.toPx()
    val radiusPx = cornerRadius.toPx()

    // 1. Create a standard Jetpack Compose Path
    val composePath = Path().apply {
        moveTo(x = 0f, y = size.height - leftHeightPx)
        lineTo(x = size.width, y = size.height - rightHeightPx)
        lineTo(x = size.width, y = size.height)
        lineTo(x = 0f, y = size.height)
        close()
    }

    // 2. Convert it to a native Android framework Path
    val androidPath = composePath.asAndroidPath()

    // 3. Create native Android Paint objects for clipping and filling
    val fillPaint = android.graphics.Paint().apply {
        color = backgroundColor.toArgb()
        isAntiAlias = true
        style = android.graphics.Paint.Style.FILL
        // This is what smoothly rounds every single corner vertex flawlessly
        pathEffect = CornerPathEffect(radiusPx)
    }

    onDrawWithContent {
        drawIntoCanvas { canvas ->
            val nativeCanvas = canvas.nativeCanvas

            // Save the state of the canvas before applying modifications
            val checkpoint = nativeCanvas.save()

            // Native canvas supports clipping using an Android Path that respects PathEffects
            nativeCanvas.clipPath(androidPath)

            // Draw the background color using our rounded path effect paint
            nativeCanvas.drawPath(androidPath, fillPaint)

            // Render the text/images inside the Box (they will be clipped perfectly)
            drawContent()

            // Restore the canvas to normal
            nativeCanvas.restoreToCount(checkpoint)
        }
    }
}