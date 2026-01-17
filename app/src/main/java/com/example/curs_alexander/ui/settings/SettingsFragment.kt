package com.example.curs_alexander.ui.settings

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.curs_alexander.R
import com.example.curs_alexander.data.db.DbProvider
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Внешний вид
        val btnTheme = view.findViewById<MaterialButton>(R.id.btnTheme)
        val btnFont = view.findViewById<MaterialButton>(R.id.btnFont)

        // Уведомления
        switchReminders = view.findViewById(R.id.switchReminders)
        tvTime = view.findViewById(R.id.tvReminderTime)
        val btnPickTime = view.findViewById<MaterialButton>(R.id.btnPickTime)

        // Данные
        val btnClear = view.findViewById<MaterialButton>(R.id.btnClearData)
        val btnExport = view.findViewById<MaterialButton>(R.id.btnExport)

        // О приложении
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

        btnClear.setOnClickListener { confirmClearData() }
        btnExport.setOnClickListener {
            // Переход к существующему экспорту PDF (мед. карта)
            findNavController().navigate(R.id.medicalCardFragment)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vm.themeMode.collect { mode ->
                        // Подпись на кнопке
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
                        switchReminders.isChecked = n.enabled
                        tvTime.text = String.format(Locale.getDefault(), "%02d:%02d", n.hour, n.minute)
                    }
                }
            }
        }

        switchReminders.setOnCheckedChangeListener { _, isChecked ->
            vm.setRemindersEnabled(isChecked)
            applyReminderScheduling(isChecked)
        }
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
        viewLifecycleOwner.lifecycleScope.launch {
            val ok = withContext(Dispatchers.IO) {
                runCatching {
                    val db = DbProvider.get(requireContext())
                    // В этой базе нет сгенерированного Room-метода clearAllTables(),
                    // поэтому чистим таблицы вручную в одной транзакции.
                    db.runInTransaction {
                        db.openHelper.writableDatabase.apply {
                            delete("blood_pressure", null, null)
                            delete("symptom", null, null)
                            delete("reminder", null, null)
                        }
                    }
                }.isSuccess
            }
            if (ok) {
                Toast.makeText(requireContext(), R.string.settings_clear_done, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), R.string.settings_clear_failed, Toast.LENGTH_LONG).show()
            }
        }
    }
}
