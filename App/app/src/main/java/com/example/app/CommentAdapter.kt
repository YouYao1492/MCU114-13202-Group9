package com.example.app

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Locale

class CommentAdapter(
    private val onReplyClicked: ((Comment) -> Unit)? = null
) : ListAdapter<Comment, CommentAdapter.CommentViewHolder>(CommentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameTextView: TextView = itemView.findViewById(R.id.item_comment_textview_username)
        private val commentTextView: TextView = itemView.findViewById(R.id.item_comment_textview_text)
        private val timestampTextView: TextView = itemView.findViewById(R.id.item_comment_textview_timestamp)
        private val replyTextView: TextView = itemView.findViewById(R.id.item_comment_textview_reply)
        private val profileImageView: ImageView = itemView.findViewById(R.id.item_comment_imageview_profile)

        fun bind(comment: Comment) {
            usernameTextView.text = comment.username
            commentTextView.text = comment.text
            timestampTextView.text = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(comment.timestamp)

            val indentation = (comment.level * 32).dpToPx(itemView.context)
            itemView.setPadding(indentation, itemView.paddingTop, itemView.paddingRight, itemView.paddingBottom)

            if (comment.level >= 2) {
                replyTextView.visibility = View.GONE
            } else {
                replyTextView.visibility = View.VISIBLE
                replyTextView.setOnClickListener {
                    onReplyClicked?.invoke(comment)
                }
            }

            Glide.with(itemView.context)
                .load(comment.photoUrl)
                .placeholder(R.drawable.ic_profile)
                .into(profileImageView)
        }
    }
}

class CommentDiffCallback : DiffUtil.ItemCallback<Comment>() {
    override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
        return oldItem == newItem
    }
}