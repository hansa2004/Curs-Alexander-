package com.example.curs_alexander.data

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var onboardingCompleted: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        set(value) { prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, value).apply() }

    var userName: String?
        get() = prefs.getString(KEY_USER_NAME, null)
        set(value) { prefs.edit().putString(KEY_USER_NAME, value).apply() }

    companion object {
        private const val PREFS_NAME = "health_support_prefs"
        private const val KEY_ONBOARDING_COMPLETED = "onboardingCompleted"
        private const val KEY_USER_NAME = "userName"
    }
}

