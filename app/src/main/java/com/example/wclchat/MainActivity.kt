package com.example.wclchat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.wclchat.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.example.wclchat.utils.openFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        onBottomNavClicks()
        openFragment(ChatFragment())
    }

    private fun onBottomNavClicks() {
        binding.bottomNavigation.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.nav_chat -> openFragment(ChatFragment())
                R.id.nav_profile -> openFragment(ProfileFragment())
                R.id.nav_settings -> openFragment(SettingsFragment())
            }
            true
        }
    }
}