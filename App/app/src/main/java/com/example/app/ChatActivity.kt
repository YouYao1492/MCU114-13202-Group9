package com.example.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import java.util.Date

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var conversationId: String
    private var otherUser: User? = null
    private var isActivityInForeground: Boolean = false

    companion object {
        private const val TAG = "ChatActivity"
        private const val CHANNEL_ID = "new_message_channel"
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            // Optionally, show a toast or a dialog explaining why the permission is needed
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val toolbar = findViewById<Toolbar>(R.id.activity_chat_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val otherUserId = intent.getStringExtra("USER_ID")
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (otherUserId == null || currentUserId == null) {
            finish()
            return
        }

        createNotificationChannel()
        askNotificationPermission()

        messageEditText = findViewById(R.id.activity_chat_edittext_message)
        sendButton = findViewById(R.id.activity_chat_button_send)

        messageEditText.isEnabled = false
        sendButton.isEnabled = false

        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(otherUserId).get().addOnSuccessListener {
            otherUser = it.toObject(User::class.java)
            findViewById<TextView>(R.id.activity_chat_textview_username).text = otherUser?.username
            messageEditText.isEnabled = true
            sendButton.isEnabled = true
        }

        findViewById<ImageButton>(R.id.activity_chat_imagebutton_back).setOnClickListener {
            finish()
        }

        conversationId = getConversationId(currentUserId, otherUserId)

        recyclerView = findViewById(R.id.activity_chat_recyclerview_messages)
        messageAdapter = MessageAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        recyclerView.adapter = messageAdapter

        sendButton.setOnClickListener {
            val messageText = messageEditText.text.toString()
            if (messageText.isNotEmpty()) {
                sendMessage(currentUserId, messageText)
            }
        }

        listenForMessages(currentUserId)
    }

    override fun onResume() {
        super.onResume()
        isActivityInForeground = true
    }

    override fun onPause() {
        super.onPause()
        isActivityInForeground = false
    }

    private fun getConversationId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) {
            "${userId1}_${userId2}"
        } else {
            "${userId2}_${userId1}"
        }
    }

    private fun sendMessage(senderId: String, text: String) {
        val safeOtherUser = otherUser ?: return // Failsafe

        val message = Message(conversationId = conversationId, senderId = senderId, text = text, timestamp = Date())
        val currentMessages = messageAdapter.currentList.toMutableList()
        currentMessages.add(message)
        messageAdapter.submitList(currentMessages)
        recyclerView.scrollToPosition(currentMessages.size - 1)
        messageEditText.text.clear()

        val db = FirebaseFirestore.getInstance()
        db.collection("messages").add(message)

        val currentUser = FirebaseAuth.getInstance().currentUser!!
        val conversationUpdates = mapOf(
            "lastMessage" to text.take(100),
            "lastMessageTimestamp" to message.timestamp,
            "participantIds" to listOf(senderId, safeOtherUser.uid),
            "participantUsernames" to mapOf(senderId to currentUser.displayName!!, safeOtherUser.uid to safeOtherUser.username),
            "participantPhotoUrls" to mapOf(senderId to currentUser.photoUrl.toString(), safeOtherUser.uid to safeOtherUser.photoUrl)
        )

        db.collection("conversations").document(conversationId).set(conversationUpdates, SetOptions.merge())

        // Create a notification for the recipient
        createMessageNotification(safeOtherUser.uid, currentUser, text)
    }

    private fun createMessageNotification(recipientId: String, sender: com.google.firebase.auth.FirebaseUser, messageText: String) {
        val db = FirebaseFirestore.getInstance()
        val notification = Notification(
            userId = recipientId,
            triggerUserId = sender.uid,
            triggerUsername = sender.displayName ?: "",
            triggerUserPhotoUrl = sender.photoUrl.toString(),
            type = "message",
            text = messageText
        )
        db.collection("notifications").add(notification)
    }

    private fun listenForMessages(currentUserId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("messages")
            .whereEqualTo("conversationId", conversationId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val messages = snapshots.toObjects(Message::class.java)
                    messageAdapter.submitList(messages)
                    if (messages.isNotEmpty()) {
                        recyclerView.scrollToPosition(messages.size - 1)

                        val lastMessage = messages.last()
                        if (lastMessage.senderId != currentUserId && !isActivityInForeground) {
                            showNewMessageNotification(lastMessage)
                        }
                    }
                }
            }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "New Messages"
            val descriptionText = "Notifications for new private messages"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun showNewMessageNotification(message: Message) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_message)
            .setContentTitle(otherUser?.username ?: "New Message")
            .setContentText(message.text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(message.id.hashCode(), builder.build())
    }
}