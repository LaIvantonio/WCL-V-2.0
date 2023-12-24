package com.example.wclchat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.wclchat.databinding.FragmentQuestExecutionBinding
import com.example.wclchat.databinding.FragmentQuestsBinding

class QuestExecutionFragment : Fragment() {
    private var _binding: FragmentQuestExecutionBinding? = null
    private val binding get() = _binding!!

        override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
        ): View {
        _binding = FragmentQuestExecutionBinding.inflate(inflater, container, false)
        return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            // Здесь будет код для инициализации карты и отслеживания местоположения пользователя
            binding.btnCompleteQuest.setOnClickListener {
                // Здесь будет код для проверки, достиг ли пользователь цели квеста
            }
        }

        override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        }
        }
