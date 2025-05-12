package com.trios2025dj.superpodcast.ui

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.material.appbar.MaterialToolbar
import com.trios2025dj.superpodcast.R
import android.content.pm.ActivityInfo
import android.view.View

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
        val downloadButton = findViewById<Button>(R.id.buttonDownload)
        val shareButton = findViewById<Button>(R.id.buttonShare)
        
        // Get media URL from intent
        val audioUrl = intent.getStringExtra("audioUrl") ?: ""
        val videoUrl = intent.getStringExtra("videoUrl") ?: ""
        val title = intent.getStringExtra("title") ?: "episode"
        
        // Configure download button
        downloadButton?.setOnClickListener {
            // Always use as download button regardless of content type
            val urlToDownload = if (isVideoContent && videoUrl.isNotEmpty()) videoUrl else audioUrl
            if (urlToDownload.isNotEmpty()) {
                downloadMedia(urlToDownload, title)
            } else {
                Toast.makeText(this, "No media URL available for download", Toast.LENGTH_SHORT).show()
            }
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
        } else {
            // Enter fullscreen mode
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            isFullscreen = true
        }
    }
    
    private val STORAGE_PERMISSION_CODE = 100
    private var pendingDownloadUrl: String? = null
    private var pendingDownloadTitle: String? = null
    
    private fun downloadMedia(url: String, title: String) {
        // Save the URL and title in case we need to request permissions
        pendingDownloadUrl = url
        pendingDownloadTitle = title
        
        // Check if we have storage permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10 and above, we don't need explicit storage permission
            startDownload(url, title)
        } else {
            // For older versions, check and request storage permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                == PackageManager.PERMISSION_GRANTED) {
                // We have permission, proceed with download
                startDownload(url, title)
            } else {
                // Request permission
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_CODE
                )
                
                // Inform user
                Toast.makeText(this, "Storage permission needed to download files", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun startDownload(url: String, title: String) {
        try {
            // Create a download manager instance
            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            
            // Determine file extension based on URL or content type
            val fileExtension = when {
                url.contains(".mp3") -> ".mp3"
                url.contains(".mp4") -> ".mp4"
                url.contains(".m4a") -> ".m4a"
                else -> ".media"
            }
            
            // Sanitize the title to create a valid filename
            val sanitizedTitle = title.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
            val filename = "SuperPodcast_${sanitizedTitle}$fileExtension"
            
            // Create download request with proper settings
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle("Downloading $title")
                .setDescription("Downloading podcast episode")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
            
            // Add a MIME type based on the file extension
            when (fileExtension) {
                ".mp3" -> request.setMimeType("audio/mpeg")
                ".mp4" -> request.setMimeType("video/mp4")
                ".m4a" -> request.setMimeType("audio/m4a")
                else -> request.setMimeType("audio/mpeg")
            }
            
            // Enqueue the download and get the download ID
            val downloadId = downloadManager.enqueue(request)
            
            // Log the download details for debugging
            Log.d("EpisodeDetailActivity", "Download started: $filename from URL: $url")
            
            // Show success message to user
            Toast.makeText(this, "Download started. Check Downloads tab when complete.", Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            // Handle any errors
            Log.e("EpisodeDetailActivity", "Download failed: ${e.message}", e)
            Toast.makeText(this, "Download failed: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with download
                pendingDownloadUrl?.let { url ->
                    pendingDownloadTitle?.let { title ->
                        startDownload(url, title)
                    }
                }
            } else {
                // Permission denied
                Toast.makeText(this, "Storage permission denied. Cannot download files.", Toast.LENGTH_LONG).show()
            }
        }
    }
}