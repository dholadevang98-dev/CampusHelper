package com.devang.campushelper

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_home,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val btnNotices =
            view.findViewById<Button>(R.id.btnNotices)

        val btnEvents =
            view.findViewById<Button>(R.id.btnEvents)

        val btnTimetable =
            view.findViewById<Button>(R.id.btnTimetable)

        val btnProfile =
            view.findViewById<Button>(R.id.btnProfile)

        val btnLogout =
            view.findViewById<Button>(R.id.btnLogout)


        btnNotices.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Campus Notices",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnEvents.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Campus Events",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnTimetable.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Timetable",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnProfile.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "My Profile",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnLogout.setOnClickListener {

            findNavController().navigate(
                R.id.action_homeFragment_to_FirstFragment
            )
        }
    }
}