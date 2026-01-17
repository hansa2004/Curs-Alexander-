package com.example.curs_alexander.export

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument

/**
 * Простой генератор PDF на стандартном Android API (PdfDocument).
 * Без сторонних библиотек, учебная структура.
 */
class PdfReportGenerator {

    private val pageWidth = 595 // A4 примерно при 72dpi
    private val pageHeight = 842

    private val margin = 40

    private val paintTitle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 18f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val paintH2 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 14f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 11f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }

    private val lineSpacing = 16

    fun generate(data: PdfReportData): PdfDocument {
        val doc = PdfDocument()

        var pageNumber = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        var canvas = page.canvas
        var y = margin

        fun newPage() {
            doc.finishPage(page)
            pageNumber += 1
            page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
            canvas = page.canvas
            y = margin
        }

        fun ensureSpace(lines: Int = 1) {
            val need = lines * lineSpacing
            if (y + need > pageHeight - margin) newPage()
        }

        fun drawLine(text: String, paint: Paint = paintText) {
            ensureSpace(1)
            canvas.drawText(text, margin.toFloat(), y.toFloat(), paint)
            y += lineSpacing
        }

        fun drawParagraph(lines: List<String>) {
            lines.forEach { drawLine(it, paintText) }
        }

        // Заголовок
        drawLine(data.title, paintTitle)
        y += 8

        // Пользователь
        drawLine("Пользователь: ${data.user.name ?: "(не указано)"}")
        drawLine("Дата рождения: ${data.user.birthDate ?: "(не указано)"}")
        y += 8
        drawLine(data.periodText)
        y += 8

        // Краткое резюме (для быстрого просмотра)
        ensureSpace(6)
        drawLine("Краткое резюме", paintH2)
        drawLine("Дней наблюдений: ${data.observationDays?.toString() ?: "—"}")
        drawLine("Всего измерений давления: ${data.totalPressureCount}")
        drawLine("Всего записей симптомов: ${data.totalSymptomsCount}")
        if (data.topSymptoms.isNotEmpty()) {
            drawLine("Топ симптомов: ${data.topSymptoms.joinToString(", ")}")
        } else {
            drawLine("Топ симптомов: —")
        }
        y += 8

        // Давление
        ensureSpace(2)
        drawLine("Артериальное давление", paintH2)

        val last = data.lastPressure
        drawLine(
            if (last != null) {
                "Последнее измерение: ${last.systolic}/${last.diastolic} (${formatTs(last.timestampMillis)})"
            } else {
                "Последнее измерение: нет данных"
            }
        )

        val avg = data.avgPressure7d
        drawLine(
            if (avg != null) {
                "Среднее за 7 дней: ${avg.first}/${avg.second}"
            } else {
                "Среднее за 7 дней: нет данных"
            }
        )

        y += 4
        drawLine("Список измерений:")
        if (data.pressures.isEmpty()) {
            drawLine("— нет данных")
        } else {
            data.pressures.forEach { bp ->
                drawLine("• ${formatTs(bp.timestampMillis)} — ${bp.systolic}/${bp.diastolic}")
            }
        }

        y += 10

        // Симптомы
        ensureSpace(2)
        drawLine("Симптомы", paintH2)

        if (data.symptoms.isEmpty()) {
            drawLine("Список симптомов: нет данных")
        } else {
            drawLine("Частота и интенсивность:")
            if (data.symptomStatsLines.isEmpty()) {
                drawLine("— нет данных")
            } else {
                data.symptomStatsLines.forEach { line ->
                    drawLine("• $line")
                }
            }

            y += 6
            drawLine("Последние записи:")
            data.symptoms.take(50).forEach { s ->
                val intensityText = s.intensity?.let { "интенсивность: $it, " } ?: ""
                drawLine("• ${formatTs(s.timestampMillis)} — ${s.name} (${intensityText}${s.comment ?: "без комментария"})")
            }
        }

        y += 12

        // Примечание
        ensureSpace(3)
        drawLine("Примечание", paintH2)
        drawParagraph(
            listOf(
                "Данные носят справочный характер и не являются медицинским диагнозом.",
                "Отчёт сформирован приложением в учебных целях."
            )
        )

        // Номер страницы
        drawPageFooter(canvas, pageNumber)

        doc.finishPage(page)
        return doc
    }

    private fun drawPageFooter(canvas: Canvas, pageNumber: Int) {
        val footer = "Стр. $pageNumber"
        canvas.drawText(footer, margin.toFloat(), (pageHeight - margin / 2).toFloat(), paintText)
    }

    private fun formatTs(ts: Long): String {
        val df = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
        return df.format(java.util.Date(ts))
    }
}
