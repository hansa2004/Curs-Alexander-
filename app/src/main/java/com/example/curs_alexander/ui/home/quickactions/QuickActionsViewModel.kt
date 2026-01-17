package com.example.curs_alexander.ui.home.quickactions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class QuickActionsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = QuickActionsRepository(app)

    val actions: StateFlow<List<QuickActionType>> = repo.quickActions
        .stateIn(viewModelScope, SharingStarted.Eagerly, repo.defaultActions())

    fun move(from: Int, to: Int) {
        val current = actions.value.toMutableList()
        if (from !in current.indices || to !in current.indices) return
        val item = current.removeAt(from)
        current.add(to, item)
        save(current)
    }

    fun removeAt(position: Int) {
        val current = actions.value.toMutableList()
        if (position !in current.indices) return
        current.removeAt(position)
        save(current)
    }

    fun add(type: QuickActionType) {
        val current = actions.value.toMutableList()
        if (current.contains(type)) return
        current.add(type)
        save(current)
    }

    fun restoreDefaults() {
        save(repo.defaultActions())
    }

    private fun save(list: List<QuickActionType>) {
        viewModelScope.launch { repo.setQuickActions(list) }
    }
}

