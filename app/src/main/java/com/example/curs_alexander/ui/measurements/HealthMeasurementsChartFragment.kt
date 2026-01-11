package com.example.curs_alexander.ui.measurements

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.curs_alexander.R
import com.example.curs_alexander.data.HealthMeasurement
import com.example.curs_alexander.data.HealthMeasurementsStorage

class HealthMeasurementsChartFragment : Fragment() {

    private lateinit var storage: HealthMeasurementsStorage

    private lateinit var recycler: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var adapter: PressureAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storage = HealthMeasurementsStorage(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_health_measurements_chart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler = view.findViewById(R.id.recyclerPressure)
        tvEmpty = view.findViewById(R.id.tvEmpty)

        recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = PressureAdapter(emptyList())
        recycler.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        updateChart()
    }

    private fun updateChart() {
        val all = storage.getAll()
        val pressureItems = all.filterIsInstance<HealthMeasurement.BloodPressure>()
            .sortedBy { it.timestampMillis } // chronological order (oldest first)

        if (pressureItems.isEmpty()) {
            recycler.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = getString(R.string.measure_chart_empty)
            return
        }

        recycler.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE

        adapter.setData(pressureItems)
    }
}
