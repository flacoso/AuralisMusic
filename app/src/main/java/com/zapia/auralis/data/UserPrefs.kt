package com.zapia.auralis.data

import android.content.Context

class UserPrefs(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("auralis_user", Context.MODE_PRIVATE)

    fun isDarkTheme(): Boolean = prefs.getBoolean("dark_theme", true)
    fun setDarkTheme(enabled: Boolean) { prefs.edit().putBoolean("dark_theme", enabled).apply() }
    fun favoriteIds(): Set<Long> = prefs.getStringSet("favorites", emptySet()).orEmpty().mapNotNull { it.toLongOrNull() }.toSet()
    fun toggleFavorite(id: Long): Boolean {
        val next = favoriteIds().toMutableSet()
        val added = next.add(id)
        if (!added) next.remove(id)
        prefs.edit().putStringSet("favorites", next.map(Long::toString).toSet()).apply()
        return added
    }
    fun playlistNames(): List<String> = prefs.getStringSet("playlist_names", setOf("Favoritos")).orEmpty().toList().sorted()
    fun createPlaylist(name: String): Boolean {
        val clean = name.trim().take(32)
        if (clean.isBlank()) return false
        val names = playlistNames().toMutableSet()
        val added = names.add(clean)
        if (added) prefs.edit().putStringSet("playlist_names", names).apply()
        return added
    }
    fun playlistTrackIds(name: String): Set<Long> = prefs.getStringSet("playlist_$name", emptySet()).orEmpty().mapNotNull { it.toLongOrNull() }.toSet()
    fun addToPlaylist(name: String, id: Long) {
        val ids = playlistTrackIds(name).toMutableSet()
        ids.add(id)
        prefs.edit().putStringSet("playlist_$name", ids.map(Long::toString).toSet()).apply()
    }

    fun savePlayback(index: Int, positionMs: Long) {
        prefs.edit().putInt("last_track_index", index).putLong("last_position_ms", positionMs.coerceAtLeast(0L)).apply()
    }
    fun lastTrackIndex(): Int = prefs.getInt("last_track_index", -1)
    fun lastPositionMs(): Long = prefs.getLong("last_position_ms", 0L).coerceAtLeast(0L)
}
