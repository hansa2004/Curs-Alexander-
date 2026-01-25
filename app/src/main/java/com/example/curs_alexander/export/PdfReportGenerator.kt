package com.example.curs_alexander.export

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.curs_alexander.data.db.BloodPressureWithContext
import java.util.Date
import java.util.Locale

/**
 * Простой генератор PDF на стандартном Android API (PdfDocument).
 * Без сторонних библиотек, учебная структура.
 */
class PdfReportGenerator {

    private val pageWidth = 595 // A4 примерно при 72dpi
    private val pageHeight = 842

    private val baseMargin = 40

    private fun scaleFor(size: PdfTextSize): Float = when (size) {
        PdfTextSize.NORMAL -> 1.0f
        PdfTextSize.LARGE -> 1.25f
    }

    private fun createPaint(sizePx: Float, bold: Boolean): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = sizePx
        typeface = Typeface.create(Typeface.DEFAULT, if (bold) Typeface.BOLD else Typeface.NORMAL)
    }

    fun generate(data: PdfReportData, textSize: PdfTextSize = PdfTextSize.NORMAL): PdfDocument {
        val scale = scaleFor(textSize)

        val margin = (baseMargin * scale).toInt()
        val lineSpacing = (16 * scale).toInt().coerceAtLeast(14)

        val paintTitle = createPaint(18f * scale, bold = true)
        val paintH2 = createPaint(14f * scale, bold = true)
        val paintText = createPaint(11f * scale, bold = false)

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
        y += (8 * scale).toInt()

        // Пользователь
        drawLine("Пользователь: ${data.user.name ?: "(не указано)"}")
        drawLine("Дата рождения: ${data.user.birthDate ?: "(не указано)"}")
        y += (8 * scale).toInt()
        drawLine(data.periodText)
        y += (8 * scale).toInt()

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
        y += (8 * scale).toInt()

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

        y += (4 * scale).toInt()
        drawLine("Список измерений:")
        if (data.pressures.isEmpty()) {
            drawLine("— нет данных")
        } else {
            data.pressures.forEach { bp ->
                drawLine("• ${formatTs(bp.timestampMillis)} — ${bp.systolic}/${bp.diastolic}")
            }
        }

        y += (10 * scale).toInt()

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

            y += (6 * scale).toInt()
            drawLine("Последние записи:")
            data.symptoms.take(50).forEach { s ->
                val intensityText = s.intensity?.let { "интенсивность: $it, " } ?: ""
                drawLine("• ${formatTs(s.timestampMillis)} — ${s.name} (${intensityText}${s.comment ?: "без комментария"})")
            }
        }

        y += (10 * scale).toInt()

        // Контекст измерений (отдельный раздел, без интерпретации)
        ensureSpace(2)
        drawLine("Контекст измерения", paintH2)
        drawParagraph(
            listOf(
                "Этот раздел содержит условия, при которых были внесены измерения.",
                "Информация носит поясняющий характер и не является медицинским выводом."
            )
        )

        val ctxLines = buildContextLines(data.pressureWithContext)
        if (ctxLines.isEmpty()) {
            drawLine("— контекст не указан")
        } else {
            drawLine("Давление: контекст по измерениям")
            ctxLines.forEach { line ->
                ensureSpace(1)
                drawLine("• $line")
            }
        }

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
        drawPageFooter(canvas, pageNumber, margin, pageHeight, paintText)

        doc.finishPage(page)
        return doc
    }

    private fun drawPageFooter(canvas: Canvas, pageNumber: Int, margin: Int, pageHeight: Int, paint: Paint) {
        val footer = "Стр. $pageNumber"
        canvas.drawText(footer, margin.toFloat(), (pageHeight - margin / 2).toFloat(), paint)
    }

    private fun formatTs(ts: Long): String {
        val df = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        return df.format(Date(ts))
    }

    private fun buildContextLines(list: List<BloodPressureWithContext>): List<String> {
        if (list.isEmpty()) return emptyList()

        // Берём только записи, где пользователь действительно что-то заполнил.
        val filled = list
            .filter {
                !it.timeOfDay.isNullOrBlank() || !it.state.isNullOrBlank() || !it.contextComment.isNullOrBlank()
            }
            .take(120) // разумный лимит, чтобы отчёт не разрастался

        if (filled.isEmpty()) return emptyList()

        return filled.map { bp ->
            val dt = formatTs(bp.bp.timestampMillis)
            val parts = mutableListOf<String>()

            val time = when (bp.timeOfDay) {
                "morning" -> "утро"
                "day" -> "день"
                "evening" -> "вечер"
                else -> null
            }
            if (!time.isNullOrBlank()) parts.add(time)

            val state = when (bp.state) {
                "rest" -> "покой"
                "after_load" -> "после нагрузки"
                "after_stress" -> "после стресса"
                else -> null
            }
            if (!state.isNullOrBlank()) parts.add(state)

            val comment = bp.contextComment?.trim().orEmpty().ifBlank { null }
            if (!comment.isNullOrBlank()) parts.add(comment)

            val ctx = parts.joinToString(", ")
            "${dt} — ${bp.bp.systolic}/${bp.bp.diastolic}${if (ctx.isNotBlank()) " (${ctx})" else ""}"
        }
    }
}
