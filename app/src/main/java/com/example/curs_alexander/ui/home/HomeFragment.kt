package com.example.curs_alexander.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.curs_alexander.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

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

        val cardMeasurements = view.findViewById<MaterialCardView>(R.id.cardMeasurements)
        cardMeasurements.setOnClickListener {
            findNavController().navigate(R.id.healthMeasurementsFragment)
        }

        // Переход в дневник симптомов по карточке "Симптомы"
        val cardSymptoms = view.findViewById<MaterialCardView>(R.id.cardSymptoms)
        cardSymptoms.setOnClickListener {
            findNavController().navigate(R.id.symptomsListFragment)
        }

        // Быстрое действие: сразу открыть экран добавления симптома
        val btnAddSymptom = view.findViewById<MaterialButton>(R.id.btnAddSymptom)
        btnAddSymptom.setOnClickListener {
            findNavController().navigate(R.id.symptomAddFragment)
        }
    }
}
