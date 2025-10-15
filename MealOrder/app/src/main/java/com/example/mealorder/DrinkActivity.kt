package com.example.mealorder

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DrinkActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        setContentView(R.layout.activity_drink)
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v: View?, insets: WindowInsetsCompat? ->
            val systemBars = insets!!.getInsets(WindowInsetsCompat.Type.systemBars())
            v!!.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val drink = findViewById<RadioGroup>(R.id.radioGroup)
        val btnDone=findViewById<Button>(R.id.done)

        btnDone.setOnClickListener {
            val b = bundleOf(
                "Drink" to drink.findViewById<RadioButton>(
                    drink.checkedRadioButtonId
                ).text.toString()
            )
            val i = Intent().putExtras(b)
            setResult(RESULT_OK, i)
            finish()
        }
    }
}