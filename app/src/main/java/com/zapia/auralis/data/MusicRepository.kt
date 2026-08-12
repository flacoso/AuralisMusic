package com.zapia.auralis.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.net.Uri
import java.io.File

class MusicRepository(private val context: Context) {
    fun loadTracks(): List<AudioTrack> {
        val collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.DATA
        )
        val tracks = mutableListOf<AudioTrack>()
        val sort = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
        context.contentResolver.query(
            collection, projection, "${MediaStore.Audio.Media.IS_MUSIC} != 0", null, sort
        )?.use { cursor ->
            val id = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val title = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artist = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val album = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val duration = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumId = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val mime = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)
            val path = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            while (cursor.moveToNext()) {
                val trackId = cursor.getLong(id)
                tracks += AudioTrack(
                    trackId,
                    ContentUris.withAppendedId(collection, trackId),
                    cursor.getString(title)?.takeIf { it.isNotBlank() } ?: "Sin título",
                    cursor.getString(artist)?.takeIf { it.isNotBlank() } ?: "Artista desconocido",
                    cursor.getString(album)?.takeIf { it.isNotBlank() } ?: "Álbum desconocido",
                    cursor.getLong(duration), cursor.getLong(albumId),
                    cursor.getLong(albumId).takeIf { it > 0 }?.let { Uri.parse("content://media/external/audio/albumart/$it") },
                    if (path >= 0) File(cursor.getString(path).orEmpty()).parentFile?.name ?: "Música" else "Música",
                    if (mime >= 0) cursor.getString(mime) else null
                )
            }
        }
        return tracks
    }
}
