package com.example.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ConversationsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var conversationAdapter: ConversationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversations)

        val toolbar = findViewById<Toolbar>(R.id.activity_conversations_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        findViewById<ImageButton>(R.id.activity_conversations_imagebutton_back).setOnClickListener {
            finish()
        }

        recyclerView = findViewById(R.id.activity_conversations_recyclerview_conversations)
        recyclerView.layoutManager = LinearLayoutManager(this)
        conversationAdapter = ConversationAdapter()
        recyclerView.adapter = conversationAdapter

        findViewById<FloatingActionButton>(R.id.activity_conversations_fab_new_message).setOnClickListener {
            val intent = Intent(this, UserSearchActivity::class.java)
            startActivity(intent)
        }

        fetchConversations()
    }

    private fun fetchConversations() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        val db = FirebaseFirestore.getInstance()
        db.collection("conversations")
            .whereArrayContains("participantIds", currentUser.uid)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("ConversationsActivity", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val conversations = snapshots.toObjects(Conversation::class.java)
                    conversationAdapter.submitList(conversations)
                }
            }
    }
}