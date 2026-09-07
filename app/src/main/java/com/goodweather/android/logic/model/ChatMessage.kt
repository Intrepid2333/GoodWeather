package com.goodweather.android.logic.model

data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val isThinking: Boolean = false,
    val toolCalls: List<DeepSeekToolCall>? = null,
    val toolCallId: String? = null
)