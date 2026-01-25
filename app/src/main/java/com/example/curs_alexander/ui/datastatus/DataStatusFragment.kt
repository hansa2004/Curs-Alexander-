package com.example.curs_alexander.ui.datastatus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.curs_alexander.R
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.launch

class DataStatusFragment : Fragment() {

    private val viewModel: DataStatusViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_data_status, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvStatus = view.findViewById<TextView>(R.id.tvOverallStatus)
        val tvDays = view.findViewById<TextView>(R.id.tvDaysValue)
        val progress = view.findViewById<LinearProgressIndicator>(R.id.progressData)
        val tvPercent = view.findViewById<TextView>(R.id.tvPercent)

        val tvTodayValue = view.findViewById<TextView>(R.id.tvIndicatorTodayValue)
        val tvLast3Value = view.findViewById<TextView>(R.id.tvIndicatorLast3Value)
        val tvLast7Value = view.findViewById<TextView>(R.id.tvIndicatorLast7Value)
        val tvGapsValue = view.findViewById<TextView>(R.id.tvIndicatorGapsValue)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { s ->
                    tvStatus.text = when (s.status) {
                        DataStatusViewModel.OverallStatus.ENOUGH -> getString(R.string.data_status_enough)
                        DataStatusViewModel.OverallStatus.PARTIAL -> getString(R.string.data_status_partial)
                        DataStatusViewModel.OverallStatus.NOT_ENOUGH -> getString(R.string.data_status_not_enough)
                    }

                    tvDays.text = getString(R.string.data_status_days_value, s.daysWithData)

                    progress.max = 100
                    progress.progress = s.percent
                    tvPercent.text = getString(R.string.data_status_percent_value, s.percent)

                    tvTodayValue.text = yesNo(s.hasToday)
                    tvLast3Value.text = yesNo(s.hasLast3Days)
                    tvLast7Value.text = yesNo(s.hasLast7Days)
                    // Для "пропусков" удобнее показать "Есть"/"Нет"
                    tvGapsValue.text = if (s.hasGapMoreThan2Days) {
                        getString(R.string.data_status_gaps_yes)
                    } else {
                        getString(R.string.data_status_gaps_no)
                    }
                }
            }
        }
    }

    private fun yesNo(value: Boolean): String {
        return if (value) getString(R.string.data_status_yes) else getString(R.string.data_status_no)
    }
}

