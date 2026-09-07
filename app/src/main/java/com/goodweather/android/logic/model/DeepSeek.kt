package com.goodweather.android.logic.model

/**
 * DeepSeek API 消息体
 * @param role 角色：system / user / assistant
 * @param content 消息内容
 */
data class DeepSeekMessage(
    val role: String,
    val content: String,
    val tool_calls: List<DeepSeekToolCall>? = null,
    val tool_call_id: String? = null
)

data class DeepSeekToolCall(
    val id: String,
    val type: String,
    val function: DeepSeekToolCallFunction
)

data class DeepSeekToolCallFunction(
    val name: String,
    val arguments: String
)

/**
 * DeepSeek API 请求体
 * @param model 使用的模型名称
 * @param messages 对话消息列表
 * @param temperature 温度参数，控制随机性（0-1）
 * @param maxTokens 最大返回token数
 */

data class DeepSeekFunction(
    val name: String,
    val description: String,
    val parameters: Map<String, Any>? = null
)

data class DeepSeekTool(
    val type: String = "function",
    val function: DeepSeekFunction
)

data class DeepSeekRequest(
    val model: String = "deepseek-v4-flash",
    val messages: List<DeepSeekMessage>,
    val temperature: Double = 0.7,
    val max_tokens: Int = 1000,
    // 新增可选字段（按需使用）
    val thinking: ThinkingConfig? = null,
    val reasoning_effort: String? = null,
    val stream: Boolean = false,
    val tools: List<DeepSeekTool>? = null
)

data class ThinkingConfig(
    val type: String = "enabled"   // 目前仅支持 "enabled"
)

/**
 * DeepSeek API 响应体
 */
data class DeepSeekResponse(
    val id: String? = null,
    val choices: List<DeepSeekChoice> = emptyList()
)

data class DeepSeekChoice(
    val index: Int = 0,
    val message: DeepSeekMessage = DeepSeekMessage("", ""),
    val finish_reason: String? = null
)

/**
 * DeepSeek API 流式数据响应体
 */
data class DeepSeekStreamResponse(
    val choices: List<DeepSeekStreamChoice>
)

data class DeepSeekStreamChoice(
    val delta: DeepSeekDelta
)

data class DeepSeekDelta(
    val content: String?
)