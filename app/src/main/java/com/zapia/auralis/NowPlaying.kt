package com.zapia.auralis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.zapia.auralis.data.AudioTrack
import com.zapia.auralis.data.asTime
import com.zapia.auralis.playback.MusicPlayerController
import kotlinx.coroutines.delay

@Composable
fun NowPlayingScreen(modifier: Modifier, track: AudioTrack?, playing: Boolean, progress: Long, controller: MusicPlayerController, shuffle: Boolean, repeat: Int, volume: Float, favorite: Boolean, toggleFavorite: () -> Unit, openEq: () -> Unit) {
    if (track == null) {
        Column(modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Text("Elige una canción para empezar", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        return
    }

    var currentPosition by remember(track.id) { mutableLongStateOf(progress.coerceAtLeast(0L)) }
    var dragging by remember { mutableStateOf(false) }
    val duration = controller.durationMs().coerceAtLeast(1L)

    LaunchedEffect(playing, track.id) {
        while (true) {
            if (!dragging) {
                controller.updateProgress()
                currentPosition = controller.progressMs()
            }
            delay(250L)
        }
    }

    val spin = rememberInfiniteTransition(label = "albumSpin").animateFloat(0f, 360f, infiniteRepeatable(tween(12000), RepeatMode.Restart), label = "albumRotation")
    Column(modifier.padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(24.dp))
        AlbumArt(track, 250.dp, Modifier.graphicsLayer { rotationZ = if (playing) spin.value else 0f })
        Spacer(Modifier.height(24.dp))
        Text(track.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(track.artist, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(track.album, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        IconButton(onClick = toggleFavorite) { Icon(if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, "Favorito", tint = if (favorite) Color(0xFFFF7597) else MaterialTheme.colorScheme.onSurfaceVariant) }
        Spacer(Modifier.height(12.dp))

        Slider(
            value = currentPosition.toFloat().coerceIn(0f, duration.toFloat()),
            onValueChange = { value -> dragging = true; currentPosition = value.toLong() },
            onValueChangeFinished = { controller.seekTo(currentPosition); dragging = false },
            valueRange = 0f..duration.toFloat(),
            modifier = Modifier.fillMaxWidth()
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(currentPosition.asTime(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(duration.asTime(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.VolumeUp, "Volumen", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp)); Slider(value = volume, onValueChange = controller::setVolume, modifier = Modifier.weight(1f)) }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = { controller.setShuffle(!shuffle) }) { Icon(Icons.Default.Shuffle, "Aleatorio", tint = if (shuffle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) }
            IconButton(onClick = { controller.previous() }, modifier = Modifier.size(52.dp)) { Icon(Icons.Default.SkipPrevious, "Anterior", modifier = Modifier.size(34.dp)) }
            IconButton(onClick = { controller.togglePlayPause() }, modifier = Modifier.size(70.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary)) { Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, "Reproducir", tint = Color(0xFF142000), modifier = Modifier.size(36.dp)) }
            IconButton(onClick = { controller.next() }, modifier = Modifier.size(52.dp)) { Icon(Icons.Default.SkipNext, "Siguiente", modifier = Modifier.size(34.dp)) }
            IconButton(onClick = { controller.cycleRepeat() }) { Icon(Icons.Default.Repeat, "Repetir", tint = if (repeat != Player.REPEAT_MODE_OFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(onClick = { controller.stop() }) { Icon(Icons.Default.Stop, null); Spacer(Modifier.width(5.dp)); Text("Detener") }
            TextButton(onClick = openEq) { Icon(Icons.Default.GraphicEq, null); Spacer(Modifier.width(5.dp)); Text("Ecualizador") }
        }
        Spacer(Modifier.height(22.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.GraphicEq, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(12.dp)); Column { Text("Sonido Auralis", fontWeight = FontWeight.Bold); Text("Reproduce tus archivos en alta calidad", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        }
    }
}
