package com.example.curs_alexander.ui.symptoms

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.curs_alexander.R
import com.google.android.material.chip.Chip
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SymptomAddFragment : Fragment() {

    private lateinit var etName: EditText
    private lateinit var seekIntensity: SeekBar
    private lateinit var tvIntensityValue: TextView
    private lateinit var tvDateTime: TextView
    private lateinit var etComment: EditText
    private lateinit var btnSave: Button
    private lateinit var chipHeadache: Chip
    private lateinit var chipFatigue: Chip
    private lateinit var chipNausea: Chip
    private lateinit var chipStomachPain: Chip

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_symptom_add, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        etName = view.findViewById(R.id.etSymptomName)
        seekIntensity = view.findViewById(R.id.seekIntensity)
        tvIntensityValue = view.findViewById(R.id.tvIntensityValue)
        tvDateTime = view.findViewById(R.id.tvDateTime)
        etComment = view.findViewById(R.id.etComment)
        btnSave = view.findViewById(R.id.btnSave)
        chipHeadache = view.findViewById(R.id.chipHeadache)
        chipFatigue = view.findViewById(R.id.chipFatigue)
        chipNausea = view.findViewById(R.id.chipNausea)
        chipStomachPain = view.findViewById(R.id.chipStomachPain)

        val now = Date()
        val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        tvDateTime.text = formatter.format(now)

        updateIntensityLabel()
        seekIntensity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateIntensityLabel()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Валидация названия: включаем кнопку, только если поле не пустое
        etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                btnSave.isEnabled = !s.isNullOrBlank()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Шаблоны симптомов: подстановка текста в поле названия
        val templateClickListener = View.OnClickListener { chipView ->
            val text = (chipView as Chip).text.toString()
            etName.setText(text)
            etName.setSelection(text.length)
        }
        chipHeadache.setOnClickListener(templateClickListener)
        chipFatigue.setOnClickListener(templateClickListener)
        chipNausea.setOnClickListener(templateClickListener)
        chipStomachPain.setOnClickListener(templateClickListener)

        btnSave.setOnClickListener {
            saveSymptom()
        }
    }

    private fun updateIntensityLabel() {
        val value = seekIntensity.progress + 1 // 1..5
        tvIntensityValue.text = "Интенсивность: $value из 5"
    }

    private fun saveSymptom() {
        val name = etName.text.toString().trim()
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(requireContext(), "Введите название симптома", Toast.LENGTH_SHORT).show()
            return
        }

        val intensity = seekIntensity.progress + 1 // 1..5
        val comment = etComment.text.toString().trim().ifEmpty { null }
        val timestamp = System.currentTimeMillis()

        val item = SymptomItem(
            name = name,
            intensity = intensity,
            timestampMillis = timestamp,
            comment = comment
        )

        val prefs = requireContext().getSharedPreferences("symptoms_diary", Context.MODE_PRIVATE)
        val jsonOld = prefs.getString("items", null)
        val list: MutableList<SymptomItem> = if (jsonOld.isNullOrEmpty()) {
            mutableListOf()
        } else {
            val type = object : TypeToken<List<SymptomItem>>() {}.type
            Gson().fromJson<List<SymptomItem>>(jsonOld, type).toMutableList()
        }

        list.add(item)

        val jsonNew = Gson().toJson(list)
        prefs.edit().putString("items", jsonNew).apply()

        findNavController().navigateUp()
    }
}
