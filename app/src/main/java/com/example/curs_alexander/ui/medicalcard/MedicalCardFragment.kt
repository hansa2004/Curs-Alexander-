package com.example.curs_alexander.ui.medicalcard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.curs_alexander.R
import com.example.curs_alexander.export.PdfTextSize
import com.example.curs_alexander.settings.SettingsCache
import com.example.curs_alexander.ui.analytics.ExportPdfUiState
import com.example.curs_alexander.ui.analytics.ExportPdfViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.radiobutton.MaterialRadioButton
import kotlinx.coroutines.launch

/**
 * Экран "Медицинская карта".
 * Здесь собраны действия, которые обычно нужны для демонстрации врачу/преподавателю:
 * - сформировать отчёт в PDF
 * - открыть сохранённый PDF
 * - отправить/поделиться PDF
 */
class MedicalCardFragment : Fragment() {

    private val exportViewModel: ExportPdfViewModel by viewModels()

    private var chooserShownForUri: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_medical_card, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnExport = view.findViewById<MaterialButton>(R.id.btnExportPdf)
        val btnOpen = view.findViewById<MaterialButton>(R.id.btnOpenPdf)
        val btnShare = view.findViewById<MaterialButton>(R.id.btnSharePdf)

        val rbNormal = view.findViewById<MaterialRadioButton>(R.id.rbPdfTextNormal)
        val rbLarge = view.findViewById<MaterialRadioButton>(R.id.rbPdfTextLarge)

        // Применяем сохранённый выбор
        val saved = SettingsCache.getPdfTextSize(requireContext()) ?: PdfTextSize.NORMAL
        rbNormal.isChecked = saved == PdfTextSize.NORMAL
        rbLarge.isChecked = saved == PdfTextSize.LARGE

        fun currentSize(): PdfTextSize {
            return if (rbLarge.isChecked) PdfTextSize.LARGE else PdfTextSize.NORMAL
        }

        rbNormal.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) SettingsCache.setPdfTextSize(requireContext(), PdfTextSize.NORMAL)
        }
        rbLarge.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) SettingsCache.setPdfTextSize(requireContext(), PdfTextSize.LARGE)
        }

        btnOpen.isEnabled = false
        btnShare.isEnabled = false

        btnExport.setOnClickListener { exportViewModel.export(currentSize()) }

        btnOpen.setOnClickListener {
            val s = exportViewModel.uiState.value
            if (s is ExportPdfUiState.Success) {
                openPdf(s.uri.toString())
            }
        }

        btnShare.setOnClickListener {
            val s = exportViewModel.uiState.value
            if (s is ExportPdfUiState.Success) {
                sharePdf(s.uri.toString())
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                exportViewModel.uiState.collect { state ->
                    when (state) {
                        is ExportPdfUiState.Idle -> {
                            btnExport.isEnabled = true
                            btnOpen.isEnabled = false
                            btnShare.isEnabled = false
                        }
                        is ExportPdfUiState.Exporting -> {
                            btnExport.isEnabled = false
                            btnOpen.isEnabled = false
                            btnShare.isEnabled = false
                            Toast.makeText(requireContext(), "Формирование отчёта...", Toast.LENGTH_SHORT).show()
                        }
                        is ExportPdfUiState.Success -> {
                            btnExport.isEnabled = true
                            btnOpen.isEnabled = true
                            btnShare.isEnabled = true
                            Toast.makeText(requireContext(), "PDF сохранён в 'Загрузки'", Toast.LENGTH_LONG).show()

                            // Авто-открытие системного выбора «Открыть/Поделиться»
                            // (показываем один раз для конкретного файла)
                            val uriStr = state.uri.toString()
                            if (chooserShownForUri != uriStr) {
                                chooserShownForUri = uriStr
                                showOpenOrShareChooser(uriStr)
                            }
                        }
                        is ExportPdfUiState.Error -> {
                            btnExport.isEnabled = true
                            btnOpen.isEnabled = false
                            btnShare.isEnabled = false
                            Toast.makeText(requireContext(), "Ошибка: ${state.message}", Toast.LENGTH_LONG).show()
                            exportViewModel.consumeResult()
                        }
                    }
                }
            }
        }
    }

    private fun showOpenOrShareChooser(uriStr: String) {
        val uri = android.net.Uri.parse(uriStr)

        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, "Открыть или отправить отчёт")
        // Добавляем вариант «Открыть» как дополнительную опцию в chooser
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(viewIntent))

        runCatching { startActivity(chooser) }
            .onFailure {
                // запасной вариант: хотя бы открыть
                runCatching { startActivity(viewIntent) }
                    .onFailure {
                        Toast.makeText(requireContext(), "Не найдено приложение для открытия/отправки PDF", Toast.LENGTH_LONG).show()
                    }
            }
    }

    private fun openPdf(uriStr: String) {
        val uri = android.net.Uri.parse(uriStr)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        runCatching { startActivity(intent) }
            .onFailure { Toast.makeText(requireContext(), "Не найдено приложение для открытия PDF", Toast.LENGTH_LONG).show() }
    }

    private fun sharePdf(uriStr: String) {
        val uri = android.net.Uri.parse(uriStr)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        runCatching { startActivity(Intent.createChooser(intent, "Поделиться отчётом")) }
            .onFailure { Toast.makeText(requireContext(), "Не удалось открыть меню отправки", Toast.LENGTH_LONG).show() }
    }
}
