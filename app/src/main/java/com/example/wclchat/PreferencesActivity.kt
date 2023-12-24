package com.example.wclchat

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.wclchat.databinding.ActivityPreferencesBinding

class PreferencesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPreferencesBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreferencesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Наблюдение за состоянием загрузки
        viewModel.isLoading.observe(this, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSavePreferences.isEnabled = !isLoading
        })

        // Наблюдение за результатом сохранения
        viewModel.isSaveSuccessful.observe(this, Observer { isSuccessful ->
            if (isSuccessful) {
                Toast.makeText(this, "Предпочтения сохранены", Toast.LENGTH_SHORT).show()
                finish() // Закрыть активити после сохранения
            } else {
                Toast.makeText(this, "Ошибка сохранения предпочтений", Toast.LENGTH_SHORT).show()
            }
        })

        binding.btnSavePreferences.setOnClickListener {
            savePreferences()
        }
    }

    private fun savePreferences() {
        // Проверяем, выбран ли хотя бы один чекбокс
        if (!binding.checkboxMonuments.isChecked &&
            !binding.checkboxMuseums.isChecked &&
            !binding.checkboxParks.isChecked &&
            !binding.checkboxTheaters.isChecked) {
            // Если ни один чекбокс не выбран, показываем Toast с предупреждением
            Toast.makeText(this, "Выберите хотя бы одну категорию", Toast.LENGTH_LONG).show()
            return // Прерываем выполнение метода, чтобы не сохранять пустые предпочтения
        }

        // Если хотя бы один чекбокс выбран, продолжаем сохранение предпочтений
        val preferences = Preferences(
            monuments = binding.checkboxMonuments.isChecked,
            museums = binding.checkboxMuseums.isChecked,
            parks = binding.checkboxParks.isChecked,
            theaters = binding.checkboxTheaters.isChecked
            // Добавьте другие категории по желанию
        )
        viewModel.savePreferences(preferences)
        // Закрываем активити после сохранения предпочтений
        finish()
    }

}
