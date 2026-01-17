package com.example.curs_alexander.userparams

/**
 * Индивидуальные параметры пользователя.
 *
 * Важно: это не медицинские рекомендации, а пользовательские ориентировочные значения
 * для личного удобства и наблюдений.
 */
data class UserParams(
    val upperSystolic: Int,
    val upperDiastolic: Int,
    val lowerSystolic: Int,
    val lowerDiastolic: Int,
    val ageCategory: AgeCategory
)

enum class AgeCategory(val id: String) {
    UNDER_30("under_30"),
    FROM_30_TO_50("30_50"),
    ABOVE_50("50_plus");

    companion object {
        fun fromId(id: String?): AgeCategory = when (id) {
            UNDER_30.id -> UNDER_30
            FROM_30_TO_50.id -> FROM_30_TO_50
            ABOVE_50.id -> ABOVE_50
            else -> FROM_30_TO_50
        }
    }
}

