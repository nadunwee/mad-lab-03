package com.example.labexam03.repository

import com.example.labexam03.database.HabitDao
import com.example.labexam03.models.Habit
import kotlinx.coroutines.flow.Flow

/**
 * Repository for Habit data
 * Provides a clean API for data access to the rest of the application
 */
class HabitRepository(private val habitDao: HabitDao) {
    
    /**
     * Get all habits as Flow
     */
    val allHabits: Flow<List<Habit>> = habitDao.getAllHabitsFlow()
    
    /**
     * Insert a new habit
     */
    suspend fun insert(habit: Habit) {
        habitDao.insert(habit)
    }
    
    /**
     * Update an existing habit
     */
    suspend fun update(habit: Habit) {
        habitDao.update(habit)
    }
    
    /**
     * Delete a habit
     */
    suspend fun delete(habit: Habit) {
        habitDao.delete(habit)
    }
    
    /**
     * Get all habits
     */
    suspend fun getAllHabits(): List<Habit> {
        return habitDao.getAllHabits()
    }
    
    /**
     * Get a habit by ID
     */
    suspend fun getHabitById(habitId: String): Habit? {
        return habitDao.getHabitById(habitId)
    }
}
