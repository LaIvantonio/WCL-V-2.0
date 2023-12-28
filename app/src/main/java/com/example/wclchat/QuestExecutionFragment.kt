package com.example.wclchat

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.example.wclchat.databinding.FragmentQuestExecutionBinding
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.Marker
import com.google.android.gms.location.*

class QuestExecutionFragment : Fragment() {
    private var _binding: FragmentQuestExecutionBinding? = null
    private val binding get() = _binding!!
    private lateinit var map: MapView
    private lateinit var userLocationMarker: Marker
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var userLocation: Location? = null
    private var quest: Quest? = null
    private var currentRoute: Polyline? = null
    // Добавляем переменную для хранения последнего известного местоположения пользователя
    private var lastKnownLocation: GeoPoint? = null
    private val routeThreshold = 50 // Расстояние в метрах, при превышении которого будет запрошен новый маршрут

    companion object {
        private const val ARG_QUEST = "quest"
        private const val ARG_CURRENT_LOCATION = "current_location"

        fun newInstance(quest: Quest, currentLocation: Location): QuestExecutionFragment {
            val args = Bundle().apply {
                putSerializable(ARG_QUEST, quest)
                putParcelable(ARG_CURRENT_LOCATION, currentLocation)
            }
            return QuestExecutionFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuestExecutionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        quest = arguments?.getSerializable(ARG_QUEST) as? Quest
        userLocation = arguments?.getParcelable(ARG_CURRENT_LOCATION)

        // Проверяем, что quest и userLocation не null перед инициализацией карты
        if (quest != null && userLocation != null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            initializeMap()
            startLocationUpdates()
        } else {
            // Обработка ошибки, если quest или userLocation null
        }
    }

    private fun initializeMap() {
        val ctx = requireContext()
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        map = binding.map.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setBuiltInZoomControls(true)
            setMultiTouchControls(true)
        }

        // Инициализация маркера пользователя
        userLocationMarker = Marker(map)
        map.overlays.add(userLocationMarker)

        quest?.let { nonNullQuest ->
            val questLocation = nonNullQuest.location.split(",").let {
                GeoPoint(it[0].toDouble(), it[1].toDouble())
            }
            addMarkerAtLocation(questLocation)
            val mapController = map.controller
            mapController.setZoom(15.0)
            mapController.setCenter(questLocation)
            // Инициализация маркера пользователя с пользовательской иконкой
            userLocationMarker = Marker(map).apply {
                icon = ContextCompat.getDrawable(requireContext(), R.drawable.dialog_frame) // Замените на вашу иконку
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }
            map.overlays.add(userLocationMarker)
            userLocation?.let { nonNullUserLocation ->
                val startPoint = GeoPoint(nonNullUserLocation.latitude, nonNullUserLocation.longitude)
                userLocationMarker.position = startPoint
                userLocationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                map.invalidate() // Обновляем карту, чтобы показать местоположение пользователя
                requestRoute(questLocation, startPoint)
            }
        }
    }

    private fun startLocationUpdates() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    updateUserLocation(location)
                }
            }
        }
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }

    private fun updateUserLocation(location: Location) {
        // Логируем новое местоположение
        Log.d("QuestExecutionFragment", "Location updated: Lat=${location.latitude}, Lon=${location.longitude}")
        val userGeoPoint = GeoPoint(location.latitude, location.longitude)
        lastKnownLocation?.let {
            // Проверяем, превышает ли изменение расстояния порог для обновления маршрута
            if (it.distanceToAsDouble(userGeoPoint) > routeThreshold) {
                quest?.location?.let { questLocationString ->
                    val questLocation = questLocationString.split(",").let { loc ->
                        GeoPoint(loc[0].toDouble(), loc[1].toDouble())
                    }
                    requestRoute(questLocation, userGeoPoint)
                }
            }
        }
        lastKnownLocation = userGeoPoint // Обновляем последнее известное местоположение

        // Обновляем местоположение маркера пользователя на карте
        userLocationMarker.position = userGeoPoint
        map.invalidate() // Обновляем карту, чтобы показать новое местоположение пользователя

        // Опционально: запрос нового маршрута, если пользователь сильно отклонился от курса
        if (shouldRequestNewRoute(userGeoPoint)) {
            quest?.location?.let { questLocationString ->
                val questLocation = questLocationString.split(",").let {
                    GeoPoint(it[0].toDouble(), it[1].toDouble())
                }
                requestRoute(questLocation, userGeoPoint)
            }
        }
    }

    private fun shouldRequestNewRoute(userGeoPoint: GeoPoint): Boolean {
        // Удаляем старый маршрут с карты
        currentRoute?.let {
            map.overlays.remove(it)
            currentRoute = null
        }
        map.invalidate()
        return false // Пример: всегда возвращает false, замените на вашу логику
    }
        private fun requestRoute(destination: GeoPoint, startPoint: GeoPoint) {
        val endPoint = "${destination.latitude},${destination.longitude}"
        val url = "https://api.openrouteservice.org/v2/directions/foot-walking" +
                "?api_key=5b3ce3597851110001cf6248b5a6f8b1a61c4d438e8fe37a263d11c4" +
                "&start=${startPoint.longitude},${startPoint.latitude}" +
                "&end=${destination.longitude},${destination.latitude}"


        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        val routePoints = parseRoute(responseBody)
                        withContext(Dispatchers.Main) {
                            drawRouteOnMap(routePoints)
                        }
                    }
                } else {
                    Log.e("QuestDetailsFragment", "Request to routing service API failed: ${response.code}")
                }

            } catch (e: Exception) {
                // Обработка исключения
            }
        }
    }

    private fun parseRoute(responseBody: String): List<GeoPoint> {
        val json = JSONObject(responseBody)
        val features = json.getJSONArray("features")
        val feature = features.getJSONObject(0)
        val geometry = feature.getJSONObject("geometry")
        val coordinates = geometry.getJSONArray("coordinates")

        val routePoints = mutableListOf<GeoPoint>()
        for (i in 0 until coordinates.length()) {
            val coord = coordinates.getJSONArray(i)
            val lon = coord.getDouble(0)
            val lat = coord.getDouble(1)
            routePoints.add(GeoPoint(lat, lon))
        }
        return routePoints
    }


    private fun drawRouteOnMap(routePoints: List<GeoPoint>) {
        Log.d("QuestDetailsFragment", "Drawing route with ${routePoints.size} points")
        val polyline = Polyline().apply {
            setPoints(routePoints)
            color = Color.BLUE
        }
        map.overlays.add(polyline)
        map.invalidate()
    }

    private fun addMarkerAtLocation(geoPoint: GeoPoint) {
        val marker = Marker(map)
        marker.position = geoPoint
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        map.overlays.add(marker)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        map.onDetach()
    }

}
