package com.example.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var myAdapter: MyAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val posts = listOf(
            Post(
                username = "alice",
                caption = "Enjoying the sunset 🌅",
                timestamp = "2 hours ago",
                imageUrl = "https://picsum.photos/600/400?random=1"
            ),
            Post(
                username = "bob",
                caption = "Coffee time ☕",
                timestamp = "5 hours ago",
                imageUrl = "https://picsum.photos/600/400?random=2"
            ),
            Post(
                username = "charlie",
                caption = "Weekend vibes 🎉",
                timestamp = "1 day ago",
                imageUrl = "https://picsum.photos/600/400?random=3"
            )
        )

        val feedItems = posts.map { FeedItem.PostItem(it) }

        myAdapter = MyAdapter(feedItems)
        recyclerView.adapter = myAdapter

        return view
    }

}