package com.example.wclchat

import java.io.Serializable

data class Quest(
    val id: String,
    val title: String,
    val description: String,
    val location: String // Может содержать координаты или адрес
    // Добавьте другие поля, которые могут понадобиться для квеста
) : Serializable
