package com.example.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class UserActivity : BaseActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var myAdapter: MyAdapter
    private lateinit var emptyView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        val toolbar = findViewById<Toolbar>(R.id.activity_user_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val usernameTextView = findViewById<TextView>(R.id.activity_user_textview_username)
        val settingsButton = findViewById<ImageButton>(R.id.activity_user_imagebutton_settings)
        val profileImageView = findViewById<ImageView>(R.id.activity_user_imageview_profile)
        emptyView = findViewById(R.id.activity_user_textview_empty)
        recyclerView = findViewById(R.id.activity_user_recyclerview_posts)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            usernameTextView.text = currentUser.displayName
            Glide.with(this)
                .load(currentUser.photoUrl)
                .placeholder(R.drawable.ic_profile)
                .into(profileImageView)
        } else {
            usernameTextView.text = "Guest"
        }

        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        myAdapter = MyAdapter { post -> onLikeClicked(post) }
        recyclerView.adapter = myAdapter

        setupBottomNavigationBar()
        checkEmptyView(true) // Check initial state
        fetchCurrentUserPosts()
    }

    private fun fetchCurrentUserPosts() {
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
            Log.w("UserActivity", "Not logged in, cannot fetch posts.")
            Toast.makeText(this, "請登錄后查看", Toast.LENGTH_SHORT).show()
            myAdapter.submitList(emptyList())
            checkEmptyView(false)
            return
        }

        db.collection("posts")
            .whereEqualTo("userId", currentUser.uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("UserActivity", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val posts = snapshots.toObjects(Post::class.java)
                    myAdapter.submitList(posts.map { FeedItem.PostItem(it) })
                    checkEmptyView(posts.isEmpty())
                    Log.d("UserActivity", "Fetched ${posts.size} posts for current user.")
                }
            }
    }

    private fun checkEmptyView(isEmpty: Boolean) {
        if (isEmpty) {
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
        }
    }

    private fun onLikeClicked(post: Post) {
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        val postRef = db.collection("posts").document(post.id)

        val newLikes = post.likes.toMutableList()
        if (newLikes.contains(currentUser.uid)) {
            newLikes.remove(currentUser.uid)
            postRef.update("likes", FieldValue.arrayRemove(currentUser.uid))
            postRef.update("likeCount", FieldValue.increment(-1))
        } else {
            newLikes.add(currentUser.uid)
            postRef.update("likes", FieldValue.arrayUnion(currentUser.uid))
            postRef.update("likeCount", FieldValue.increment(1))
            createLikeNotification(post)
        }
    }

    private fun createLikeNotification(post: Post) {
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser!!

        val notification = Notification(
            userId = post.userId,
            triggerUserId = currentUser.uid,
            triggerUsername = currentUser.displayName ?: "",
            triggerUserPhotoUrl = currentUser.photoUrl.toString(),
            type = "like",
            postId = post.id,
            text = "贊了你的貼文"
        )

        db.collection("notifications").add(notification)
    }
}