package com.example.curs_alexander.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.curs_alexander.R

class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cardMeasurements = view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardMeasurements)
        cardMeasurements.setOnClickListener {
            findNavController().navigate(R.id.healthMeasurementsFragment)
        }
    }
}
