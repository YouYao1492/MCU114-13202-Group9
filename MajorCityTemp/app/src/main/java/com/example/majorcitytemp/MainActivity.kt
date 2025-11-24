package com.example.majorcitytemp

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class MainActivity : AppCompatActivity() {

    // Gson 解析用資料類別
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
        btnGetTemp.setOnClickListener {
            fetchWeatherForAllCities()
        }
    }

    private fun fetchWeatherForAllCities() {
        val cities = listOf(
            "New Taipei", "Taipei", "Taoyuan", "Hsinchu", "Miaoli",
            "Taichung", "Changhua", "Nantou", "Yunlin", "Chiayi",
            "Tainan", "Kaohsiung", "Pingtung", "Yilan", "Hualien",
            "Taitung", "Keelung", "Penghu", "Kinmen", "Lienchiang"
        )

        val apiKey = "28edde27cdedb0ee42c71569d1689492"
        val client = OkHttpClient()

        // 使用 lifecycleScope 啟動協程
        lifecycleScope.launch(Dispatchers.IO) {
            val results = mutableListOf<String>()

            for (city in cities) {
                try {
                    val url = "https://api.openweathermap.org/data/2.5/weather?q=$city&units=metric&appid=$apiKey"
                    val request = Request.Builder().url(url).build()
                    val response = client.newCall(request).execute()

                    if (response.isSuccessful) {
                        val responseData = response.body?.string()
                        if (responseData != null) {
                            val weatherData = Gson().fromJson(responseData, WeatherResponse::class.java)
                            results.add("${weatherData.name}: ${weatherData.main.temp}°C, ${weatherData.main.humidity}%")
                        } else {
                            results.add("$city: No data")
                        }
                    } else {
                        results.add("$city: Failed (Code ${response.code})")
                    }
                } catch (e: IOException) {
                    results.add("$city: Network error")
                }
            }

            // 回到主線程更新 UI
            withContext(Dispatchers.Main) {
                showAlertDialog("Taiwan Major City Weather", results.joinToString("\n"))
            }
        }
    }

    private fun showAlertDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }
}
