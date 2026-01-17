package com.example.curs_alexander.ui.analysis

data class AnalysisCard(
    val title: String,
    val body: String
)

data class AnalysisUiState(
    val isLoading: Boolean = true,
    val cards: List<AnalysisCard> = emptyList()
)

