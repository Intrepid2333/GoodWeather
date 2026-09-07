package com.goodweather.android.logic.ai

import com.goodweather.android.logic.Repository
import com.google.gson.Gson
import com.google.gson.JsonObject
import android.util.Log
import androidx.lifecycle.asFlow
import kotlinx.coroutines.flow.first

suspend fun executeTool(toolName: String, arguments: String): Result<String> {
    return when (toolName) {
        "search_place" -> {

            Log.d("WEATHER_TOOL", "开始执行 get_place")
            Log.d("WEATHER_TOOL", "arguments = $arguments")

            val json = Gson().fromJson(
                arguments,
                JsonObject::class.java
            )

            val placeName = json.get("placeName").asString
            val result = Repository.searchPlaceDirect(placeName)


            if (result.isSuccess) {
                val places = result.getOrNull()
                if (places.isNullOrEmpty()) {

                    return Result.failure(
                        RuntimeException("未找到地点：$placeName")
                    )
                }
                val place = places[0]

                Log.d(
                    "WEATHER_TOOL",
                    "地点查询成功"
                )

                Result.success(
                    """
                    地点：${place.name}
                    经度：${place.location.lng}
                    纬度：${place.location.lat}
                    """.trimIndent()
                )
            } else {
                Result.failure(
                    result.exceptionOrNull()
                        ?: RuntimeException("地点查询失败")
                )
            }
        }
        "get_current_weather" -> {

            Log.d("WEATHER_TOOL", "开始执行 get_current_weather")
            Log.d("WEATHER_TOOL", "arguments = $arguments")

            val json = Gson().fromJson(
                arguments,
                JsonObject::class.java
            )

            val lng = json.get("lng").asString
            val lat = json.get("lat").asString

            Log.d(
                "WEATHER_TOOL",
                "解析出的坐标：lng=$lng, lat=$lat"
            )

            val result = Repository.refreshWeather(
                lng = lng,
                lat = lat
            ).asFlow().first()
            if (result.isSuccess) {

                val weather = result.getOrNull()

                if (weather == null) {
                    return Result.failure(
                        RuntimeException("天气数据为空")
                    )
                }
                val realtime = weather.realtime
                Log.d(
                    "WEATHER_TOOL",
                    "天气查询成功：temperature=${realtime.temperature}, " +
                            "skycon=${realtime.skycon}, " +
                            "aqi=${realtime.airQuality.aqi.chn}"
                )
                Result.success(
                    """
                    当前温度：${realtime.temperature}℃
                    天气：${realtime.skycon}
                    空气质量指数：${realtime.airQuality.aqi.chn}
                    """.trimIndent()
                )
            } else {
                Log.e(
                    "WEATHER_TOOL",
                    "天气查询失败",
                    result.exceptionOrNull()
                )
                Result.failure(
                    result.exceptionOrNull()
                        ?: RuntimeException("天气查询失败")
                )
            }
        }
        "get_daily_weather" -> {
            Log.d("WEATHER_TOOL", "开始执行 get_daily_weather")
            Log.d("WEATHER_TOOL", "arguments = $arguments")
            val json = Gson().fromJson(
                arguments,
                JsonObject::class.java
            )
            val lng = json.get("lng").asString
            val lat = json.get("lat").asString
            Log.d("WEATHER_TOOL", "解析出的坐标：lng=$lng, lat=$lat")
            val result = Repository.refreshWeather(
                lng = lng,
                lat = lat
            ).asFlow().first()
            if (result.isSuccess) {
                val weather = result.getOrNull()
                if (weather == null) {
                    return Result.failure(
                        RuntimeException("天气数据为空")
                    )
                }
                val daily = weather.daily
                val dailyText = daily.skycon.indices.joinToString("\n") { i ->
                    val skycon = daily.skycon[i]
                    val temperature = daily.temperature[i]
                    "${skycon.date}：${skycon.value}，" +
                            "${temperature.min}℃ ~ ${temperature.max}℃"
                }
                Log.d("WEATHER_TOOL", "未来天气查询成功")

                Result.success(
                    """
                    未来天气预报：
                    $dailyText
                    """.trimIndent()
                )
            } else {
                Log.e(
                    "WEATHER_TOOL",
                    "未来天气查询失败",
                    result.exceptionOrNull()
                )
                Result.failure(
                    result.exceptionOrNull()
                        ?: RuntimeException("未来天气查询失败")
                )
            }
        }
        else -> {
            Result.failure(
                RuntimeException("未知工具：$toolName")
            )
        }
    }
}



suspend fun searchPlace(placeName: String): Result<String> {
    val result = Repository.searchPlaceDirect(placeName)
    val places = result.getOrNull()
    if (places.isNullOrEmpty()) {
        return Result.failure(
            RuntimeException("未找到相关地点")
        )
    }
    val place = places[0]
    return Result.success(
        """
        地点：${place.name}
        经度：${place.location.lng}
        纬度：${place.location.lat}
        """.trimIndent()
    )
}