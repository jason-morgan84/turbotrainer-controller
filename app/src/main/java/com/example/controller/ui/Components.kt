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

@Composable
fun Label(
    value: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    //fontFamily: FontFamily = NunitoFontFamily
) {
    Text(
        text = value,
        modifier = modifier,
        fontSize = fontSize,
        //fontFamily = fontFamily
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

class AlertDefinitions (val title: String = "Alert",
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

                TextButton({
                    onDismiss()
                    onClose()
                })
                { Text(dismissText) }
            }
        )
    }
}