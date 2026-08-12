package com.zapia.auralis

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun FolderDialog(folders: List<String>, selected: String?, choose: (String) -> Unit, clear: () -> Unit) {
    AlertDialog(
        onDismissRequest = clear,
        title = { Text("Reproducir por carpeta") },
        text = { Column {
            TextButton(onClick = clear, modifier = Modifier.fillMaxWidth()) { Text("Todas las canciones") }
            folders.forEach { folder -> TextButton(onClick = { choose(folder) }, modifier = Modifier.fillMaxWidth()) { Text(if (folder == selected) "✓ $folder" else folder) } }
        } },
        confirmButton = { TextButton(onClick = clear) { Text("Cerrar") } }
    )
}
