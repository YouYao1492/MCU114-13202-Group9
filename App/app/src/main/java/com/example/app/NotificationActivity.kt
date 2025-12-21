package com.example.app

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class NotificationActivity : BaseActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var myAdapter: MyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        val toolbar = findViewById<Toolbar>(R.id.activity_notification_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val clearAllButton = findViewById<Button>(R.id.activity_notification_button_clear_all)
        clearAllButton.setOnClickListener {
            clearAllNotifications()
        }

        recyclerView = findViewById(R.id.activity_notification_recyclerview_notifications)
        myAdapter = MyAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = myAdapter

        setupBottomNavigationBar()

        fetchNotifications()
    }

    private fun fetchNotifications() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            myAdapter.submitList(emptyList())
            return
        }

        val db = FirebaseFirestore.getInstance()
        db.collection("notifications")
            .whereEqualTo("userId", currentUser.uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("NotificationActivity", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val notifications = snapshots.toObjects(Notification::class.java)
                    myAdapter.submitList(notifications.map { FeedItem.NotificationItem(it) })
                }
            }
    }

    private fun clearAllNotifications() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            return
        }

        val db = FirebaseFirestore.getInstance()
        db.collection("notifications")
            .whereEqualTo("userId", currentUser.uid)
            .get()
            .addOnSuccessListener { documents ->
                val batch = db.batch()
                for (document in documents) {
                    batch.delete(document.reference)
                }
                batch.commit()
                    .addOnSuccessListener {
                        Toast.makeText(this, "All notifications cleared.", Toast.LENGTH_SHORT).show()
                        // Manually clear the adapter for immediate UI update
                        myAdapter.submitList(emptyList())
                    }
            }
    }
}