package com.example.curs_alexander.ui.measurements

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.curs_alexander.R
import com.example.curs_alexander.data.HealthMeasurement
import com.example.curs_alexander.data.HealthMeasurementsStorage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HealthMeasurementsFragment : Fragment() {

    private lateinit var storage: HealthMeasurementsStorage
    private lateinit var adapter: MeasurementsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storage = HealthMeasurementsStorage(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_health_measurements, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.rvMeasurements)
        val btnAdd = view.findViewById<Button>(R.id.btnAddMeasurement)

        recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = MeasurementsAdapter()
        recycler.adapter = adapter

        btnAdd.setOnClickListener {
            findNavController().navigate(R.id.healthMeasurementAddFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        // Обновляем список при возврате на экран
        adapter.submitList(storage.getAll())
    }

    private class MeasurementsAdapter :
        RecyclerView.Adapter<MeasurementsAdapter.MeasurementViewHolder>() {

        private val items = mutableListOf<HealthMeasurement>()
        private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

        fun submitList(newItems: List<HealthMeasurement>) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeasurementViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_health_measurement, parent, false)
            return MeasurementViewHolder(view)
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: MeasurementViewHolder, position: Int) {
            holder.bind(items[position], dateFormat)
        }

        class MeasurementViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val tvTitle = view.findViewById<android.widget.TextView>(R.id.tvTitle)
            private val tvValue = view.findViewById<android.widget.TextView>(R.id.tvValue)
            private val tvDate = view.findViewById<android.widget.TextView>(R.id.tvDate)
            private val tvComment = view.findViewById<android.widget.TextView>(R.id.tvComment)

            fun bind(item: HealthMeasurement, dateFormat: SimpleDateFormat) {
                when (item) {
                    is HealthMeasurement.BloodPressure -> {
                        tvTitle.text = itemView.context.getString(R.string.measure_type_blood_pressure)
                        tvValue.text = "${item.systolic}/${item.diastolic}"
                    }
                    is HealthMeasurement.Pulse -> {
                        tvTitle.text = itemView.context.getString(R.string.measure_type_pulse)
                        tvValue.text = item.bpm.toString()
                    }
                }
                tvDate.text = dateFormat.format(Date(item.timestampMillis))
                if (item.comment.isNullOrBlank()) {
                    tvComment.visibility = View.GONE
                } else {
                    tvComment.visibility = View.VISIBLE
                    tvComment.text = item.comment
                }
            }
        }
    }
}

