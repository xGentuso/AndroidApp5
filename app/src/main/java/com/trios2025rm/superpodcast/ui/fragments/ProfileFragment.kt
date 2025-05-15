package com.trios2025rm.superpodcast.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.trios2025rm.superpodcast.R

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        val textViewUsername = view.findViewById<TextView>(R.id.textViewUsername)
        val textViewEmail = view.findViewById<TextView>(R.id.textViewEmail)
        val buttonEditProfile = view.findViewById<Button>(R.id.buttonEditProfile)
        val buttonLogout = view.findViewById<Button>(R.id.buttonLogout)

        // Set profile data
        textViewUsername.text = "Podcast Enthusiast"
        textViewEmail.text = "podcast.lover@example.com"

        // Set up button click listeners
        buttonEditProfile.setOnClickListener {
            Toast.makeText(requireContext(), "Edit profile functionality coming soon", Toast.LENGTH_SHORT).show()
        }
        
        buttonLogout.setOnClickListener {
            Toast.makeText(requireContext(), "Logout functionality coming soon", Toast.LENGTH_SHORT).show()
        }
    }
}
