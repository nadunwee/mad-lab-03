# Quick Reference: Room Database Implementation

## Files Added

### Database Layer (3 files)
```
app/src/main/java/com/example/labexam03/database/
├── AppDatabase.kt         # Room database instance (singleton)
├── HabitDao.kt           # Data Access Object for habits
└── MoodEntryDao.kt       # Data Access Object for mood entries
```

### Repository Layer (2 files)
```
app/src/main/java/com/example/labexam03/repository/
├── HabitRepository.kt    # Habit data access abstraction
└── MoodEntryRepository.kt # MoodEntry data access abstraction
```

### Documentation (3 files)
```
docs/
├── ROOM_INTEGRATION.md          # Technical integration guide
├── LAB_EXAM_4_SUMMARY.md        # Lab requirements & implementation
└── BEFORE_AFTER_COMPARISON.md   # Before/after comparison
```

## Files Modified

### Build Configuration (2 files)
- `gradle/libs.versions.toml` - Added Room and KSP versions
- `app/build.gradle.kts` - Added Room dependencies and KSP plugin

### Data Models (2 files)
- `models/Habit.kt` - Added `@Entity` and `@PrimaryKey` annotations
- `models/MoodEntry.kt` - Added `@Entity` and `@PrimaryKey` annotations

### Data Management (1 file)
- `utils/PreferencesManager.kt` - Integrated Room, added migration logic

### UI Layer (2 files)
- `fragments/HabitTrackerFragment.kt` - Updated to use Room with coroutines
- `fragments/MoodJournalFragment.kt` - Updated to use Room with coroutines

### Documentation (3 files)
- `README.md` - Updated to reflect Room usage
- `SUMMARY.md` - Updated architecture and file counts
- `IMPLEMENTATION.md` - Updated data persistence section

## Quick Code Reference

### Entity Definition
```kotlin
@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val targetCount: Int,
    var currentCount: Int = 0,
    var lastUpdated: String = ""
)
```

### DAO Interface
```kotlin
@Dao
interface HabitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(habit: Habit)
    
    @Query("SELECT * FROM habits ORDER BY name ASC")
    suspend fun getAllHabits(): List<Habit>
}
```

### Database Class
```kotlin
@Database(entities = [Habit::class, MoodEntry::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun moodEntryDao(): MoodEntryDao
    
    companion object {
        fun getDatabase(context: Context): AppDatabase { /* singleton */ }
    }
}
```

### Repository
```kotlin
class HabitRepository(private val habitDao: HabitDao) {
    val allHabits: Flow<List<Habit>> = habitDao.getAllHabitsFlow()
    suspend fun insert(habit: Habit) = habitDao.insert(habit)
}
```

### Fragment Usage
```kotlin
class HabitTrackerFragment : Fragment() {
    private lateinit var prefsManager: PreferencesManager
    
    private fun loadHabits() {
        lifecycleScope.launch {
            val habits = prefsManager.habitRepository.getAllHabits()
            habitAdapter.updateHabits(habits)
        }
    }
    
    private fun addHabit(habit: Habit) {
        lifecycleScope.launch {
            prefsManager.habitRepository.insert(habit)
            loadHabits()
        }
    }
}
```

## Database Schema

### habits table
| Column | Type | Constraint |
|--------|------|-----------|
| id | TEXT | PRIMARY KEY |
| name | TEXT | NOT NULL |
| targetCount | INTEGER | NOT NULL |
| currentCount | INTEGER | NOT NULL |
| lastUpdated | TEXT | NOT NULL |

### mood_entries table
| Column | Type | Constraint |
|--------|------|-----------|
| id | TEXT | PRIMARY KEY |
| emoji | TEXT | NOT NULL |
| note | TEXT | NOT NULL |
| timestamp | INTEGER | NOT NULL |
| dateString | TEXT | NOT NULL |
| timeString | TEXT | NOT NULL |

## Key Annotations

### @Entity
Marks a class as a database entity (table)
```kotlin
@Entity(tableName = "habits")  // Optional custom table name
data class Habit(...)
```

### @PrimaryKey
Marks a field as the primary key
```kotlin
@PrimaryKey
val id: String
```

### @Dao
Marks an interface as a Data Access Object
```kotlin
@Dao
interface HabitDao { ... }
```

### @Database
Marks a class as a Room database
```kotlin
@Database(entities = [...], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase()
```

### @Query
Defines a SQL query method
```kotlin
@Query("SELECT * FROM habits WHERE id = :habitId")
suspend fun getHabitById(habitId: String): Habit?
```

### @Insert, @Update, @Delete
Convenience methods for common operations
```kotlin
@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun insert(habit: Habit)

@Update
suspend fun update(habit: Habit)

@Delete
suspend fun delete(habit: Habit)
```

## Dependencies

