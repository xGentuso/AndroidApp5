package com.trios2025dj.superpodcast.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Data class representing a saved video
 */
data class SavedVideo(
    val id: String = UUID.randomUUID().toString(), // Using String ID for better JSON serialization
    val title: String,
    val videoUrl: String,
    val thumbnailUrl: String = "",
    val duration: String = "Unknown",
    val savedDate: Long = System.currentTimeMillis(),
    val description: String = "",
    val podcastName: String = ""
) {
    fun getFormattedDate(): String {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return dateFormat.format(Date(savedDate))
    }
}
