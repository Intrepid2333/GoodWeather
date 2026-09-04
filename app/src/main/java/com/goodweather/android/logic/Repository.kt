package com.goodweather.android.logic


import android.content.Context
import androidx.lifecycle.liveData
import com.goodweather.android.logic.dao.PlaceDao
import com.goodweather.android.logic.model.Place
import com.goodweather.android.logic.model.Weather
import com.goodweather.android.logic.network.GoodWeatherNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlin.coroutines.CoroutineContext

object Repository {
    fun searchPlace(query: String) = fire(Dispatchers.IO) {
        val placeResponse = GoodWeatherNetwork.searchPlaces(query)

        if (placeResponse.status == "ok") {
            val places = placeResponse.places
            Result.success(places)
        } else {
            Result.failure((RuntimeException("response status is ${placeResponse.status}")))
        }
    }

    fun refreshWeather(lng: String, lat: String) = fire(Dispatchers.IO) {
        coroutineScope {
            val dailyResponse =
                GoodWeatherNetwork.getDailyWeather(lng, lat)
            delay(1000)
            val realtimeResponse =
                GoodWeatherNetwork.getRealtimeWeather(lng, lat)

//            val deferredRealtime = async {
//                GoodWeatherNetwork.getRealtimeWeather(lng, lat)
//            }
//            val deffedDaily = async {
//                GoodWeatherNetwork.getDailyWeather(lng, lat)
//            }
//            val realtimeResponse = deferredRealtime.await()
//            val dailyResponse = deffedDaily.await()

            if (realtimeResponse.status == "ok" && dailyResponse.status == "ok") {
                val weather = Weather(realtimeResponse.result.realtime, dailyResponse.result.daily)
                Result.success(weather)
            } else {
                Result.failure(
                    RuntimeException(
                        "real time response status is ${realtimeResponse.status}" + "daily response status is ${dailyResponse.status}"
                    )
                )
            }
        }
    }

    fun savePlace(place: Place) = PlaceDao.savePlace(place)
    fun getSavedPlace() = PlaceDao.getSavedPlace()
    fun isPlaceSaved() = PlaceDao.isPlaceSaved()

    private fun <T> fire(context: CoroutineContext, block: suspend () -> Result<T>) =
        liveData<Result<T>>(context) {
            val result = try {
                block()
            } catch (e: Exception) {
                Result.failure<T>(e)
            }
            emit(result)
        }
}


