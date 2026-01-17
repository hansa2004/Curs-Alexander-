package com.example.curs_alexander.userparams

import android.content.Context
import kotlinx.coroutines.flow.Flow

/**
 * Тонкая прослойка, чтобы удобно переиспользовать репозиторий параметров.
 */
class UserParamsProvider(context: Context) {
    private val repo = UserParamsRepository(context.applicationContext)
    val params: Flow<UserParams> = repo.params
}

