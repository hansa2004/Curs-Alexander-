package com.example.curs_alexander.ui.home.quickactions

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.curs_alexander.R
import com.google.android.material.imageview.ShapeableImageView

class QuickActionsAdapter(
    private val onClick: (QuickActionType) -> Unit,
    private val onDelete: (Int) -> Unit,
    private val onStartDrag: (RecyclerView.ViewHolder) -> Unit
) : RecyclerView.Adapter<QuickActionsAdapter.VH>() {

    private val items = mutableListOf<QuickActionType>()
    var editMode: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun submit(list: List<QuickActionType>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_quick_action, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val ivIcon: ShapeableImageView = itemView.findViewById(R.id.ivIcon)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(item: QuickActionType) {
            tvTitle.setText(item.titleRes)

            ivIcon.setImageResource(item.iconRes)
            val accent = ContextCompat.getColor(itemView.context, item.accentColorRes)
            // Кружок под иконкой
            ivIcon.backgroundTintList = ColorStateList.valueOf(accent)
            // Иконка белая на акценте
            ivIcon.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(itemView.context, android.R.color.white)
            )

            // 'Премиум' визуальный режим редактирования
            val targetScale = if (editMode) 0.98f else 1f
            val targetAlpha = if (editMode) 0.96f else 1f
            itemView.animate().cancel()
            itemView.animate()
                .scaleX(targetScale)
                .scaleY(targetScale)
                .alpha(targetAlpha)
                .setDuration(120)
                .start()

            // Обычный режим: тап открывает действие
            itemView.setOnClickListener {
                if (!editMode) onClick(item)
            }

            // Режим редактирования: долгий тап начинает перетаскивание
            itemView.setOnLongClickListener {
                if (editMode) {
                    onStartDrag(this)
                    true
                } else {
                    false
                }
            }

            btnDelete.visibility = if (editMode) View.VISIBLE else View.GONE

            btnDelete.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) onDelete(pos)
            }
        }
    }
}
