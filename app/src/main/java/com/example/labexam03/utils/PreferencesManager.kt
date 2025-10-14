package com.example.labexam03.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.labexam03.database.AppDatabase
import com.example.labexam03.models.Habit
import com.example.labexam03.models.MoodEntry
import com.example.labexam03.repository.HabitRepository
import com.example.labexam03.repository.MoodEntryRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Utility class for managing app data persistence using Room Database and SharedPreferences
 * Handles storage and retrieval of habits, mood entries, and app settings
 */
class PreferencesManager(context: Context) {
    
    private val prefs: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    // Room Database and Repositories
    private val database = AppDatabase.getDatabase(context)
    val habitRepository = HabitRepository(database.habitDao())
    val moodEntryRepository = MoodEntryRepository(database.moodEntryDao())
    
    companion object {
        private const val PREFS_NAME = "wellness_app_prefs"
        private const val KEY_HABITS = "habits"
        private const val KEY_MOOD_ENTRIES = "mood_entries"
        private const val KEY_REMINDER_ENABLED = "reminder_enabled"
        private const val KEY_REMINDER_INTERVAL = "reminder_interval"
        private const val KEY_LAST_REMINDER_TIME = "last_reminder_time"
        private const val KEY_MIGRATED_TO_ROOM = "migrated_to_room"
    }
    
    init {
        // Migrate data from SharedPreferences to Room if not already done
        migrateToRoom()
    }
    
    /**
     * Migrate existing data from SharedPreferences to Room Database
     */
    private fun migrateToRoom() {
        if (!prefs.getBoolean(KEY_MIGRATED_TO_ROOM, false)) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Migrate habits
                    val habitsJson = prefs.getString(KEY_HABITS, null)
                    if (!habitsJson.isNullOrEmpty()) {
                        val type = object : TypeToken<List<Habit>>() {}.type
                        val habits: List<Habit>? = gson.fromJson(habitsJson, type)
                        habits?.forEach { habit ->
                            habitRepository.insert(habit)
                        }
                    }
                    
                    // Migrate mood entries
                    val moodEntriesJson = prefs.getString(KEY_MOOD_ENTRIES, null)
                    if (!moodEntriesJson.isNullOrEmpty()) {
                        val type = object : TypeToken<List<MoodEntry>>() {}.type
                        val moodEntries: List<MoodEntry>? = gson.fromJson(moodEntriesJson, type)
                        moodEntries?.forEach { entry ->
                            moodEntryRepository.insert(entry)
                        }
                    }
                    
                    // Mark migration as complete
                    prefs.edit().putBoolean(KEY_MIGRATED_TO_ROOM, true).apply()
                    
                    // Clear old data from SharedPreferences (optional)
                    prefs.edit()
                        .remove(KEY_HABITS)
                        .remove(KEY_MOOD_ENTRIES)
                        .apply()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    /**
     * Save list of habits to Room Database (kept for backward compatibility)
     */
    @Deprecated("Use habitRepository.insert() instead", ReplaceWith("habitRepository.insert(habit)"))
    fun saveHabits(habits: List<Habit>) {
        // This method is kept for backward compatibility
        // New code should use habitRepository directly
        CoroutineScope(Dispatchers.IO).launch {
            habits.forEach { habit ->
                habitRepository.insert(habit)
            }
        }
    }
    
    /**
     * Retrieve list of habits from Room Database (kept for backward compatibility)
     */
    @Deprecated("Use habitRepository.getAllHabits() instead", ReplaceWith("habitRepository.getAllHabits()"))
    suspend fun getHabits(): MutableList<Habit> {
        // This method is kept for backward compatibility
        // New code should use habitRepository directly
        return habitRepository.getAllHabits().toMutableList()
    }
    
    /**
     * Save list of mood entries to Room Database (kept for backward compatibility)
     */
    @Deprecated("Use moodEntryRepository.insert() instead", ReplaceWith("moodEntryRepository.insert(moodEntry)"))
    fun saveMoodEntries(entries: List<MoodEntry>) {
        // This method is kept for backward compatibility
        // New code should use moodEntryRepository directly
        CoroutineScope(Dispatchers.IO).launch {
            entries.forEach { entry ->
                moodEntryRepository.insert(entry)
            }
        }
    }
    
    /**
     * Retrieve list of mood entries from Room Database (kept for backward compatibility)
     */
    @Deprecated("Use moodEntryRepository.getAllMoodEntries() instead", ReplaceWith("moodEntryRepository.getAllMoodEntries()"))
    suspend fun getMoodEntries(): MutableList<MoodEntry> {
        // This method is kept for backward compatibility
        // New code should use moodEntryRepository directly
        return moodEntryRepository.getAllMoodEntries().toMutableList()
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
        CoroutineScope(Dispatchers.IO).launch {
            database.habitDao().deleteAll()
            database.moodEntryDao().deleteAll()
        }
    }
}
