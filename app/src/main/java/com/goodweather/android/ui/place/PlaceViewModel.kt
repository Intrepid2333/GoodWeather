package com.goodweather.android.ui.place


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.goodweather.android.logic.Place
import com.goodweather.android.logic.Repository

class PlaceViewModel: ViewModel() {
    private val searchLiveData = MutableLiveData<String>()

    val placeList = ArrayList<Place>()

    val placeLiveData = searchLiveData.switchMap { query ->
        Repository.searchPlace(query)
    }

    fun searchPlaces(query: String){
        searchLiveData.value = query
    }
}