```kotlin
// build.gradle.kts (app level)
dependencies {
    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
}

plugins {
    id("com.google.devtools.ksp") version "2.0.21-1.0.25"
}
```

## Migration Strategy

```kotlin
// PreferencesManager.kt
init {
    migrateToRoom()  // Automatic migration on first launch
}

private fun migrateToRoom() {
    if (!prefs.getBoolean(KEY_MIGRATED_TO_ROOM, false)) {
        CoroutineScope(Dispatchers.IO).launch {
            // 1. Read from SharedPreferences
            // 2. Insert into Room
            // 3. Mark as migrated
            // 4. Clean up old data
        }
    }
}
```

## Testing Checklist

- [x] App compiles successfully
- [ ] Add new habit → persists after app restart
- [ ] Edit habit → changes persist
- [ ] Delete habit → removed from database
- [ ] Add mood entry → persists after app restart
- [ ] App works on fresh install (no migration)
- [ ] App works on upgrade (migration from SharedPreferences)
- [ ] All existing features work identically

## Common Operations

### Insert
```kotlin
lifecycleScope.launch {
    val habit = Habit(name = "Exercise", targetCount = 5)
    repository.insert(habit)
}
```

### Query All
```kotlin
lifecycleScope.launch {
    val habits = repository.getAllHabits()
    // Use habits
}
```

### Update
```kotlin
lifecycleScope.launch {
    val updated = habit.copy(currentCount = habit.currentCount + 1)
    repository.update(updated)
}
```

### Delete
```kotlin
lifecycleScope.launch {
    repository.delete(habit)
}
```

### Observe with Flow
```kotlin
lifecycleScope.launch {
    repository.allHabits.collect { habits ->
        // UI updates automatically
        habitAdapter.updateHabits(habits)
    }
}
```

## Benefits Summary

✅ **Type Safety**: Compile-time SQL verification
✅ **Performance**: Optimized queries, connection pooling
✅ **Scalability**: Handles large datasets efficiently
✅ **Maintainability**: Clean architecture with repositories
✅ **Reactivity**: Flow-based reactive updates
✅ **Queries**: Support for complex SQL queries
✅ **Migration**: Built-in versioning and migration support

## Lab Exam 4 Requirements

✅ **SQLite**: Android's built-in SQLite database via Room
✅ **ORM**: Complete Room ORM implementation
- ✅ Entity classes with annotations
- ✅ DAO interfaces with SQL queries
- ✅ Database class with Room
- ✅ Repository pattern
- ✅ Coroutines for async operations
- ✅ Type-safe queries

## Additional Resources

- **ROOM_INTEGRATION.md**: Detailed technical guide with examples
- **LAB_EXAM_4_SUMMARY.md**: Complete lab exam implementation summary
- **BEFORE_AFTER_COMPARISON.md**: Side-by-side comparison with old approach
- **README.md**: Updated project overview
- **IMPLEMENTATION.md**: Technical implementation details

## Architecture Diagram

```
┌─────────────────────────────────────┐
│     UI Layer (Fragments)            │
│  HabitTrackerFragment               │
│  MoodJournalFragment                │
└─────────────┬───────────────────────┘
              │ Access via
┌─────────────▼───────────────────────┐
│     PreferencesManager              │
│  (Provides repository instances)    │
└─────────────┬───────────────────────┘
              │ Delegates to
┌─────────────▼───────────────────────┐
│     Repository Layer                │
│  HabitRepository                    │
│  MoodEntryRepository                │
└─────────────┬───────────────────────┘
              │ Uses
┌─────────────▼───────────────────────┐
│     DAO Layer (Interfaces)          │
│  HabitDao                           │
│  MoodEntryDao                       │
└─────────────┬───────────────────────┘
              │ Generates SQL
┌─────────────▼───────────────────────┐
│     Room Database                   │
│  AppDatabase (singleton)            │
└─────────────┬───────────────────────┘
              │ Manages
┌─────────────▼───────────────────────┐
│     SQLite                          │
│  wellness_database                  │
│  - habits table                     │
│  - mood_entries table               │
└─────────────────────────────────────┘
```

## Key Differences from Lab Exam 3

| Aspect | Lab Exam 3 | Lab Exam 4 |
|--------|-----------|-----------|
| Data Storage | SharedPreferences | SQLite via Room |
| Serialization | Manual JSON | Automatic |
| Queries | Load all + filter | SQL queries |
| Type Safety | Runtime | Compile-time |
| Operations | Synchronous | Asynchronous |
| Architecture | Direct access | Repository pattern |
| Scalability | Limited | High |

---

**Status**: ✅ Complete and ready for testing
**Lab Requirements**: ✅ All met
**Documentation**: ✅ Comprehensive
**Code Quality**: ✅ Follows best practices
