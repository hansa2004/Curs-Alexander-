package com.example.curs_alexander.ui.userparams

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.curs_alexander.userparams.AgeCategory
import com.example.curs_alexander.userparams.UserParams
import com.example.curs_alexander.userparams.UserParamsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserParamsViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = UserParamsRepository(app)

    val params: StateFlow<UserParams> = repo.params
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            UserParams(
                upperSystolic = UserParamsRepository.DEFAULT_UPPER_SYS,
                upperDiastolic = UserParamsRepository.DEFAULT_UPPER_DIA,
                lowerSystolic = UserParamsRepository.DEFAULT_LOWER_SYS,
                lowerDiastolic = UserParamsRepository.DEFAULT_LOWER_DIA,
                ageCategory = AgeCategory.FROM_30_TO_50
            )
        )

    fun saveAll(upperSys: Int, upperDia: Int, lowerSys: Int, lowerDia: Int, ageCategory: AgeCategory) {
        viewModelScope.launch {
            repo.setAll(
                UserParams(
                    upperSystolic = upperSys,
                    upperDiastolic = upperDia,
                    lowerSystolic = lowerSys,
                    lowerDiastolic = lowerDia,
                    ageCategory = ageCategory
                )
            )
        }
    }
}

