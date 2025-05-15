package com.trios2025rm.superpodcast.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.trios2025rm.superpodcast.R
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.trios2025rm.superpodcast.data.Episode
import com.trios2025rm.superpodcast.data.Podcast
import com.trios2025rm.superpodcast.data.PodcastDatabase
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.net.URL

class PodcastDetailActivity : AppCompatActivity() {

    private lateinit var database: PodcastDatabase
    private lateinit var buttonSubscribe: Button
    private lateinit var currentPodcast: Podcast
    private var isSubscribed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_podcast_detail)

        // Initialize database
        database = PodcastDatabase(this)

        // Set up toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Get views
        val imageArtwork: ImageView = findViewById(R.id.imageViewDetailArtwork)
        val textTitle: TextView = findViewById(R.id.textViewDetailTitle)
        val textArtist: TextView = findViewById(R.id.textViewDetailArtist)
        val textFeedUrl: TextView = findViewById(R.id.textViewDetailFeedUrl)
        buttonSubscribe = findViewById(R.id.buttonSubscribe)

        // Get data from Intent
        val title = intent.getStringExtra("title") ?: ""
        supportActionBar?.title = title
        val artist = intent.getStringExtra("artist") ?: ""
        val artworkUrl = intent.getStringExtra("artworkUrl") ?: ""
        val feedUrl = intent.getStringExtra("feedUrl") ?: ""

        // Create podcast object
        currentPodcast = Podcast(title, artist, artworkUrl, feedUrl)

        // Set data
        textTitle.text = title
        textArtist.text = artist
        textFeedUrl.text = feedUrl

        Glide.with(this)
            .load(artworkUrl)
            .into(imageArtwork)

        // Check subscription status and update button
        checkSubscriptionStatus()

        // Set up subscribe button
        buttonSubscribe.setOnClickListener {
            if (isSubscribed) {
                unsubscribeFromPodcast()
            } else {
                subscribeToPodcast()
            }
        }

        // Set up episodes list
        val recyclerViewEpisodes: RecyclerView = findViewById(R.id.recyclerViewEpisodes)
        recyclerViewEpisodes.layoutManager = LinearLayoutManager(this)

        // Load episodes if feed URL is available
        if (feedUrl.isNotEmpty()) {
            loadEpisodes(feedUrl) { episodes ->
                // Update episode count
                findViewById<TextView>(R.id.textViewEpisodesCount)?.text = "${episodes.size} episodes"
                
                recyclerViewEpisodes.adapter = EpisodeAdapter(episodes) { episode ->
                    val intent = Intent(this, EpisodeDetailActivity::class.java)
                    intent.putExtra("title", episode.title)
                    intent.putExtra("pubDate", episode.pubDate)
                    intent.putExtra("description", episode.description)
                    intent.putExtra("audioUrl", episode.audioUrl)
                    intent.putExtra("hasVideo", episode.hasVideo)
                    intent.putExtra("videoUrl", episode.videoUrl)
                    startActivity(intent)
                }
            }
        }
    }
    
    private fun checkSubscriptionStatus() {
        isSubscribed = database.isSubscribed(currentPodcast.feedUrl)
        updateSubscribeButton()
    }
    
    private fun subscribeToPodcast() {
        database.subscribeToPodcast(currentPodcast)
        isSubscribed = true
        updateSubscribeButton()
        Toast.makeText(this, "Subscribed to ${currentPodcast.collectionName}", Toast.LENGTH_SHORT).show()
    }
    
    private fun unsubscribeFromPodcast() {
        database.unsubscribeFromPodcast(currentPodcast.feedUrl)
        isSubscribed = false
        updateSubscribeButton()
        Toast.makeText(this, "Unsubscribed from ${currentPodcast.collectionName}", Toast.LENGTH_SHORT).show()
    }
    
    private fun updateSubscribeButton() {
        if (isSubscribed) {
            buttonSubscribe.text = "Unsubscribe"
            // Simplify by removing icon setting for now
        } else {
            buttonSubscribe.text = "Subscribe"
            // Simplify by removing icon setting for now
        }
    }

    private fun loadEpisodes(feedUrl: String, callback: (List<Episode>) -> Unit) {
        Thread {
            try {
                val url = URL(feedUrl)
                val connection = url.openConnection()
                val inputStream = connection.getInputStream()

                val episodes = parseEpisodes(inputStream)

                runOnUiThread {
                    callback(episodes)
                }

                inputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    callback(emptyList())
                }
            }
        }.start()
    }


    private fun parseEpisodes(inputStream: InputStream): List<Episode> {
        val episodes = mutableListOf<Episode>()

        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(inputStream, null)

        var eventType = parser.eventType
        var insideItem = false
        var title = ""
        var pubDate = ""
        var audioUrl = ""
        var videoUrl = ""
        var description = ""
        var hasVideo = false

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tagName = parser.name

            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (tagName.equals("item", ignoreCase = true)) {
                        insideItem = true
                    } else if (insideItem) {
                        when {
                            tagName.equals("title", ignoreCase = true) -> title = parser.nextText()
                            tagName.equals("pubDate", ignoreCase = true) -> pubDate = parser.nextText()
                            tagName.equals("enclosure", ignoreCase = true) -> {
                                val type = parser.getAttributeValue(null, "type") ?: ""
                                val url = parser.getAttributeValue(null, "url") ?: ""
                                
                                if (type.startsWith("video/")) {
                                    videoUrl = url
                                    hasVideo = true
                                } else if (type.startsWith("audio/") || audioUrl.isEmpty()) {
                                    audioUrl = url
                                }
                            }
                            tagName.equals("description", ignoreCase = true) -> description = parser.nextText()
                            // Look for media:content tags (common in podcast feeds with video)
                            tagName.equals("media:content", ignoreCase = true) || tagName.endsWith(":content") -> {
                                val medium = parser.getAttributeValue(null, "medium")
                                val type = parser.getAttributeValue(null, "type") ?: ""
                                val url = parser.getAttributeValue(null, "url") ?: ""
                                
                                if (medium == "video" || type.startsWith("video/")) {
                                    videoUrl = url
                                    hasVideo = true
                                }
                            }
                        }
                    }
                }

                XmlPullParser.END_TAG -> {
                    if (tagName.equals("item", ignoreCase = true)) {
                        episodes.add(Episode(title, pubDate, audioUrl, description, hasVideo, videoUrl))
                        // Reset
                        title = ""
                        pubDate = ""
                        audioUrl = ""
                        videoUrl = ""
                        description = ""
                        hasVideo = false
                        insideItem = false
                    }
                }
            }

            eventType = parser.next()
        }

        return episodes
    }

}