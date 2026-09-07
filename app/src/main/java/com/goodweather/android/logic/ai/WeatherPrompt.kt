package com.goodweather.android.logic.ai

import com.goodweather.android.logic.model.Weather

fun buildWeatherPrompt(weather: Weather?): String {

    if (weather == null) {
        return "当前没有可用的天气数据。"
    }

    val forecastText = weather?.daily?.let { dailyWeather ->

        dailyWeather.skycon.indices.joinToString("\n") { i ->

            val skycon = dailyWeather.skycon[i]
            val temperature = dailyWeather.temperature[i]

            "${skycon.date}：${skycon.value}，" +
                    "${temperature.min}℃ ~ ${temperature.max}℃"
        }

    } ?: "暂无未来天气数据"

    return """
        当前天气信息：
        温度：${weather?.realtime?.temperature}℃
        天气：${weather?.realtime?.skycon}
        空气质量指数：${weather?.realtime?.airQuality?.aqi?.chn}

        未来天气预报：
        $forecastText
    """.trimIndent()
}