# Room Database Integration Guide

## Overview

This document describes the integration of **SQLite** database with **Room ORM (Object-Relational Mapping)** into the Personal Wellness Android App. This upgrade replaces the previous SharedPreferences-based data persistence with a robust, scalable database solution.

## What Was Added

### 1. Room Database Dependencies

Added to `gradle/libs.versions.toml`:
```toml
room = "2.6.1"
ksp = "2.0.21-1.0.25"
```

Added to `app/build.gradle.kts`:
```kotlin
// Room dependencies
implementation(libs.androidx.room.runtime)
implementation(libs.androidx.room.ktx)
ksp(libs.androidx.room.compiler)
```

### 2. Database Architecture

#### Entity Classes

**Habit.kt** and **MoodEntry.kt** were converted to Room Entities:

```kotlin
@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val targetCount: Int,
    var currentCount: Int = 0,
    var lastUpdated: String = ""
)

@Entity(tableName = "mood_entries")
data class MoodEntry(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val emoji: String,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String,
    val timeString: String
)
```

#### DAO (Data Access Object) Interfaces

Created two DAO interfaces for database operations:

**HabitDao.kt**:
- `insert(habit: Habit)` - Insert new habit
- `update(habit: Habit)` - Update existing habit
- `delete(habit: Habit)` - Delete habit
- `getAllHabits()` - Get all habits
- `getAllHabitsFlow()` - Get habits as Flow for reactive updates
- `getHabitById(habitId: String)` - Get specific habit

**MoodEntryDao.kt**:
- `insert(moodEntry: MoodEntry)` - Insert new mood entry
- `update(moodEntry: MoodEntry)` - Update existing mood entry
- `delete(moodEntry: MoodEntry)` - Delete mood entry
- `getAllMoodEntries()` - Get all mood entries (ordered by timestamp DESC)
- `getAllMoodEntriesFlow()` - Get mood entries as Flow
- `getMoodEntryById(entryId: String)` - Get specific mood entry

#### Room Database Class

**AppDatabase.kt**:
```kotlin
@Database(
    entities = [Habit::class, MoodEntry::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun moodEntryDao(): MoodEntryDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wellness_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

#### Repository Layer

Created repository classes to abstract data access:

**HabitRepository.kt**:
- Provides clean API for habit data operations
- Exposes Flow for reactive UI updates
- Handles all database interactions for habits

**MoodEntryRepository.kt**:
- Provides clean API for mood entry operations
- Exposes Flow for reactive UI updates
- Handles all database interactions for mood entries

### 3. Updated Components

#### PreferencesManager.kt

Enhanced to:
- Initialize Room database and repositories
- Migrate existing SharedPreferences data to Room on first run
- Provide repository instances to fragments
- Keep settings-related methods using SharedPreferences (reminder settings)

Key features:
```kotlin
// Room Database and Repositories
private val database = AppDatabase.getDatabase(context)
val habitRepository = HabitRepository(database.habitDao())
val moodEntryRepository = MoodEntryRepository(database.moodEntryDao())

