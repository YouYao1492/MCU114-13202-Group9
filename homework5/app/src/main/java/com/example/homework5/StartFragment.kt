package com.example.homework5

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

class StartFragment : Fragment(R.layout.fragment_start) {
    private lateinit var vm: WorkViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm = ViewModelProvider(requireActivity())[WorkViewModel::class.java]

        view.findViewById<Button>(R.id.btnStart).setOnClickListener { vm.start() }
        view.findViewById<Button>(R.id.btnCancel).setOnClickListener { vm.cancel() }
    }
}
