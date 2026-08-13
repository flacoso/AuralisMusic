package com.zapia.auralis.playback

import android.content.Context
import android.media.audiofx.Equalizer
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.zapia.auralis.data.AudioTrack
import com.zapia.auralis.data.UserPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MusicPlayerController(context: Context) {
    val player: ExoPlayer = ExoPlayer.Builder(context.applicationContext)
        .setAudioAttributes(AudioAttributes.Builder().setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).setUsage(C.USAGE_MEDIA).build(), true)
        .build()

    private val prefs = UserPrefs(context)
    private val _currentIndex = MutableStateFlow(-1)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    private val _position = MutableStateFlow(0L)
    val position: StateFlow<Long> = _position.asStateFlow()
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()
    private val _shuffle = MutableStateFlow(false)
    val shuffle: StateFlow<Boolean> = _shuffle.asStateFlow()
    private val _repeat = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeat: StateFlow<Int> = _repeat.asStateFlow()
    private var tracks: List<AudioTrack> = emptyList()
    private var equalizer: Equalizer? = null
    private var equalizerSessionId: Int = 0
    private val mediaSession = MediaSession.Builder(context.applicationContext, player).setId("AuralisSession").build()
    private val _volume = MutableStateFlow(1f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) { _isPlaying.value = isPlaying }
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                _currentIndex.value = player.currentMediaItemIndex
                _position.value = player.currentPosition.coerceAtLeast(0)
                _duration.value = player.duration.coerceAtLeast(0)
            }
            override fun onAudioSessionIdChanged(audioSessionId: Int) { attachEqualizer(audioSessionId) }
            override fun onPlaybackStateChanged(playbackState: Int) {
                _position.value = player.currentPosition.coerceAtLeast(0)
                _duration.value = player.duration.coerceAtLeast(0)
            }
        })
        attachEqualizer(player.audioSessionId)
    }

    fun setQueue(items: List<AudioTrack>) {
        tracks = items
        player.setMediaItems(items.map { MediaItem.fromUri(it.uri) })
        player.prepare()
        val savedIndex = prefs.lastTrackIndex().takeIf { it in items.indices } ?: if (items.isEmpty()) -1 else 0
        _currentIndex.value = savedIndex
        if (savedIndex >= 0) {
            val savedPosition = prefs.lastPositionMs()
            player.seekTo(savedIndex, savedPosition.coerceAtMost(600000000L))
            _position.value = savedPosition
        }
        _duration.value = player.duration.coerceAtLeast(0)
    }

    fun playAt(index: Int) {
        if (index !in tracks.indices) return
        if (player.currentMediaItemIndex != index) player.seekToDefaultPosition(index)
        player.play()
        _currentIndex.value = index
    }

    fun togglePlayPause() { if (player.isPlaying) player.pause() else if (player.mediaItemCount > 0) player.play() }
    fun stop() { savePlayback(); player.stop(); _isPlaying.value = false }
    fun next() { savePlayback(); if (player.hasNextMediaItem()) player.seekToNextMediaItem() else player.seekTo(0); player.play() }
    fun previous() { savePlayback(); if (player.currentPosition > 3_000) player.seekTo(0) else if (player.hasPreviousMediaItem()) player.seekToPreviousMediaItem() else player.seekTo(0); player.play() }
    fun seekTo(position: Long) { player.seekTo(position.coerceAtLeast(0)); _position.value = player.currentPosition }
    fun setVolume(value: Float) { _volume.value = value.coerceIn(0f, 1f); player.volume = _volume.value }
    fun progressMs(): Long = player.currentPosition.coerceAtLeast(0)
    fun durationMs(): Long = player.duration.coerceAtLeast(0)
    fun updateProgress() { _position.value = player.currentPosition.coerceAtLeast(0); _duration.value = player.duration.coerceAtLeast(0) }

    fun savePlayback() {
        val index = player.currentMediaItemIndex
        if (index in tracks.indices) prefs.savePlayback(index, player.currentPosition.coerceAtLeast(0))
    }

    fun setShuffle(enabled: Boolean) { _shuffle.value = enabled; player.shuffleModeEnabled = enabled }
    fun cycleRepeat() {
        val next = when (_repeat.value) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        _repeat.value = next; player.repeatMode = next
    }

    private fun attachEqualizer(sessionId: Int) {
        if (sessionId <= 0) return
        if (equalizerSessionId == sessionId && equalizer != null) return
        try { equalizer?.release(); equalizer = Equalizer(0, sessionId).apply { enabled = true }; equalizerSessionId = sessionId }
        catch (_: Throwable) { equalizer = null; equalizerSessionId = 0 }
    }
    fun hasEqualizer(): Boolean = equalizer != null
    fun equalizerBandCount(): Int = equalizer?.numberOfBands?.toInt() ?: 5
    fun equalizerBandRange(): IntArray = equalizer?.bandLevelRange?.map { it.toInt() }?.toIntArray() ?: intArrayOf(-1500, 1500)
    fun bandLevel(band: Int): Short = try { equalizer?.getBandLevel(band.toShort()) ?: 0 } catch (_: Throwable) { 0 }
    fun setBandLevel(band: Int, value: Short) { try { equalizer?.setBandLevel(band.toShort(), value) } catch (_: Throwable) {} }
    fun presetNames(): List<String> = try { equalizer?.let { eq -> (0 until eq.numberOfPresets.toInt()).map { eq.getPresetName(it.toShort()).toString() } } ?: emptyList() } catch (_: Throwable) { emptyList() }
    fun usePreset(index: Int) { try { equalizer?.usePreset(index.toShort()) } catch (_: Throwable) {} }
    fun release() { savePlayback(); equalizer?.release(); equalizer = null; equalizerSessionId = 0; mediaSession.release(); player.release() }
}
