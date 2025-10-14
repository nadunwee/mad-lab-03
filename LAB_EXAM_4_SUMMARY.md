# Lab Exam 4 - SQLite and Room ORM Integration

## ✅ Completed Implementation

This document summarizes the integration of SQLite database with Room ORM into the Personal Wellness Android App, fulfilling the requirements for Lab Exam 4.

## What Was Implemented

### 1. ✅ SQLite Database
- **Database Engine**: SQLite (Android's built-in database)
- **Database Name**: `wellness_database`
- **Tables**: 
  - `habits` - Stores daily habit tracking data
  - `mood_entries` - Stores mood journal entries
- **Location**: `/data/data/com.example.labexam03/databases/wellness_database`

### 2. ✅ Room ORM (Object-Relational Mapping)
- **Room Version**: 2.6.1
- **Components**:
  - Entities: `@Entity` annotated data classes
  - DAOs: `@Dao` interfaces with database operations
  - Database: `@Database` class managing the database
  - Repositories: Clean architecture pattern

## Technical Implementation

### Architecture Overview

```
┌─────────────────────────────────────────────┐
│           UI Layer (Fragments)              │
│  HabitTrackerFragment │ MoodJournalFragment │
└──────────────┬──────────────────────────────┘
               │
┌──────────────▼──────────────────────────────┐
│         PreferencesManager                  │
│  (Provides access to repositories)          │
└──────────────┬──────────────────────────────┘
               │
┌──────────────▼──────────────────────────────┐
│      Repository Layer                       │
│  HabitRepository │ MoodEntryRepository      │
└──────────────┬──────────────────────────────┘
               │
┌──────────────▼──────────────────────────────┐
│           DAO Layer                         │
│      HabitDao │ MoodEntryDao                │
└──────────────┬──────────────────────────────┘
               │
┌──────────────▼──────────────────────────────┐
│         Room Database                       │
│          AppDatabase                        │
└──────────────┬──────────────────────────────┘
               │
┌──────────────▼──────────────────────────────┐
│           SQLite                            │
│      (wellness_database)                    │
└─────────────────────────────────────────────┘
```

### Database Schema

#### habits Table
```sql
CREATE TABLE habits (
    id TEXT PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    targetCount INTEGER NOT NULL,
    currentCount INTEGER NOT NULL,
    lastUpdated TEXT NOT NULL
);
```

#### mood_entries Table
```sql
CREATE TABLE mood_entries (
    id TEXT PRIMARY KEY NOT NULL,
    emoji TEXT NOT NULL,
    note TEXT NOT NULL,
    timestamp INTEGER NOT NULL,
    dateString TEXT NOT NULL,
    timeString TEXT NOT NULL
);
```

## Key Features

### 1. Type-Safe Database Operations
```kotlin
// Room provides compile-time verification
@Query("SELECT * FROM habits WHERE id = :habitId")
suspend fun getHabitById(habitId: String): Habit?
```

### 2. Reactive Data Updates with Flow
```kotlin
@Query("SELECT * FROM habits ORDER BY name ASC")
fun getAllHabitsFlow(): Flow<List<Habit>>

// Auto-updates UI when data changes
lifecycleScope.launch {
    habitRepository.allHabits.collect { habits ->
        habitAdapter.updateHabits(habits)
    }
}
```

### 3. Async Operations with Coroutines
```kotlin
// All database operations are async
lifecycleScope.launch {
    val habit = Habit(name = "Exercise", targetCount = 5)
    habitRepository.insert(habit)
}
```

### 4. Automatic Migration
```kotlin
// Migrates existing SharedPreferences data to Room
private fun migrateToRoom() {
    if (!prefs.getBoolean(KEY_MIGRATED_TO_ROOM, false)) {
        // Read from SharedPreferences
        // Insert into Room
        // Mark as migrated
    }
}
```

## Files Created/Modified

### New Files (9 files)

**Database Layer:**
1. `app/src/main/java/com/example/labexam03/database/AppDatabase.kt`
   - Room database instance with singleton pattern
   - Manages database lifecycle

2. `app/src/main/java/com/example/labexam03/database/HabitDao.kt`
   - Data Access Object for Habit operations
   - Defines CRUD operations with SQL queries

3. `app/src/main/java/com/example/labexam03/database/MoodEntryDao.kt`
   - Data Access Object for MoodEntry operations
   - Defines CRUD operations with SQL queries

**Repository Layer:**
4. `app/src/main/java/com/example/labexam03/repository/HabitRepository.kt`
   - Abstracts data access for habits
   - Provides clean API to fragments

5. `app/src/main/java/com/example/labexam03/repository/MoodEntryRepository.kt`
   - Abstracts data access for mood entries
   - Provides clean API to fragments

**Documentation:**
6. `ROOM_INTEGRATION.md`
   - Comprehensive guide to Room integration
   - Usage examples and best practices

### Modified Files (8 files)

**Build Configuration:**
1. `gradle/libs.versions.toml`
   - Added Room dependencies (runtime, ktx, compiler)
   - Added KSP plugin for annotation processing

2. `app/build.gradle.kts`
   - Added Room dependencies
   - Added KSP plugin

**Data Models:**
3. `app/src/main/java/com/example/labexam03/models/Habit.kt`
   - Added `@Entity` annotation for Room
   - Added `@PrimaryKey` annotation
   - Specified table name

4. `app/src/main/java/com/example/labexam03/models/MoodEntry.kt`
   - Added `@Entity` annotation for Room
   - Added `@PrimaryKey` annotation
   - Specified table name

**Data Management:**
5. `app/src/main/java/com/example/labexam03/utils/PreferencesManager.kt`
   - Integrated Room database
   - Added repository instances
   - Implemented automatic migration
   - Kept SharedPreferences for settings

**UI Layer:**
6. `app/src/main/java/com/example/labexam03/fragments/HabitTrackerFragment.kt`
   - Updated to use Room repository
   - All operations now use coroutines
   - Uses `lifecycleScope.launch` for async operations

7. `app/src/main/java/com/example/labexam03/fragments/MoodJournalFragment.kt`
   - Updated to use Room repository
   - All operations now use coroutines
   - Uses `lifecycleScope.launch` for async operations

**Documentation:**
8. `README.md`, `SUMMARY.md`, `IMPLEMENTATION.md`
   - Updated to reflect Room database usage
   - Added database architecture documentation

## Key Improvements Over SharedPreferences

### Before (SharedPreferences)
```kotlin
// Manual JSON serialization
fun saveHabits(habits: List<Habit>) {
    val json = gson.toJson(habits)
    prefs.edit().putString(KEY_HABITS, json).apply()
}

// Manual JSON deserialization
fun getHabits(): MutableList<Habit> {
    val json = prefs.getString(KEY_HABITS, null) ?: return mutableListOf()
    val type = object : TypeToken<MutableList<Habit>>() {}.type
    return gson.fromJson(json, type) ?: mutableListOf()
}
```

### After (Room Database)
```kotlin
// Type-safe, automatic object mapping
@Query("SELECT * FROM habits ORDER BY name ASC")
suspend fun getAllHabits(): List<Habit>

@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun insert(habit: Habit)
```

### Benefits
1. **Type Safety**: Compile-time SQL verification
2. **No Boilerplate**: No manual JSON serialization
3. **Better Performance**: Optimized queries, connection pooling
4. **Complex Queries**: Support for JOIN, WHERE, ORDER BY, etc.
5. **Reactive Updates**: Flow-based reactive programming
6. **Migration Support**: Built-in database versioning

## Testing the Implementation

### How to Verify

1. **Install the App**:
   ```bash
   ./gradlew installDebug
   ```

2. **Add Data**:
   - Open the app
   - Add several habits in the Habits tab
   - Log moods in the Mood tab

3. **Test Persistence**:
   - Force stop the app
   - Reopen the app
   - Verify all data is still there

4. **Test Migration** (if upgrading from old version):
   - Install old version
   - Add data
   - Install new version with Room
   - Data should automatically migrate

### Database Inspection

Use Android Studio's Database Inspector:
1. Run app on emulator/device
2. View → Tool Windows → App Inspection
3. Select "Database Inspector" tab
4. Browse tables and data

Or use ADB:
```bash
adb shell
run-as com.example.labexam03
cd databases
ls -l wellness_database
```

## Code Examples

### Adding a Habit
```kotlin
lifecycleScope.launch {
    val habit = Habit(
        name = "Morning Run",
        targetCount = 7
    )
    prefsManager.habitRepository.insert(habit)
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
        // UI automatically updates when data changes
        habitAdapter.updateHabits(habits)
    }
}
```

### Updating a Habit
```kotlin
lifecycleScope.launch {
    val updatedHabit = habit.copy(currentCount = habit.currentCount + 1)
    prefsManager.habitRepository.update(updatedHabit)
}
```

### Deleting a Habit
```kotlin
lifecycleScope.launch {
    prefsManager.habitRepository.delete(habit)
}
```

## Dependencies Added

```kotlin
// Room Database
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
ksp("androidx.room:room-compiler:2.6.1")

// KSP Plugin
id("com.google.devtools.ksp") version "2.0.21-1.0.25"
```

## Performance Characteristics

- **Insert**: O(1) with prepared statements
- **Query**: O(n) with indexes on primary keys
- **Update**: O(1) for single record by ID
- **Delete**: O(1) for single record by ID
- **Migration**: One-time O(n) for all existing data

## Future Possibilities

With Room in place, the following enhancements become easier:

1. **Full-Text Search**: Search notes with SQL FTS
2. **Complex Queries**: Filter habits by completion rate
3. **Relationships**: Link habits to categories
4. **Statistics**: Generate reports with SQL aggregations
5. **Backup/Restore**: Export database to file
6. **Cloud Sync**: Sync with backend using repository pattern

## Conclusion

✅ **Lab Exam 4 Requirements Met:**

1. ✅ **SQLite Integration**: Using Android's SQLite through Room
2. ✅ **ORM (Room)**: Complete Room implementation with:
   - Entity classes with `@Entity` annotation
   - DAO interfaces with `@Dao` annotation
   - Database class with `@Database` annotation
   - Repository pattern for clean architecture
   - Coroutines for async operations
   - Flow for reactive updates

The implementation follows Android best practices and provides a solid foundation for future enhancements. All existing functionality is preserved while gaining the benefits of a proper database solution.

## Additional Resources

- **ROOM_INTEGRATION.md**: Detailed integration guide
- **README.md**: Updated project overview
- **SUMMARY.md**: Complete implementation summary
- **IMPLEMENTATION.md**: Technical implementation details
