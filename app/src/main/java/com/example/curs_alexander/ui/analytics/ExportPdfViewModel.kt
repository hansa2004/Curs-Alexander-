package com.example.curs_alexander.ui.analytics

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.curs_alexander.export.ExportNotification
import com.example.curs_alexander.export.PdfReportGenerator
import com.example.curs_alexander.export.PdfReportRepository
import com.example.curs_alexander.export.PdfStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ExportPdfUiState {
    data object Idle : ExportPdfUiState
    data object Exporting : ExportPdfUiState
    data class Success(val uri: Uri) : ExportPdfUiState
    data class Error(val message: String) : ExportPdfUiState
}

class ExportPdfViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = PdfReportRepository(app)
    private val generator = PdfReportGenerator()

    private val _uiState = MutableStateFlow<ExportPdfUiState>(ExportPdfUiState.Idle)
    val uiState: StateFlow<ExportPdfUiState> = _uiState.asStateFlow()

    fun export() {
        // защита от двойных нажатий
        if (_uiState.value is ExportPdfUiState.Exporting) return

        viewModelScope.launch {
            _uiState.value = ExportPdfUiState.Exporting
            try {
                val data = repo.loadReportData()
                val pdf = generator.generate(data)

                val fileName = "health_report_${System.currentTimeMillis()}.pdf"
                val uri = PdfStorage.saveToDownloads(getApplication(), fileName) { out ->
                    pdf.writeTo(out)
                }
                pdf.close()

                if (uri != null) {
                    // Уведомление может не показаться, если пользователь запретил POST_NOTIFICATIONS,
                    // но сам экспорт при этом успешный.
                    runCatching { ExportNotification.showSaved(getApplication(), uri) }
                    _uiState.value = ExportPdfUiState.Success(uri)
                } else {
                    _uiState.value = ExportPdfUiState.Error("Не удалось сохранить PDF в папку 'Загрузки'.")
                }
            } catch (t: Throwable) {
                _uiState.value = ExportPdfUiState.Error(t.message ?: "Ошибка экспорта PDF")
            }
        }
    }

    fun consumeResult() {
        // чтобы сообщение/состояние не повторялось при повороте экрана
        if (_uiState.value !is ExportPdfUiState.Exporting) {
            _uiState.value = ExportPdfUiState.Idle
        }
    }
}
