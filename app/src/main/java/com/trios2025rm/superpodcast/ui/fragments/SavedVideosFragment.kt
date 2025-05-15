package com.trios2025rm.superpodcast.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.trios2025rm.superpodcast.R
import com.trios2025rm.superpodcast.data.SavedVideo
import com.trios2025rm.superpodcast.data.VideoPreferences
import com.trios2025rm.superpodcast.ui.EpisodeDetailActivity
import com.trios2025rm.superpodcast.ui.adapters.SavedVideosAdapter

/**
 * Fragment to display saved videos
 */
class SavedVideosFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var adapter: SavedVideosAdapter
    private lateinit var videoPrefs: VideoPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_saved_videos, container, false)
        
        // Initialize VideoPreferences
        videoPrefs = VideoPreferences(requireContext())
        
        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerViewSavedVideos)
        emptyView = view.findViewById(R.id.textViewNoSavedVideos)
        
        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = SavedVideosAdapter(emptyList()) { savedVideo ->
            // Handle click on saved video - open in EpisodeDetailActivity
            val intent = Intent(requireContext(), EpisodeDetailActivity::class.java).apply {
                putExtra("title", savedVideo.title)
                putExtra("description", savedVideo.description)
                putExtra("hasVideo", true)
                putExtra("videoUrl", savedVideo.videoUrl)
                putExtra("artist", savedVideo.podcastName)
                putExtra("pubDate", savedVideo.getFormattedDate())
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter
        
        return view
    }
    
    override fun onResume() {
        super.onResume()
        loadSavedVideos()
    }
    
    private fun loadSavedVideos() {
        // Get all saved videos from SharedPreferences
        val savedVideos = videoPrefs.getAllSavedVideos()
        
        // Update UI based on whether there are saved videos
        if (savedVideos.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
            adapter.updateData(savedVideos)
        }
    }
}
