package com.example.wclchat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wclchat.databinding.FragmentChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatFragment : Fragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("message")

        binding.bSend.setOnClickListener {
            val messageText = binding.edMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                val filteredMessageText = filterBadWords(messageText)
                val messageId = myRef.push().key ?: "Error"
                myRef.child(messageId).setValue(User(auth.currentUser?.uid, auth.currentUser?.displayName, filteredMessageText, messageId))
                binding.edMessage.text.clear()
            }
        }

        onChangeListener(myRef)
        initRcView()
        initAdapterDataObserver()
        initScrollDownFab()
        toggleScrollDownFabVisibility()
    }

    private fun initRcView() {
        adapter = UserAdapter(auth.currentUser?.uid ?: "")
        binding.chatsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.chatsRecyclerView.adapter = adapter
    }


    private fun onChangeListener(dRef: DatabaseReference) {
        dRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = ArrayList<User>()
                for (s in snapshot.children) {
                    val user = s.getValue(User::class.java)?.copy(messageId = s.key)
                    if (user != null) list.add(user)
                }
                adapter.submitList(list)
            }


            override fun onCancelled(error: DatabaseError) {
                // Обработка ошибки
            }

        })
    }

    private fun filterBadWords(text: String): String {
        val badWords = listOf("сука", "блять", "епта") // Список запрещенных слов
        var filteredText = text
        badWords.forEach { badWord ->
            val regex = Regex("(?i)\b$badWord\b") // Создаем регулярное выражение, игнорируя регистр
            val replacement = "*".repeat(badWord.length) // Создаем строку для замены из звездочек
            filteredText = regex.replace(filteredText, replacement) // Заменяем запрещенные слова на звездочки
        }
        return filteredText
    }

    private fun initAdapterDataObserver() {
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                binding.chatsRecyclerView.scrollToPosition(adapter.itemCount - 1)
            }
        })
    }

    private fun initScrollDownFab() {
        binding.scrollDownFab.setOnClickListener {
            binding.chatsRecyclerView.scrollToPosition(adapter.itemCount - 1)
        }
    }

    private fun toggleScrollDownFabVisibility() {
        binding.chatsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()


                if (lastVisibleItemPosition < adapter.itemCount - 1) {
                    binding.scrollDownFab.show()
                } else {
                    binding.scrollDownFab.hide()
                }
            }

        })


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
