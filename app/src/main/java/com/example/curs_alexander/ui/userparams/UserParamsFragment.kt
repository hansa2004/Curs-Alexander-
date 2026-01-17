package com.example.curs_alexander.ui.userparams

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.curs_alexander.R
import com.example.curs_alexander.userparams.AgeCategory
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class UserParamsFragment : Fragment() {

    private val vm: UserParamsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_user_params, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tilUpperSys = view.findViewById<TextInputLayout>(R.id.tilUpperSys)
        val tilUpperDia = view.findViewById<TextInputLayout>(R.id.tilUpperDia)
        val tilLowerSys = view.findViewById<TextInputLayout>(R.id.tilLowerSys)
        val tilLowerDia = view.findViewById<TextInputLayout>(R.id.tilLowerDia)

        val etUpperSys = view.findViewById<TextInputEditText>(R.id.etUpperSys)
        val etUpperDia = view.findViewById<TextInputEditText>(R.id.etUpperDia)
        val etLowerSys = view.findViewById<TextInputEditText>(R.id.etLowerSys)
        val etLowerDia = view.findViewById<TextInputEditText>(R.id.etLowerDia)

        val spinner = view.findViewById<android.widget.AutoCompleteTextView>(R.id.spAgeCategory)
        val btnSave = view.findViewById<MaterialButton>(R.id.btnSaveUserParams)

        val ageItems = listOf(
            getString(R.string.user_params_age_under_30),
            getString(R.string.user_params_age_30_50),
            getString(R.string.user_params_age_50_plus)
        )
        spinner.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, ageItems))

        fun ageFromSelection(text: String?): AgeCategory = when (text) {
            getString(R.string.user_params_age_under_30) -> AgeCategory.UNDER_30
            getString(R.string.user_params_age_50_plus) -> AgeCategory.ABOVE_50
            else -> AgeCategory.FROM_30_TO_50
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.params.collect { p ->
                    if (etUpperSys.text.isNullOrBlank()) etUpperSys.setText(p.upperSystolic.toString())
                    if (etUpperDia.text.isNullOrBlank()) etUpperDia.setText(p.upperDiastolic.toString())
                    if (etLowerSys.text.isNullOrBlank()) etLowerSys.setText(p.lowerSystolic.toString())
                    if (etLowerDia.text.isNullOrBlank()) etLowerDia.setText(p.lowerDiastolic.toString())

                    val ageLabel = when (p.ageCategory) {
                        AgeCategory.UNDER_30 -> getString(R.string.user_params_age_under_30)
                        AgeCategory.FROM_30_TO_50 -> getString(R.string.user_params_age_30_50)
                        AgeCategory.ABOVE_50 -> getString(R.string.user_params_age_50_plus)
                    }
                    if (spinner.text.isNullOrBlank()) spinner.setText(ageLabel, false)
                }
            }
        }

        btnSave.setOnClickListener {
            tilUpperSys.error = null
            tilUpperDia.error = null
            tilLowerSys.error = null
            tilLowerDia.error = null

            val upperSys = etUpperSys.text?.toString()?.trim()?.toIntOrNull()
            val upperDia = etUpperDia.text?.toString()?.trim()?.toIntOrNull()
            val lowerSys = etLowerSys.text?.toString()?.trim()?.toIntOrNull()
            val lowerDia = etLowerDia.text?.toString()?.trim()?.toIntOrNull()

            var ok = true
            if (upperSys == null || upperSys <= 0) { tilUpperSys.error = getString(R.string.error_invalid_input); ok = false }
            if (upperDia == null || upperDia <= 0) { tilUpperDia.error = getString(R.string.error_invalid_input); ok = false }
            if (lowerSys == null || lowerSys <= 0) { tilLowerSys.error = getString(R.string.error_invalid_input); ok = false }
            if (lowerDia == null || lowerDia <= 0) { tilLowerDia.error = getString(R.string.error_invalid_input); ok = false }

            if (ok && upperSys != null && upperDia != null && lowerSys != null && lowerDia != null) {
                val age = ageFromSelection(spinner.text?.toString())
                vm.saveAll(upperSys, upperDia, lowerSys, lowerDia, age)
                Toast.makeText(requireContext(), R.string.user_params_saved, Toast.LENGTH_SHORT).show()
            }
        }
    }
}

