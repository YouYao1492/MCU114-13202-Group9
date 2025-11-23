package com.example.app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class NotificationFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var myAdapter: MyAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val notificationList = listOf(
            Notification(
                timestamp = "2 hours ago",
                notificationText = "alice liked your post"
            ),
            Notification(
                timestamp = "5 hours ago",
                notificationText = "bob commented on your post"
            )
        )

        val feedItems = notificationList.map { FeedItem.NotificationItem(it) }
        myAdapter = MyAdapter(feedItems)
        recyclerView.adapter = myAdapter
    }


}