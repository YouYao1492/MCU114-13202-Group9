package com.example.homework6

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

class ResultFragment : Fragment(R.layout.fragment_result) {
    private lateinit var vm: WorkViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm = ViewModelProvider(requireActivity())[WorkViewModel::class.java]

        val status = view.findViewById<TextView>(R.id.txtResult)
        vm.status.observe(viewLifecycleOwner) { status.text = it }
    }
}
