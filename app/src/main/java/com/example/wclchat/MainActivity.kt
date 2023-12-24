package com.example.wclchat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.wclchat.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        auth = Firebase.auth

        // Настройка BottomNavigationView
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chat -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ChatFragment())
                        .commit()
                    true
                }
                // Обработка других элементов меню
                // ...
                else -> false
            }
        }

        // Установка начального фрагмента
        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.nav_chat
        }
    }
}