package com.zapia.auralis

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SleepTimerDialog(remaining: Long?, setTimer: (Long?) -> Unit, cancelTimer: () -> Unit) {
    var selected by remember(remaining) { mutableIntStateOf((remaining ?: 30L).toInt().coerceIn(5, 120)) }
    AlertDialog(
        onDismissRequest = cancelTimer,
        title = { Text("Temporizador", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                Text(if (remaining == null) "La música se detendrá automáticamente después del tiempo elegido." else "Temporizador activo: $remaining minutos aproximadamente.")
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(5, 15, 30, 60).forEach { minutes ->
                        Button(onClick = { selected = minutes }) { Text("${minutes}m") }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("Seleccionado: $selected minutos", style = MaterialTheme.typography.bodyMedium)
            }
        },
        confirmButton = { TextButton(onClick = { setTimer(selected.toLong()) }) { Text("Activar") } },
        dismissButton = { TextButton(onClick = cancelTimer) { Text("Cancelar") } }
    )
}
