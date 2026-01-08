package com.example.curs_alexander.ui.measurements

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.curs_alexander.R
import com.example.curs_alexander.data.HealthMeasurement
import com.example.curs_alexander.data.HealthMeasurementsStorage
import java.util.Calendar

class HealthMeasurementAddFragment : Fragment() {

    private lateinit var storage: HealthMeasurementsStorage

    private lateinit var spinnerType: Spinner
    private lateinit var layoutBloodPressure: View
    private lateinit var layoutPulse: View
    private lateinit var etSystolic: EditText
    private lateinit var etDiastolic: EditText
    private lateinit var etPulse: EditText
    private lateinit var tvDateTime: TextView
    private lateinit var etComment: EditText
    private lateinit var btnSave: Button

    private var selectedType: HealthMeasurement.Type = HealthMeasurement.Type.BLOOD_PRESSURE
    private var selectedDateTimeMillis: Long = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storage = HealthMeasurementsStorage(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_health_measurement_add, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spinnerType = view.findViewById(R.id.spinnerType)
        layoutBloodPressure = view.findViewById(R.id.layoutBloodPressure)
        layoutPulse = view.findViewById(R.id.layoutPulse)
        etSystolic = view.findViewById(R.id.etSystolic)
        etDiastolic = view.findViewById(R.id.etDiastolic)
        etPulse = view.findViewById(R.id.etPulse)
        tvDateTime = view.findViewById(R.id.tvDateTime)
        etComment = view.findViewById(R.id.etComment)
        btnSave = view.findViewById(R.id.btnSave)

        setupTypeSpinner()
        setupDateTimePicker()

        btnSave.setOnClickListener { saveMeasurement() }
    }

    private fun setupTypeSpinner() {
        val items = listOf(
            getString(R.string.measure_type_blood_pressure),
            getString(R.string.measure_type_pulse)
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = adapter

        spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedType = if (position == 0) {
                    HealthMeasurement.Type.BLOOD_PRESSURE
                } else {
                    HealthMeasurement.Type.PULSE
                }
                updateTypeVisibility()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // ничего
            }
        }

        updateTypeVisibility()
    }

    private fun updateTypeVisibility() {
        if (selectedType == HealthMeasurement.Type.BLOOD_PRESSURE) {
            layoutBloodPressure.visibility = View.VISIBLE
            layoutPulse.visibility = View.GONE
        } else {
            layoutBloodPressure.visibility = View.GONE
            layoutPulse.visibility = View.VISIBLE
        }
    }

    private fun setupDateTimePicker() {
        updateDateTimeText()
        tvDateTime.setOnClickListener {
            val cal = Calendar.getInstance().apply { timeInMillis = selectedDateTimeMillis }
            DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
                    cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    cal.set(Calendar.MINUTE, minute)
                    selectedDateTimeMillis = cal.timeInMillis
                    updateDateTimeText()
                }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun updateDateTimeText() {
        val cal = Calendar.getInstance().apply { timeInMillis = selectedDateTimeMillis }
        val text = String.format(
            "%02d.%02d.%04d %02d:%02d",
            cal.get(Calendar.DAY_OF_MONTH),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.YEAR),
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE)
        )
        tvDateTime.text = text
    }

    private fun saveMeasurement() {
        if (selectedType == HealthMeasurement.Type.BLOOD_PRESSURE) {
            val systolicText = etSystolic.text?.toString()?.trim().orEmpty()
            val diastolicText = etDiastolic.text?.toString()?.trim().orEmpty()

            val systolic = systolicText.toIntOrNull()
            val diastolic = diastolicText.toIntOrNull()

            if (systolic == null || diastolic == null || systolic <= 0 || diastolic <= 0) {
                Toast.makeText(requireContext(), R.string.error_invalid_input, Toast.LENGTH_SHORT).show()
                return
            }

            val item = HealthMeasurement.BloodPressure(
                systolic = systolic,
                diastolic = diastolic,
                timestampMillis = selectedDateTimeMillis,
                comment = etComment.text?.toString()?.trim().orEmpty().ifBlank { null }
            )
            storage.add(item)
        } else {
            val pulseText = etPulse.text?.toString()?.trim().orEmpty()
            val pulse = pulseText.toIntOrNull()
            if (pulse == null || pulse <= 0) {
                Toast.makeText(requireContext(), R.string.error_invalid_input, Toast.LENGTH_SHORT).show()
                return
            }

            val item = HealthMeasurement.Pulse(
                bpm = pulse,
                timestampMillis = selectedDateTimeMillis,
                comment = etComment.text?.toString()?.trim().orEmpty().ifBlank { null }
            )
            storage.add(item)
        }

        findNavController().popBackStack()
    }
}

