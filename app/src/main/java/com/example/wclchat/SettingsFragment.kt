package com.example.wclchat


import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat.recreate
import androidx.fragment.app.Fragment
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)

        // Установка текущей темы при первом запуске
        val themePreference = findPreference<ListPreference>("theme_preference")
        if (themePreference != null) {
            val savedTheme = getSavedTheme()
            themePreference.value = savedTheme
            updateThemePreferenceSummary(themePreference)
            updateAppTheme(savedTheme)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            "theme_preference" -> {
                val selectedTheme = sharedPreferences?.getString(key, "light") ?: "light"
                updateThemePreferenceSummary(findPreferenceByKey(key) as ListPreference)
                updateAppTheme(selectedTheme)
                saveThemePreference(selectedTheme)
            }
            // Другие обработки изменений настроек...
        }
    }


    private fun getSavedTheme(): String {
        return PreferenceManager.getDefaultSharedPreferences(requireContext())
            .getString("theme_preference", "light") ?: "light"
    }

    private fun saveThemePreference(theme: String) {
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .edit()
            .putString("theme_preference", theme)
            .apply()
    }

    private fun updateAppTheme(themeValue: String?) {
        when (themeValue) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        fragmentManager?.beginTransaction()?.detach(this)?.attach(this)?.commit()
    }

    private fun updateThemePreferenceSummary(preference: ListPreference) {
        // Установка текущего значения в summary
        val index = preference.findIndexOfValue(preference.value)
        if (index >= 0) {
            preference.summary = preference.entries[index]
        }
    }

    private fun findPreferenceByKey(key: String): Preference? {
        return findPreference<Preference>(key)
    }


    override fun onDestroy() {
        super.onDestroy()
        // Отменяем регистрацию слушателя при уничтожении фрагмента
        preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }
}