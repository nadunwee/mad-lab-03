package com.example.labexam03.fragments

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.labexam03.R
import com.example.labexam03.receivers.HydrationReminderReceiver
import com.example.labexam03.utils.PreferencesManager

/**
 * Fragment for app settings
 * Manages hydration reminder configuration with AlarmManager
 */
class SettingsFragment : Fragment() {
    
    private lateinit var prefsManager: PreferencesManager
    private lateinit var reminderSwitch: Switch
    private lateinit var intervalSeekBar: SeekBar
    private lateinit var intervalText: TextView
    private lateinit var saveButton: Button
    private lateinit var testNotificationButton: Button
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize preferences manager
        prefsManager = PreferencesManager(requireContext())
        
        // Initialize views
        reminderSwitch = view.findViewById(R.id.reminderSwitch)
        intervalSeekBar = view.findViewById(R.id.intervalSeekBar)
        intervalText = view.findViewById(R.id.intervalText)
        saveButton = view.findViewById(R.id.saveSettingsButton)
        testNotificationButton = view.findViewById(R.id.testNotificationButton)
        
        // Load current settings
        loadSettings()
        
        // Set up listeners
        setupListeners()
    }
    
    /**
     * Load current settings from SharedPreferences
     */
    private fun loadSettings() {
        reminderSwitch.isChecked = prefsManager.isReminderEnabled()
        
        val interval = prefsManager.getReminderInterval()
        intervalSeekBar.progress = interval / 15  // Convert minutes to seekbar position (15 min increments)
        updateIntervalText(interval)
    }
    
    /**
     * Set up UI listeners
     */
    private fun setupListeners() {
        // SeekBar for interval selection
        intervalSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val minutes = (progress + 1) * 15  // Min 15 minutes, increment by 15
                updateIntervalText(minutes)
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Save button
        saveButton.setOnClickListener {
            saveSettings()
        }
        
        // Test notification button
        testNotificationButton.setOnClickListener {
            sendTestNotification()
        }
    }
    
    /**
     * Update interval text display
     */
    private fun updateIntervalText(minutes: Int) {
        val hours = minutes / 60
        val mins = minutes % 60
        
        intervalText.text = when {
            hours > 0 && mins > 0 -> "$hours hour${if (hours > 1) "s" else ""} $mins min"
            hours > 0 -> "$hours hour${if (hours > 1) "s" else ""}"
            else -> "$mins minutes"
        }
    }
    
    /**
     * Save settings to SharedPreferences and configure AlarmManager
     */
    private fun saveSettings() {
        val enabled = reminderSwitch.isChecked
        val interval = (intervalSeekBar.progress + 1) * 15
        
        // Save to preferences
        prefsManager.setReminderEnabled(enabled)
        prefsManager.setReminderInterval(interval)
        
        if (enabled) {
            scheduleHydrationReminders(interval)
            Toast.makeText(requireContext(), "Reminders enabled!", Toast.LENGTH_SHORT).show()
        } else {
            cancelHydrationReminders()
            Toast.makeText(requireContext(), "Reminders disabled", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Schedule repeating hydration reminders using AlarmManager
     */
    private fun scheduleHydrationReminders(intervalMinutes: Int) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), HydrationReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Calculate interval in milliseconds
        val intervalMillis = intervalMinutes * 60 * 1000L
        
        // Schedule repeating alarm
        // Use setRepeating for regular intervals
        val triggerTime = System.currentTimeMillis() + intervalMillis
        
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            intervalMillis,
            pendingIntent
        )
    }
    
    /**
     * Cancel all scheduled hydration reminders
     */
    private fun cancelHydrationReminders() {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), HydrationReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
    }
    
    /**
     * Send a test notification immediately
     */
    private fun sendTestNotification() {
        val intent = Intent(requireContext(), HydrationReminderReceiver::class.java)
        requireContext().sendBroadcast(intent)
        Toast.makeText(requireContext(), "Test notification sent!", Toast.LENGTH_SHORT).show()
    }
}
