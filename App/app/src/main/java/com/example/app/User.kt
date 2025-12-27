package com.example.app

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId val uid: String = "",
    val email: String = "",
    val username: String = "",
    val searchableUsername: String = "",
    val photoUrl: String = "",
    val followers: List<String> = emptyList(),
    val following: List<String> = emptyList()
)