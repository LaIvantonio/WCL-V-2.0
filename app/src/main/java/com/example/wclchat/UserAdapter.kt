package com.example.wclchat

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.wclchat.databinding.UserListItemBinding
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class UserAdapter(private val currentUserId: String) : ListAdapter<User, UserAdapter.ItemHolder>(ItemComparator()) {

    class ItemHolder(private val binding: UserListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) = with(binding) {
            message.text = user.message
            userName.text = user.name

            // Изменение цвета текста сообщений и имени пользователя
            message.setTextColor(ContextCompat.getColor(binding.root.context, R.color.textAccent))
            userName.setTextColor(ContextCompat.getColor(binding.root.context, R.color.colorPrimaryDark))
        }
        companion object {
            fun create (parent: ViewGroup) : ItemHolder {
                return ItemHolder(UserListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
        }
    }

    class ItemComparator : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder.create(parent)
    }


    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val message = getItem(position)
        holder.bind(message)

        holder.itemView.setOnLongClickListener {
            if (message.userId == currentUserId) {
                showDeleteMessageDialog(holder.itemView.context, message)
            }
            true
        }
    }

    private fun showDeleteMessageDialog(context: Context, message: User) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Удалить сообщение")
        builder.setMessage("Вы уверены, что хотите удалить это сообщение?")

        builder.setPositiveButton("Да") { _, _ ->
            val database = Firebase.database
            val myRef = database.getReference("message")
            myRef.child(message.messageId!!).removeValue()
        }

        builder.setNegativeButton("Отмена") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }
}