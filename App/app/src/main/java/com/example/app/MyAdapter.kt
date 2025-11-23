package com.example.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

data class Post(
    val username: String,
    val caption: String,
    val timestamp: String,
    val imageUrl: String
)

data class Notification(
    val timestamp: String,
    val notificationText: String
)

sealed class FeedItem {
    data class PostItem(val post: Post) : FeedItem()
    data class NotificationItem(val notification: Notification) : FeedItem()
}


class MyAdapter(private val items: List<FeedItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_POST = 0
        private const val TYPE_NOTIFICATION = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is FeedItem.PostItem -> TYPE_POST
            is FeedItem.NotificationItem -> TYPE_NOTIFICATION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_POST -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_post, parent, false)
                PostViewHolder(view)
            }
            TYPE_NOTIFICATION -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_notifications, parent, false)
                NotificationViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is FeedItem.PostItem -> {
                val postHolder = holder as PostViewHolder
                val post = item.post
                postHolder.textUsername.text = post.username
                postHolder.textCaption.text = "${post.username}: ${post.caption}"
                postHolder.textTimestamp.text = post.timestamp

                Glide.with(postHolder.itemView.context)
                    .load(post.imageUrl)
                    .into(postHolder.imagePost)
            }
            is FeedItem.NotificationItem -> {
                val notifHolder = holder as NotificationViewHolder
                val notif = item.notification
                notifHolder.textTimestamp.text = notif.timestamp
                notifHolder.textNotification.text = notif.notificationText
            }
        }
    }

    override fun getItemCount(): Int = items.size

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageProfile: ImageView = itemView.findViewById(R.id.imageProfile)
        val imagePost: ImageView = itemView.findViewById(R.id.imagePost)
        val imageLike: ImageView = itemView.findViewById(R.id.imageLike)
        val imageComment: ImageView = itemView.findViewById(R.id.imageComment)
        val textUsername: TextView = itemView.findViewById(R.id.textUsername)
        val textCaption: TextView = itemView.findViewById(R.id.textCaption)
        val textTimestamp: TextView = itemView.findViewById(R.id.textTimestamp)
    }

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textTimestamp: TextView = itemView.findViewById(R.id.notificationTimeStamp)
        val textNotification: TextView = itemView.findViewById(R.id.notification)
    }
}
