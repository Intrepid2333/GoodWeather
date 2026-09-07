package com.goodweather.android.ui.ai

import com.goodweather.android.BuildConfig
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goodweather.android.logic.AIRepository
import com.goodweather.android.logic.ai.buildWeatherPrompt
import com.goodweather.android.logic.model.ChatMessage
import kotlinx.coroutines.launch
import com.goodweather.android.logic.model.DeepSeekMessage
import com.goodweather.android.logic.model.Weather

class AIViewModel : ViewModel() {
    private val repository = AIRepository()
    private val _messages = MutableLiveData<List<ChatMessage>>(emptyList())
    private val conversationMessages = mutableListOf<DeepSeekMessage>()
    val messages: LiveData<List<ChatMessage>>
        get() = _messages
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    fun sendMessage(content: String, weather: Weather?) {
        if (content.isBlank()) {
            return
        }

        _isLoading.value = true

        // 添加用户消息
        addMessage(
            ChatMessage(
                content = content,
                isUser = true
            )
        )
        conversationMessages.add(
            DeepSeekMessage(
                role = "user",
                content = content
            )
        )

        addMessage(
            ChatMessage(
                content = "正在思考...",
                isUser = false,
                isThinking = true
            )
        )


        viewModelScope.launch {
            val deepSeekMessages = mutableListOf(
                DeepSeekMessage(
                    role = "system",
                    content = """
                        你是 GoodWeather 天气助手。
                        你的主要任务是帮助用户理解和分析天气信息，包括：
                        1. 当前天气
                        2. 未来天气预报
                        3. 温度变化
                        4. 空气质量
                        5. 出行、运动、穿衣等天气相关建议                 
                        请遵守以下规则：
                        - 使用简洁、自然的中文回答。
                        - 优先根据提供的天气数据回答问题。
                        - 不要编造天气数据。
                        - 如果提供的数据无法回答用户的问题，请明确告诉用户。
                        - 如果用户的问题与天气无关，可以简短回答，但不要假装自己拥有不存在的信息。                   
                        ${buildWeatherPrompt(weather)}
                    """.trimIndent()
                )
            )
            deepSeekMessages.addAll(conversationMessages)
            try {
                val result = repository.chat(
                    apiKey = BuildConfig.DEEPSEEK_API_KEY,
                    messages = deepSeekMessages
                )

                conversationMessages.addAll(result.toolMessages)

                val messages = _messages.value.orEmpty().toMutableList()
                messages.removeAll { it.isThinking }
                _messages.value = messages
                addMessage(
                    ChatMessage(
                        content = result.content,
                        isUser = false
                    )
                )
                conversationMessages.add(
                    DeepSeekMessage(
                        role = "assistant",
                        content = result.content
                    )
                )
            } catch (e: Exception) {
                val messages = _messages.value.orEmpty().toMutableList()
                messages.removeAll { it.isThinking }
                _messages.value = messages
                addMessage(
                    ChatMessage(
                        content = "请求失败，请稍后再试。",
                        isUser = false
                    )
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun addMessage(message: ChatMessage) {
        val messages = _messages.value.orEmpty().toMutableList()
        messages.add(message)
        _messages.value = messages
    }
}