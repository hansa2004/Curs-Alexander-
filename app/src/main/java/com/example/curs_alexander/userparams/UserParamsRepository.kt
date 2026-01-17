package com.example.curs_alexander.userparams

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.curs_alexander.settings.settingsDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Репозиторий индивидуальных параметров.
 *
 * Хранение локально в DataStore (единый settingsDataStore).
 */
class UserParamsRepository(private val context: Context) {

    val params: Flow<UserParams> = context.settingsDataStore.data.map { prefs ->
        val upperSys = prefs[KEY_UPPER_SYS] ?: DEFAULT_UPPER_SYS
        val upperDia = prefs[KEY_UPPER_DIA] ?: DEFAULT_UPPER_DIA
        val lowerSys = prefs[KEY_LOWER_SYS] ?: DEFAULT_LOWER_SYS
        val lowerDia = prefs[KEY_LOWER_DIA] ?: DEFAULT_LOWER_DIA
        val age = AgeCategory.fromId(prefs[KEY_AGE_CATEGORY])

        UserParams(
            upperSystolic = upperSys,
            upperDiastolic = upperDia,
            lowerSystolic = lowerSys,
            lowerDiastolic = lowerDia,
            ageCategory = age
        )
    }

    suspend fun setUpper(systolic: Int, diastolic: Int) {
        context.settingsDataStore.edit { prefs ->
            prefs[KEY_UPPER_SYS] = systolic
            prefs[KEY_UPPER_DIA] = diastolic
        }
    }

    suspend fun setLower(systolic: Int, diastolic: Int) {
        context.settingsDataStore.edit { prefs ->
            prefs[KEY_LOWER_SYS] = systolic
            prefs[KEY_LOWER_DIA] = diastolic
        }
    }

    suspend fun setAgeCategory(category: AgeCategory) {
        context.settingsDataStore.edit { prefs ->
            prefs[KEY_AGE_CATEGORY] = category.id
        }
    }

    suspend fun setAll(params: UserParams) {
        context.settingsDataStore.edit { prefs ->
            prefs[KEY_UPPER_SYS] = params.upperSystolic
            prefs[KEY_UPPER_DIA] = params.upperDiastolic
            prefs[KEY_LOWER_SYS] = params.lowerSystolic
            prefs[KEY_LOWER_DIA] = params.lowerDiastolic
            prefs[KEY_AGE_CATEGORY] = params.ageCategory.id
        }
    }

    companion object {
        private val KEY_UPPER_SYS = intPreferencesKey("user_upper_sys")
        private val KEY_UPPER_DIA = intPreferencesKey("user_upper_dia")
        private val KEY_LOWER_SYS = intPreferencesKey("user_lower_sys")
        private val KEY_LOWER_DIA = intPreferencesKey("user_lower_dia")
        private val KEY_AGE_CATEGORY = stringPreferencesKey("user_age_category")

        const val DEFAULT_UPPER_SYS = 140
        const val DEFAULT_UPPER_DIA = 90
        const val DEFAULT_LOWER_SYS = 90
        const val DEFAULT_LOWER_DIA = 60
    }
}

