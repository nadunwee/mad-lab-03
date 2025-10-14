package com.example.labexam03.database

import androidx.room.*
import com.example.labexam03.models.Habit
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Habit entity
 * Provides database operations for habits
 */
@Dao
interface HabitDao {
    
    /**
     * Insert a new habit
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(habit: Habit)
    
    /**
     * Update an existing habit
     */
    @Update
    suspend fun update(habit: Habit)
    
    /**
     * Delete a habit
     */
    @Delete
    suspend fun delete(habit: Habit)
    
    /**
     * Get all habits as Flow for reactive updates
     */
    @Query("SELECT * FROM habits ORDER BY name ASC")
    fun getAllHabitsFlow(): Flow<List<Habit>>
    
    /**
     * Get all habits
     */
    @Query("SELECT * FROM habits ORDER BY name ASC")
    suspend fun getAllHabits(): List<Habit>
    
    /**
     * Get a habit by ID
     */
    @Query("SELECT * FROM habits WHERE id = :habitId")
    suspend fun getHabitById(habitId: String): Habit?
    
    /**
     * Delete all habits
     */
    @Query("DELETE FROM habits")
    suspend fun deleteAll()
}
