package com.extremeaudiolab.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Knob(label: String, value: Float, range: ClosedFloatingPointRange<Float>, on: (Float) -> Unit) {
    Column(Modifier.padding(horizontal = 8.dp)) {
        Text("$label: ${"%.2f".format(value)}", style = MaterialTheme.typography.labelSmall)
        Slider(value = value.coerceIn(range), valueRange = range, onValueChange = on)
    }
}
