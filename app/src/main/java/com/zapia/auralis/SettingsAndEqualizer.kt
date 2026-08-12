package com.zapia.auralis

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.zapia.auralis.playback.MusicPlayerController
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(modifier: Modifier, songCount: Int, darkTheme: Boolean, onThemeChange: (Boolean) -> Unit, playlists: List<String>, createPlaylist: (String) -> Unit, sleepRemaining: Long?, setSleepTimer: (Long?) -> Unit, openEq: () -> Unit, openSleep: () -> Unit) {
    var newPlaylist by remember { mutableStateOf("") }
    Column(modifier.padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(12.dp)); Text("Ajustes", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Spacer(Modifier.height(16.dp))
        SettingCard(Icons.Default.GraphicEq, "Ecualizador", "Personaliza graves, medios y agudos", openEq)
        SettingCard(Icons.Default.Timer, "Temporizador", if (sleepRemaining == null) "Detener la música automáticamente" else "Se detiene en aproximadamente $sleepRemaining min.", openSleep)
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(16.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.PlaylistPlay, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(14.dp)); Column { Text("Playlists", fontWeight = FontWeight.SemiBold); Text("Organiza tu música", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }; Spacer(Modifier.height(10.dp)); LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) { itemsIndexed(playlists) { _, name -> FilterChip(true, {}, label = { Text(name) }) } }; Spacer(Modifier.height(8.dp)); Row(verticalAlignment = Alignment.CenterVertically) { androidx.compose.material3.OutlinedTextField(newPlaylist, { newPlaylist = it }, label = { Text("Nueva playlist") }, singleLine = true, modifier = Modifier.weight(1f)); TextButton(onClick = { if (newPlaylist.isNotBlank()) { createPlaylist(newPlaylist); newPlaylist = "" } }) { Text("Crear") } } }
        }
        SettingCard(Icons.Default.MusicNote, "Formatos compatibles", "MP3 · WAV · FLAC · M4A · AAC · OGG", {})
        SettingCard(Icons.Default.CheckCircle, "Biblioteca", "$songCount canciones detectadas en este dispositivo", {})
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(if (darkTheme) Icons.Default.Nightlight else Icons.Default.LightMode, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(14.dp)); Column(Modifier.weight(1f)) { Text("Tema de la aplicación", fontWeight = FontWeight.SemiBold); Text(if (darkTheme) "Modo oscuro" else "Modo claro", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; androidx.compose.material3.Switch(checked = darkTheme, onCheckedChange = onThemeChange) } }
        Spacer(Modifier.height(20.dp)); Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Auralis 3.0 · Reproductor local sin anuncios", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
private fun SettingCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit) { Card(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(14.dp)); Column { Text(title, fontWeight = FontWeight.SemiBold); Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } } } }

@Composable
fun EqualizerDialog(controller: MusicPlayerController, close: () -> Unit) {
    val count = controller.equalizerBandCount(); val range = controller.equalizerBandRange(); val min = range.getOrElse(0) { -1500 }; val max = range.getOrElse(1) { 1500 }
    var values by remember(controller, count) { mutableStateOf(List(count) { ((controller.bandLevel(it).toInt() - min).toFloat() / (max - min).coerceAtLeast(1)).coerceIn(0f, 1f) }) }
    val presets = remember(controller) { controller.presetNames().take(12) }
    AlertDialog(onDismissRequest = close, properties = DialogProperties(usePlatformDefaultWidth = false), containerColor = MaterialTheme.colorScheme.surface, title = { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.GraphicEq, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(10.dp)); Text("Ecualizador", fontWeight = FontWeight.Bold) } }, text = { Column { Text(if (controller.hasEqualizer()) "Ajuste fino para tu sesión" else "Se activará al reproducir una canción", color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(14.dp)); if (presets.isNotEmpty()) { Text("Preajustes", fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(7.dp)); LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) { itemsIndexed(presets) { index, name -> FilterChip(false, { controller.usePreset(index) }, label = { Text(name) }) } }; Spacer(Modifier.height(16.dp)) }; Row(Modifier.fillMaxWidth().height(190.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) { values.forEachIndexed { index, value -> Column(horizontalAlignment = Alignment.CenterHorizontally) { Slider(value, { next -> values = values.toMutableList().also { it[index] = next }; controller.setBandLevel(index, (min + next * (max - min)).roundToInt().toShort()) }, modifier = Modifier.size(width = 44.dp, height = 150.dp), valueRange = 0f..1f); Text(bandLabel(index, count), style = MaterialTheme.typography.labelSmall) } } } } }, confirmButton = { TextButton(close) { Text("Listo") } })
}

private fun bandLabel(index: Int, count: Int): String = if (count <= 5) listOf("60", "230", "910", "3.6k", "14k").getOrElse(index) { "Band" } else if (index < 3) "${60 * (index + 1)}" else "${(index + 1) * 2}k"
