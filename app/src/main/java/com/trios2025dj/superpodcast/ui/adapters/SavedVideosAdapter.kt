package com.trios2025dj.superpodcast.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.trios2025dj.superpodcast.R
import com.trios2025dj.superpodcast.data.SavedVideo

/**
 * Adapter for displaying saved videos in a RecyclerView
 */
class SavedVideosAdapter(
    private var savedVideos: List<SavedVideo>,
    private val onItemClick: (SavedVideo) -> Unit
) : RecyclerView.Adapter<SavedVideosAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.textViewVideoTitle)
        val podcastNameTextView: TextView = view.findViewById(R.id.textViewPodcastName)
        val dateTextView: TextView = view.findViewById(R.id.textViewSavedDate)
        val thumbnailImageView: ImageView = view.findViewById(R.id.imageViewVideoThumbnail)
        val durationTextView: TextView = view.findViewById(R.id.textViewVideoDuration)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_saved_video, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val savedVideo = savedVideos[position]
        
        // Set video details
        holder.titleTextView.text = savedVideo.title
        holder.podcastNameTextView.text = savedVideo.podcastName
        holder.dateTextView.text = savedVideo.getFormattedDate()
        holder.durationTextView.text = savedVideo.duration
        
        // Set thumbnail if available
        if (savedVideo.thumbnailUrl.isNotEmpty()) {
            // You could use a library like Glide or Picasso to load the image
            // For now, just set a default video thumbnail
            holder.thumbnailImageView.setImageResource(R.drawable.ic_launcher_foreground)
        } else {
            holder.thumbnailImageView.setImageResource(R.drawable.ic_launcher_foreground)
        }
        
        // Set click listener
        holder.itemView.setOnClickListener {
            onItemClick(savedVideo)
        }
    }

    override fun getItemCount() = savedVideos.size

    /**
     * Update the adapter data
     */
    fun updateData(newSavedVideos: List<SavedVideo>) {
        this.savedVideos = newSavedVideos
        notifyDataSetChanged()
    }
}
