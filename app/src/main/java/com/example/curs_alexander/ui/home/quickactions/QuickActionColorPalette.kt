package com.example.curs_alexander.ui.home.quickactions

import androidx.annotation.ColorInt

/**
 * Небольшая палитра цветов для индивидуальной окраски быстрых действий.
 * Храним целочисленный ARGB в DataStore.
 */
object QuickActionColorPalette {

    // Простые, читаемые цвета (ARGB)
    val colors: List<Int> = listOf(
        0xFF2563EB.toInt(), // blue
        0xFFDB2777.toInt(), // pink
        0xFF7C3AED.toInt(), // violet
        0xFF059669.toInt(), // emerald
        0xFFEA580C.toInt(), // orange
        0xFF475569.toInt(), // slate
        0xFF0EA5E9.toInt(), // sky
        0xFFF59E0B.toInt(), // amber
        0xFF22C55E.toInt(), // green
        0xFFEF4444.toInt()  // red
    )

    @ColorInt
    fun fallbackColor(@ColorInt defaultColor: Int): Int = defaultColor
}

