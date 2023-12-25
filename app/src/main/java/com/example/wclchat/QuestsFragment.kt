package com.example.wclchat

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
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
                // Обработка ошибок запроса
            }
        }
    }
    // Функция для генерации квестов на основе найденных мест
    private fun generateQuestsBasedOnPlaces(places: List<Place>) {
        // Преобразование списка мест в список квестов
        // ...
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
                        generateQuestsBasedOnPreferences(preferences)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Обработка ошибок, например, показать сообщение
                }
            })
        }
    }

    private fun generateQuestsBasedOnPreferences(preferences: Preferences) {
        // Здесь будет ваша логика для генерации списка квестов
        val quests = mutableListOf<Quest>()
        // Заполните список quests на основе предпочтений пользователя

        // Обновление RecyclerView с новым списком квестов
        updateQuestsRecyclerView(quests)
    }

    private fun updateQuestsRecyclerView(quests: List<Quest>) {
        val adapter = QuestAdapter(quests) { quest ->
            // Обработка клика по квесту, например, открытие QuestDetailsFragment
            openQuestDetails(quest)
        }
        binding.rvQuests.adapter = adapter
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
