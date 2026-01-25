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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.curs_alexander.R
import com.example.curs_alexander.data.db.BloodPressureWithContext
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

        val tvNotEnoughPoints = view.findViewById<TextView>(R.id.tvNotEnoughPoints)
        val btnOpenDataStatus = view.findViewById<MaterialButton>(R.id.btnOpenDataStatus)

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

        btnOpenDataStatus.setOnClickListener {
            findNavController().navigate(R.id.dataStatusFragment)
        }

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
                            PressureHint.WITHIN_USER_THRESHOLDS ->
                                getString(R.string.analytics_pressure_hint_within)
                            PressureHint.ABOVE_USER_THRESHOLD ->
                                getString(R.string.analytics_pressure_hint_above_user)
                            PressureHint.BELOW_USER_THRESHOLD ->
                                getString(R.string.analytics_pressure_hint_below_user)
                        }
                    }

                    pressureAdapter.submit(state.pressureHistory)

                    // Интеграция с динамикой: если точек < 2 — не строим динамику и показываем сообщение.
                    tvNotEnoughPoints.isVisible = state.pressureHistory.size < 2

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

        private val items = mutableListOf<BloodPressureWithContext>()

        fun submit(list: List<BloodPressureWithContext>) {
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

            fun bind(item: BloodPressureWithContext) {
                tvTitle.text = "${item.bp.systolic}/${item.bp.diastolic}"

                val dt = df.format(Date(item.bp.timestampMillis))
                val ctxText = formatContext(item, itemView.context)
                tvSubtitle.text = if (ctxText.isNullOrBlank()) {
                    dt
                } else {
                    "$dt • $ctxText"
                }
            }

            private fun formatContext(item: BloodPressureWithContext, context: android.content.Context): String? {
                val parts = mutableListOf<String>()

                val time = when (item.timeOfDay) {
                    "morning" -> context.getString(R.string.measure_context_time_morning)
                    "day" -> context.getString(R.string.measure_context_time_day)
                    "evening" -> context.getString(R.string.measure_context_time_evening)
                    else -> null
                }
                if (!time.isNullOrBlank()) parts.add(time)

                val state = when (item.state) {
                    "rest" -> context.getString(R.string.measure_context_state_rest)
                    "after_load" -> context.getString(R.string.measure_context_state_after_load)
                    "after_stress" -> context.getString(R.string.measure_context_state_after_stress)
                    else -> null
                }
                if (!state.isNullOrBlank()) parts.add(state)

                val comment = item.contextComment?.trim().orEmpty().ifBlank { null }
                if (!comment.isNullOrBlank()) parts.add(comment)

                return parts.takeIf { it.isNotEmpty() }?.joinToString(", ")
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
                val comment = item.comment?.trim().orEmpty().ifBlank { null }
                tvSubtitle.text = if (comment == null) dt else "$dt • $comment"
            }
        }
    }
}
