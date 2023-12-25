package com.example.wclchat

import java.io.Serializable

data class Quest(
    val id: String, // Уникальный идентификатор квеста
    val title: String, // Название квеста
    val description: String, // Описание квеста
    val location: String // Местоположение квеста в формате "широта,долгота"
)
 : Serializable
