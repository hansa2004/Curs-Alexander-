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
    private lateinit var toolbar: MaterialToolbar

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

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Важно: стартовый граф задаём только при ПЕРВОМ запуске.
        // При recreate() (например, после смены шрифта) Navigation сам восстановит back stack.
        if (savedInstanceState == null) {
            val prefs = Prefs(this)
            val graph = navController.navInflater.inflate(R.navigation.nav_graph)
            val startDest = if (prefs.onboardingCompleted) R.id.homeFragment else R.id.onboardingFragment
            graph.setStartDestination(startDest)
            navController.graph = graph
        }

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Показываем заголовок в верхней полосе и ставим название приложения.
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title = getString(R.string.app_name)
        toolbar.title = getString(R.string.app_name)

        setupActionBarWithNavController(navController)

        // Тулбар-меню пока не используем (шестерёнка в Home).
        toolbar.menu.clear()

        // Держим заголовок стабильным при переходах.
        navController.addOnDestinationChangedListener { _, _, _ ->
            supportActionBar?.title = getString(R.string.app_name)
            toolbar.title = getString(R.string.app_name)
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