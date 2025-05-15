package com.trios2025rm.superpodcast.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 * Helper class to manage saved videos using SharedPreferences
 */
class VideoPreferences(context: Context) {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREF_NAME, Context.MODE_PRIVATE
    )
    private val gson = Gson()
    
    companion object {
        private const val PREF_NAME = "saved_videos_preferences"
        private const val KEY_SAVED_VIDEOS = "saved_videos"
    }
    
    /**
     * Save a video to SharedPreferences
     */
    fun saveVideo(video: SavedVideo) {
        val savedVideos = getAllSavedVideos().toMutableList()
        
        // Check if video with same URL already exists
        val existingIndex = savedVideos.indexOfFirst { it.videoUrl == video.videoUrl }
        if (existingIndex >= 0) {
            // Replace existing video
            savedVideos[existingIndex] = video
        } else {
            // Add new video
            savedVideos.add(video)
        }
        
        // Save updated list
        val editor = sharedPreferences.edit()
        val json = gson.toJson(savedVideos)
        editor.putString(KEY_SAVED_VIDEOS, json)
        editor.apply()
    }
    
    /**
     * Delete a saved video
     */
    fun deleteSavedVideo(videoUrl: String) {
        val savedVideos = getAllSavedVideos().toMutableList()
        
        // Remove video with matching URL
        savedVideos.removeAll { it.videoUrl == videoUrl }
        
        // Save updated list
        val editor = sharedPreferences.edit()
        val json = gson.toJson(savedVideos)
        editor.putString(KEY_SAVED_VIDEOS, json)
        editor.apply()
    }
    
    /**
     * Check if a video is saved
     */
    fun isVideoSaved(videoUrl: String): Boolean {
        return getAllSavedVideos().any { it.videoUrl == videoUrl }
    }
    
    /**
     * Get all saved videos
     */
    fun getAllSavedVideos(): List<SavedVideo> {
        val json = sharedPreferences.getString(KEY_SAVED_VIDEOS, null) ?: return emptyList()
        
        val type: Type = object : TypeToken<List<SavedVideo>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
