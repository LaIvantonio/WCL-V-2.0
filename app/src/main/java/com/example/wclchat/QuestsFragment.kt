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
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import android.os.Looper


class QuestsFragment : Fragment() {
    private var _binding: FragmentQuestsBinding? = null
    private val binding get() = _binding!!
    private val allQuests = mutableListOf<Quest>()

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

    private fun loadUserPreferencesAndRequestPlaces() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val databaseReference = Firebase.database.getReference("usersPreferences")
            databaseReference.child(userId).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val preferences = snapshot.getValue(Preferences::class.java)
                    if (preferences != null) {
                        Log.d("QuestsFragment", "User preferences loaded: $preferences")
                        getLocation { location ->
                            requestPlaces(location, preferences)
                        }
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

    private fun requestPlaces(location: Location, preferences: Preferences) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val types = mutableListOf<String>()
                if (preferences.monuments) types.add("monument")
                if (preferences.museums) types.add("museum")
                if (preferences.parks) types.add("park")
                if (preferences.theaters) types.add("theatre")

                val allQuests = mutableListOf<Quest>()
                val client = OkHttpClient()

                // Очистите список квестов перед новыми запросами
                allQuests.clear()

                // Последовательно выполняйте запросы к API для каждого типа места
                types.forEach { type ->
                    val url = "https://nominatim.openstreetmap.org/search?format=json&limit=5&q=$type&bounded=1&viewbox=${location.longitude - 0.01},${location.latitude + 0.01},${location.longitude + 0.01},${location.latitude - 0.01}"
                    Log.d("QuestsFragment", "Requesting places for type: $type with URL: $url")
                    val request = Request.Builder().url(url).build()
                    val response = client.newCall(request).execute()

                    val responseBody = response.body?.string()
                    Log.d("QuestsFragment", "Response for type $type: $responseBody")

                    if (response.isSuccessful && responseBody != null) {
                        val placesType = object : TypeToken<List<Place>>() {}.type
                        val places: List<Place> = Gson().fromJson(responseBody, placesType)

                        // Генерируем квесты на основе полученных мест и добавляем их в общий список
                        val quests = generateQuestsBasedOnPlaces(places)
                        allQuests.addAll(quests)
                    } else {
                        Log.d("QuestsFragment", "Request for type $type failed: ${response.code}")
                    }
                }

                // Переключитесь на основной поток для обновления UI
                withContext(Dispatchers.Main) {
                    updateQuestsRecyclerView(allQuests)
                }
            } catch (e: Exception) {
                Log.e("QuestsFragment", "Error requesting places", e)
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
                        // Теперь вызываем requestPlaces с текущим местоположением пользователя и его предпочтениями
                        getLocation { location ->
                            requestPlaces(location, preferences)
                        }
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


    // Функция для генерации квестов на основе найденных мест
    private fun generateQuestsBasedOnPlaces(places: List<Place>): List<Quest> {
        return places.mapIndexed { index, place ->
            Quest(
                id = "quest_${index + 1}",
                title = "Квест: ${place.display_name}",
                description = "Исследуйте это место: ${place.display_name}",
                location = "${place.lat},${place.lon}"
            )
        }
    }

        private fun updateQuestsRecyclerView(quests: List<Quest>) {
            // Получаем адаптер из RecyclerView и приводим его к типу QuestAdapter
            Log.d("QuestsFragment", "Updating quests RecyclerView with quests: $quests")
            val adapter = binding.rvQuests.adapter as? QuestAdapter
            adapter?.submitList(quests)
        }

    private fun openQuestDetails(quest: Quest) {
        val fragment = QuestDetailsFragment.newInstance(quest)
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.placeHolder, fragment)
            .addToBackStack(null) // Добавляем транзакцию в back stack, чтобы пользователь мог вернуться назад
            .commit()
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
            loadUserPreferencesAndRequestPlaces()
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
                    loadUserPreferencesAndRequestPlaces()
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
    private fun getLocation(callback: (Location) -> Unit) {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val locationRequest = LocationRequest.create().apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                interval = 10000
                fastestInterval = 5000
            }
            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult ?: return
                    for (location in locationResult.locations) {
                        // Убедитесь, что останавливаете обновления местоположения после получения первого обновления
                        fusedLocationProviderClient.removeLocationUpdates(this)
                        callback(location)
                        break // Выходим из цикла после обработки первого местоположения
                    }
                }
            }
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

}


