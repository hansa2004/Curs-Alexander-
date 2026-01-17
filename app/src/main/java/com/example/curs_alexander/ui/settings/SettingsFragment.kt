package com.example.curs_alexander.ui.settings

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.CompoundButton
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.curs_alexander.R
import com.example.curs_alexander.data.AppDataCleaner
import com.example.curs_alexander.notifications.ReminderAlarmScheduler
import com.example.curs_alexander.settings.FontScale
import com.example.curs_alexander.settings.SettingsApplier
import com.example.curs_alexander.settings.ThemeMode
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class SettingsFragment : Fragment() {

    private val vm: SettingsViewModel by viewModels()

    private lateinit var switchReminders: MaterialSwitch
    private lateinit var tvTime: MaterialTextView

    private var isClearingData: Boolean = false

    // Добавляем ссылки, чтобы не искать вьюхи повторно и централизовать render
    private var btnClear: MaterialButton? = null
    private var progressClear: ProgressBar? = null

    private fun renderDataClearing() {
        btnClear?.isEnabled = !isClearingData
        progressClear?.visibility = if (isClearingData) View.VISIBLE else View.GONE
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnTheme = view.findViewById<MaterialButton>(R.id.btnTheme)
        val btnFont = view.findViewById<MaterialButton>(R.id.btnFont)
        val btnPrivacySecurity = view.findViewById<MaterialButton>(R.id.btnPrivacySecurity)

        switchReminders = view.findViewById(R.id.switchReminders)
        tvTime = view.findViewById(R.id.tvReminderTime)
        val btnPickTime = view.findViewById<MaterialButton>(R.id.btnPickTime)
        btnClear = view.findViewById(R.id.btnClearData)
        progressClear = view.findViewById(R.id.progressClearData)

        fun renderNotificationsUi(enabled: Boolean) {
            // Когда напоминания выключены — выбор времени недоступен
            btnPickTime.isEnabled = enabled
            tvTime.isEnabled = enabled
            tvTime.alpha = if (enabled) 1f else 0.5f
        }

        view.findViewById<MaterialTextView>(R.id.tvAppName).text = getString(R.string.app_name)
        val versionName = runCatching {
            val pm = requireContext().packageManager
            val pInfo = pm.getPackageInfo(requireContext().packageName, 0)
            pInfo.versionName ?: ""
        }.getOrDefault("")
        view.findViewById<MaterialTextView>(R.id.tvAppVersion).text = if (versionName.isNotBlank()) {
            "v$versionName"
        } else {
            ""
        }

        btnTheme.setOnClickListener { showThemeDialog() }
        btnFont.setOnClickListener { showFontDialog() }

        btnPickTime.setOnClickListener { showTimePicker() }

        btnClear?.setOnClickListener { confirmClearData() }

        btnPrivacySecurity.setOnClickListener {
            findNavController().navigate(R.id.privacySecurityFragment)
        }

        val remindersListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            vm.setRemindersEnabled(isChecked)
            applyReminderScheduling(isChecked)
            renderNotificationsUi(isChecked)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vm.themeMode.collect { mode ->
                        btnTheme.text = when (mode) {
                            ThemeMode.LIGHT -> getString(R.string.settings_theme_light)
                            ThemeMode.DARK -> getString(R.string.settings_theme_dark)
                            ThemeMode.SYSTEM -> getString(R.string.settings_theme_system)
                        }
                    }
                }
                launch {
                    vm.fontScale.collect { scale ->
                        btnFont.text = when (scale) {
                            FontScale.SMALL -> getString(R.string.settings_font_small)
                            FontScale.MEDIUM -> getString(R.string.settings_font_medium)
                            FontScale.LARGE -> getString(R.string.settings_font_large)
                        }
                    }
                }
                launch {
                    vm.notifications.collect { n ->
                        // Важно: не триггерим listener на программном обновлении
                        switchReminders.setOnCheckedChangeListener(null)
                        if (switchReminders.isChecked != n.enabled) {
                            switchReminders.isChecked = n.enabled
                        }
                        switchReminders.setOnCheckedChangeListener(remindersListener)

                        tvTime.text = String.format(Locale.getDefault(), "%02d:%02d", n.hour, n.minute)
                        renderNotificationsUi(n.enabled)
                    }
                }
            }
        }

        switchReminders.setOnCheckedChangeListener(remindersListener)
        renderDataClearing()
        renderNotificationsUi(switchReminders.isChecked)
    }

    private fun showThemeDialog() {
        val items = arrayOf(
            getString(R.string.settings_theme_light),
            getString(R.string.settings_theme_dark),
            getString(R.string.settings_theme_system)
        )

        val current = vm.themeMode.value
        val checked = when (current) {
            ThemeMode.LIGHT -> 0
            ThemeMode.DARK -> 1
            ThemeMode.SYSTEM -> 2
        }

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.settings_theme_title)
            .setSingleChoiceItems(items, checked) { dialog, which ->
                val selected = when (which) {
                    0 -> ThemeMode.LIGHT
                    1 -> ThemeMode.DARK
                    else -> ThemeMode.SYSTEM
                }
                vm.setTheme(selected)
                SettingsApplier.applyTheme(selected)
                // Не переходим никуда: остаёмся в Settings
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showFontDialog() {
        val items = arrayOf(
            getString(R.string.settings_font_small),
            getString(R.string.settings_font_medium),
            getString(R.string.settings_font_large)
        )

        val current = vm.fontScale.value
        val checked = when (current) {
            FontScale.SMALL -> 0
            FontScale.MEDIUM -> 1
            FontScale.LARGE -> 2
        }

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.settings_font_title)
            .setSingleChoiceItems(items, checked) { dialog, which ->
                val selected = when (which) {
                    0 -> FontScale.SMALL
                    1 -> FontScale.MEDIUM
                    else -> FontScale.LARGE
                }
                vm.setFont(selected)
                // Применение размера шрифта сделано в MainActivity.attachBaseContext.
                // Чтобы UI пересобрался с новым fontScale — пересоздаём Activity.
                // Навигацию не трогаем, остаёмся в настройках.
                requireActivity().recreate()
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showTimePicker() {
        val n = vm.notifications.value
        TimePickerDialog(
            requireContext(),
            { _, hour, minute ->
                vm.setReminderTime(hour, minute)
                applyReminderScheduling(switchReminders.isChecked)
            },
            n.hour,
            n.minute,
            true
        ).show()
    }

    private fun applyReminderScheduling(enabled: Boolean) {
        // Учебная, простая логика: один ежедневный будильник (id=1) открывает экран добавления давления.
        val hour = vm.notifications.value.hour
        val minute = vm.notifications.value.minute
        if (enabled) {
            ReminderAlarmScheduler.scheduleDaily(requireContext(), 1L, hour, minute)
        } else {
            ReminderAlarmScheduler.cancel(requireContext(), 1L)
        }
    }

    private fun confirmClearData() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.settings_clear_title)
            .setMessage(R.string.settings_clear_message)
            .setPositiveButton(R.string.settings_clear_confirm) { _, _ ->
                clearAllData()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun clearAllData() {
        if (isClearingData) return
        isClearingData = true
        renderDataClearing()

        viewLifecycleOwner.lifecycleScope.launch {
            val ok = withContext(Dispatchers.IO) {
                runCatching { AppDataCleaner.clearAll(requireContext()) }.isSuccess
            }

            isClearingData = false
            renderDataClearing()

            if (ok) {
                Toast.makeText(requireContext(), R.string.settings_clear_done, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), R.string.settings_clear_failed, Toast.LENGTH_LONG).show()
            }
        }
    }
}
