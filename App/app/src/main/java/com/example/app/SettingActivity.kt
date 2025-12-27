package com.example.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.Glide
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class SettingActivity : AppCompatActivity() {

    private lateinit var profilePicPreview: ImageView
    private var imageUri: Uri? = null
    private lateinit var networkStatusTextView: TextView
    private lateinit var networkChangeReceiver: BroadcastReceiver

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            profilePicPreview.setImageURI(it)
            updateProfilePicture(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        overridePendingTransition(R.anim.fade_in, 0)

        profilePicPreview = findViewById(R.id.activity_setting_imageview_profile_preview)
        networkStatusTextView = findViewById(R.id.activity_setting_textview_network_status)
        val changeProfilePicBtn = findViewById<Button>(R.id.activity_setting_button_change_profile_pic)
        val personalInfoLabel = findViewById<TextView>(R.id.activity_setting_textview_personal_info_label)
        val musicSwitch = findViewById<SwitchMaterial>(R.id.activity_setting_switch_music)
        val logoutBtn = findViewById<Button>(R.id.activity_setting_button_logout)
        val backBtn = findViewById<ImageButton>(R.id.activity_setting_imagebutton_back)
        val themeSpinner = findViewById<Spinner>(R.id.activity_setting_spinner_theme)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            Glide.with(this)
                .load(currentUser.photoUrl)
                .placeholder(R.drawable.ic_profile)
                .into(profilePicPreview)
        }

        changeProfilePicBtn.setOnClickListener {
            pickImage.launch("image/*")
        }

        personalInfoLabel.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        backBtn.setOnClickListener {
            finish()
        }

        val sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)

        musicSwitch.isChecked = sharedPreferences.getBoolean("music_enabled", false)

        musicSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("music_enabled", isChecked).apply()
            if (isChecked) {
                startService(Intent(this, MusicService::class.java).apply { action = "PLAY" })
            } else {
                startService(Intent(this, MusicService::class.java).apply { action = "STOP" })
            }
        }

        logoutBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        val themes = arrayOf("Light", "Dark", "System Default")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, themes)
        themeSpinner.adapter = adapter

        val selectedTheme = sharedPreferences.getInt("theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        themeSpinner.setSelection(when (selectedTheme) {
            AppCompatDelegate.MODE_NIGHT_NO -> 0
            AppCompatDelegate.MODE_NIGHT_YES -> 1
            else -> 2
        })

        themeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val theme = when (position) {
                    0 -> AppCompatDelegate.MODE_NIGHT_NO
                    1 -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
                AppCompatDelegate.setDefaultNightMode(theme)
                sharedPreferences.edit().putInt("theme", theme).apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        setupNetworkStatusReceiver()
    }

    private fun setupNetworkStatusReceiver() {
        networkChangeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                updateNetworkStatusUI()
            }
        }
        registerReceiver(networkChangeReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        updateNetworkStatusUI() // Initial check
    }

    private fun updateNetworkStatusUI() {
        val connManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connManager.activeNetworkInfo
        if (activeNetwork?.isConnected == true) {
            networkStatusTextView.text = "Good"
        } else {
            networkStatusTextView.text = "Bad"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(networkChangeReceiver)
    }

    private fun updateProfilePicture(uri: Uri) {
        val randomImageUrl = "https://picsum.photos/seed/${UUID.randomUUID()}/200/200"
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setPhotoUri(Uri.parse(randomImageUrl))
            .build()

        currentUser.updateProfile(profileUpdates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                updateUserRelatedData(randomImageUrl)
            }
        }
    }

    private fun updateUserRelatedData(newPhotoUrl: String) {
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        // Update the user's document in the 'users' collection
        val userRef = db.collection("users").document(currentUser.uid)
        userRef.update("photoUrl", newPhotoUrl)

        // Update the user's posts in the 'posts' collection
        db.collection("posts")
            .whereEqualTo("userId", currentUser.uid)
            .get()
            .addOnSuccessListener { documents ->
                val batch = db.batch()
                for (document in documents) {
                    batch.update(document.reference, "photoUrl", newPhotoUrl)
                }
                batch.commit().addOnSuccessListener {
                    Toast.makeText(this, "成功更新頭像", Toast.LENGTH_SHORT).show()
                }
            }
    }
}