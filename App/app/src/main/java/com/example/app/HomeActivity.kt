package com.example.app

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HomeActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var myAdapter: MyAdapter
    private lateinit var noResultsTextView: TextView
    private var allPosts = listOf<FeedItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        recyclerView = findViewById(R.id.activity_home_recyclerview_posts)
        noResultsTextView = findViewById(R.id.activity_home_textview_no_results)
        myAdapter = MyAdapter { post -> onLikeClicked(post) }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = myAdapter

        val searchView = findViewById<SearchView>(R.id.activity_home_searchview_search)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterPosts(newText)
                return true
            }
        })


        setupBottomNavigationBar()

        fetchPosts()
    }

    private fun fetchPosts() {
        val db = FirebaseFirestore.getInstance()
        db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("HomeActivity", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val posts = snapshots.toObjects(Post::class.java)
                    allPosts = posts.map { FeedItem.PostItem(it) }
                    filterPosts(findViewById<SearchView>(R.id.activity_home_searchview_search).query.toString())
                }
            }
    }

    private fun filterPosts(query: String?) {
        val filteredList = if (query.isNullOrEmpty()) {
            allPosts
        } else {
            allPosts.filter {
                it is FeedItem.PostItem && it.post.caption.contains(query, ignoreCase = true)
            }
        }

        if (filteredList.isEmpty() && !query.isNullOrEmpty()) {
            noResultsTextView.visibility = View.VISIBLE
        } else {
            noResultsTextView.visibility = View.GONE
        }
        myAdapter.submitList(filteredList)
    }

    private fun onLikeClicked(post: Post) {
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        val postRef = db.collection("posts").document(post.id)
        if (post.likes.contains(currentUser.uid)) {
            postRef.update("likes", FieldValue.arrayRemove(currentUser.uid))
            postRef.update("likeCount", FieldValue.increment(-1))
        } else {
            postRef.update("likes", FieldValue.arrayUnion(currentUser.uid))
            postRef.update("likeCount", FieldValue.increment(1))
            createLikeNotification(post)
        }
    }

    private fun createLikeNotification(post: Post) {
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser!!

        val notification = Notification(
            userId = post.userId,
            triggerUserId = currentUser.uid,
            triggerUsername = currentUser.displayName ?: "",
            triggerUserPhotoUrl = currentUser.photoUrl.toString(),
            type = "like",
            postId = post.id,
            text = "liked your post"
        )

        db.collection("notifications").add(notification)
    }
}