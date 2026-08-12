package com.zapia.auralis.data

import android.net.Uri

data class AudioTrack(
    val id: Long,
    val uri: Uri,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val albumId: Long,
    val albumArtUri: Uri?,
    val folder: String,
    val mimeType: String?
)

fun Long.asTime(): String {
    val total = this / 1000
    return "%d:%02d".format(total / 60, total % 60)
}
