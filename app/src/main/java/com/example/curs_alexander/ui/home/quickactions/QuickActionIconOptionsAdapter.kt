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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quick_action_icon_option, parent, false)
        return VH(v, onClick)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    class VH(itemView: View, private val onClick: (QuickActionIconStyle) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val iv: ShapeableImageView = itemView.findViewById(R.id.iv)

        fun bind(item: QuickActionIconStyle) {
            iv.setImageResource(item.iconRes)
            itemView.setOnClickListener { onClick(item) }
        }
    }
}

