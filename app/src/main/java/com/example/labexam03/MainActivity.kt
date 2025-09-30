package com.example.labexam03

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.labexam03.fragments.HabitTrackerFragment
import com.example.labexam03.fragments.MoodJournalFragment
import com.example.labexam03.fragments.SettingsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Main Activity that hosts the wellness app
 * Uses BottomNavigationView to switch between fragments
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var bottomNav: BottomNavigationView
    
    companion object {
        private const val NOTIFICATION_PERMISSION_CODE = 100
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Request notification permission for Android 13+
        requestNotificationPermission()
        
        // Initialize bottom navigation
        bottomNav = findViewById(R.id.bottomNavigation)
        
        // Set up bottom navigation listener
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_habits -> {
                    loadFragment(HabitTrackerFragment())
                    true
                }
                R.id.nav_mood -> {
                    loadFragment(MoodJournalFragment())
                    true
                }
                R.id.nav_settings -> {
                    loadFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }
        
        // Load initial fragment (Habits)
        if (savedInstanceState == null) {
            loadFragment(HabitTrackerFragment())
            bottomNav.selectedItemId = R.id.nav_habits
        }
    }
    
    /**
     * Load a fragment into the container
     */
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
    
    /**
     * Request notification permission for Android 13 and above
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_CODE
                )
            }
        }
    }
}
