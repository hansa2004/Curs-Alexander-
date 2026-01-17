package com.example.curs_alexander

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.curs_alexander.data.Prefs
import com.example.curs_alexander.settings.SettingsApplier
import com.example.curs_alexander.settings.SettingsCache
import com.example.curs_alexander.settings.SettingsRepository
import com.example.curs_alexander.settings.ThemeMode
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    override fun attachBaseContext(newBase: android.content.Context) {
        // Применяем размер шрифта на уровне ресурсов, чтобы влиял на TextView во всём XML UI.
        // Стараемся не блокировать старт: сначала читаем кэш, иначе — один быстрый read из DataStore.
        val repo = SettingsRepository(newBase)
        val scale = SettingsCache.getFontScale(newBase) ?: runCatching {
            kotlinx.coroutines.runBlocking { repo.fontScale.first() }
        }.getOrDefault(com.example.curs_alexander.settings.FontScale.MEDIUM)

        val factor = SettingsApplier.fontScaleFactor(scale)
        val config = Configuration(newBase.resources.configuration)
        config.fontScale = factor
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Тема должна быть применена ДО super.onCreate() (иначе возможен "миг")
        applySavedThemeBlocking()
        super.onCreate(savedInstanceState)

        // После старта тихо синхронизируем кэш с DataStore (важно для первого запуска, когда кэш ещё пуст).
        warmUpSettingsCacheAsync()

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val prefs = Prefs(this)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val graph = navController.navInflater.inflate(R.navigation.nav_graph)
        val startDest = if (prefs.onboardingCompleted) R.id.homeFragment else R.id.onboardingFragment
        graph.setStartDestination(startDest)
        navController.graph = graph

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Меню тулбара (шестерёнка справа)
        toolbar.menu.clear()
        toolbar.inflateMenu(R.menu.menu_main)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_settings -> {
                    if (navController.currentDestination?.id != R.id.settingsFragment) {
                        navController.navigate(R.id.settingsFragment)
                    }
                    true
                }
                else -> false
            }
        }

        setupActionBarWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            supportActionBar?.title = when (destination.id) {
                R.id.onboardingFragment -> getString(R.string.onboarding_title)
                R.id.profileFragment -> getString(R.string.profile_title)
                R.id.homeFragment -> getString(R.string.app_name)
                R.id.healthMeasurementsFragment -> getString(R.string.home_section_measurements)
                R.id.healthMeasurementsChartFragment -> getString(R.string.measure_open_chart)
                R.id.symptomsListFragment -> getString(R.string.home_section_symptoms)
                R.id.symptomAddFragment -> getString(R.string.home_action_add_symptom)
                R.id.analysisFragment -> "Анализ"
                R.id.remindersFragment -> getString(R.string.home_section_reminders)
                R.id.analyticsFragment -> getString(R.string.analytics_title)
                R.id.medicalCardFragment -> getString(R.string.medical_card_title)
                R.id.settingsFragment -> getString(R.string.settings_title)
                else -> getString(R.string.app_name)
            }

            // Диагностика: если вдруг шестерёнка не появляется, пробуем ещё раз на всякий случай
            toolbar.menu.findItem(R.id.action_settings)?.isVisible = true
        }

        handleNavigationFromIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNavigationFromIntent(intent)
    }

    private fun handleNavigationFromIntent(intent: Intent?) {
        val dest = intent?.getIntExtra(
            com.example.curs_alexander.notifications.ReminderReceiver.EXTRA_DESTINATION_ID,
            -1
        ) ?: -1
        if (dest != -1) {
            navController.navigate(dest)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun applySavedThemeBlocking() {
        val repo = SettingsRepository(this)
        val mode = SettingsCache.getThemeMode(this) ?: runCatching {
            kotlinx.coroutines.runBlocking { repo.themeMode.first() }
        }.getOrDefault(ThemeMode.SYSTEM)

        SettingsApplier.applyTheme(mode)
    }

    private fun warmUpSettingsCacheAsync() {
        // Не блокируем UI: просто гарантируем, что после первого чтения DataStore кэш будет заполнен.
        val needTheme = SettingsCache.getThemeMode(this) == null
        val needFont = SettingsCache.getFontScale(this) == null
        if (!needTheme && !needFont) return

        val repo = SettingsRepository(this)
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            runCatching {
                if (needTheme) {
                    val mode = repo.themeMode.first()
                    SettingsCache.setThemeMode(this@MainActivity, mode)
                }
                if (needFont) {
                    val scale = repo.fontScale.first()
                    SettingsCache.setFontScale(this@MainActivity, scale)
                }
            }
        }
    }
}