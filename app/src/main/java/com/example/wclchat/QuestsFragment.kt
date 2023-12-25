package com.example.wclchat

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wclchat.databinding.FragmentQuestsBinding
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request

class QuestsFragment : Fragment() {
    private var _binding: FragmentQuestsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvQuests.layoutManager = LinearLayoutManager(context)
        // Инициализируем адаптер с лямбда-функцией для обработки клика по квесту
        val adapter = QuestAdapter { quest ->
            // Обработка клика по квесту
            openQuestDetails(quest)
        }
        binding.rvQuests.adapter = adapter
        loadUserPreferences()
        checkLocationPermission()
    }


    private fun requestPlaces(location: Location) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()
                val url = "https://nominatim.openstreetmap.org/search?format=json&limit=5&q=park&bounded=1&viewbox=${location.longitude - 0.01},${location.latitude + 0.01},${location.longitude + 0.01},${location.latitude - 0.01}"
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                val responseBody = response.body?.string()

                Log.d("QuestsFragment", "Response: $responseBody")
                if (response.isSuccessful && responseBody != null) {
                    val placesType = object : TypeToken<List<Place>>() {}.type
                    val places: List<Place> = Gson().fromJson(responseBody, placesType)

                    withContext(Dispatchers.Main) {
                        // Обрабатываем полученные данные и обновляем UI
                        generateQuestsBasedOnPlaces(places)
                    }
                } else {
                    // Обработка ошибок запроса
                }
            } catch (e: Exception) {
                Log.e("QuestsFragment", "Error requesting places", e)
                // Обработка ошибок запроса
            }
        }
    }

    // Класс для представления места, полученного от Nominatim API
    data class Place(
        val lat: Double,
        val lon: Double,
        val display_name: String
        // Добавьте другие поля, если они вам нужны
    )

    private fun loadUserPreferences() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val databaseReference = Firebase.database.getReference("usersPreferences")
            databaseReference.child(userId).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val preferences = snapshot.getValue(Preferences::class.java)
                    if (preferences != null) {
                        Log.d("QuestsFragment", "User preferences loaded: $preferences")
                        generateQuestsBasedOnPreferences(preferences)
                    } else {
                        Log.d("QuestsFragment", "User preferences are null")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("QuestsFragment", "Database error: ${error.message}")
                }
            })
        } else {
            Log.d("QuestsFragment", "User ID is null")
        }
    }

    private fun generateQuestsBasedOnPreferences(preferences: Preferences) {
        val quests = mutableListOf<Quest>()
        // Примерная логика генерации квестов
        if (preferences.monuments) {
            quests.add(Quest(id = "1", title = "Посетить памятник 'Мать Родина'", description = "Описание квеста...", location = "47.222078,39.720349"))
        }
        if (preferences.museums) {
            quests.add(Quest(id = "2", title = "Посетить Ростовский музей изобразительных искусств", description = "Описание квеста...", location = "47.231349,39.723097"))
        }
        if (preferences.parks) {
            quests.add(Quest(id = "3", title = "Прогулка в парке Горького", description = "Описание квеста...", location = "47.222831,39.716775"))
        }
        if (preferences.theaters) {
            quests.add(Quest(id = "4", title = "Посетить Ростовский академический театр драмы", description = "Описание квеста...", location = "47.234383,39.712020"))
        }
        // Добавьте больше квестов в зависимости от предпочтений пользователя

        Log.d("QuestsFragment", "Generated quests based on preferences: $quests")
        updateQuestsRecyclerView(quests)
    }

    // Функция для генерации квестов на основе найденных мест
    private fun generateQuestsBasedOnPlaces(places: List<Place>) {
        val quests = places.mapIndexed { index, place ->
            Quest(
                id = "quest_${index + 1}",
                title = "Квест: ${place.display_name}",
                description = "Исследуйте это место: ${place.display_name}",
                location = "${place.lat},${place.lon}"
            )
        }
        Log.d("QuestsFragment", "Generated quests based on places: $quests")
        updateQuestsRecyclerView(quests)
    }

        private fun updateQuestsRecyclerView(quests: List<Quest>) {
            // Получаем адаптер из RecyclerView и приводим его к типу QuestAdapter
            Log.d("QuestsFragment", "Updating quests RecyclerView with quests: $quests")
            val adapter = binding.rvQuests.adapter as? QuestAdapter
            adapter?.submitList(quests)
        }

    private fun openQuestDetails(quest: Quest) {
        // Здесь будет код для открытия QuestDetailsFragment с деталями квеста
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            getLocation()
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    getLocation()
                } else {
                    // Разрешение не получено, нужно обработать этот случай
                }
                return
            }
        }
    }
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
    private fun getLocation() {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    // У вас есть местоположение пользователя, можно использовать его для запроса квестов
                    requestPlaces(it)
                }
            }
        }
    }

}
