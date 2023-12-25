package com.example.wclchat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.wclchat.databinding.ItemQuestBinding

class QuestAdapter(private val onClick: (Quest) -> Unit) :
    ListAdapter<Quest, QuestAdapter.QuestViewHolder>(DiffCallback()) {

    class QuestViewHolder(val binding: ItemQuestBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(quest: Quest, onClick: (Quest) -> Unit) {
            binding.tvQuestTitle.text = quest.title
            binding.root.setOnClickListener { onClick(quest) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Quest>() {
        override fun areItemsTheSame(oldItem: Quest, newItem: Quest): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Quest, newItem: Quest): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestViewHolder {
        val binding = ItemQuestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QuestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuestViewHolder, position: Int) {
        val quest = getItem(position)
        holder.bind(quest, onClick)
    }
}
