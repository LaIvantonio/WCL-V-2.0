package com.example.wclchat

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileViewModel : ViewModel() {

    val userName = MutableLiveData<String>()
    val achievements = MutableLiveData<Int>()
    val completedKilometers = MutableLiveData<Int>()
    val level = MutableLiveData<Int>()
    val rating = MutableLiveData<Double>()

    init {
        // Инициализация значений по умолчанию
        achievements.value = 0
        completedKilometers.value = 0
        level.value = 1
        rating.value = 0.0
    }

}