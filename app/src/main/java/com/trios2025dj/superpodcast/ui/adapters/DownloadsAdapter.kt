package com.trios2025dj.superpodcast.ui.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.trios2025dj.superpodcast.R
import com.trios2025dj.superpodcast.data.DownloadedEpisode
import com.trios2025dj.superpodcast.ui.EpisodeDetailActivity
import java.io.File

class DownloadsAdapter(private var items: List<DownloadedEpisode>) :
    RecyclerView.Adapter<DownloadsAdapter.DownloadViewHolder>() {

    fun updateList(newItems: List<DownloadedEpisode>) {
        val diffCallback = DownloadsDiffCallback(items, newItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        
        items = newItems
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_download, parent, false)
        return DownloadViewHolder(view)
    }

    override fun onBindViewHolder(holder: DownloadViewHolder, position: Int) {
        if (position < items.size) {
            holder.bind(items[position])
        }
    }

    override fun getItemCount() = items.size

    class DownloadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textTitle: TextView = itemView.findViewById(R.id.textViewTitle)
        private val textInfo: TextView = itemView.findViewById(R.id.textViewInfo)
        private val imageType: ImageView = itemView.findViewById(R.id.imageViewType)
        private val imagePlay: ImageView = itemView.findViewById(R.id.imageViewPlay)
        private val imageDelete: ImageView = itemView.findViewById(R.id.imageViewDelete)

        fun bind(episode: DownloadedEpisode) {
            textTitle.text = episode.title
            textInfo.text = "${episode.fileSize} • ${episode.getFormattedDate()}"
            
            // Set the appropriate icon based on file type
            imageType.setImageResource(
                if (episode.isVideo) android.R.drawable.ic_media_play
                else android.R.drawable.ic_lock_silent_mode_off
            )

            // Play button click listener
            imagePlay.setOnClickListener {
                val context = itemView.context
                val file = File(episode.filePath)
                
                if (file.exists()) {
                    if (episode.isVideo) {
                        // For video files, open in the EpisodeDetailActivity
                        val intent = Intent(context, EpisodeDetailActivity::class.java).apply {
                            putExtra("title", episode.title)
                            putExtra("audioUrl", episode.filePath)
                            putExtra("hasVideo", true)
                            putExtra("videoUrl", episode.filePath)
                            putExtra("description", "Downloaded episode")
                            putExtra("pubDate", episode.getFormattedDate())
                        }
                        context.startActivity(intent)
                    } else {
                        // For audio files, open in the default media player
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(Uri.fromFile(file), "audio/*")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        
                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                        }
                    }
                }
            }

            // Delete button click listener
            imageDelete.setOnClickListener {
                val file = File(episode.filePath)
                if (file.exists() && file.delete()) {
                    // File deleted successfully
                    // In a real app, you would update the adapter here
                }
            }
        }
    }
    
    // DiffUtil implementation for efficient updates
    private class DownloadsDiffCallback(
        private val oldList: List<DownloadedEpisode>,
        private val newList: List<DownloadedEpisode>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size
        
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].filePath == newList[newItemPosition].filePath
        }
        
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return oldItem.title == newItem.title && 
                   oldItem.fileSize == newItem.fileSize &&
                   oldItem.dateDownloaded == newItem.dateDownloaded
        }
    }
}
