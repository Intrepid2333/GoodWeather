package com.goodweather.android.ui.weather

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.goodweather.android.R
import com.goodweather.android.logic.model.ChatMessage
import com.goodweather.android.logic.model.Weather
import com.goodweather.android.logic.model.getSky
import com.goodweather.android.ui.ai.AIViewModel
import com.goodweather.android.ui.ai.ChatAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat
import java.util.Locale

class WeatherActivity : AppCompatActivity() {
    private var currentWeather: Weather? = null
    val viewModel by lazy { ViewModelProvider(this).get(WeatherViewModel::class.java) }
    private val aiViewModel by lazy { ViewModelProvider(this).get(AIViewModel::class.java) }
    private lateinit var swipeRefresh: SwipeRefreshLayout
    public lateinit var drawerLayout: DrawerLayout

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_weather)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        drawerLayout=findViewById<DrawerLayout>(R.id.drawerLayout)
        val navBtn=findViewById<Button>(R.id.navBtn)
        val aiBtn=findViewById<Button>(R.id.aiBtn)
        aiBtn.setOnClickListener {
            showAIChat()
        }
        navBtn.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener{
            override fun onDrawerStateChanged(newState: Int) {
            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            }
            override fun onDrawerOpened(drawerView: View) {
            }
            override fun onDrawerClosed(drawerView: View) {
                val manager=getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(drawerView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        })

        if(viewModel.locationLng.isEmpty()){
            viewModel.locationLng=intent.getStringExtra("location_lng")?:""
        }
        if(viewModel.locationLat.isEmpty()){
            viewModel.locationLat=intent.getStringExtra("location_lat")?:""
        }
        if(viewModel.placeName.isEmpty()){
            viewModel.placeName=intent.getStringExtra("place_name")?:""
        }


        viewModel.weatherLiveData.observe (this, Observer{ result ->
            val weather = result.getOrNull()
            if (weather!=null){
                showWeatherInfo(weather)
                currentWeather = weather
            }else{
                Toast.makeText(this, "无法成功获取天气信息", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
            swipeRefresh.isRefreshing=false
        })
        swipeRefresh.setColorSchemeColors(R.color.colorPrimary)
        refreshWeather()
        swipeRefresh.setOnRefreshListener {
            refreshWeather()
        }
        viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)
    }

    fun refreshWeather(){
        viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)
        swipeRefresh.isRefreshing=true
    }

    private fun showWeatherInfo(weather: Weather){
        val placeName = findViewById<TextView>(R.id.placeName)
        val currentTemp = findViewById<TextView>(R.id.currentTemp)
        val currentSky = findViewById<TextView>(R.id.currentSky)
        val currentAQI = findViewById<TextView>(R.id.currentAQI)
        val nowLayout = findViewById<RelativeLayout>(R.id.nowLayout)
        val forecastLayout = findViewById<LinearLayout>(R.id.forecastLayout)
        val weatherLayout = findViewById<ScrollView>(R.id.weatherLayout)
        val coldRiskText = findViewById<TextView>(R.id.coldRiskText)
        val dressingText = findViewById<TextView>(R.id.dressingText)
        val ultravioletText = findViewById<TextView>(R.id.ultravioletText)
        val carWashingText = findViewById<TextView>(R.id.carWashingText)
        Log.e("WeatherActivity", viewModel.placeName)
        placeName.text=viewModel.placeName.split(" ").last()
        val realtime = weather.realtime
        val daily = weather.daily

        //填充now.xml布局中的数据
        val currentTempText="${realtime.temperature.toInt()}℃"
        currentTemp.text=currentTempText
        currentSky.text= getSky(realtime.skycon).info
        val currentPM25Text="空气指数${realtime.airQuality.aqi.chn.toInt()}"
        currentAQI.text=currentPM25Text
        nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)

        // 填充forecast.xml布局中的数据
        forecastLayout.removeAllViews()
        val days = daily.skycon.size
        for (i in 0 until days) {
            val skycon = daily.skycon[i]
            val temperature = daily.temperature[i]
            val view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false)
            val dateInfo = view.findViewById(R.id.dateInfo) as TextView
            val skyIcon = view.findViewById(R.id.skyIcon) as ImageView
            val skyInfo = view.findViewById(R.id.skyInfo) as TextView
            val temperatureInfo = view.findViewById(R.id.temperatureInfo) as TextView
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateInfo.text = simpleDateFormat.format(skycon.date)
            val sky = getSky(skycon.value)
            skyIcon.setImageResource(sky.icon)
            skyInfo.text = sky.info
            val tempText = "${temperature.min.toInt()} ~ ${temperature.max.toInt()} ℃"
            temperatureInfo.text = tempText
            forecastLayout.addView(view)
        }

        // 填充life_index.xml布局中的数据
        val lifeIndex = daily.lifeIndex
        coldRiskText.text = lifeIndex.coldRisk[0].desc
        dressingText.text = lifeIndex.dressing[0].desc
        ultravioletText.text = lifeIndex.ultraviolet[0].desc
        carWashingText.text = lifeIndex.carWashing[0].desc
        weatherLayout.visibility = View.VISIBLE

    }

    fun showAIChat(){
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(
            R.layout.ai_chat,
            null
        )
        dialog.setContentView(view)
        val bottomSheet =
            dialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            )
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            val height = (resources.displayMetrics.heightPixels * 0.8).toInt()
            it.layoutParams.height = height
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isDraggable = true
        }
        dialog.show()

        // 创建 Adapter
        val adapter = ChatAdapter(mutableListOf())
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewChat)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // 观察聊天消息
        aiViewModel.messages.observe(this) { messages ->
            adapter.updateMessages(messages)
            if (messages.isNotEmpty()) {
                recyclerView.scrollToPosition(
                    messages.size - 1
                )
            }
        }
        val etMessage = view.findViewById<EditText>(R.id.etMessage)
        val btnSend = view.findViewById<ImageButton>(R.id.btnSend)

        aiViewModel.isLoading.observe(this) { isLoading ->
            btnSend.isEnabled = !isLoading
        }


        btnSend.setOnClickListener {
            val message = etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                aiViewModel.sendMessage(message, currentWeather)
                etMessage.text.clear()
            }

        }
    }
}