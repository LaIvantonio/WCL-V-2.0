package com.example.wclchat

import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.example.wclchat.databinding.FragmentQuestDetailsBinding
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

class QuestDetailsFragment : Fragment() {
        private var _binding: FragmentQuestDetailsBinding? = null
        private val binding get() = _binding!!
        private lateinit var map: MapView

        companion object {
                private const val ARG_QUEST = "quest"
                private const val ARG_CURRENT_LOCATION = "current_location"

                fun newInstance(quest: Quest, currentLocation: Location): QuestDetailsFragment {
                        val args = Bundle().apply {
                                putSerializable(ARG_QUEST, quest)
                                putParcelable(ARG_CURRENT_LOCATION, currentLocation)
                        }
                        return QuestDetailsFragment().apply {
                                arguments = args
                        }
                }
        }

        override fun onCreateView(
                inflater: LayoutInflater, container: ViewGroup?,
                savedInstanceState: Bundle?
        ): View {
                _binding = FragmentQuestDetailsBinding.inflate(inflater, container, false)
                return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
                super.onViewCreated(view, savedInstanceState)
                val quest = arguments?.getSerializable(ARG_QUEST) as? Quest
                val currentLocation = arguments?.getParcelable<Location>(ARG_CURRENT_LOCATION)
                quest?.let {
                        binding.tvQuestDetailsTitle.text = it.title
                        binding.tvQuestDescription.text = it.description
                        if (currentLocation != null) {
                                initializeMap(it.location, currentLocation)
                        }
                }
        }

        private fun initializeMap(questLocationString: String, currentLocation: Location) {
                val ctx = requireContext()
                Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
                map = binding.map.apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setBuiltInZoomControls(true)
                        setMultiTouchControls(true)
                }

                val questLocation = questLocationString.split(",").let { GeoPoint(it[0].toDouble(), it[1].toDouble()) }
                addMarkerAtLocation(questLocation) // Добавляем маркер на карту
                val mapController = map.controller
                mapController.setZoom(15.0)
                mapController.setCenter(questLocation)
                // Запрос маршрута
                val startPoint = GeoPoint(currentLocation.latitude, currentLocation.longitude)
                requestRoute(questLocation, startPoint)

        }

        // Используйте currentLocation для запроса маршрута:
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
                                        Log.d("QuestDetailsFragment", "Response from routing service: $responseBody")
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
                val routes = json.getJSONArray("routes")
                val route = routes.getJSONObject(0)
                val geometry = route.getJSONObject("geometry")
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
                _binding = null
        }

        override fun onDestroy() {
                super.onDestroy()
                map.onDetach()
        }
}