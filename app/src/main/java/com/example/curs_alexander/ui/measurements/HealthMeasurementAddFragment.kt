package com.example.curs_alexander.ui.measurements

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.curs_alexander.R
import com.example.curs_alexander.data.HealthMeasurement
import com.example.curs_alexander.data.HealthMeasurementsStorage
import com.example.curs_alexander.data.db.BloodPressureEntity
import com.example.curs_alexander.data.db.DbProvider
import com.example.curs_alexander.data.db.MeasurementContextEntity
import com.example.curs_alexander.data.db.PulseEntity
import com.example.curs_alexander.userparams.PressureThresholdsChecker
import com.example.curs_alexander.userparams.UserParamsRepository
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class HealthMeasurementAddFragment : Fragment() {

    companion object {
        const val ARG_INITIAL_TYPE = "initialType"
        const val INITIAL_TYPE_BP = "bp"
        const val INITIAL_TYPE_PULSE = "pulse"

        private const val CTX_TYPE_BP = "bp"
        private const val CTX_TYPE_PULSE = "pulse"

        private const val CTX_TIME_MORNING = "morning"
        private const val CTX_TIME_DAY = "day"
        private const val CTX_TIME_EVENING = "evening"

        private const val CTX_STATE_REST = "rest"
        private const val CTX_STATE_AFTER_LOAD = "after_load"
        private const val CTX_STATE_AFTER_STRESS = "after_stress"
    }

    private lateinit var storage: HealthMeasurementsStorage

    private lateinit var spinnerType: Spinner
    private lateinit var layoutBloodPressure: View
    private lateinit var layoutPulse: View
    private lateinit var etSystolic: EditText
    private lateinit var etDiastolic: EditText
    private lateinit var etPulse: EditText
    private lateinit var tvDateTime: TextView
    private lateinit var etComment: EditText

    // Контекст
    private lateinit var chipGroupTimeOfDay: ChipGroup
    private lateinit var chipTimeMorning: Chip
    private lateinit var chipTimeDay: Chip
    private lateinit var chipTimeEvening: Chip

    private lateinit var chipGroupState: ChipGroup
    private lateinit var chipStateRest: Chip
    private lateinit var chipStateAfterLoad: Chip
    private lateinit var chipStateAfterStress: Chip

    private lateinit var etContextComment: TextInputEditText

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

        // Контекст
        chipGroupTimeOfDay = view.findViewById(R.id.chipGroupTimeOfDay)
        chipTimeMorning = view.findViewById(R.id.chipTimeMorning)
        chipTimeDay = view.findViewById(R.id.chipTimeDay)
        chipTimeEvening = view.findViewById(R.id.chipTimeEvening)

        chipGroupState = view.findViewById(R.id.chipGroupState)
        chipStateRest = view.findViewById(R.id.chipStateRest)
        chipStateAfterLoad = view.findViewById(R.id.chipStateAfterLoad)
        chipStateAfterStress = view.findViewById(R.id.chipStateAfterStress)

        etContextComment = view.findViewById(R.id.etContextComment)

        // Применяем стартовый тип (если передан из quick action)
        val initial = arguments?.getString(ARG_INITIAL_TYPE).orEmpty()
        selectedType = when (initial) {
            INITIAL_TYPE_PULSE -> HealthMeasurement.Type.PULSE
            INITIAL_TYPE_BP -> HealthMeasurement.Type.BLOOD_PRESSURE
            else -> selectedType
        }

        setupTypeSpinner()
        setupDateTimePicker()

        // После установки адаптера можем выставить selection спиннера под already-selected type
        spinnerType.setSelection(if (selectedType == HealthMeasurement.Type.BLOOD_PRESSURE) 0 else 1, false)
        updateTypeVisibility()

        // Автоподсказка времени суток по выбранному времени
        suggestTimeOfDayBySelectedTime()

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
                    suggestTimeOfDayBySelectedTime()
                }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun updateDateTimeText() {
        val cal = Calendar.getInstance().apply { timeInMillis = selectedDateTimeMillis }
        val text = String.format(
            Locale.getDefault(),
            "%02d.%02d.%04d %02d:%02d",
            cal.get(Calendar.DAY_OF_MONTH),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.YEAR),
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE)
        )
        tvDateTime.text = text
    }

    private fun suggestTimeOfDayBySelectedTime() {
        // Не мешаем пользователю: подставляем только если он ещё ничего не выбрал
        if (chipGroupTimeOfDay.checkedChipId != View.NO_ID) return

        val hour = Calendar.getInstance().apply { timeInMillis = selectedDateTimeMillis }
            .get(Calendar.HOUR_OF_DAY)

        // Простые границы (можно обсудить и поменять):
        // утро 05:00–11:59, день 12:00–17:59, вечер 18:00–04:59
        when (hour) {
            in 5..11 -> chipTimeMorning.isChecked = true
            in 12..17 -> chipTimeDay.isChecked = true
            else -> chipTimeEvening.isChecked = true
        }
    }

    private fun readContextOrNull(measurementType: String, measurementId: Long): MeasurementContextEntity? {
        val timeOfDay = when (chipGroupTimeOfDay.checkedChipId) {
            R.id.chipTimeMorning -> CTX_TIME_MORNING
            R.id.chipTimeDay -> CTX_TIME_DAY
            R.id.chipTimeEvening -> CTX_TIME_EVENING
            else -> null
        }

        val state = when (chipGroupState.checkedChipId) {
            R.id.chipStateRest -> CTX_STATE_REST
            R.id.chipStateAfterLoad -> CTX_STATE_AFTER_LOAD
            R.id.chipStateAfterStress -> CTX_STATE_AFTER_STRESS
            else -> null
        }

        val ctxComment = etContextComment.text?.toString()?.trim().orEmpty().ifBlank { null }

        // Если ничего не заполнено — не сохраняем
        if (timeOfDay == null && state == null && ctxComment == null) return null

        return MeasurementContextEntity(
            measurementType = measurementType,
            measurementId = measurementId,
            timeOfDay = timeOfDay,
            state = state,
            comment = ctxComment
        )
    }

    private fun saveMeasurement() {
        val db = DbProvider.get(requireContext())

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

            val entity = BloodPressureEntity(
                timestampMillis = item.timestampMillis,
                systolic = item.systolic,
                diastolic = item.diastolic,
                comment = item.comment
            )

            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                val bpId = db.bloodPressureDao().insert(entity)
                val ctx = readContextOrNull(CTX_TYPE_BP, bpId)
                if (ctx != null) {
                    db.measurementContextDao().insert(ctx)
                }
            }

            // Новое: предупреждение по пользовательским порогам (только информационная подсказка)
            viewLifecycleOwner.lifecycleScope.launch {
                val params = UserParamsRepository(requireContext().applicationContext).params.first()
                when (PressureThresholdsChecker.check(systolic, diastolic, params)) {
                    PressureThresholdsChecker.Result.ABOVE_USER_THRESHOLD -> {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.measure_warning_above_user_threshold),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    PressureThresholdsChecker.Result.BELOW_USER_THRESHOLD -> {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.measure_warning_below_user_threshold),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    PressureThresholdsChecker.Result.WITHIN_USER_THRESHOLDS -> Unit
                }
            }
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

            val entity = PulseEntity(
                timestampMillis = item.timestampMillis,
                bpm = item.bpm,
                comment = item.comment
            )

            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                val pulseId = db.pulseDao().insert(entity)
                val ctx = readContextOrNull(CTX_TYPE_PULSE, pulseId)
                if (ctx != null) {
                    db.measurementContextDao().insert(ctx)
                }
            }
        }

        findNavController().popBackStack()
    }
}
