package com.example.app

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class Notification(
    @DocumentId val id: String = "",
    val userId: String = "", // The user who receives the notification
    val triggerUserId: String = "",
    val triggerUsername: String = "",
    val triggerUserPhotoUrl: String = "",
    val type: String = "", // "like" or "comment"
    val postId: String = "",
    val text: String = "", // e.g., "liked your post" or "commented on your post: ..."
    val timestamp: Date = Date()
)