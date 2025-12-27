package com.example.app

import android.os.Bundle
import android.widget.ImageButton
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class UserSearchActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var userSearchAdapter: UserSearchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_search)

        val toolbar = findViewById<Toolbar>(R.id.activity_user_search_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        findViewById<ImageButton>(R.id.activity_user_search_imagebutton_back).setOnClickListener {
            finish()
        }

        recyclerView = findViewById(R.id.activity_user_search_recyclerview_users)
        recyclerView.layoutManager = LinearLayoutManager(this)
        userSearchAdapter = UserSearchAdapter()
        recyclerView.adapter = userSearchAdapter

        val searchView = findViewById<SearchView>(R.id.activity_user_search_searchview)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    userSearchAdapter.submitList(emptyList())
                } else {
                    searchUsers(newText)
                }
                return true
            }
        })
    }

    private fun searchUsers(query: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .orderBy("searchableUsername")
            .startAt(query.toLowerCase())
            .endAt(query.toLowerCase() + "\uf8ff")
            .get()
            .addOnSuccessListener { documents ->
                val users = documents.toObjects(User::class.java)
                userSearchAdapter.submitList(users)
            }
    }
}