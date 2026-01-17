package com.example.curs_alexander.ui.education

import com.example.curs_alexander.R

/**
 * Простейший локальный источник данных для образовательного модуля.
 */
object EducationRepository {

    fun getAll(): List<EducationArticle> = listOf(
        EducationArticle(
            id = "bp_control",
            titleRes = R.string.education_title_bp_control,
            bodyRes = R.string.education_body_bp_control
        ),
        EducationArticle(
            id = "symptoms_diary",
            titleRes = R.string.education_title_symptoms_diary,
            bodyRes = R.string.education_body_symptoms_diary
        ),
        EducationArticle(
            id = "see_doctor",
            titleRes = R.string.education_title_see_doctor,
            bodyRes = R.string.education_body_see_doctor
        ),
        EducationArticle(
            id = "self_monitoring",
            titleRes = R.string.education_title_self_monitoring,
            bodyRes = R.string.education_body_self_monitoring
        )
    )

    fun getById(id: String): EducationArticle? = getAll().firstOrNull { it.id == id }
}

