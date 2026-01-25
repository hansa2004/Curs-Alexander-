package com.example.curs_alexander.ui.home.quickactions

import androidx.annotation.DrawableRes
import com.example.curs_alexander.R

/**
 * Пользовательский стиль иконки для плиток быстрых действий.
 * Выбирается в режиме редактирования.
 */
enum class QuickActionIconStyle(
    val id: String,
    @DrawableRes val iconRes: Int
) {
    PLUS("plus", R.drawable.ic_add_24),
    BELL("bell", R.drawable.ic_reminder_24),
    HEART("heart", R.drawable.ic_heart_24),
    ANALYTICS("analytics", R.drawable.ic_analytics_24);

    companion object {
        fun fromId(id: String?): QuickActionIconStyle? = entries.firstOrNull { it.id == id }
    }
}

