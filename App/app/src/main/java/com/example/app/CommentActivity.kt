package com.example.app

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class CommentActivity : AppCompatActivity() {

    private lateinit var commentRecyclerView: RecyclerView
    private lateinit var commentAdapter: CommentAdapter
    private var postId: String? = null
    private var parentComment: Comment? = null

    private lateinit var replyInfoLayout: LinearLayout
    private lateinit var replyInfoTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        val toolbar = findViewById<Toolbar>(R.id.activity_comment_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        postId = intent.getStringExtra("POST_ID")

        replyInfoLayout = findViewById(R.id.activity_comment_linearlayout_reply_info)
        replyInfoTextView = findViewById(R.id.activity_comment_textview_reply_info)

        commentRecyclerView = findViewById(R.id.activity_comment_recyclerview_comments)
        commentRecyclerView.layoutManager = LinearLayoutManager(this)
        commentAdapter = CommentAdapter { comment -> replyToComment(comment) }
        commentRecyclerView.adapter = commentAdapter

        val commentEditText = findViewById<EditText>(R.id.activity_comment_edittext_comment)
        val sendButton = findViewById<Button>(R.id.activity_comment_button_send)
        val cancelReplyButton = findViewById<Button>(R.id.activity_comment_button_cancel_reply)
        val backButton = findViewById<ImageButton>(R.id.activity_comment_imagebutton_back)

        sendButton.setOnClickListener {
            val commentText = commentEditText.text.toString()
            if (commentText.isNotEmpty()) {
                addComment(commentText)
            }
        }

        cancelReplyButton.setOnClickListener {
            cancelReply()
        }

        backButton.setOnClickListener {
            finish()
        }

        fetchComments()
    }

    private fun fetchComments() {
        if (postId == null) return

        val db = FirebaseFirestore.getInstance()
        db.collection("comments")
            .whereEqualTo("postId", postId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("CommentActivity", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val allComments = snapshots.toObjects(Comment::class.java)
                    val commentMap = allComments.groupBy { it.parentId }

                    val sortedComments = mutableListOf<Comment>()
                    fun addReplies(comment: Comment, level: Int) {
                        sortedComments.add(comment.copy(level = level))
                        commentMap[comment.id]?.forEach { reply ->
                            addReplies(reply, level + 1)
                        }
                    }

                    commentMap[null]?.forEach { rootComment ->
                        addReplies(rootComment, 0)
                    }

                    commentAdapter.submitList(sortedComments)
                }
            }
    }

    private fun addComment(commentText: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null || postId == null) {
            Toast.makeText(this, "必須登錄才能留言", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance()

        val level = parentComment?.level?.plus(1) ?: 0
        val parentId = parentComment?.id

        val comment = Comment(
            postId = postId!!,
            userId = currentUser.uid,
            username = currentUser.displayName ?: "",
            photoUrl = currentUser.photoUrl.toString(),
            text = commentText,
            level = level,
            parentId = parentId
        )

        db.collection("comments").add(comment)
            .addOnSuccessListener {
                db.collection("posts").document(postId!!)
                    .update("commentCount", FieldValue.increment(1))
                createCommentNotification(postId!!, commentText)
                
                // Refresh the activity to show the new comment instantly
                finish()
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "留言失敗: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createCommentNotification(postId: String, commentText: String) {
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser!!

        db.collection("posts").document(postId).get().addOnSuccessListener { postDocument ->
            val post = postDocument.toObject(Post::class.java)
            if (post != null) {
                val notification = Notification(
                    userId = post.userId,
                    triggerUserId = currentUser.uid,
                    triggerUsername = currentUser.displayName ?: "",
                    triggerUserPhotoUrl = currentUser.photoUrl.toString(),
                    type = "comment",
                    postId = postId,
                    text = "對你的貼文留言: $commentText"
                )
                db.collection("notifications").add(notification)
            }
        }
    }

    private fun replyToComment(comment: Comment) {
        parentComment = comment
        replyInfoTextView.text = "Replying to ${comment.username}"
        replyInfoLayout.visibility = View.VISIBLE
        findViewById<EditText>(R.id.activity_comment_edittext_comment).requestFocus()
    }

    private fun cancelReply() {
        parentComment = null
        replyInfoLayout.visibility = View.GONE
    }
}