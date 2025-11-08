package com.example.homework6

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

class ProgressFragment : Fragment(R.layout.fragment_progress) {
    private lateinit var vm: WorkViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm = ViewModelProvider(requireActivity())[WorkViewModel::class.java]

        val bar = view.findViewById<ProgressBar>(R.id.progressBar)
        val text = view.findViewById<TextView>(R.id.txtPercent)

        bar.isIndeterminate = true

        vm.status.observe(viewLifecycleOwner) { s ->
            text.text = s
            if (s.startsWith("Working")) bar.isIndeterminate = false
        }

        vm.progress.observe(viewLifecycleOwner) { p ->
            if (!bar.isIndeterminate) {
                bar.max = 100
                bar.progress = p
                text.text = "Working… %d%%".format(p)
            }
        }
    }
}
