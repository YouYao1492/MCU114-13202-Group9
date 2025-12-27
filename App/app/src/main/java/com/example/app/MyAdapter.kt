package com.example.app

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale

class MyAdapter(
    private val onLikeClicked: ((Post) -> Unit)? = null
) : ListAdapter<FeedItem, RecyclerView.ViewHolder>(FeedItemDiffCallback()) {

    companion object {
        private const val TYPE_POST = 0
        private const val TYPE_NOTIFICATION = 1
    }

    override fun getItemViewType(position: Int):
 Int {
        return when (getItem(position)) {
            is FeedItem.PostItem -> TYPE_POST
            is FeedItem.NotificationItem -> TYPE_NOTIFICATION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_POST -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
                PostViewHolder(view)
            }
            TYPE_NOTIFICATION -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notifications, parent, false)
                NotificationViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is FeedItem.PostItem -> (holder as PostViewHolder).bind(item.post)
            is FeedItem.NotificationItem -> (holder as NotificationViewHolder).bind(item.notification)
        }
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameTextView: TextView = itemView.findViewById(R.id.textUsername)
        private val captionTextView: TextView = itemView.findViewById(R.id.textCaption)
        private val timestampTextView: TextView = itemView.findViewById(R.id.textTimestamp)
        private val postImageView: ImageView = itemView.findViewById(R.id.imagePost)
        private val profileImageView: ImageView = itemView.findViewById(R.id.imageProfile)
        private val likeCountTextView: TextView = itemView.findViewById(R.id.likeCount)
        private val commentCountTextView: TextView = itemView.findViewById(R.id.commentCount)
        private val likeButton: ImageView = itemView.findViewById(R.id.imageLike)
        private val commentButton: ImageView = itemView.findViewById(R.id.imageComment)

        fun bind(post: Post) {
            usernameTextView.text = post.username
            captionTextView.text = post.caption
            timestampTextView.text = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(post.timestamp)
            likeCountTextView.text = post.likeCount.toString()
            commentCountTextView.text = post.commentCount.toString()

            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                if (post.likes.contains(currentUser.uid)) {
                    likeButton.setImageResource(R.drawable.ic_liked)
                } else {
                    likeButton.setImageResource(R.drawable.ic_like)
                }
            }

            Glide.with(itemView.context)
                .load(post.photoUrl)
                .placeholder(R.drawable.ic_profile)
                .into(profileImageView)

            Glide.with(itemView.context)
                .load(post.contentUrl)
                .into(postImageView)

            likeButton.setOnClickListener {
                it.startAnimation(AnimationUtils.loadAnimation(itemView.context, R.anim.like_anim))
                if (currentUser != null) {
                    onLikeClicked?.invoke(post)
                }
            }

            // This sets up the click listener for the comment icon
            commentButton.setOnClickListener {
                val intent = Intent(itemView.context, CommentActivity::class.java)
                intent.putExtra("POST_ID", post.id)
                itemView.context.startActivity(intent)
            }
        }
    }

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val notificationTextView: TextView = itemView.findViewById(R.id.notificationText)
        private val timestampTextView: TextView = itemView.findViewById(R.id.notificationTimestamp)
        private val profileImageView: ImageView = itemView.findViewById(R.id.triggerUserProfilePic)

        fun bind(notification: Notification) {
            notificationTextView.text = when (notification.type) {
                "message" -> "${notification.triggerUsername}: ${notification.text}"
                else -> "${notification.triggerUsername} ${notification.text}"
            }
            timestampTextView.text = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(notification.timestamp)

            if (notification.triggerUserPhotoUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(notification.triggerUserPhotoUrl)
                    .placeholder(R.drawable.ic_profile)
                    .into(profileImageView)
            } else {
                profileImageView.setImageResource(R.drawable.ic_profile)
            }

            if (notification.type == "message") {
                itemView.setOnClickListener {
                    val intent = Intent(itemView.context, ChatActivity::class.java)
                    intent.putExtra("USER_ID", notification.triggerUserId)
                    itemView.context.startActivity(intent)
                }
            }
        }
    }
}

class FeedItemDiffCallback : DiffUtil.ItemCallback<FeedItem>() {
    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return when {
            oldItem is FeedItem.PostItem && newItem is FeedItem.PostItem -> oldItem.post.id == newItem.post.id
            oldItem is FeedItem.NotificationItem && newItem is FeedItem.NotificationItem -> oldItem.notification.id == newItem.notification.id
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return oldItem == newItem
    }
}