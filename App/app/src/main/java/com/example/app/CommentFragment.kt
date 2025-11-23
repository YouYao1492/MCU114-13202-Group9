package com.example.app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CommentFragment : Fragment() {

    private lateinit var commentRecyclerView: RecyclerView
    private lateinit var commentAdapter: CommentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_comment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        commentRecyclerView = view.findViewById(R.id.commentRecyclerView)
        commentRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val comments = createDummyComments()
        commentAdapter = CommentAdapter(comments)
        commentRecyclerView.adapter = commentAdapter
    }

    private fun createDummyComments(): List<Comment> {
        val reply1L1 = Comment(username = "charlie", text = "I agree!", timestamp = "1 hour ago", level = 2)
        val reply1 = Comment(username = "bob", text = "Great post!", timestamp = "2 hours ago", replies = listOf(reply1L1), level = 1)
        val comment1 = Comment(username = "alice", text = "This is the first comment.", timestamp = "3 hours ago", replies = listOf(reply1), level = 0)

        val comment2 = Comment(username = "david", text = "Second comment here.", timestamp = "4 hours ago", level = 0)

        return listOf(comment1, comment2)
    }
}