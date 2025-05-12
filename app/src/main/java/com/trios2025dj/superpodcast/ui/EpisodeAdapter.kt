package com.trios2025dj.superpodcast.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.trios2025dj.superpodcast.R
import com.trios2025dj.superpodcast.data.Episode

class EpisodeAdapter(
    private val episodes: List<Episode>,
    private val onItemClick: (Episode) -> Unit
) : RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder>() {

    inner class EpisodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewTitle: TextView = itemView.findViewById(R.id.textViewEpisodeTitle)
        val textViewDate: TextView = itemView.findViewById(R.id.textViewEpisodeDate)
        val textViewDescription: TextView = itemView.findViewById(R.id.textViewEpisodeDescription)
        val imageViewVideoIndicator: ImageView? = itemView.findViewById(R.id.imageViewVideoIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_episode, parent, false)
        return EpisodeViewHolder(view)
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        val episode = episodes[position]
        holder.textViewTitle.text = episode.title
        holder.textViewDate.text = episode.pubDate
        holder.textViewDescription.text = HtmlCompat.fromHtml(episode.description, HtmlCompat.FROM_HTML_MODE_LEGACY)
        
        // Show video indicator if episode has video
        holder.imageViewVideoIndicator?.visibility = if (episode.hasVideo) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            onItemClick(episode)
        }
    }

    override fun getItemCount(): Int = episodes.size
}