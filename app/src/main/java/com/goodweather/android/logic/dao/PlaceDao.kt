package com.goodweather.android.logic.dao

import android.content.Context
import com.goodweather.android.GoodWeatherApplication
import com.goodweather.android.logic.model.Place
import androidx.core.content.edit
import com.google.gson.Gson

object PlaceDao {
    fun savePlace(place: Place){
        val json = Gson().toJson(place)

        sharedPreferences().edit {
            putString("place", json)
        }
    }

    fun getSavedPlace(): Place {
        val placeJson = sharedPreferences().getString("place", "")
        return Gson().fromJson(placeJson, Place::class.java)
    }

    fun isPlaceSaved() = sharedPreferences().contains("place")

    private fun sharedPreferences() = GoodWeatherApplication.context.getSharedPreferences("good_weather",
        Context.MODE_PRIVATE)
}