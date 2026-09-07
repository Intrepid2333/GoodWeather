package com.goodweather.android.logic.model

data class AIChatResult(
    val content: String,
    val toolMessages: List<DeepSeekMessage> = emptyList()
)