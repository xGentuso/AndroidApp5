package com.trios2025dj.superpodcast.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.trios2025dj.superpodcast.R
import com.trios2025dj.superpodcast.data.ITunesApi
import com.trios2025dj.superpodcast.data.Podcast
import com.trios2025dj.superpodcast.ui.PodcastAdapter
import com.trios2025dj.superpodcast.ui.PodcastDetailActivity
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.UnknownHostException

class DiscoverFragment : Fragment() {

    private lateinit var editTextSearch: EditText
    private lateinit var buttonSearch: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var textViewEmptyState: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var adapter: PodcastAdapter
    private var currentSearchTerm = ""

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://itunes.apple.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val itunesApi = retrofit.create(ITunesApi::class.java)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_discover, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        editTextSearch = view.findViewById(R.id.editTextSearch)
        buttonSearch = view.findViewById(R.id.buttonSearch)
        recyclerView = view.findViewById(R.id.recyclerView)
        progressIndicator = view.findViewById(R.id.progressIndicator)
        textViewEmptyState = view.findViewById(R.id.textViewEmptyState)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = PodcastAdapter(emptyList())
        recyclerView.adapter = adapter

        // Set up search button click listener
        buttonSearch.setOnClickListener {
            performSearch()
        }

        // Set up search EditText action listener
        editTextSearch.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                performSearch()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        // Set up swipe refresh listener
        swipeRefreshLayout.setOnRefreshListener {
            if (currentSearchTerm.isNotEmpty()) {
                searchPodcasts(currentSearchTerm)
            } else {
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun performSearch() {
        val searchTerm = editTextSearch.text.toString().trim()
        if (searchTerm.isNotEmpty()) {
            currentSearchTerm = searchTerm
            searchPodcasts(searchTerm)
        } else {
            Toast.makeText(requireContext(), "Please enter a search term", Toast.LENGTH_SHORT).show()
        }
    }

    private fun searchPodcasts(term: String) {
        // Show loading state
        progressIndicator.visibility = View.VISIBLE
        textViewEmptyState.visibility = View.GONE
        recyclerView.visibility = View.GONE

        // Perform search
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = itunesApi.searchPodcasts(term, "podcast")
                val podcasts = response.results

                // Update UI based on results
                if (podcasts.isEmpty()) {
                    showEmptyState("No podcasts found for \"$term\"")
                } else {
                    showResults(podcasts)
                }
            } catch (e: UnknownHostException) {
                showError("No internet connection. Please check your network.")
            } catch (e: Exception) {
                showError("Error: ${e.message}")
            } finally {
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun showResults(podcasts: List<Podcast>) {
        progressIndicator.visibility = View.GONE
        textViewEmptyState.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        adapter.updateList(podcasts)
    }

    private fun showEmptyState(message: String) {
        progressIndicator.visibility = View.GONE
        textViewEmptyState.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        textViewEmptyState.text = message
    }

    private fun showError(message: String) {
        progressIndicator.visibility = View.GONE
        textViewEmptyState.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        textViewEmptyState.text = message
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
}
