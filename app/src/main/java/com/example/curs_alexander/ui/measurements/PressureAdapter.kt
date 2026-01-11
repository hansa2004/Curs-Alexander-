package com.example.curs_alexander.ui.measurements

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.curs_alexander.R
import com.example.curs_alexander.data.HealthMeasurement
import java.text.SimpleDateFormat
import java.util.*

class PressureAdapter(private var items: List<HealthMeasurement.BloodPressure>) :
    RecyclerView.Adapter<PressureAdapter.Holder>() {

    private var maxValue: Int = 1
    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    fun setData(list: List<HealthMeasurement.BloodPressure>) {
        items = list
        // вычисляем глобальный максимум для масштабирования индикаторов
        maxValue = (items.flatMap { listOf(it.systolic, it.diastolic) }.maxOrNull() ?: 1)
        if (maxValue <= 0) maxValue = 1
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_pressure, parent, false)
        return Holder(v)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = items[position]
        holder.bind(item, maxValue, sdf)
    }

    override fun getItemCount(): Int = items.size

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvTimestamp: TextView = view.findViewById(R.id.tvTimestamp)
        private val tvSystolic: TextView = view.findViewById(R.id.tvSystolic)
        private val tvDiastolic: TextView = view.findViewById(R.id.tvDiastolic)
        private val containerSystolic: View = view.findViewById(R.id.containerSystolic)
        private val foregroundSystolic: View = view.findViewById(R.id.foregroundSystolic)
        private val containerDiastolic: View = view.findViewById(R.id.containerDiastolic)
        private val foregroundDiastolic: View = view.findViewById(R.id.foregroundDiastolic)

        fun bind(item: HealthMeasurement.BloodPressure, maxValue: Int, sdf: SimpleDateFormat) {
            tvTimestamp.text = sdf.format(Date(item.timestampMillis))
            tvSystolic.text = "${item.systolic}"
            tvDiastolic.text = "${item.diastolic}"

            // рассчитываем доли (0..1)
            val ratioSys = item.systolic.toFloat() / maxValue.toFloat()
            val ratioDia = item.diastolic.toFloat() / maxValue.toFloat()

            // Устанавливаем ширины передних View после layout (чтобы знать ширину контейнера)
            containerSystolic.post {
                val full = containerSystolic.width
                val w = (full * ratioSys).toInt().coerceAtLeast(1)
                val lp = foregroundSystolic.layoutParams
                lp.width = w
                foregroundSystolic.layoutParams = lp
            }

            containerDiastolic.post {
                val full = containerDiastolic.width
                val w = (full * ratioDia).toInt().coerceAtLeast(1)
                val lp = foregroundDiastolic.layoutParams
                lp.width = w
                foregroundDiastolic.layoutParams = lp
            }
        }
    }
}
