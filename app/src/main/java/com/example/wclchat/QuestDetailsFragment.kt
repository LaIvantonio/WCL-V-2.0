package com.example.wclchat

import android.graphics.Color
import android.os.Bundle
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

class QuestDetailsFragment : Fragment() {
        private var _binding: FragmentQuestDetailsBinding? = null
        private val binding get() = _binding!!
        private lateinit var map: MapView

        companion object {
                private const val ARG_QUEST = "quest"

                fun newInstance(quest: Quest): QuestDetailsFragment {
                        val args = Bundle().apply {
                                putSerializable(ARG_QUEST, quest)
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
                quest?.let {
                        binding.tvQuestDetailsTitle.text = it.title
                        binding.tvQuestDescription.text = it.description
                        initializeMap(it.location)
                }
        }

        private fun initializeMap(location: String) {
                val ctx = requireContext()
                Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
                map = binding.map.apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setBuiltInZoomControls(true)
                        setMultiTouchControls(true)
                }

                val questLocation = location.split(",").let { GeoPoint(it[0].toDouble(), it[1].toDouble()) }
                val mapController = map.controller
                mapController.setZoom(15.0)
                mapController.setCenter(questLocation)

                // Здесь будет код для добавления маркера на карту
                // ...

                // Запрос маршрута
                requestRoute(questLocation)
        }

        private fun requestRoute(destination: GeoPoint) {
                // Замените на ваш URL и API ключ
                val url = "URL_ДЛЯ_ЗАПРОСА_МАРШРУТА"
                val apiKey = "ВАШ_API_КЛЮЧ"

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
                                        // Обработка ошибки
                                }
                        } catch (e: Exception) {
                                // Обработка исключения
                        }
                }
        }

        private fun parseRoute(responseBody: String): List<GeoPoint> {
                val json = JSONObject(responseBody)
                // Здесь должен быть ваш код для разбора JSON и создания списка GeoPoint
                // ...
                return emptyList() // Верните список точек маршрута
        }

        private fun drawRouteOnMap(routePoints: List<GeoPoint>) {
                val polyline = Polyline().apply {
                        setPoints(routePoints)
                        color = Color.BLUE
                }
                map.overlays.add(polyline)
                map.invalidate()
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
