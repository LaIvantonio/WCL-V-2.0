package com.example.wclchat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.wclchat.databinding.FragmentQuestDetailsBinding
import com.example.wclchat.databinding.FragmentQuestsBinding

class QuestDetailsFragment : Fragment() {
private var _binding: FragmentQuestDetailsBinding? = null
private val binding get() = _binding!!

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
                val quest = arguments?.getSerializable(ARG_QUEST) as Quest?
                quest?.let {
                        binding.tvQuestDetailsTitle.text = it.title
                        binding.tvQuestDescription.text = it.description
                        // Здесь будет код для отображения карты и других деталей квеста
                }
        }

        override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        }

}
