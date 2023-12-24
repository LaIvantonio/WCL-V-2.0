package com.example.wclchat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class MainViewModel() : ViewModel() {

    fun savePreferences(preferences: Preferences) = viewModelScope.launch {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let {
            val databaseReference = Firebase.database.getReference("usersPreferences")
            databaseReference.child(it).setValue(preferences)
        }
    }

}