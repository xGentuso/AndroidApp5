package com.trios2025dj.superpodcast.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.trios2025dj.superpodcast.R
import com.trios2025dj.superpodcast.data.PodcastDatabase
import com.trios2025dj.superpodcast.ui.PodcastAdapter

class SubscriptionsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateTextView: TextView
    private lateinit var adapter: PodcastAdapter
    private lateinit var database: PodcastDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_subscriptions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize database
        database = PodcastDatabase(requireContext())

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerViewSubscriptions)
        emptyStateTextView = view.findViewById(R.id.textViewEmptyState)

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = PodcastAdapter(emptyList())
        recyclerView.adapter = adapter

        // Load subscriptions
        loadSubscriptions()
    }

    override fun onResume() {
        super.onResume()
        // Refresh subscriptions when returning to this fragment
        loadSubscriptions()
    }

    private fun loadSubscriptions() {
        val subscriptions = database.getAllSubscriptions()
        
        if (subscriptions.isEmpty()) {
            // Show empty state
            recyclerView.visibility = View.GONE
            emptyStateTextView.visibility = View.VISIBLE
            emptyStateTextView.text = "You haven't subscribed to any podcasts yet"
        } else {
            // Show subscriptions
            recyclerView.visibility = View.VISIBLE
            emptyStateTextView.visibility = View.GONE
            adapter.updateList(subscriptions)
        }
    }
}
