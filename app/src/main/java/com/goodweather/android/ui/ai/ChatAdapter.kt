package com.goodweather.android.ui.ai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.goodweather.android.R
import com.goodweather.android.logic.model.ChatMessage

class ChatAdapter(
    private val messageList: MutableList<ChatMessage>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_AI = 0
        private const val TYPE_USER = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (messageList[position].isUser) {
            TYPE_USER
        } else {
            TYPE_AI
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return if (viewType == TYPE_AI) {
            val view = LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.ai_message_item,
                    parent,
                    false
                )
            AIViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.user_message_item,
                    parent,
                    false
                )
            UserViewHolder(view)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val message = messageList[position]
        if (holder is AIViewHolder) {
            holder.messageText.text = message.content
        }
        if (holder is UserViewHolder) {
            holder.messageText.text = message.content
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }


    class AIViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {
        val messageText: TextView = view.findViewById(R.id.aiMessageText)
    }

    class UserViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {
        val messageText: TextView = view.findViewById(R.id.userMessageText)
    }

    fun updateMessages(messages: List<ChatMessage>) {
        messageList.clear()
        messageList.addAll(messages)
        notifyDataSetChanged()
    }

}