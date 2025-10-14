package com.example.labexam03.database

import androidx.room.*
import com.example.labexam03.models.MoodEntry
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for MoodEntry entity
 * Provides database operations for mood entries
 */
@Dao
interface MoodEntryDao {
    
    /**
     * Insert a new mood entry
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(moodEntry: MoodEntry)
    
    /**
     * Update an existing mood entry
     */
    @Update
    suspend fun update(moodEntry: MoodEntry)
    
    /**
     * Delete a mood entry
     */
    @Delete
    suspend fun delete(moodEntry: MoodEntry)
    
    /**
     * Get all mood entries as Flow for reactive updates
     * Ordered by timestamp descending (newest first)
     */
    @Query("SELECT * FROM mood_entries ORDER BY timestamp DESC")
    fun getAllMoodEntriesFlow(): Flow<List<MoodEntry>>
    
    /**
     * Get all mood entries
     * Ordered by timestamp descending (newest first)
     */
    @Query("SELECT * FROM mood_entries ORDER BY timestamp DESC")
    suspend fun getAllMoodEntries(): List<MoodEntry>
    
    /**
     * Get a mood entry by ID
     */
    @Query("SELECT * FROM mood_entries WHERE id = :entryId")
    suspend fun getMoodEntryById(entryId: String): MoodEntry?
    
    /**
     * Delete all mood entries
     */
    @Query("DELETE FROM mood_entries")
    suspend fun deleteAll()
}
