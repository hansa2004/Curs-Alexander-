package com.example.curs_alexander.ui.analytics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.curs_alexander.R
import com.example.curs_alexander.data.db.BloodPressureEntity
import com.example.curs_alexander.data.db.SymptomEntity
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AnalyticsFragment : Fragment() {

    private val viewModel: AnalyticsViewModel by viewModels()
    private val exportViewModel: ExportPdfViewModel by viewModels()

    private val df = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_analytics, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val progress = view.findViewById<ProgressBar>(R.id.progress)
        val tabs = view.findViewById<TabLayout>(R.id.tabs)
        val btnExport = view.findViewById<MaterialButton>(R.id.btnExportPdf)

        val pressureContainer = view.findViewById<View>(R.id.containerPressure)
        val symptomsContainer = view.findViewById<View>(R.id.containerSymptoms)

        val tvAvg = view.findViewById<TextView>(R.id.tvAvgPressure)
        val tvLast = view.findViewById<TextView>(R.id.tvLastPressure)
        val tvHint = view.findViewById<TextView>(R.id.tvPressureHint)
        val rvPressure = view.findViewById<RecyclerView>(R.id.recyclerPressure)

        val rvSymptomStats = view.findViewById<RecyclerView>(R.id.recyclerSymptomStats)
        val rvSymptomLast = view.findViewById<RecyclerView>(R.id.recyclerSymptomLast)

        rvPressure.layoutManager = LinearLayoutManager(requireContext())
        val pressureAdapter = PressureHistoryAdapter(df)
        rvPressure.adapter = pressureAdapter

        rvSymptomStats.layoutManager = LinearLayoutManager(requireContext())
        val symptomStatsAdapter = SymptomStatsAdapter()
        rvSymptomStats.adapter = symptomStatsAdapter

        rvSymptomLast.layoutManager = LinearLayoutManager(requireContext())
        val symptomLastAdapter = SymptomLastAdapter(df)
        rvSymptomLast.adapter = symptomLastAdapter

        tabs.addTab(tabs.newTab().setText(getString(R.string.analytics_tab_pressure)))
        tabs.addTab(tabs.newTab().setText(getString(R.string.analytics_tab_symptoms)))

        fun setTab(position: Int) {
            pressureContainer.isVisible = position == 0
            symptomsContainer.isVisible = position == 1
        }
        setTab(0)

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) = setTab(tab.position)
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    progress.isVisible = state.isLoading

                    // Давление
                    val summary = state.pressureSummary
                    if (summary == null) {
                        tvAvg.text = getString(R.string.analytics_pressure_avg_empty)
                        tvLast.text = getString(R.string.analytics_pressure_last_empty)
                        tvHint.text = ""
                    } else {
                        tvAvg.text = if (summary.avgSystolic7d != null && summary.avgDiastolic7d != null) {
                            getString(
                                R.string.analytics_pressure_avg_value,
                                summary.avgSystolic7d,
                                summary.avgDiastolic7d
                            )
                        } else {
                            getString(R.string.analytics_pressure_avg_empty)
                        }

                        tvLast.text = summary.last?.let {
                            val dt = df.format(Date(it.timestampMillis))
                            getString(R.string.analytics_pressure_last_value, it.systolic, it.diastolic, dt)
                        } ?: getString(R.string.analytics_pressure_last_empty)

                        tvHint.text = when (summary.hint) {
                            PressureHint.NORMAL -> getString(R.string.analytics_pressure_hint_normal)
                            PressureHint.HIGH -> getString(R.string.analytics_pressure_hint_high)
                        }
                    }
                    pressureAdapter.submit(state.pressureHistory)

                    // Симптомы
                    symptomStatsAdapter.submit(state.symptomStats)
                    symptomLastAdapter.submit(state.symptomLast)
                }
            }
        }

        // Состояние экспорта PDF: прогресс/успех/ошибка
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                exportViewModel.uiState.collect { s ->
                    when (s) {
                        is ExportPdfUiState.Idle -> {
                            btnExport.isEnabled = true
                        }
                        is ExportPdfUiState.Exporting -> {
                            btnExport.isEnabled = false
                            Toast.makeText(requireContext(), "Формирование отчёта...", Toast.LENGTH_SHORT).show()
                        }
                        is ExportPdfUiState.Success -> {
                            btnExport.isEnabled = true
                            Toast.makeText(requireContext(), "PDF сохранён в 'Загрузки'", Toast.LENGTH_LONG).show()
                            exportViewModel.consumeResult()
                        }
                        is ExportPdfUiState.Error -> {
                            btnExport.isEnabled = true
                            Toast.makeText(requireContext(), "Ошибка: ${s.message}", Toast.LENGTH_LONG).show()
                            exportViewModel.consumeResult()
                        }
                    }
                }
            }
        }

        viewModel.load()

        btnExport.setOnClickListener {
            exportViewModel.export()
        }
    }

    private class PressureHistoryAdapter(
        private val df: SimpleDateFormat
    ) : RecyclerView.Adapter<PressureHistoryAdapter.VH>() {

        private val items = mutableListOf<BloodPressureEntity>()

        fun submit(list: List<BloodPressureEntity>) {
            items.clear()
            items.addAll(list)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_simple_row, parent, false)
            return VH(v, df)
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.bind(items[position])
        }

        class VH(itemView: View, private val df: SimpleDateFormat) : RecyclerView.ViewHolder(itemView) {
            private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
            private val tvSubtitle: TextView = itemView.findViewById(R.id.tvSubtitle)

            fun bind(item: BloodPressureEntity) {
                tvTitle.text = "${item.systolic}/${item.diastolic}"
                tvSubtitle.text = df.format(Date(item.timestampMillis))
            }
        }
    }

    private class SymptomStatsAdapter : RecyclerView.Adapter<SymptomStatsAdapter.VH>() {
        private val items = mutableListOf<SymptomStats>()

        fun submit(list: List<SymptomStats>) {
            items.clear()
            items.addAll(list)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_simple_row, parent, false)
            return VH(v)
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.bind(items[position])
        }

        class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
            private val tvSubtitle: TextView = itemView.findViewById(R.id.tvSubtitle)

            fun bind(item: SymptomStats) {
                tvTitle.text = item.name
                tvSubtitle.text = if (item.avgIntensity != null) {
                    "Повторений: ${item.count}, средняя интенсивность: ${String.format(Locale.getDefault(), "%.1f", item.avgIntensity)}"
                } else {
                    "Повторений: ${item.count}"
                }
            }
        }
    }

    private class SymptomLastAdapter(
        private val df: SimpleDateFormat
    ) : RecyclerView.Adapter<SymptomLastAdapter.VH>() {
        private val items = mutableListOf<SymptomEntity>()

        fun submit(list: List<SymptomEntity>) {
            items.clear()
            items.addAll(list)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_simple_row, parent, false)
            return VH(v, df)
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.bind(items[position])
        }

        class VH(itemView: View, private val df: SimpleDateFormat) : RecyclerView.ViewHolder(itemView) {
            private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
            private val tvSubtitle: TextView = itemView.findViewById(R.id.tvSubtitle)

            fun bind(item: SymptomEntity) {
                tvTitle.text = item.name
                val dt = df.format(Date(item.timestampMillis))
                tvSubtitle.text = if (item.intensity != null) {
                    "Интенсивность: ${item.intensity}, $dt"
                } else {
                    dt
                }
            }
        }
    }
}
