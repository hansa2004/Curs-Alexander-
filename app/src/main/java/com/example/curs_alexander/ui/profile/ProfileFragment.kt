package com.example.curs_alexander.ui.profile

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.curs_alexander.R
import com.example.curs_alexander.data.Prefs
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar

class ProfileFragment : Fragment() {

    private lateinit var prefs: Prefs

    private lateinit var etFirstName: TextInputEditText
    private lateinit var etLastName: TextInputEditText
    private lateinit var etBirthDate: TextInputEditText
    private lateinit var etHeight: TextInputEditText
    private lateinit var etWeight: TextInputEditText
    private lateinit var btnSubmit: MaterialButton

    private lateinit var btnGenderMale: MaterialButton
    private lateinit var btnGenderFemale: MaterialButton
    private lateinit var btnGenderNone: MaterialButton

    private var selectedGender: Gender? = null

    private enum class Gender { MALE, FEMALE, NONE }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_profile_form, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etFirstName = view.findViewById(R.id.etFirstName)
        etLastName = view.findViewById(R.id.etLastName)
        etBirthDate = view.findViewById(R.id.etBirthDate)
        etHeight = view.findViewById(R.id.etHeight)
        etWeight = view.findViewById(R.id.etWeight)
        btnSubmit = view.findViewById(R.id.btnSubmitProfile)

        btnGenderMale = view.findViewById(R.id.btnGenderMale)
        btnGenderFemale = view.findViewById(R.id.btnGenderFemale)
        btnGenderNone = view.findViewById(R.id.btnGenderNone)

        setupBirthDatePicker()
        setupGenderButtons()

        btnSubmit.setOnClickListener {
            if (validateAndSave()) {
                findNavController().navigate(R.id.homeFragment)
            }
        }
    }

    private fun setupBirthDatePicker() {
        etBirthDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val dialog = DatePickerDialog(requireContext(), { _, y, m, d ->
                val selectedCal = Calendar.getInstance().apply {
                    set(y, m, d, 0, 0, 0)
                }
                val now = Calendar.getInstance()
                if (selectedCal.after(now)) {
                    Toast.makeText(requireContext(), R.string.profile_error_future_date, Toast.LENGTH_SHORT).show()
                } else {
                    val formatted = String.format("%02d.%02d.%04d", d, m + 1, y)
                    etBirthDate.setText(formatted)
                }
            }, year, month, day)

            dialog.show()
        }
    }

    private fun setupGenderButtons() {
        fun updateSelection(gender: Gender) {
            selectedGender = gender

            btnGenderMale.isSelected = (gender == Gender.MALE)
            btnGenderFemale.isSelected = (gender == Gender.FEMALE)
            btnGenderNone.isSelected = (gender == Gender.NONE)
        }

        btnGenderMale.setOnClickListener { updateSelection(Gender.MALE) }
        btnGenderFemale.setOnClickListener { updateSelection(Gender.FEMALE) }
        btnGenderNone.setOnClickListener { updateSelection(Gender.NONE) }
    }

    private fun validateAndSave(): Boolean {
        val firstName = etFirstName.text?.toString()?.trim().orEmpty()
        val lastName = etLastName.text?.toString()?.trim().orEmpty()
        val birthDate = etBirthDate.text?.toString()?.trim().orEmpty()
        val heightText = etHeight.text?.toString()?.trim().orEmpty()
        val weightText = etWeight.text?.toString()?.trim().orEmpty()

        if (firstName.isEmpty()) {
            etFirstName.error = getString(R.string.profile_error_first_name_required)
            return false
        }
        if (birthDate.isEmpty()) {
            etBirthDate.error = getString(R.string.profile_error_birth_date_required)
            return false
        }
        if (selectedGender == null) {
            Toast.makeText(requireContext(), R.string.profile_error_gender_required, Toast.LENGTH_SHORT).show()
            return false
        }

        val height = heightText.toIntOrNull()
        val weight = weightText.toIntOrNull()

        if (height != null && height <= 0) {
            etHeight.error = getString(R.string.profile_error_height_invalid)
            return false
        }
        if (weight != null && weight <= 0) {
            etWeight.error = getString(R.string.profile_error_weight_invalid)
            return false
        }

        prefs.userName = firstName
        prefs.profileCompleted = true

        return true
    }
}
