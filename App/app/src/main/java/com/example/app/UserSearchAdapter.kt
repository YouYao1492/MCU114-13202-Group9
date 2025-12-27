package com.example.app

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.app.User

class UserSearchAdapter : ListAdapter<User, UserSearchAdapter.UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user_search_message, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameTextView: TextView = itemView.findViewById(R.id.item_user_search_message_textview_username)
        private val profileImageView: ImageView = itemView.findViewById(R.id.item_user_search_message_imageview_profile)
        private val messageButton: ImageButton = itemView.findViewById(R.id.item_user_search_message_imagebutton_message)

        fun bind(user: User) {
            usernameTextView.text = user.username
            Glide.with(itemView.context)
                .load(user.photoUrl)
                .placeholder(R.drawable.ic_profile)
                .into(profileImageView)

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, ChatActivity::class.java)
                intent.putExtra("USER_ID", user.uid)
                itemView.context.startActivity(intent)
            }
            messageButton.setOnClickListener {
                val intent = Intent(itemView.context, ChatActivity::class.java)
                intent.putExtra("USER_ID", user.uid)
                itemView.context.startActivity(intent)
            }
        }
    }
}

class UserDiffCallback : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.uid == newItem.uid
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }
}