// Automatic migration from SharedPreferences to Room
private fun migrateToRoom() { ... }
```

#### HabitTrackerFragment.kt

Updated to use Room with coroutines:
- All database operations now use `lifecycleScope.launch { ... }`
- Replaced `prefsManager.saveHabits()` with `prefsManager.habitRepository.insert/update/delete()`
- Uses suspend functions for async database operations

Example:
```kotlin
private fun loadHabits() {
    lifecycleScope.launch {
        habits.clear()
        habits.addAll(prefsManager.habitRepository.getAllHabits())
        habitAdapter.updateHabits(habits)
        updateEmptyState()
    }
}
```

#### MoodJournalFragment.kt

Updated to use Room with coroutines:
- All database operations now use `lifecycleScope.launch { ... }`
- Replaced `prefsManager.saveMoodEntries()` with `prefsManager.moodEntryRepository.insert/update/delete()`
- Uses suspend functions for async database operations

## Benefits of Room ORM

### 1. Type Safety
- Compile-time verification of SQL queries
- No runtime errors from typos in SQL
- Auto-completion in IDE

### 2. Less Boilerplate
- No need to write SQLiteOpenHelper
- No manual cursor management
- Automatic object mapping

### 3. Better Performance
- Efficient query optimization
- Connection pooling
- Prepared statements

### 4. LiveData/Flow Support
- Reactive data updates
- Automatic UI refresh when data changes
- No manual observer management needed

### 5. Migration Support
- Built-in migration framework
- Version control for database schema
- Easier to maintain over time

### 6. Complex Queries
- Support for JOIN operations
- Complex WHERE clauses
- Aggregation functions
- Unlike SharedPreferences which only stores key-value pairs

## Database Schema

### habits Table
| Column | Type | Constraints |
|--------|------|-------------|
| id | TEXT | PRIMARY KEY |
| name | TEXT | NOT NULL |
| targetCount | INTEGER | NOT NULL |
| currentCount | INTEGER | NOT NULL |
| lastUpdated | TEXT | NOT NULL |

### mood_entries Table
| Column | Type | Constraints |
|--------|------|-------------|
| id | TEXT | PRIMARY KEY |
| emoji | TEXT | NOT NULL |
| note | TEXT | NOT NULL |
| timestamp | INTEGER | NOT NULL |
| dateString | TEXT | NOT NULL |
| timeString | TEXT | NOT NULL |

## Data Migration

The app automatically migrates existing data from SharedPreferences to Room:

1. On first launch with Room enabled, `PreferencesManager` checks migration status
2. If not migrated, reads habits and mood entries from SharedPreferences
3. Inserts each item into Room database
4. Marks migration as complete
5. Clears old data from SharedPreferences (optional)

This ensures users don't lose their existing data when upgrading to the new version.

## Usage Examples

### Adding a Habit
```kotlin
lifecycleScope.launch {
    val habit = Habit(
        name = "Exercise",
        targetCount = 5
    )
    prefsManager.habitRepository.insert(habit)
}
```

### Updating a Habit
```kotlin
lifecycleScope.launch {
    val updatedHabit = habit.copy(currentCount = habit.currentCount + 1)
    prefsManager.habitRepository.update(updatedHabit)
}
```

### Querying All Habits
```kotlin
lifecycleScope.launch {
    val habits = prefsManager.habitRepository.getAllHabits()
    // Use habits
}
```

### Observing Changes with Flow
```kotlin
lifecycleScope.launch {
    prefsManager.habitRepository.allHabits.collect { habits ->
        // UI updates automatically when data changes
        habitAdapter.updateHabits(habits)
    }
}
```

## Testing

To test the Room implementation:

1. **Fresh Install**: Install app on clean device/emulator
2. **Add Data**: Create habits and mood entries
3. **Close App**: Force stop the app
4. **Reopen**: Launch app again - data should persist
5. **Migration Test**: Install old version, add data, upgrade to new version - data should be preserved

## Files Added/Modified

### New Files
- `app/src/main/java/com/example/labexam03/database/AppDatabase.kt`
- `app/src/main/java/com/example/labexam03/database/HabitDao.kt`
- `app/src/main/java/com/example/labexam03/database/MoodEntryDao.kt`
- `app/src/main/java/com/example/labexam03/repository/HabitRepository.kt`
- `app/src/main/java/com/example/labexam03/repository/MoodEntryRepository.kt`

### Modified Files
- `gradle/libs.versions.toml` - Added Room and KSP dependencies
- `app/build.gradle.kts` - Added Room dependencies and KSP plugin
- `app/src/main/java/com/example/labexam03/models/Habit.kt` - Added Room annotations
- `app/src/main/java/com/example/labexam03/models/MoodEntry.kt` - Added Room annotations
- `app/src/main/java/com/example/labexam03/utils/PreferencesManager.kt` - Integrated Room database
- `app/src/main/java/com/example/labexam03/fragments/HabitTrackerFragment.kt` - Updated to use Room
- `app/src/main/java/com/example/labexam03/fragments/MoodJournalFragment.kt` - Updated to use Room

## Technical Details

### Coroutines Integration
All database operations are performed asynchronously using Kotlin coroutines:
- `suspend` functions for single operations
- `Flow` for continuous data streams
- `lifecycleScope` for lifecycle-aware coroutines

### Thread Safety
- Room automatically handles threading
- Main thread queries are not allowed (compile-time check)
- All queries run on background thread pool

### Database Location
The database file is stored at:
```
/data/data/com.example.labexam03/databases/wellness_database
```

## Future Enhancements

With Room in place, future enhancements are easier:

1. **Backup/Restore**: Export database to file
2. **Cloud Sync**: Sync with Firebase/backend server
3. **Complex Queries**: Search, filter, sort with SQL
4. **Relationships**: Add one-to-many relationships
5. **Full-Text Search**: Search notes and habit names
6. **Statistics**: Generate reports with SQL aggregations

## Conclusion

The integration of Room ORM provides a robust, scalable foundation for data persistence in the Personal Wellness App. The implementation follows Android best practices and sets the stage for future enhancements while maintaining backward compatibility with existing data.
