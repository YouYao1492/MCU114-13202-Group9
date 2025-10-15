package com.example.mealorder

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainMealActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_meal)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    val mainMeal = findViewById<RadioGroup>(R.id.radioGroup)
    val btnDone=findViewById<Button>(R.id.done)

    btnDone.setOnClickListener {
        val b = bundleOf(
            "Main Meal" to mainMeal.findViewById<RadioButton>(
                mainMeal.checkedRadioButtonId
            ).text.toString()
        )
        val i = Intent().putExtras(b)
        setResult(RESULT_OK, i)
        finish()
    }
    }
}