package com.example.homework8

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var items = ArrayList<String>()
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var dbrw: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        dbrw = MyDBHelper(this).writableDatabase

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        findViewById<ListView>(R.id.listView).adapter = adapter

        setListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        dbrw.close()
    }

    private fun setListener() {
        val edBrand = findViewById<EditText>(R.id.edBrand)
        val edYear = findViewById<EditText>(R.id.edyear)
        val edPrice = findViewById<EditText>(R.id.edPrice)

        // 新增
        findViewById<Button>(R.id.btnInsert).setOnClickListener {
            val brand = edBrand.text.toString()
            val yearStr = edYear.text.toString()
            val priceStr = edPrice.text.toString()

            if (brand.isEmpty() || yearStr.isEmpty() || priceStr.isEmpty()) {
                showToast("欄位請勿留空")
                return@setOnClickListener
            }

            val year = yearStr.toIntOrNull()
            val price = priceStr.toIntOrNull()
            if (year == null || price == null) {
                showToast("年份與價格必須是數字")
                return@setOnClickListener
            }

            try {
                dbrw.execSQL(
                    "INSERT INTO myTable(brand, carYear, price) VALUES(?,?,?)",
                    arrayOf(brand, year, price)
                )
                showToast("新增成功")
                cleanEditText()
            } catch (e: Exception) {
                showToast("新增失敗：${e.message}")
            }
        }

        findViewById<Button>(R.id.btnUpdate).setOnClickListener {
            val brand = edBrand.text.toString()
            val yearStr = edYear.text.toString()
            val priceStr = edPrice.text.toString()

            if (brand.isEmpty()) {
                showToast("廠牌請勿留空")
                return@setOnClickListener
            }

            val year = yearStr.toIntOrNull()
            val price = priceStr.toIntOrNull()
            if (year == null || price == null) {
                showToast("年份與價格必須是數字")
                return@setOnClickListener
            }

            try {
                dbrw.execSQL(
                    "UPDATE myTable SET carYear=?, price=? WHERE brand=?",
                    arrayOf(year, price, brand)
                )
                showToast("修改成功")
                cleanEditText()
            } catch (e: Exception) {
                showToast("修改失敗：${e.message}")
            }
        }

        findViewById<Button>(R.id.btnDelete).setOnClickListener {
            val brand = edBrand.text.toString()
            if (brand.isEmpty()) {
                showToast("廠牌請勿留空")
                return@setOnClickListener
            }

            try {
                dbrw.execSQL(
                    "DELETE FROM myTable WHERE brand=?",
                    arrayOf(brand)
                )
                showToast("刪除成功")
                cleanEditText()
            } catch (e: Exception) {
                showToast("刪除失敗：${e.message}")
            }
        }

        findViewById<Button>(R.id.btnQuery).setOnClickListener {
            val brand = edBrand.text.toString()
            val query = if (brand.isEmpty()) {
                "SELECT * FROM myTable"
            } else {
                "SELECT * FROM myTable WHERE brand=?"
            }

            val c = if (brand.isEmpty()) {
                dbrw.rawQuery(query, null)
            } else {
                dbrw.rawQuery(query, arrayOf(brand))
            }

            items.clear()
            showToast("共有 ${c.count} 筆資料")

            if (c.moveToFirst()) {
                do {
                    val b = c.getString(0)
                    val y = c.getInt(1)
                    val p = c.getInt(2)
                    items.add("廠牌:$b   年份:$y   價格:$p")
                } while (c.moveToNext())
            }

            adapter.notifyDataSetChanged()
            c.close()
        }
    }

    private fun showToast(text: String) {
        Toast.makeText(this@MainActivity, text, Toast.LENGTH_LONG).show()
    }

    private fun cleanEditText() {
        findViewById<EditText>(R.id.edBrand).setText("")
        findViewById<EditText>(R.id.edyear).setText("")
        findViewById<EditText>(R.id.edPrice).setText("")
    }
}
