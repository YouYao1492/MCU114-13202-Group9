package com.example.mealorder

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SideDishesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        setContentView(R.layout.activity_side_dishes)
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v: View?, insets: WindowInsetsCompat? ->
            val systemBars = insets!!.getInsets(WindowInsetsCompat.Type.systemBars())
            v!!.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val checkBox1 = findViewById<CheckBox>(R.id.checkBox1)
        val checkBox2 = findViewById<CheckBox>(R.id.checkBox2)
        val checkBox3 = findViewById<CheckBox>(R.id.checkBox3)
        val btnDone=findViewById<Button>(R.id.done)

        btnDone.setOnClickListener {
            val selectedDrink=mutableListOf<String>()

            if(checkBox1.isChecked) selectedDrink.add(checkBox1.text.toString())
            if(checkBox2.isChecked) selectedDrink.add(checkBox2.text.toString())
            if(checkBox3.isChecked) selectedDrink.add(checkBox3.text.toString())

            val result=selectedDrink.joinToString(", ")

            val b=bundleOf("Side Dishes" to result)
            val i = Intent().putExtras(b)
            setResult(RESULT_OK, i)
            finish()
        }
    }
}