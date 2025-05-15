package com.trios2025dj.superpodcast.ui

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.material.appbar.MaterialToolbar
import com.trios2025dj.superpodcast.R
import com.trios2025dj.superpodcast.data.SavedVideo
import com.trios2025dj.superpodcast.data.VideoPreferences

class EpisodeDetailActivity : AppCompatActivity() {

    private var exoPlayer: ExoPlayer? = null
    private lateinit var playerView: PlayerView

    private var isFullscreen = false
    private var isVideoContent = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_episode_detail)

        // Initialize player view
        playerView = findViewById(R.id.playerView)
        
        // Set player view properties
        playerView.useController = true
        playerView.controllerShowTimeoutMs = 3000   // Auto-hide after 3 sec
        playerView.controllerHideOnTouch = true     // Tapping will toggle controls
        
        // Show the controller
        playerView.showController()

        // Set up toolbar with back button
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Get data from intent
        val title = intent.getStringExtra("title") ?: ""
        val pubDate = intent.getStringExtra("pubDate") ?: ""
        val description = intent.getStringExtra("description") ?: ""
        val audioUrl = intent.getStringExtra("audioUrl") ?: ""
        val hasVideo = intent.getBooleanExtra("hasVideo", false)
        val videoUrl = intent.getStringExtra("videoUrl") ?: ""

        // Set episode info in UI
        findViewById<TextView>(R.id.textViewEpisodeTitle).text = title
        findViewById<TextView>(R.id.textViewEpisodeDate).text = pubDate
        findViewById<TextView>(R.id.textViewEpisodeDescription).text =
            HtmlCompat.fromHtml(description, HtmlCompat.FROM_HTML_MODE_LEGACY)

        // Determine if we have video content
        isVideoContent = hasVideo && videoUrl.isNotEmpty()
        
        // Configure player view based on content type
        if (isVideoContent && videoUrl.isNotEmpty()) {
            // For video content
            configurePlayerForVideo(videoUrl)
        } else if (audioUrl.isNotEmpty()) {
            // For audio-only content
            configurePlayerForAudio(audioUrl)
        }
        
        // Set up buttons
        setupButtons(isVideoContent)
    }
    
    private fun setupButtons(isVideoContent: Boolean) {
        // Get button references
        val saveButton = findViewById<Button>(R.id.buttonDownload) // Reusing the download button for save functionality
        val shareButton = findViewById<Button>(R.id.buttonShare)
        
        // Get media info from intent
        val audioUrl = intent.getStringExtra("audioUrl") ?: ""
        val videoUrl = intent.getStringExtra("videoUrl") ?: ""
        val title = intent.getStringExtra("title") ?: "episode"
        val description = intent.getStringExtra("description") ?: ""
        val podcastName = intent.getStringExtra("artist") ?: ""
        
        // Initialize VideoPreferences
        val videoPrefs = VideoPreferences(this)
        
        // Configure save button - only show for video content
        if (isVideoContent && videoUrl.isNotEmpty()) {
            // Change button text to "Save Video"
            saveButton?.text = if (videoPrefs.isVideoSaved(videoUrl)) "Remove Video" else "Save Video"
            
            // Make button visible
            saveButton?.visibility = View.VISIBLE
            
            // Set click listener
            saveButton?.setOnClickListener {
                if (videoPrefs.isVideoSaved(videoUrl)) {
                    // Video is already saved, so remove it
                    videoPrefs.deleteSavedVideo(videoUrl)
                    saveButton.text = "Save Video"
                    Toast.makeText(this, "Video removed from saved videos", Toast.LENGTH_SHORT).show()
                } else {
                    // Save the video
                    val savedVideo = SavedVideo(
                        title = title,
                        videoUrl = videoUrl,
                        description = description,
                        podcastName = podcastName
                    )
                    
                    videoPrefs.saveVideo(savedVideo)
                    saveButton.text = "Remove Video"
                    Toast.makeText(this, "Video saved successfully", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // For audio content, hide the save button
            saveButton?.visibility = View.GONE
        }
        
        // Configure share button
        shareButton?.setOnClickListener {
            Toast.makeText(this, "Share functionality coming soon", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun configurePlayerForVideo(videoUrl: String) {
        try {
            // Initialize the player
            initializePlayer(videoUrl)
            
            // Set player view properties for video
            val params = playerView.layoutParams
            params.height = 600 // Fixed height in pixels for video
            playerView.layoutParams = params
            playerView.requestLayout()
            
            // Add a fullscreen button to the layout
            addFullscreenButton()
            
            // Update UI to indicate video content
            findViewById<TextView>(R.id.textViewDescriptionLabel)?.text = "Video Description"
        } catch (e: Exception) {
            Toast.makeText(this, "Error setting up video player: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
    
    private fun configurePlayerForAudio(audioUrl: String) {
        try {
            // Initialize the player
            initializePlayer(audioUrl)
            
            // Set player view properties for audio
            val params = playerView.layoutParams
            params.height = 200 // Fixed height in pixels for audio
            playerView.layoutParams = params
            playerView.requestLayout()
            
            // Update UI to indicate audio content
            findViewById<TextView>(R.id.textViewDescriptionLabel)?.text = "Episode Description"
        } catch (e: Exception) {
            Toast.makeText(this, "Error setting up audio player: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun initializePlayer(url: String) {
        try {
            // Create ExoPlayer instance
            val player = ExoPlayer.Builder(this).build()
            
            // Set player to our PlayerView
            playerView.player = player
            
            // Create media item from URL
            val mediaItem = MediaItem.fromUri(Uri.parse(url))
            
            // Set the media item to be played
            player.setMediaItem(mediaItem)
            
            // Prepare the player
            player.prepare()
            
            // Start playback
            player.play()
            
            // Save reference to player
            exoPlayer = player
        } catch (e: Exception) {
            // Handle any errors
            Toast.makeText(this, "Error playing media: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
    
    private fun addFullscreenButton() {
        try {
            // Find the fullscreen button that's already in the layout
            val fullscreenButton = findViewById<ImageButton>(R.id.buttonFullscreen)
            
            // Only show the button for video content
            if (isVideoContent) {
                // Make sure the button is visible
                fullscreenButton?.visibility = View.VISIBLE
                
                // Set click listener - directly enter fullscreen mode on first click
                fullscreenButton?.setOnClickListener {
                    // Force fullscreen state to false to ensure it enters fullscreen on first click
                    isFullscreen = false
                    toggleFullscreen()
                }
                
                Log.d("EpisodeDetailActivity", "Fullscreen button set up successfully")
            } else {
                // Hide the button for audio-only content
                fullscreenButton?.visibility = View.GONE
            }
        } catch (e: Exception) {
            // Log any errors that occur during setup
            Log.e("EpisodeDetailActivity", "Error setting up fullscreen button: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun updateFullscreenButton(fullscreenButton: ImageButton) {
        // Change the icon based on current fullscreen state
        if (isFullscreen) {
            fullscreenButton.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
        } else {
            fullscreenButton.setImageResource(android.R.drawable.ic_menu_view)
        }
    }

    override fun onPause() {
        super.onPause()
        // Pause playback when activity is paused
        exoPlayer?.pause()
    }
    
    override fun onStop() {
        super.onStop()
        // Ensure player is paused when activity is stopped
        exoPlayer?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release player resources when activity is destroyed
        exoPlayer?.release()
        exoPlayer = null
    }
    
    private fun toggleFullscreen() {
        if (isFullscreen) {
            // Exit fullscreen mode
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            isFullscreen = false
            
            // Restore original layout
            setContentView(R.layout.activity_episode_detail)
            
            // Re-initialize player view and other UI elements
            playerView = findViewById(R.id.playerView)
            playerView.player = exoPlayer
            
            // Set player view properties
            playerView.useController = true
            playerView.controllerShowTimeoutMs = 3000
            playerView.controllerHideOnTouch = true
            playerView.showController()
            
            // Set player view size for video
            val params = playerView.layoutParams
            params.height = 600 // Fixed height in pixels for video
            playerView.layoutParams = params
            
            // Re-setup the fullscreen button
            addFullscreenButton()
            
            // Re-setup toolbar
            val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
            toolbar.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
            
            // Restore episode info in UI
            restoreEpisodeInfo()
        } else {
            // Save current playback position
            val currentPosition = exoPlayer?.currentPosition ?: 0
            
            // Enter fullscreen mode with a completely new layout
            setContentView(R.layout.fullscreen_player)
            
            // Get the new player view
            playerView = findViewById(R.id.fullscreenPlayerView)
            playerView.player = exoPlayer
            
            // Set player view properties
            playerView.useController = true
            playerView.controllerShowTimeoutMs = 3000
            playerView.controllerHideOnTouch = true
            
            // Set window flags for true fullscreen
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            
            // Set system UI flags for true fullscreen
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
            
            // Switch to landscape orientation
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            isFullscreen = true
            
            // Add exit fullscreen button
            val exitFullscreenButton = findViewById<ImageButton>(R.id.exitFullscreenButton)
            exitFullscreenButton?.setOnClickListener {
                toggleFullscreen()
            }
            
            // Show the controller
            playerView.showController()
        }
    }
    
    private fun restoreEpisodeInfo() {
        // Get data from intent
        val title = intent.getStringExtra("title") ?: ""
        val pubDate = intent.getStringExtra("pubDate") ?: ""
        val description = intent.getStringExtra("description") ?: ""
        
        // Set episode info in UI
        findViewById<TextView>(R.id.textViewEpisodeTitle)?.text = title
        findViewById<TextView>(R.id.textViewEpisodeDate)?.text = pubDate
        findViewById<TextView>(R.id.textViewEpisodeDescription)?.text =
            HtmlCompat.fromHtml(description, HtmlCompat.FROM_HTML_MODE_LEGACY)
            
        // Update UI to indicate video content if needed
        if (isVideoContent) {
            findViewById<TextView>(R.id.textViewDescriptionLabel)?.text = "Video Description"
        }
    }
}
