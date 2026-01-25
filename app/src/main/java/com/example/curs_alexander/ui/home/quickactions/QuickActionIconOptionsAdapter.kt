package com.example.curs_alexander.ui.home.quickactions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.curs_alexander.R
import com.google.android.material.imageview.ShapeableImageView

class QuickActionIconOptionsAdapter(
    private val onClick: (QuickActionIconStyle) -> Unit
) : RecyclerView.Adapter<QuickActionIconOptionsAdapter.VH>() {

    private val items = QuickActionIconStyle.entries.toList()
    private var selectedId: String? = null

    fun setSelected(styleId: String?) {
        selectedId = styleId
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quick_action_icon_option, parent, false)
        return VH(v, onClick)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position], selectedId)
    }

    class VH(itemView: View, private val onClick: (QuickActionIconStyle) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val iv: ShapeableImageView = itemView.findViewById(R.id.iv)
        private val card: com.google.android.material.card.MaterialCardView = itemView as com.google.android.material.card.MaterialCardView

        fun bind(item: QuickActionIconStyle, selectedId: String?) {
            iv.setImageResource(item.iconRes)
            val isSelected = selectedId != null && selectedId == item.id
            card.strokeWidth = if (isSelected) itemView.resources.displayMetrics.density.toInt() * 2 else 0
            card.strokeColor = com.google.android.material.color.MaterialColors.getColor(card, com.google.android.material.R.attr.colorPrimary)
            itemView.setOnClickListener { onClick(item) }
        }
    }
}
