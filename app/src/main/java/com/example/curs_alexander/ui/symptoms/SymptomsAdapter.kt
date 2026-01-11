package com.example.curs_alexander.ui.symptoms

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.curs_alexander.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Простая модель записи симптома
data class SymptomItem(
    val name: String,
    val intensity: Int?, // 1..5 или null
    val timestampMillis: Long,
    val comment: String?
)

class SymptomsAdapter(
    private var items: List<SymptomItem>
) : RecyclerView.Adapter<SymptomsAdapter.ViewHolder>() {

    private val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    fun setData(newItems: List<SymptomItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_symptom, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, formatter)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvSymptomName)
        private val tvDateTime: TextView = itemView.findViewById(R.id.tvSymptomDateTime)
        private val tvIntensity: TextView = itemView.findViewById(R.id.tvSymptomIntensity)
        private val tvComment: TextView = itemView.findViewById(R.id.tvSymptomComment)
        private val indicator: View = itemView.findViewById(R.id.viewIntensityBar)

        fun bind(item: SymptomItem, formatter: SimpleDateFormat) {
            tvName.text = item.name
            tvDateTime.text = formatter.format(Date(item.timestampMillis))

            if (item.intensity != null) {
                tvIntensity.visibility = View.VISIBLE
                tvIntensity.text = "Интенсивность: ${item.intensity}"

                // Простой индикатор: ширина полосы пропорциональна интенсивности (1..5)
                val params = indicator.layoutParams
                val base = indicator.resources.displayMetrics.density * 40 // 40dp базовая длина
                params.width = (base * item.intensity).toInt()
                indicator.layoutParams = params
                indicator.visibility = View.VISIBLE
            } else {
                tvIntensity.visibility = View.GONE
                indicator.visibility = View.GONE
            }

            if (!item.comment.isNullOrEmpty()) {
                tvComment.visibility = View.VISIBLE
                tvComment.text = item.comment
            } else {
                tvComment.visibility = View.GONE
            }
        }
    }
}

