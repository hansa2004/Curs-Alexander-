package com.example.curs_alexander

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.NavController
import com.example.curs_alexander.data.Prefs

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val prefs = Prefs(this)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Программно выбираем стартовый экран
        val navInflater = navController.navInflater
        val graph = navInflater.inflate(R.navigation.nav_graph)
        val startDest = if (prefs.onboardingCompleted) R.id.homeFragment else R.id.onboardingFragment
        graph.setStartDestination(startDest)
        navController.graph = graph
    }
}