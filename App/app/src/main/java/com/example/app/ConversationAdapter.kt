package com.example.app

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale

class ConversationAdapter : ListAdapter<Conversation, ConversationAdapter.ConversationViewHolder>(ConversationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_conversation, parent, false)
        return ConversationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ConversationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameTextView: TextView = itemView.findViewById(R.id.item_conversation_textview_username)
        private val lastMessageTextView: TextView = itemView.findViewById(R.id.item_conversation_textview_last_message)
        private val timestampTextView: TextView = itemView.findViewById(R.id.item_conversation_textview_timestamp)
        private val profileImageView: ImageView = itemView.findViewById(R.id.item_conversation_imageview_profile)

        fun bind(conversation: Conversation) {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            val otherUserId = conversation.participantIds.first { it != currentUserId }

            usernameTextView.text = conversation.participantUsernames[otherUserId]
            lastMessageTextView.text = conversation.lastMessage
            timestampTextView.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(conversation.lastMessageTimestamp)

            Glide.with(itemView.context)
                .load(conversation.participantPhotoUrls[otherUserId])
                .placeholder(R.drawable.ic_profile)
                .into(profileImageView)

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, ChatActivity::class.java)
                intent.putExtra("USER_ID", otherUserId)
                itemView.context.startActivity(intent)
            }
        }
    }
}

class ConversationDiffCallback : DiffUtil.ItemCallback<Conversation>() {
    override fun areItemsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
        return oldItem == newItem
    }
}