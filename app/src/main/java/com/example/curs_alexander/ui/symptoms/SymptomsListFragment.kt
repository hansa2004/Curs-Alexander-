package com.example.curs_alexander.ui.symptoms

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.curs_alexander.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SymptomsListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var btnAdd: Button
    private lateinit var adapter: SymptomsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_symptoms_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerSymptoms)
        tvEmpty = view.findViewById(R.id.tvEmptySymptoms)
        btnAdd = view.findViewById(R.id.btnAddSymptom)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = SymptomsAdapter(emptyList())
        recyclerView.adapter = adapter

        btnAdd.setOnClickListener {
            findNavController().navigate(R.id.action_symptomsListFragment_to_symptomAddFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        val prefs = requireContext().getSharedPreferences("symptoms_diary", Context.MODE_PRIVATE)
        val json = prefs.getString("items", null)
        val items: List<SymptomItem> = if (json.isNullOrEmpty()) {
            emptyList()
        } else {
            val type = object : TypeToken<List<SymptomItem>>() {}.type
            Gson().fromJson(json, type)
        }

        if (items.isEmpty()) {
            recyclerView.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE
            adapter.setData(items.sortedByDescending { it.timestampMillis })
        }
    }
}

