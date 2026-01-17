package com.example.curs_alexander.ui.education

import androidx.annotation.StringRes

/**
 * Локальная модель статьи для раздела "Полезная информация".
 *
 * Данные хранятся в ресурсах (strings) — без интернета, API и сторонних библиотек.
 */
data class EducationArticle(
    val id: String,
    @StringRes val titleRes: Int,
    @StringRes val bodyRes: Int
)

