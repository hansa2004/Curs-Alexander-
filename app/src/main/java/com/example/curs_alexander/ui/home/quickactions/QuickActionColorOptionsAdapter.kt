package com.example.curs_alexander.ui.home.quickactions

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.curs_alexander.R
import com.google.android.material.imageview.ShapeableImageView

class QuickActionColorOptionsAdapter(
    private val colors: List<Int>,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<QuickActionColorOptionsAdapter.VH>() {

    private var selected: Int? = null

    fun setSelected(color: Int?) {
        selected = color
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quick_action_color_swatch, parent, false)
        return VH(v, onClick)
    }

    override fun getItemCount(): Int = colors.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(colors[position], selected)
    }

    class VH(itemView: View, private val onClick: (Int) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val ivSwatch: ShapeableImageView = itemView.findViewById(R.id.ivSwatch)
        private val ivFill: ShapeableImageView = itemView.findViewById(R.id.ivFill)
        private val ivCheck: ShapeableImageView = itemView.findViewById(R.id.ivCheck)

        fun bind(colorInt: Int, selected: Int?) {
            ivFill.setBackgroundResource(R.drawable.qa_color_dot)
            ivFill.backgroundTintList = ColorStateList.valueOf(colorInt)

            val isSelected = selected != null && selected == colorInt
            ivCheck.visibility = if (isSelected) View.VISIBLE else View.GONE
            ivSwatch.setBackgroundResource(
                if (isSelected) R.drawable.qa_selected_outline else R.drawable.qa_unselected_outline
            )

            itemView.setOnClickListener { onClick(colorInt) }
        }
    }
}

