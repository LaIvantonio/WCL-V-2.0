package com.example.wclchat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wclchat.databinding.ItemQuestBinding

class QuestAdapter(private val quests: List<Quest>, private val onClick: (Quest) -> Unit) :
    RecyclerView.Adapter<QuestAdapter.QuestViewHolder>() {

    class QuestViewHolder(val binding: ItemQuestBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestViewHolder {
        val binding = ItemQuestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QuestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuestViewHolder, position: Int) {
        val quest = quests[position]
        holder.binding.tvQuestTitle.text = quest.title
        holder.binding.root.setOnClickListener { onClick(quest) }
    }

    override fun getItemCount(): Int = quests.size
}
