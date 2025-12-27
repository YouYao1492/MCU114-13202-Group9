package com.example.app

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class Conversation(
    @DocumentId val id: String = "",
    val participantIds: List<String> = emptyList(),
    val participantUsernames: Map<String, String> = emptyMap(),
    val participantPhotoUrls: Map<String, String> = emptyMap(),
    val lastMessage: String = "",
    val lastMessageTimestamp: Date = Date()
)