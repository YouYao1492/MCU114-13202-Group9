package com.example.majorcitytemp

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class MainActivity : AppCompatActivity() {

    // Data Classes for Gson Parsing
    data class WeatherResponse(val main: MainData, val name: String)
    data class MainData(val temp: Double, val humidity: Int)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnGetTemp = findViewById<Button>(R.id.btnGetTemp)

        // 原本的 cities 名稱保持不變
        val cities = listOf(
            "New Taipei", "Taipei", "Taoyuan", "Hsinchu", "Miaoli",
            "Taichung", "Changhua", "Nantou", "Yunlin", "Chiayi",
            "Tainan", "Kaohsiung", "Pingtung", "Yilan", "Hualien",
            "Taitung", "Keelung", "Penghu", "Jingmen", "Lianjiang"
        )

        // 按下按鈕 → 跳出城市選單
        btnGetTemp.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Select a City")
                .setItems(cities.toTypedArray()) { _, which ->
                    val selectedCity = cities[which]
                    fetchWeather(selectedCity)
                }
                .show()
        }
    }

    // 單一城市查詢 API
    private fun fetchWeather(cityName: String) {
        val apiKey = "28edde27cdedb0ee42c71569d1689492"
        val client = OkHttpClient()

        CoroutineScope(Dispatchers.IO).launch {
            val url =
                "https://api.openweathermap.org/data/2.5/weather?q=$cityName&units=metric&appid=$apiKey"

            try {
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (body != null) {
                        val weatherData = Gson().fromJson(body, WeatherResponse::class.java)

                        val result =
                            "${weatherData.name}\nTemperature: ${weatherData.main.temp}°C\nHumidity: ${weatherData.main.humidity}%"

                        withContext(Dispatchers.Main) {
                            showAlertDialog("Weather Info", result)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showAlertDialog("Error", "Failed (code ${response.code})")
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    showAlertDialog("Error", "Network error")
                }
            }
        }
    }

    // 顯示結果
    private fun showAlertDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }
}
