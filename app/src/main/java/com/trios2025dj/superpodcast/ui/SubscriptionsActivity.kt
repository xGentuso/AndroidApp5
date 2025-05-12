package com.trios2025dj.superpodcast.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.trios2025dj.superpodcast.R
import com.trios2025dj.superpodcast.data.PodcastDatabase

class SubscriptionsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PodcastAdapter
    private lateinit var emptyStateTextView: TextView
    private lateinit var database: PodcastDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscriptions)

        // Initialize database
        database = PodcastDatabase(this)

        // Set up toolbar
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewSubscriptions)
        emptyStateTextView = findViewById(R.id.textViewEmptyState)

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PodcastAdapter(emptyList())
        recyclerView.adapter = adapter

        // Load subscriptions
        loadSubscriptions()
    }

    override fun onResume() {
        super.onResume()
        // Refresh subscriptions when returning to this activity
        loadSubscriptions()
    }

    private fun loadSubscriptions() {
        val subscriptions = database.getAllSubscriptions()
        
        if (subscriptions.isEmpty()) {
            // Show empty state
            recyclerView.visibility = View.GONE
            emptyStateTextView.visibility = View.VISIBLE
        } else {
            // Show subscriptions
            recyclerView.visibility = View.VISIBLE
            emptyStateTextView.visibility = View.GONE
            adapter.updateList(subscriptions)
        }
    }
}
