package com.example.app

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val toolbar = findViewById<Toolbar>(R.id.activity_edit_profile_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val usernameEditText = findViewById<TextInputEditText>(R.id.activity_edit_profile_edittext_username)
        val saveBtn = findViewById<Button>(R.id.activity_edit_profile_button_save)
        val backBtn = findViewById<ImageButton>(R.id.activity_edit_profile_imagebutton_back)

        val currentUser = FirebaseAuth.getInstance().currentUser
        usernameEditText.setText(currentUser?.displayName ?: "")

        backBtn.setOnClickListener {
            finish()
        }

        saveBtn.setOnClickListener {
            val newUsername = usernameEditText.text.toString()
            if (newUsername.isNotEmpty()) {
                updateUsername(newUsername)
            } else {
                Toast.makeText(this, "用戶名不能爲空", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUsername(newUsername: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "你必須登錄才能修改用戶名", Toast.LENGTH_SHORT).show()
            return
        }

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(newUsername)
            .build()

        currentUser.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateUserPosts(newUsername)
                }
            }
    }

    private fun updateUserPosts(newUsername: String) {
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid).update("searchableUsername", newUsername.toLowerCase())

            db.collection("posts")
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .addOnSuccessListener { documents ->
                    val batch = db.batch()
                    for (document in documents) {
                        batch.update(document.reference, "username", newUsername)
                    }
                    batch.commit()
                        .addOnSuccessListener {
                            Toast.makeText(this, "資料更新成功", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                }
        }
    }
}