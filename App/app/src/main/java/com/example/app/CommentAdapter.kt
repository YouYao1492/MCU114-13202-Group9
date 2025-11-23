package com.example.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Comment(
    val username: String,
    val text: String,
    val timestamp: String,
    val replies: List<Comment> = emptyList(),
    val level: Int = 0
)

class CommentAdapter(private val comments: List<Comment>) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    private val flattenedComments = flattenComments(comments)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(flattenedComments[position])
    }

    override fun getItemCount(): Int = flattenedComments.size

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameTextView: TextView = itemView.findViewById(R.id.textUsername)
        private val commentTextView: TextView = itemView.findViewById(R.id.textComment)
        private val timestampTextView: TextView = itemView.findViewById(R.id.textTimestamp)

        fun bind(comment: Comment) {
            usernameTextView.text = comment.username
            commentTextView.text = comment.text
            timestampTextView.text = comment.timestamp

            val indent = (comment.level * 32).dpToPx(itemView.context)
            val layoutParams = itemView.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.marginStart = indent
            itemView.layoutParams = layoutParams
        }
    }

    private fun flattenComments(comments: List<Comment>): List<Comment> {
        val flattenedList = mutableListOf<Comment>()
        for (comment in comments) {
            flattenedList.add(comment)
            if (comment.replies.isNotEmpty()) {
                flattenedList.addAll(flattenComments(comment.replies))
            }
        }
        return flattenedList
    }
}

private fun Int.dpToPx(context: android.content.Context): Int {
    return (this * context.resources.displayMetrics.density).toInt()
}