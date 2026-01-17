package com.example.curs_alexander

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.NavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.curs_alexander.data.Prefs
import com.google.android.material.appbar.MaterialToolbar

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Важно для edge-to-edge: добавляем отступы под system bars,
        // чтобы контент не "уезжал" под status bar.
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
        setupActionBarWithNavController(navController)

        // Обновляем заголовок при смене фрагментов
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
                else -> getString(R.string.app_name)
            }
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
}