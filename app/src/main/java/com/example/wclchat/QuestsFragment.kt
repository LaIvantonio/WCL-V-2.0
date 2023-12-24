package com.example.wclchat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wclchat.databinding.FragmentQuestsBinding

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

    // Добавьте временный список квестов для тестирования
    private val quests = listOf(
        Quest("1", "Поиск старинного маяка", "Описание квеста...", "Location1"),
        Quest("2", "Тайна заброшенного парка", "Описание квеста...", "Location2"),
        Quest("3", "Погоня за историей", "Описание квеста...", "Location3")
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = QuestAdapter(quests) { quest ->
            // Обработка клика по квесту
            openQuestDetails(quest)
        }
        binding.rvQuests.adapter = adapter
        binding.rvQuests.layoutManager = LinearLayoutManager(context)
    }

    private fun openQuestDetails(quest: Quest) {
        // Здесь будет код для открытия QuestDetailsFragment с деталями квеста
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
