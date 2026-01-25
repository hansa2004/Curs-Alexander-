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
import com.example.curs_alexander.data.db.DbProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class HealthMeasurementsChartFragment : Fragment() {

    private lateinit var storage: HealthMeasurementsStorage

    private lateinit var recycler: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var tvContextHint: TextView
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
        tvContextHint = view.findViewById(R.id.tvContextHint)

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
            tvContextHint.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = getString(R.string.measure_chart_empty)
            return
        }

        recycler.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE

        adapter.setData(pressureItems)

        // Пояснение про контекст (если он разнородный/неполный)
        showContextHintIfNeeded()
    }

    private fun showContextHintIfNeeded() {
        val db = DbProvider.get(requireContext())

        // Быстро: экран «динамики» сейчас не реактивный, поэтому делаем синхронно,
        // но в IO-контексте и с быстрым запросом.
        val contextList = runBlocking {
            withContext(Dispatchers.IO) {
                db.bloodPressureDao().getAllWithContext()
            }
        }

        if (contextList.size < 2) {
            tvContextHint.visibility = View.GONE
            return
        }

        val hasAnyContext = contextList.any {
            !it.timeOfDay.isNullOrBlank() || !it.state.isNullOrBlank() || !it.contextComment.isNullOrBlank()
        }
        if (!hasAnyContext) {
            tvContextHint.visibility = View.GONE
            return
        }

        val filledCount = contextList.count {
            !it.timeOfDay.isNullOrBlank() || !it.state.isNullOrBlank() || !it.contextComment.isNullOrBlank()
        }

        val distinctTime = contextList.mapNotNull { it.timeOfDay?.takeIf { v -> v.isNotBlank() } }.distinct().size
        val distinctState = contextList.mapNotNull { it.state?.takeIf { v -> v.isNotBlank() } }.distinct().size

        val isHeterogeneous = distinctTime > 1 || distinctState > 1
        val isPartial = filledCount in 1 until contextList.size

        if (!(isHeterogeneous || isPartial)) {
            tvContextHint.visibility = View.GONE
            return
        }

        tvContextHint.visibility = View.VISIBLE
        tvContextHint.text = buildString {
            append("Подсказка: измерения сделаны в разных условиях. ")
            append("Чтобы корректнее сравнивать динамику, учитывайте контекст (время суток, состояние, комментарий). ")
            append("Это пояснение и не является медицинским выводом.")
        }
    }
}
