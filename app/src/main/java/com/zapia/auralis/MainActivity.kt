package com.zapia.auralis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zapia.auralis.data.AudioTrack
import com.zapia.auralis.data.MusicRepository
import com.zapia.auralis.data.UserPrefs
import com.zapia.auralis.playback.MusicPlayerController
import com.zapia.auralis.ui.theme.AuralisTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private lateinit var controller: MusicPlayerController
    private lateinit var repository: MusicRepository
    private lateinit var prefs: UserPrefs
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { if (it) setContentView() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        controller = MusicPlayerController(this)
        repository = MusicRepository(this)
        prefs = UserPrefs(this)
        setContentView()
        if (!hasAudioPermission(this)) permissionLauncher.launch(audioPermission())
    }
    private fun setContentView() { setContent { AuralisApp(controller, repository, prefs) } }
    override fun onStop() { controller.savePlayback(); super.onStop() }
    override fun onDestroy() { controller.release(); super.onDestroy() }
}

@Composable
private fun AuralisApp(controller: MusicPlayerController, repository: MusicRepository, prefs: UserPrefs) {
    var tracks by remember { mutableStateOf(emptyList<AudioTrack>()) }
    var tab by remember { mutableIntStateOf(0) }
    var dark by remember { mutableStateOf(prefs.isDarkTheme()) }
    var selected by remember { mutableStateOf<AudioTrack?>(null) }
    var showEqualizer by remember { mutableStateOf(false) }
    var showSleepTimer by remember { mutableStateOf(false) }
    var sleepRemaining by remember { mutableStateOf<Long?>(null) }
    val playing by controller.isPlaying.collectAsStateWithLifecycle()
    val currentIndex by controller.currentIndex.collectAsStateWithLifecycle()
    val shuffle by controller.shuffle.collectAsStateWithLifecycle()
    val repeat by controller.repeat.collectAsStateWithLifecycle()
    val volume by controller.volume.collectAsStateWithLifecycle()
    var favoriteIds by remember { mutableStateOf(prefs.favoriteIds()) }

    if (tracks.isEmpty() && hasAudioPermission(androidx.compose.ui.platform.LocalContext.current)) {
        tracks = remember { repository.loadTracks() }
        controller.setQueue(tracks)
    }
    if (currentIndex in tracks.indices) selected = tracks[currentIndex]

    LaunchedEffect(playing) {
        while (true) {
            if (playing) controller.savePlayback()
            delay(1000L)
        }
    }
    LaunchedEffect(sleepRemaining) {
        val minutes = sleepRemaining ?: return@LaunchedEffect
        delay(minutes * 60_000L)
        controller.stop()
        sleepRemaining = null
    }

    AuralisTheme(darkTheme = dark) {
        Scaffold(bottomBar = {
            NavigationBar {
                NavigationBarItem(tab == 0, { tab = 0 }, icon = { Icon(Icons.Default.LibraryMusic, null) }, label = { Text("Biblioteca") })
                NavigationBarItem(tab == 1, { tab = 1 }, icon = { Icon(Icons.Default.MusicNote, null) }, label = { Text("Ahora") })
                NavigationBarItem(tab == 2, { tab = 2 }, icon = { Icon(Icons.Default.Settings, null) }, label = { Text("Ajustes") })
            }
        }) { padding ->
            when (tab) {
                0 -> LibraryScreen(Modifier.padding(padding), tracks, selected, favoriteIds, { track -> controller.playAt(tracks.indexOf(track)); selected = track; tab = 1 }, { id -> favoriteIds = favoriteIds.toMutableSet().also { if (!it.add(id)) it.remove(id) }; prefs.toggleFavorite(id) })
                1 -> NowPlayingScreen(Modifier.padding(padding).fillMaxSize(), selected, playing, controller.progressMs(), controller, shuffle, repeat, volume, selected?.id in favoriteIds, { selected?.let { favoriteIds = favoriteIds.toMutableSet().also { set -> if (!set.add(it.id)) set.remove(it.id) }; prefs.toggleFavorite(it.id) } }, { showEqualizer = true })
                else -> SettingsScreen(Modifier.padding(padding).fillMaxSize(), tracks.size, dark, { dark = it; prefs.setDarkTheme(it) }, prefs.playlistNames(), prefs::createPlaylist, sleepRemaining, { sleepRemaining = it }, { showEqualizer = true }, { showSleepTimer = true })
            }
        }
        if (showEqualizer) EqualizerDialog(controller) { showEqualizer = false }
        if (showSleepTimer) SleepTimerDialog(sleepRemaining, { sleepRemaining = it; showSleepTimer = false }, { sleepRemaining = null; showSleepTimer = false })
    }
}

@Composable
private fun LibraryScreen(modifier: Modifier, tracks: List<AudioTrack>, selected: AudioTrack?, favorites: Set<Long>, play: (AudioTrack) -> Unit, toggleFavorite: (Long) -> Unit) {
    Column(modifier.padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(18.dp)); Text("Auralis", style = MaterialTheme.typography.headlineMedium); Text("Tu música, sin anuncios", color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(14.dp)); Text("${tracks.size} canciones", style = MaterialTheme.typography.titleMedium); Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) { itemsIndexed(tracks) { _, track ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) { AlbumArt(track, 52.dp); Column(Modifier.weight(1f).padding(horizontal = 12.dp)) { Text(track.title, maxLines = 1); Text(track.artist, maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }; IconButton(onClick = { toggleFavorite(track.id) }) { Icon(if (track.id in favorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null) }; IconButton(onClick = { play(track) }) { Icon(Icons.Default.PlayArrow, null) } }
            }
        } }
    }
}

@Composable
fun AlbumArt(track: AudioTrack, size: Dp, modifier: Modifier = Modifier) {
    coil.compose.AsyncImage(model = track.albumArtUri, contentDescription = "Carátula de ${track.album}", modifier = modifier.size(size).clip(RoundedCornerShape(18.dp)), contentScale = androidx.compose.ui.layout.ContentScale.Crop, placeholder = androidx.compose.ui.graphics.painter.ColorPainter(MaterialTheme.colorScheme.surfaceVariant), error = androidx.compose.ui.graphics.painter.ColorPainter(MaterialTheme.colorScheme.surfaceVariant))
}
