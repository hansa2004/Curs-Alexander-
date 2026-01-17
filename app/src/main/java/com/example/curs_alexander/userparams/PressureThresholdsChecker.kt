package com.example.curs_alexander.userparams

/**
 * Утилита сравнения измерения давления с пользовательскими порогами.
 *
 * Важно: это не медицинские рекомендации и не постановка диагноза.
 * Мы сравниваем только с ориентировочными значениями, которые задал сам пользователь.
 */
object PressureThresholdsChecker {

    enum class Result {
        ABOVE_USER_THRESHOLD,
        BELOW_USER_THRESHOLD,
        WITHIN_USER_THRESHOLDS
    }

    fun check(systolic: Int, diastolic: Int, params: UserParams): Result {
        return when {
            systolic > params.upperSystolic || diastolic > params.upperDiastolic -> Result.ABOVE_USER_THRESHOLD
            systolic < params.lowerSystolic || diastolic < params.lowerDiastolic -> Result.BELOW_USER_THRESHOLD
            else -> Result.WITHIN_USER_THRESHOLDS
        }
    }
}

