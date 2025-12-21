package com.example.app

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.util.UUID

class PostActivity : AppCompatActivity() {

    private lateinit var imagePreview: ImageView
    private var imageUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            imagePreview.setImageURI(it)
        }
    }

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success) {
            imageUri?.let {
                imagePreview.setImageURI(it)
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                launchCamera()
            } else {
                Toast.makeText(this, "Camera permission is required to take pictures.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        val toolbar = findViewById<Toolbar>(R.id.activity_post_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        imagePreview = findViewById(R.id.activity_post_imageview_preview)
        val editContent = findViewById<EditText>(R.id.activity_post_edittext_content)
        val postButton = findViewById<Button>(R.id.activity_post_button_post)
        val cameraButton = findViewById<ImageButton>(R.id.activity_post_imagebutton_camera)
        val galleryButton = findViewById<ImageButton>(R.id.activity_post_imagebutton_file)
        val backButton = findViewById<ImageButton>(R.id.activity_post_imagebutton_back)

        backButton.setOnClickListener {
            finish()
        }

        cameraButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                launchCamera()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        galleryButton.setOnClickListener {
            pickImage.launch("image/*")
        }

        postButton.setOnClickListener {
            val caption = editContent.text.toString()
            val currentUser = FirebaseAuth.getInstance().currentUser

            if (currentUser == null) {
                Toast.makeText(this, "You must be logged in to post.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (imageUri != null && caption.isNotEmpty()) {
                val randomImageUrl = "https://picsum.photos/seed/${UUID.randomUUID()}/800/600"
                createPost(randomImageUrl, caption)
            } else {
                Toast.makeText(this, "Please select an image and write a caption.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun launchCamera() {
        val photoUri = getTmpFileUri()
        imageUri = photoUri
        takePicture.launch(photoUri)
    }

    private fun getTmpFileUri(): Uri {
        val tmpFile = File.createTempFile("tmp_image_file", ".png", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }

        return FileProvider.getUriForFile(applicationContext, "${applicationContext.packageName}.provider", tmpFile)
    }

    private fun createPost(imageUrl: String, caption: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser!! // We know the user is not null here

        val db = FirebaseFirestore.getInstance()
        val post = Post(
            userId = currentUser.uid,
            username = currentUser.displayName ?: "",
            photoUrl = currentUser.photoUrl.toString(),
            contentUrl = imageUrl,
            caption = caption
        )

        db.collection("posts").add(post)
            .addOnSuccessListener {
                Toast.makeText(this, "Post created successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to create post: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}