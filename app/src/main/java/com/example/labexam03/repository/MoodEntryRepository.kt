package com.example.labexam03.repository

import com.example.labexam03.database.MoodEntryDao
import com.example.labexam03.models.MoodEntry
import kotlinx.coroutines.flow.Flow

/**
 * Repository for MoodEntry data
 * Provides a clean API for data access to the rest of the application
 */
class MoodEntryRepository(private val moodEntryDao: MoodEntryDao) {
    
    /**
     * Get all mood entries as Flow
     */
    val allMoodEntries: Flow<List<MoodEntry>> = moodEntryDao.getAllMoodEntriesFlow()
    
    /**
     * Insert a new mood entry
     */
    suspend fun insert(moodEntry: MoodEntry) {
        moodEntryDao.insert(moodEntry)
    }
    
    /**
     * Update an existing mood entry
     */
    suspend fun update(moodEntry: MoodEntry) {
        moodEntryDao.update(moodEntry)
    }
    
    /**
     * Delete a mood entry
     */
    suspend fun delete(moodEntry: MoodEntry) {
        moodEntryDao.delete(moodEntry)
    }
    
    /**
     * Get all mood entries
     */
    suspend fun getAllMoodEntries(): List<MoodEntry> {
        return moodEntryDao.getAllMoodEntries()
    }
    
    /**
     * Get a mood entry by ID
     */
    suspend fun getMoodEntryById(entryId: String): MoodEntry? {
        return moodEntryDao.getMoodEntryById(entryId)
    }
}
