package com.example.labexam03.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.labexam03.models.Habit
import com.example.labexam03.models.MoodEntry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Utility class for managing app data persistence using SharedPreferences
 * Handles storage and retrieval of habits, mood entries, and app settings
 */
class PreferencesManager(context: Context) {
    
    private val prefs: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val PREFS_NAME = "wellness_app_prefs"
        private const val KEY_HABITS = "habits"
        private const val KEY_MOOD_ENTRIES = "mood_entries"
        private const val KEY_REMINDER_ENABLED = "reminder_enabled"
        private const val KEY_REMINDER_INTERVAL = "reminder_interval"
        private const val KEY_LAST_REMINDER_TIME = "last_reminder_time"
    }
    
    /**
     * Save list of habits to SharedPreferences
     */
    fun saveHabits(habits: List<Habit>) {
        val json = gson.toJson(habits)
        prefs.edit().putString(KEY_HABITS, json).apply()
    }
    
    /**
     * Retrieve list of habits from SharedPreferences
     */
    fun getHabits(): MutableList<Habit> {
        val json = prefs.getString(KEY_HABITS, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<Habit>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }
    
    /**
     * Save list of mood entries to SharedPreferences
     */
    fun saveMoodEntries(entries: List<MoodEntry>) {
        val json = gson.toJson(entries)
        prefs.edit().putString(KEY_MOOD_ENTRIES, json).apply()
    }
    
    /**
     * Retrieve list of mood entries from SharedPreferences
     */
    fun getMoodEntries(): MutableList<MoodEntry> {
        val json = prefs.getString(KEY_MOOD_ENTRIES, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<MoodEntry>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }
    
    /**
     * Save hydration reminder settings
     */
    fun setReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REMINDER_ENABLED, enabled).apply()
    }
    
    /**
     * Check if hydration reminder is enabled
     */
    fun isReminderEnabled(): Boolean {
        return prefs.getBoolean(KEY_REMINDER_ENABLED, false)
    }
    
    /**
     * Save reminder interval in minutes
     */
    fun setReminderInterval(intervalMinutes: Int) {
        prefs.edit().putInt(KEY_REMINDER_INTERVAL, intervalMinutes).apply()
    }
    
    /**
     * Get reminder interval in minutes (default: 60)
     */
    fun getReminderInterval(): Int {
        return prefs.getInt(KEY_REMINDER_INTERVAL, 60)
    }
    
    /**
     * Save last reminder time
     */
    fun setLastReminderTime(time: Long) {
        prefs.edit().putLong(KEY_LAST_REMINDER_TIME, time).apply()
    }
    
    /**
     * Get last reminder time
     */
    fun getLastReminderTime(): Long {
        return prefs.getLong(KEY_LAST_REMINDER_TIME, 0)
    }
    
    /**
     * Clear all stored data (for testing/debugging)
     */
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
