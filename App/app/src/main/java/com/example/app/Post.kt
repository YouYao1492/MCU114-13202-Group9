package com.example.app

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class Post(
    @DocumentId val id: String = "",
    val userId: String = "",
    val username: String = "",
    var photoUrl: String = "",
    val contentUrl: String = "",
    val caption: String = "",
    val timestamp: Date = Date(),
    var likeCount: Int = 0,
    val commentCount: Int = 0,
    val likes: MutableList<String> = mutableListOf()
)