package com.example.mealorder

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.w3c.dom.Text

class MainActivity : AppCompatActivity() {
    var mainMeal: String? = ""
    var sideDishes : String? = ""
    var drink : String? = ""

    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val intent = result.data
            intent?.getStringExtra("Main Meal")?.let { mainMeal = it }
            intent?.getStringExtra("Side Dishes")?.let { sideDishes = it }
            intent?.getStringExtra("Drink")?.let { drink = it }

            val menu = findViewById<TextView>(R.id.menu)
            menu.text = "Current Selection\nMain Meal: $mainMeal\nSide Dishes: $sideDishes\nDrink: $drink"
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val mainMeal=findViewById<Button>(R.id.mainMeal)
        val sideDishes=findViewById<Button>(R.id.sideDishes)
        val drink=findViewById<Button>(R.id.drink)
        val order=findViewById<Button>(R.id.order)


        mainMeal.setOnClickListener(myListener)
        sideDishes.setOnClickListener(myListener)
        drink.setOnClickListener(myListener)
        order.setOnClickListener(myListener)
    }

    private val myListener = View.OnClickListener { v ->
        when(v.id){
            R.id.mainMeal->{
                val intent = Intent(this, MainMealActivity::class.java)
                startForResult.launch(intent)
            }
            R.id.sideDishes->{
                val intent = Intent(this, SideDishesActivity::class.java)
                startForResult.launch(intent)
            }
            R.id.drink->{
                val intent = Intent(this, DrinkActivity::class.java)
                startForResult.launch(intent)
            }
            R.id.order->{
                if (mainMeal.equals("") || sideDishes.equals("") || drink.equals("")) {
                    Toast.makeText(this,"Please select a main meal, at least one side dish, and a drink.",Toast.LENGTH_LONG).show()
                } else {
                    val menuText = findViewById<TextView>(R.id.menu).text.toString()

                    AlertDialog.Builder(this)
                        .setTitle("Submit Order")
                        .setMessage(menuText)
                        .setPositiveButton("Submit") { d, _ ->
                            d.dismiss()
                            Toast.makeText(this, "Order submitted!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, ConfirmActivity::class.java)
                            intent.putExtra("Order Summary", menuText)
                            startActivity(intent)
                        }
                        .setNegativeButton("Cancel", null).show()
                }
            }
        }
    }
}