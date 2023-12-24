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
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chat -> {
                    openFragment(ChatFragment())
                    true
                }
                R.id.nav_profile -> {
                    openFragment(ProfileFragment())
                true
                }
                R.id.nav_settings -> {
                    openFragment(SettingsFragment())
                    true
                }
                R.id.nav_quests -> {
                    openFragment(QuestsFragment())
                    true
                }
                // Добавьте обработку других пунктов меню здесь, если они есть
                else -> false
            }
        }
    }
}