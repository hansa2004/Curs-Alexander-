package com.example.curs_alexander.ui.home.quickactions

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.curs_alexander.R
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel

/**
 * Адаптер для выбора быстрого действия в диалоге (иконка + заголовок + подпись).
 */
class QuickActionPickerAdapter(
    private val onClick: (QuickActionType) -> Unit
) : RecyclerView.Adapter<QuickActionPickerAdapter.VH>() {

    private val items = mutableListOf<QuickActionType>()
    private var iconOverrides: Map<String, String> = emptyMap()

    fun submit(list: List<QuickActionType>, iconOverrides: Map<String, String> = emptyMap()) {
        items.clear()
        items.addAll(list)
        this.iconOverrides = iconOverrides
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quick_action_picker_row, parent, false)
        return VH(v, onClick)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position], iconOverrides)
    }

    class VH(
        itemView: View,
        private val onClick: (QuickActionType) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val ivIcon: ShapeableImageView = itemView.findViewById(R.id.ivIcon)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvSubtitle: TextView = itemView.findViewById(R.id.tvSubtitle)

        fun bind(item: QuickActionType, iconOverrides: Map<String, String>) {
            tvTitle.setText(item.titleRes)
            tvSubtitle.text = buildSubtitle(itemView)

            val style = QuickActionIconStyle.fromId(iconOverrides[item.id])
            ivIcon.setImageResource(style?.iconRes ?: item.iconRes)

            val accent = ContextCompat.getColor(itemView.context, item.accentColorRes)
            val shape = ShapeAppearanceModel.builder().setAllCornerSizes(999f).build()
            ivIcon.background = MaterialShapeDrawable(shape).apply {
                fillColor = ColorStateList.valueOf(accent)
            }
            ivIcon.imageTintList = ColorStateList.valueOf(Color.WHITE)

            itemView.setOnClickListener { onClick(item) }
        }

        private fun buildSubtitle(view: View): String {
            // Пока без ресурсов, чтобы не плодить строки; можно вынести потом.
            // Главное: нейтральная UX-подсказка.
            return "Нажмите, чтобы добавить"
        }
    }
}
