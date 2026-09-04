package com.goodweather.android

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class GoodWeatherApplication : Application(){
    companion object{
        const val TOKEN="6kO07sLO4yTZKizH"

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context=applicationContext
    }
}