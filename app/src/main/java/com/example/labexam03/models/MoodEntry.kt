package com.example.labexam03.models

import java.io.Serializable

/**
 * Data model representing a mood journal entry
 * @param id Unique identifier for the entry
 * @param emoji Selected emoji representing the mood
 * @param note Optional note about the mood
 * @param timestamp Timestamp when mood was logged (milliseconds)
 * @param dateString Formatted date string (YYYY-MM-DD)
 * @param timeString Formatted time string (HH:mm)
 */
data class MoodEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val emoji: String,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String,
    val timeString: String
) : Serializable {
    
    /**
     * Get mood value for charting (1-5 scale)
     * Maps emojis to numeric values for trend analysis
     */
    fun getMoodValue(): Float {
        return when (emoji) {
            "😢" -> 1f  // Very sad
            "😞" -> 2f  // Sad
            "😐" -> 3f  // Neutral
            "😊" -> 4f  // Happy
            "😄" -> 5f  // Very happy
            else -> 3f  // Default to neutral
        }
    }
}
