package com.example.curs_alexander.ui.symptoms

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
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

        btnSave.setOnClickListener {
            saveSymptom()
        }
    }

    private fun updateIntensityLabel() {
        // SeekBar max = 4, так что интенсивность = progress + 1
        val value = seekIntensity.progress + 1
        tvIntensityValue.text = "Интенсивность: $value"
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

