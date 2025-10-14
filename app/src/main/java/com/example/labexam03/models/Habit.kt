package com.example.labexam03.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * Data model representing a daily habit
 * @param id Unique identifier for the habit
 * @param name Name of the habit (e.g., "Drink Water", "Meditate")
 * @param targetCount Daily target count for the habit
 * @param currentCount Current progress count
 * @param lastUpdated Last update date (YYYY-MM-DD format)
 */
@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val targetCount: Int,
    var currentCount: Int = 0,
    var lastUpdated: String = ""
) : Serializable {
    
    /**
     * Calculate completion percentage
     * @return Percentage of habit completion (0-100)
     */
    fun getCompletionPercentage(): Int {
        return if (targetCount > 0) {
            ((currentCount.toFloat() / targetCount.toFloat()) * 100).toInt().coerceIn(0, 100)
        } else 0
    }
    
    /**
     * Check if habit is completed for the day
     * @return True if current count meets or exceeds target
     */
    fun isCompleted(): Boolean {
        return currentCount >= targetCount
    }
}
