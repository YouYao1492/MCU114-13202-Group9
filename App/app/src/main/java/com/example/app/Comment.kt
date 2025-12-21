package com.example.app

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class Comment(
    @DocumentId val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val username: String = "",
    var photoUrl: String = "",
    val text: String = "",
    val timestamp: Date = Date(),
    val level: Int = 0,
    val parentId: String? = null
)