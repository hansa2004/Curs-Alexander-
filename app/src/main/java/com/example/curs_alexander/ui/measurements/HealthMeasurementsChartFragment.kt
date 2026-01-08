package com.example.curs_alexander.ui.measurements

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.curs_alexander.R
import com.example.curs_alexander.data.HealthMeasurement
import com.example.curs_alexander.data.HealthMeasurementsStorage

class HealthMeasurementsChartFragment : Fragment() {

    private lateinit var storage: HealthMeasurementsStorage

    private lateinit var chart: SimplePressureChartView
    private lateinit var tvEmpty: TextView

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
        chart = view.findViewById(R.id.lineChart)
        tvEmpty = view.findViewById(R.id.tvEmpty)
    }

    override fun onResume() {
        super.onResume()
        updateChart()
    }

    private fun updateChart() {
        val all = storage.getAll()
        val pressureItems = all.filterIsInstance<HealthMeasurement.BloodPressure>()
            .sortedBy { it.timestampMillis }

        if (pressureItems.isEmpty()) {
            chart.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = getString(R.string.measure_chart_empty)
            return
        }

        chart.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE

        val systolic = pressureItems.map { it.systolic.toFloat() }
        val diastolic = pressureItems.map { it.diastolic.toFloat() }

        chart.setData(systolic, diastolic)
    }
}
