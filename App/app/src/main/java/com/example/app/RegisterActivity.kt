package com.example.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val emailEditText = findViewById<TextInputEditText>(R.id.activity_register_edittext_account)
        val nameEditText = findViewById<TextInputEditText>(R.id.activity_register_edittext_name)
        val passwordEditText = findViewById<TextInputEditText>(R.id.activity_register_edittext_password)
        val confirmPasswordEditText = findViewById<TextInputEditText>(R.id.activity_register_edittext_confirm_password)
        val registerButton = findViewById<Button>(R.id.activity_register_button_confirm)

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val name = nameEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (email.isNotEmpty() && name.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (password == confirmPassword) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                val profileUpdates = UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build()

                                user?.updateProfile(profileUpdates)?.addOnCompleteListener { profileTask ->
                                    if (profileTask.isSuccessful) {
                                        createUserDocument(user.uid, email, name, "")
                                    }
                                }
                            } else {
                                Toast.makeText(this, "注冊失敗: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "密碼不相符", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "請填入所有資料", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createUserDocument(userId: String, email: String, username: String, photoUrl: String) {
        val db = FirebaseFirestore.getInstance()
        val user = User(uid = userId, email = email, username = username, searchableUsername = username.lowercase(Locale.getDefault()), photoUrl = photoUrl)
        db.collection("users").document(userId).set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "注冊成功", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
    }
}