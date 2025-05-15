package com.trios2025rm.superpodcast.data

data class Episode(
    val title: String,
    val pubDate: String,
    val audioUrl: String,
    val description: String,
    val hasVideo: Boolean = false,
    val videoUrl: String = ""
)
