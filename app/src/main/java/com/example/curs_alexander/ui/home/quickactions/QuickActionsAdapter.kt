package com.example.curs_alexander.ui.home.quickactions

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.curs_alexander.R
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel

class QuickActionsAdapter(
    private val onClick: (QuickActionType) -> Unit,
    private val onDelete: (Int) -> Unit,
    private val onStartDrag: (RecyclerView.ViewHolder) -> Unit,
    private val onPickColor: (QuickActionType) -> Unit,
    private val onPickIcon: (QuickActionType) -> Unit
) : RecyclerView.Adapter<QuickActionsAdapter.VH>() {

    private val items = mutableListOf<QuickActionType>()
    private var customColors: Map<String, Int> = emptyMap()
    private var iconOverrides: Map<String, String> = emptyMap()

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

    fun submitColors(map: Map<String, Int>) {
        customColors = map
        notifyDataSetChanged()
    }

    fun submitIconOverrides(map: Map<String, String>) {
        iconOverrides = map
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

    private fun blendColors(@androidx.annotation.ColorInt fg: Int, @androidx.annotation.ColorInt bg: Int, ratio: Float): Int {
        // ratio: 0..1, где 1 = fg, 0 = bg
        val r = (Color.red(bg) + (Color.red(fg) - Color.red(bg)) * ratio).toInt().coerceIn(0, 255)
        val g = (Color.green(bg) + (Color.green(fg) - Color.green(bg)) * ratio).toInt().coerceIn(0, 255)
        val b = (Color.blue(bg) + (Color.blue(fg) - Color.blue(bg)) * ratio).toInt().coerceIn(0, 255)
        return Color.rgb(r, g, b)
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView.findViewById(R.id.card)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val ivIcon: ShapeableImageView = itemView.findViewById(R.id.ivIcon)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(item: QuickActionType) {
            tvTitle.setText(item.titleRes)

            val iconStyle = QuickActionIconStyle.fromId(iconOverrides[item.id])
            ivIcon.setImageResource(iconStyle?.iconRes ?: item.iconRes)

            val defaultAccent = ContextCompat.getColor(itemView.context, item.accentColorRes)
            val accent = customColors[item.id] ?: defaultAccent

            // Фон кружка под иконкой делаем программно через MaterialShapeDrawable,
            // чтобы он гарантированно был круглым и не давал "квадратных" артефактов.
            val shape = ShapeAppearanceModel.builder()
                .setAllCornerSizes(999f)
                .build()
            val bg = MaterialShapeDrawable(shape).apply {
                fillColor = ColorStateList.valueOf(accent)
            }
            ivIcon.background = bg
            ivIcon.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(itemView.context, android.R.color.white)
            )

            // Фон карточки: делаем НЕпрозрачный пастельный оттенок (без alpha),
            // иначе на некоторых устройствах/темах появляются "светлые прямоугольники" из-за наложений.
            val surface = ContextCompat.getColor(itemView.context, android.R.color.white)
            val ratio = if (editMode) 0.18f else 0.12f // насколько сильно проявлять accent
            val tinted = blendColors(accent, surface, ratio)
            card.setCardBackgroundColor(tinted)

            // На цветных карточках state-layer (ripple/pressed overlay) может выглядеть как "плашка".
            // Для QA держим его выключенным.
            card.isClickable = true
            card.isFocusable = true
            card.foreground = null

            // Визуальный режим редактирования
            val targetScale = if (editMode) 0.98f else 1f
            itemView.animate().cancel()
            itemView.animate()
                .scaleX(targetScale)
                .scaleY(targetScale)
                .setDuration(120)
                .start()

            // Обычный режим: тап открывает действие
            itemView.setOnClickListener {
                if (!editMode) {
                    onClick(item)
                } else {
                    // В режиме редактирования тап = открыть меню настройки (иконка/цвет)
                    onPickIcon(item)
                }
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
