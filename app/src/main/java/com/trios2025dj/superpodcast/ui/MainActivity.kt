package com.trios2025dj.superpodcast.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.trios2025dj.superpodcast.R
import com.trios2025dj.superpodcast.data.ITunesApi
import com.trios2025dj.superpodcast.data.Podcast
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.launch
import java.net.UnknownHostException

class MainActivity : AppCompatActivity() {

    private lateinit var editTextSearch: EditText
    private lateinit var buttonSearch: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PodcastAdapter
    private lateinit var api: ITunesApi
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var textViewEmptyState: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var lastQuery: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up toolbar/actionbar
        setSupportActionBar(findViewById(R.id.toolbar))

        // Init Views
        editTextSearch = findViewById(R.id.editTextSearch)
        buttonSearch = findViewById(R.id.buttonSearch)
        recyclerView = findViewById(R.id.recyclerView)
        progressIndicator = findViewById(R.id.progressIndicator)
        textViewEmptyState = findViewById(R.id.textViewEmptyState)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        // Setup RecyclerView
        adapter = PodcastAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Init Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://itunes.apple.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit.create(ITunesApi::class.java)

        // Handle Search Button Click
        buttonSearch.setOnClickListener {
            performSearch()
        }
        
        // Handle keyboard search action
        editTextSearch.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || 
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                performSearch()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
        
        // Setup swipe to refresh
        swipeRefreshLayout.setOnRefreshListener {
            if (lastQuery.isNotEmpty()) {
                searchPodcasts(lastQuery)
            } else {
                swipeRefreshLayout.isRefreshing = false
            }
        }
        
        // Show empty state initially
        showEmptyState()
    }
    
    private fun performSearch() {
        val query = editTextSearch.text.toString().trim()
        if (query.isNotEmpty()) {
            lastQuery = query
            searchPodcasts(query)
        } else {
            Toast.makeText(this, "Please enter a search term", Toast.LENGTH_SHORT).show()
        }
    }

    private fun searchPodcasts(query: String) {
        showLoading()
        lifecycleScope.launch {
            try {
                val response = api.searchPodcasts(query)
                
                if (response.results.isEmpty()) {
                    showEmptyState("No podcasts found for '$query'. Try another search term.")
                } else {
                    showResults(response.results)
                }
            } catch (e: UnknownHostException) {
                showError("No internet connection. Please check your network settings.")
            } catch (e: Exception) {
                showError("An error occurred: ${e.message ?: "Unknown error"}")
                e.printStackTrace()
            } finally {
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }
    
    private fun showLoading() {
        progressIndicator.visibility = View.VISIBLE
        textViewEmptyState.visibility = View.GONE
        recyclerView.visibility = View.GONE
    }
    
    private fun showResults(results: List<Podcast>) {
        progressIndicator.visibility = View.GONE
        textViewEmptyState.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        adapter.updateList(results)
    }
    
    private fun showEmptyState(message: String = "No podcasts found. Try searching for something!") {
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
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    // Menu handling
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_subscriptions -> {
                // Open subscriptions activity
                val intent = Intent(this, SubscriptionsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}