package com.example.app

sealed class FeedItem {
    data class PostItem(val post: Post) : FeedItem()
    data class NotificationItem(val notification: Notification) : FeedItem()
}