package com.example.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        emailEditText = findViewById(R.id.activity_login_edittext_account)
        val passwordEditText = findViewById<TextInputEditText>(R.id.activity_login_edittext_password)
        val loginButton = findViewById<Button>(R.id.activity_login_button_confirm)
        val recentAccountsTextView = findViewById<TextView>(R.id.activity_login_textview_recent_accounts)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            saveRecentAccount(email)
                            Toast.makeText(this, "Login successful.", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please enter email and password.", Toast.LENGTH_SHORT).show()
            }
        }

        recentAccountsTextView.setOnClickListener {
            showRecentAccountsMenu()
        }
    }

    private fun saveRecentAccount(email: String) {
        val sharedPreferences = getSharedPreferences("accounts", Context.MODE_PRIVATE)
        val recentAccounts = sharedPreferences.getStringSet("recent_accounts", mutableSetOf()) ?: mutableSetOf()
        recentAccounts.add(email)
        sharedPreferences.edit().putStringSet("recent_accounts", recentAccounts).apply()
    }

    private fun showRecentAccountsMenu() {
        val sharedPreferences = getSharedPreferences("accounts", Context.MODE_PRIVATE)
        val recentAccounts = sharedPreferences.getStringSet("recent_accounts", null)

        if (recentAccounts.isNullOrEmpty()) {
            Toast.makeText(this, "No recent accounts.", Toast.LENGTH_SHORT).show()
            return
        }

        val popupMenu = PopupMenu(this, findViewById(R.id.activity_login_textview_recent_accounts))
        for (account in recentAccounts) {
            popupMenu.menu.add(account)
        }

        popupMenu.setOnMenuItemClickListener { item ->
            emailEditText.setText(item.title)
            true
        }

        popupMenu.show()
    }
}