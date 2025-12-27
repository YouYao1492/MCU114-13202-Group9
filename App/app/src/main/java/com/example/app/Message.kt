package com.example.app

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class Message(
    @DocumentId val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Date = Date()
)