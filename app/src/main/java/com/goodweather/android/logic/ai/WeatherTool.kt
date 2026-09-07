package com.goodweather.android.logic.ai

data class WeatherTool(
    val name: String,
    val description: String,
    val parameters: Map<String, Any>? = null
)

val searchPlaceTool = WeatherTool(
    name = "search_place",
    description = "根据地点名称查询地点的经纬度。",
    parameters = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "placeName" to mapOf(
                "type" to "string",
                "description" to "需要查询的地点名称，例如南京、苏州、上海。"
            )
        ),
        "required" to listOf("placeName")
    )
)


val getCurrentWeatherTool = WeatherTool(
    name = "get_current_weather",
    description = "根据经度和纬度获取当前天气信息，包括当前温度、天气状况和空气质量。",
    parameters = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "lng" to mapOf(
                "type" to "string",
                "description" to "地点的经度"
            ),
            "lat" to mapOf(
                "type" to "string",
                "description" to "地点的纬度"
            )
        ),
        "required" to listOf("lng", "lat")
    )
)

val getDailyWeatherTool = WeatherTool(
    name = "get_daily_weather",
    description = "根据经度和纬度获取未来天气预报，包括每天的天气状况和最高最低温度。",
    parameters = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "lng" to mapOf(
                "type" to "string",
                "description" to "地点的经度"
            ),
            "lat" to mapOf(
                "type" to "string",
                "description" to "地点的纬度"
            )
        ),
        "required" to listOf("lng", "lat")
    )
)