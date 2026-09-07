package com.goodweather.android.logic.network


import com.goodweather.android.logic.model.DeepSeekRequest
import com.goodweather.android.logic.model.DeepSeekResponse
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Streaming

interface DeepSeekService {
    @POST("chat/completions")
    suspend fun chat(
        @Header("Authorization") authorization: String,
        @Body request: DeepSeekRequest
    ): DeepSeekResponse

    @Streaming
    @POST("chat/completions")
    suspend fun chatStream(
        @Header("Authorization") authorization: String,
        @Body request: DeepSeekRequest
    ): ResponseBody
}

