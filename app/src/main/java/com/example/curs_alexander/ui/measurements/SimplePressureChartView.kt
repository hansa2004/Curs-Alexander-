package com.example.curs_alexander.ui.measurements

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * Учебный пример простого графика артериального давления на Canvas.
 *
 * Поддерживает две линии: систолическое и диастолическое давление.
 * Данные ПЕРЕД рисованием нормализуются по общему диапазону min/max
 * и масштабируются в область View с учётом padding.
 */
class SimplePressureChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val systolicPoints = mutableListOf<Float>()
    private val diastolicPoints = mutableListOf<Float>()

    // Простые цвета без медицинской семантики
    private val systolicPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF6200EE.toInt() // фиолетовый
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }

    private val diastolicPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF888888.toInt() // серый
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }

    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF000000.toInt() // чёрный
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    private val pointPaintSystolic = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF6200EE.toInt()
        style = Paint.Style.FILL
    }

    private val pointPaintDiastolic = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF888888.toInt()
        style = Paint.Style.FILL
    }

    /**
     * Передаём данные для отрисовки.
     */
    fun setData(systolic: List<Float>, diastolic: List<Float>) {
        systolicPoints.clear()
        diastolicPoints.clear()
        systolicPoints.addAll(systolic)
        diastolicPoints.addAll(diastolic)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 1. Если данных меньше 2 — ничего не рисуем (фрагмент сам показывает текст)
        val count = minOf(systolicPoints.size, diastolicPoints.size)
        if (count < 2) return

        // 2. Размеры области графика с учётом padding
        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom
        if (chartWidth <= 0 || chartHeight <= 0) return

        val left = paddingLeft.toFloat()
        val top = paddingTop.toFloat()
        val right = left + chartWidth
        val bottom = top + chartHeight

        // 3. Оси (одна и та же система координат для всего)
        canvas.drawLine(left, top, left, bottom, axisPaint)        // Y
        canvas.drawLine(left, bottom, right, bottom, axisPaint)    // X

        // 4. Диапазон значений
        val allValues = (systolicPoints.take(count) + diastolicPoints.take(count))
        val minValue = allValues.minOrNull() ?: return
        val maxValue = allValues.maxOrNull() ?: return

        // 5. Шаг по X строго по формуле: chartWidth / (data.size - 1)
        val stepX = chartWidth.toFloat() / (count - 1)

        if (maxValue == minValue) {
            // 6. Все значения одинаковые — одна горизонтальная линия по центру области графика
            val yCenter = top + chartHeight / 2f
            val startX = left
            val endX = left + chartWidth
            // Рисуем одну линию для "значения" (можно использовать цвет систолического)
            canvas.drawLine(startX, yCenter, endX, yCenter, systolicPaint)
            // Точки на этой линии (обе серии совпадают визуально)
            val pointRadius = 6f
            for (i in 0 until count) {
                val x = left + i * stepX
                canvas.drawCircle(x, yCenter, pointRadius, pointPaintSystolic)
                canvas.drawCircle(x, yCenter, pointRadius, pointPaintDiastolic)
            }
            return
        }

        // 7. Нормализация Y строго по формуле:
        // y = paddingTop + chartHeight * (1f - (value - minValue) / (maxValue - minValue))
        fun calcY(value: Float): Float {
            val fraction = (value - minValue) / (maxValue - minValue)
            return top + chartHeight * (1f - fraction)
        }

        // 8. Линии между точками (систолическое и диастолическое)
        for (i in 0 until count - 1) {
            val x1 = left + i * stepX
            val x2 = left + (i + 1) * stepX

            val y1Sys = calcY(systolicPoints[i])
            val y2Sys = calcY(systolicPoints[i + 1])
            canvas.drawLine(x1, y1Sys, x2, y2Sys, systolicPaint)

            val y1Dia = calcY(diastolicPoints[i])
            val y2Dia = calcY(diastolicPoints[i + 1])
            canvas.drawLine(x1, y1Dia, x2, y2Dia, diastolicPaint)
        }

        // 9. Точки поверх линий
        val pointRadius = 6f
        for (i in 0 until count) {
            val x = left + i * stepX

            val ySys = calcY(systolicPoints[i])
            canvas.drawCircle(x, ySys, pointRadius, pointPaintSystolic)

            val yDia = calcY(diastolicPoints[i])
            canvas.drawCircle(x, yDia, pointRadius, pointPaintDiastolic)
        }
    }
}
