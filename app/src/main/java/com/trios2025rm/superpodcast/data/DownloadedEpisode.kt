package com.trios2025rm.superpodcast.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DownloadedEpisode(
    val title: String,
    val filePath: String,
    val fileSize: String,
    val isVideo: Boolean = false,
    val dateDownloaded: Long
) {
    fun getFormattedDate(): String {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return dateFormat.format(Date(dateDownloaded))
    }
}
