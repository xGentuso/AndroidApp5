package com.trios2025dj.superpodcast.ui

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.trios2025dj.superpodcast.R
import com.trios2025dj.superpodcast.data.Podcast

class PodcastAdapter(private var items: List<Podcast>) :
    RecyclerView.Adapter<PodcastAdapter.PodcastViewHolder>() {

    fun updateList(newItems: List<Podcast>) {
        val diffCallback = PodcastDiffCallback(items, newItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        
        items = newItems
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PodcastViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_podcast, parent, false)
        return PodcastViewHolder(view)
    }

    override fun onBindViewHolder(holder: PodcastViewHolder, position: Int) {
        if (position < items.size) {
            holder.bind(items[position])
        }
    }

    override fun getItemCount() = items.size

    class PodcastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageArtwork: ImageView = itemView.findViewById(R.id.imageViewArtwork)
        private val textTitle: TextView = itemView.findViewById(R.id.textViewTitle)
        private val textArtist: TextView = itemView.findViewById(R.id.textViewArtist)

        fun bind(podcast: Podcast) {
            textTitle.text = podcast.collectionName
            textArtist.text = podcast.artistName
            
            // Load image with error handling
            Glide.with(itemView.context)
                .load(podcast.artworkUrl100)
                .apply(RequestOptions()
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground))
                .into(imageArtwork)

            // Handle click with null safety
            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, PodcastDetailActivity::class.java).apply {
                    putExtra("title", podcast.collectionName)
                    putExtra("artist", podcast.artistName)
                    putExtra("artworkUrl", podcast.artworkUrl100)
                    putExtra("feedUrl", podcast.feedUrl)
                }
                context.startActivity(intent)
            }
        }
    }
    
    // DiffUtil implementation for efficient updates
    private class PodcastDiffCallback(private val oldList: List<Podcast>, private val newList: List<Podcast>) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size
        
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].feedUrl == newList[newItemPosition].feedUrl
        }
        
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return oldItem.collectionName == newItem.collectionName && 
                   oldItem.artistName == newItem.artistName && 
                   oldItem.artworkUrl100 == newItem.artworkUrl100
        }
    }
}