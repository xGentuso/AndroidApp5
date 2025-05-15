package com.trios2025rm.superpodcast.ui.fragments

import android.app.DownloadManager
import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.trios2025rm.superpodcast.R
import com.trios2025rm.superpodcast.data.DownloadedEpisode
import com.trios2025rm.superpodcast.ui.adapters.DownloadsAdapter
import java.io.File

class DownloadsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateTextView: TextView
    private lateinit var adapter: DownloadsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_downloads, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerViewDownloads)
        emptyStateTextView = view.findViewById(R.id.textViewEmptyState)

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = DownloadsAdapter(emptyList())
        recyclerView.adapter = adapter

        // Load downloaded episodes
        loadDownloadedEpisodes()
    }

    override fun onResume() {
        super.onResume()
        // Refresh downloads when returning to this fragment
        loadDownloadedEpisodes()
    }

    private fun loadDownloadedEpisodes() {
        // Get downloaded episodes from the Downloads directory
        val downloadedEpisodes = getDownloadedEpisodes()
        
        if (downloadedEpisodes.isEmpty()) {
            // Show empty state
            recyclerView.visibility = View.GONE
            emptyStateTextView.visibility = View.VISIBLE
            emptyStateTextView.text = "You haven't downloaded any episodes yet"
        } else {
            // Show downloads
            recyclerView.visibility = View.VISIBLE
            emptyStateTextView.visibility = View.GONE
            adapter.updateList(downloadedEpisodes)
        }
    }

    private fun getDownloadedEpisodes(): List<DownloadedEpisode> {
        val downloadedEpisodes = mutableListOf<DownloadedEpisode>()
        
        try {
            // Get the Downloads directory
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            Log.d("DownloadsFragment", "Scanning directory: ${downloadsDir.absolutePath}")
            
            // Get all files in the Downloads directory
            val files = downloadsDir.listFiles()
            Log.d("DownloadsFragment", "Total files found: ${files?.size ?: 0}")
            
            // Filter and process podcast files
            files?.filter { file -> 
                file.isFile && file.name.startsWith("SuperPodcast_") && 
                (file.name.endsWith(".mp3") || file.name.endsWith(".mp4") || 
                 file.name.endsWith(".m4a") || file.name.endsWith(".media"))
            }?.forEach { file ->
                Log.d("DownloadsFragment", "Found podcast file: ${file.name}")
                
                val fileName = file.name.removePrefix("SuperPodcast_")
                val title = fileName.substringBeforeLast(".")
                    .replace("_", " ")
                val fileExtension = fileName.substringAfterLast(".")
                val isVideo = fileExtension == "mp4"
                val fileSize = formatFileSize(file.length())
                val filePath = file.absolutePath
                
                downloadedEpisodes.add(
                    DownloadedEpisode(
                        title = title,
                        filePath = filePath,
                        fileSize = fileSize,
                        isVideo = isVideo,
                        dateDownloaded = file.lastModified()
                    )
                )
            }
            
            // Also check for ongoing downloads
            val downloadManager = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val query = DownloadManager.Query()
            val cursor = downloadManager.query(query)
            
            if (cursor.moveToFirst()) {
                do {
                    val titleIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)
                    if (titleIndex != -1) {
                        val title = cursor.getString(titleIndex)
                        if (title != null && title.startsWith("Downloading ")) {
                            val episodeTitle = title.removePrefix("Downloading ")
                            Log.d("DownloadsFragment", "Found ongoing download: $episodeTitle")
                        }
                    }
                } while (cursor.moveToNext())
            }
            cursor.close()
            
            // Sort by download date (most recent first)
            downloadedEpisodes.sortByDescending { it.dateDownloaded }
            
            Log.d("DownloadsFragment", "Total podcast episodes found: ${downloadedEpisodes.size}")
            
        } catch (e: Exception) {
            Log.e("DownloadsFragment", "Error scanning downloads: ${e.message}", e)
            e.printStackTrace()
        }
        
        return downloadedEpisodes
    }
    
    private fun formatFileSize(size: Long): String {
        val kb = size / 1024.0
        val mb = kb / 1024.0
        
        return when {
            mb >= 1 -> String.format("%.2f MB", mb)
            kb >= 1 -> String.format("%.2f KB", kb)
            else -> "$size bytes"
        }
    }
}
