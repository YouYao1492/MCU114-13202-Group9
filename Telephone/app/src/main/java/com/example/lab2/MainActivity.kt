package com.example.lab2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class MainActivity : AppCompatActivity() {
    private lateinit var txtShow: TextView
        val CALL_PHONE_PERMISSION_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        txtShow=findViewById(R.id.txtShow)
        val btnZero=findViewById<Button>(R.id.btnZero)
        val btnOne=findViewById<Button>(R.id.btnOne)
        val btnTwo=findViewById<Button>(R.id.btnTwo)
        val btnThree=findViewById<Button>(R.id.btnThree)
        val btnFour=findViewById<Button>(R.id.btnFour)
        val btnFive=findViewById<Button>(R.id.btnFive)
        val btnSix=findViewById<Button>(R.id.btnSix)
        val btnSeven=findViewById<Button>(R.id.btnSeven)
        val btnEight=findViewById<Button>(R.id.btnEight)
        val btnNine=findViewById<Button>(R.id.btnNine)
        val btnStar=findViewById<Button>(R.id.btnStar)
        val btnClear=findViewById<Button>(R.id.btnClear)
        val btnCall=findViewById<Button>(R.id.btnCall)


        btnZero.setOnClickListener(myListener)
        btnOne.setOnClickListener(myListener)
        btnTwo.setOnClickListener(myListener)
        btnThree.setOnClickListener(myListener)
        btnFour.setOnClickListener(myListener)
        btnFive.setOnClickListener(myListener)
        btnSix.setOnClickListener(myListener)
        btnSeven.setOnClickListener(myListener)
        btnEight.setOnClickListener(myListener)
        btnNine.setOnClickListener(myListener)
        btnStar.setOnClickListener(myListener)
        btnClear.setOnClickListener(myListener)
        btnCall.setOnClickListener(myListener)
    }

    private val myListener = View.OnClickListener { v ->
        val s: String = txtShow.text.toString()
        when (v.id) {
            R.id.btnZero -> {
                txtShow.text = s + "0"
            }

            R.id.btnOne -> {
                txtShow.text = s + "1"
            }

            R.id.btnTwo -> {
                txtShow.text = s + "2"
            }

            R.id.btnThree -> {
                txtShow.text = s + "3"
            }

            R.id.btnFour -> {
                txtShow.text = s + "4"
            }

            R.id.btnFive -> {
                txtShow.text = s + "5"
            }

            R.id.btnSix -> {
                txtShow.text = s + "6"
            }

            R.id.btnSeven -> {
                txtShow.text = s + "7"
            }

            R.id.btnEight -> {
                txtShow.text = s + "8"
            }

            R.id.btnNine -> {
                txtShow.text = s + "9"
            }

            R.id.btnStar -> {
                txtShow.text = "$s*"
            }

            R.id.btnClear -> {
                txtShow.text = "電話號碼："
            }

            R.id.btnCall -> {
                val phoneNumber = s.replace("電話號碼：", "")

                if (!phoneNumber.isEmpty()) {
                    if (ActivityCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.CALL_PHONE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        // Make the call
                        val phoneIntent = Intent(Intent.ACTION_CALL)
                        phoneIntent.data = Uri.parse("tel:" + phoneNumber)
                        startActivity(phoneIntent)
                    } else {
                        // Request permission
                        requestCallPermission()
                    }
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Please enter a valid phone number",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
        }
    }
    private fun requestCallPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf<String>(Manifest.permission.CALL_PHONE), CALL_PHONE_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CALL_PHONE_PERMISSION_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

}