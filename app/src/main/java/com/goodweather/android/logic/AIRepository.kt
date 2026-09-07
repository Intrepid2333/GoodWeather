package com.goodweather.android.logic

import android.util.Log
import com.goodweather.android.logic.ai.WeatherTool
import com.goodweather.android.logic.model.DeepSeekFunction
import com.goodweather.android.logic.model.DeepSeekMessage
import com.goodweather.android.logic.model.DeepSeekRequest
import com.goodweather.android.logic.model.DeepSeekStreamResponse
import com.goodweather.android.logic.model.DeepSeekTool
import com.goodweather.android.logic.network.DeepSeekService
import com.goodweather.android.logic.network.DeepSeekServiceCreator
import com.google.gson.Gson
import com.goodweather.android.logic.ai.searchPlaceTool
import com.goodweather.android.logic.ai.executeTool
import com.goodweather.android.logic.ai.getCurrentWeatherTool
import com.goodweather.android.logic.ai.getDailyWeatherTool
import com.goodweather.android.logic.model.AIChatResult
import kotlinx.coroutines.delay


class AIRepository {
    private val deepSeekService = DeepSeekServiceCreator.create<DeepSeekService>()

    suspend fun chat(apiKey: String, messages: List<DeepSeekMessage>): AIChatResult {
        val tools = listOf(
            searchPlaceTool.toDeepSeekTool(),
            getCurrentWeatherTool.toDeepSeekTool(),
            getDailyWeatherTool.toDeepSeekTool()
        )

        var currentMessages = messages.toMutableList()
        var toolCallCount = 0
        val maxToolCalls = 5
        val toolRetryCount = mutableMapOf<String, Int>()

        while (true) {

            val request = DeepSeekRequest(
                messages = currentMessages,
                tools = tools
            )

            val response = deepSeekService.chat(
                authorization = "Bearer $apiKey",
                request = request
            )

            val message = response.choices[0].message

            if (message.tool_calls.isNullOrEmpty()) {

                return AIChatResult(
                    content = message.content ?: "",
                    toolMessages = currentMessages.drop(messages.size)
                )
            }

            toolCallCount++

            if (toolCallCount > maxToolCalls) {
                return AIChatResult(
                    content = "工具调用次数超过限制，请重新描述你的问题。",
                    toolMessages = currentMessages.drop(messages.size)
                )
            }

            currentMessages.add(message)

            for (toolCall in message.tool_calls) {
                delay(1000)
                val toolResult = executeTool(
                    toolName = toolCall.function.name,
                    arguments = toolCall.function.arguments
                )

                if (toolResult.isFailure) {
                    val retryKey = toolCall.function.name + toolCall.function.arguments

                    val retryCount = toolRetryCount.getOrDefault(
                        retryKey,
                        0
                    )
                    if (retryCount >= 2) {
                        currentMessages.add(
                            DeepSeekMessage(
                                role = "tool",
                                content = "工具执行失败，已达到最大重试次数，无法继续调用该工具。",
                                tool_call_id = toolCall.id
                            )
                        )
                        continue
                    }

                    toolRetryCount[retryKey] = retryCount + 1

                    currentMessages.add(
                        DeepSeekMessage(
                            role = "tool",
                            content = "工具执行失败：${toolResult.exceptionOrNull()?.message}",
                            tool_call_id = toolCall.id
                        )
                    )

                    continue
                }

                val toolContent = toolResult.getOrNull() ?: ""

                currentMessages.add(
                    DeepSeekMessage(
                        role = "tool",
                        content = toolContent,
                        tool_call_id = toolCall.id
                    )
                )
            }
        }
    }

    suspend fun chatStream(apiKey: String, messages: List<DeepSeekMessage>, onMessage: (String) -> Unit){
        val request = DeepSeekRequest(
            messages = messages,
            stream = true
        )
        val responseBody = deepSeekService.chatStream(
            authorization = "Bearer $apiKey",
            request = request
        )

        responseBody.byteStream().bufferedReader().useLines { lines ->
            lines.forEach { line ->
                if (line.startsWith("data: ")) {
                    val data = line.removePrefix("data: ")
                    if (data != "[DONE]") {
                        val streamResponse =
                            Gson().fromJson(data, DeepSeekStreamResponse::class.java)
                        val content = streamResponse.choices
                            .firstOrNull()
                            ?.delta
                            ?.content

                        if (!content.isNullOrEmpty()) {
                            Log.d("AI_STREAM", "收到: $content")
                            onMessage(content)
                        }
                    }
                }
            }
        }
    }

    private fun WeatherTool.toDeepSeekTool(): DeepSeekTool {
        return DeepSeekTool(
            function = DeepSeekFunction(
                name = name,
                description = description,
                parameters = parameters
            )
        )
    }

}