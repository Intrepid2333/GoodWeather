package com.goodweather.android.logic


import android.util.Log
import androidx.lifecycle.liveData
import com.goodweather.android.logic.network.GoodWeatherNetwork
import kotlinx.coroutines.Dispatchers

object Repository {
    fun searchPlace(query: String)= liveData(Dispatchers.IO){
        val result= try{
            Log.e("PLACE_SEARCH", "query = $query")

            val placeResponse = GoodWeatherNetwork.searchPlaces(query)

            Log.e("PLACE_SEARCH", "status = ${placeResponse.status}")
            Log.e("PLACE_SEARCH", "response = $placeResponse")

            if (placeResponse.status=="ok"){
                val places=placeResponse.places
                Result.success(places)
            }else{
                Result.failure((RuntimeException("response status is ${placeResponse.status}")))
            }
        }catch (e: Exception){
            Result.failure<List<Place>>(e)
        }
        emit(result)
    }
}