package com.example.app

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    protected fun setupBottomNavigationBar() {
        val homeButton = findViewById<ImageButton>(R.id.homeBtn)
        val addButton = findViewById<ImageButton>(R.id.addBtn)
        val messageButton = findViewById<ImageButton>(R.id.messageBtn)
        val profileButton = findViewById<ImageButton>(R.id.profileBtn)

        homeButton.setOnClickListener {
            if (this !is HomeActivity) {
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
            }
        }

        addButton.setOnClickListener {
            if (this !is PostActivity) {
                val intent = Intent(this, PostActivity::class.java)
                startActivity(intent)
            }
        }

        messageButton.setOnClickListener {
            if (this !is NotificationActivity) {
                val intent = Intent(this, NotificationActivity::class.java)
                startActivity(intent)
            }
        }

        profileButton.setOnClickListener {
            if (this !is UserActivity) {
                val intent = Intent(this, UserActivity::class.java)
                startActivity(intent)
            }
        }
    }
}