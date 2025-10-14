# Before and After: SharedPreferences vs Room Database

This document shows the key differences between the old SharedPreferences implementation and the new Room Database implementation.

## Data Persistence Approach

### Before (SharedPreferences + JSON)

```kotlin
// PreferencesManager.kt (OLD)
class PreferencesManager(context: Context) {
    private val prefs = context.getSharedPreferences("wellness_app_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    // Save habits - manual JSON serialization
    fun saveHabits(habits: List<Habit>) {
        val json = gson.toJson(habits)
        prefs.edit().putString("habits", json).apply()
    }
    
    // Load habits - manual JSON deserialization
    fun getHabits(): MutableList<Habit> {
        val json = prefs.getString("habits", null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<Habit>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }
}
```

**Problems:**
- ❌ No type safety
- ❌ Runtime errors from invalid JSON
- ❌ No query support (must load all data)
- ❌ Manual serialization/deserialization
- ❌ No relationship support
- ❌ Difficult to maintain as data grows

### After (Room Database)

```kotlin
// Habit.kt - Room Entity
@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val targetCount: Int,
    var currentCount: Int = 0,
    var lastUpdated: String = ""
)

// HabitDao.kt - Data Access Object
@Dao
interface HabitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(habit: Habit)
    
    @Update
    suspend fun update(habit: Habit)
    
    @Delete
    suspend fun delete(habit: Habit)
    
    @Query("SELECT * FROM habits ORDER BY name ASC")
    suspend fun getAllHabits(): List<Habit>
    
    @Query("SELECT * FROM habits ORDER BY name ASC")
    fun getAllHabitsFlow(): Flow<List<Habit>>
}

// AppDatabase.kt - Database
@Database(entities = [Habit::class, MoodEntry::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun moodEntryDao(): MoodEntryDao
}

// HabitRepository.kt - Repository
class HabitRepository(private val habitDao: HabitDao) {
    val allHabits: Flow<List<Habit>> = habitDao.getAllHabitsFlow()
    
    suspend fun insert(habit: Habit) = habitDao.insert(habit)
    suspend fun update(habit: Habit) = habitDao.update(habit)
    suspend fun delete(habit: Habit) = habitDao.delete(habit)
}
```

**Benefits:**
- ✅ Type-safe queries (compile-time verification)
- ✅ Automatic object mapping
- ✅ Complex query support
- ✅ Reactive updates with Flow
- ✅ Better performance
- ✅ Scalable architecture

## Fragment Implementation

### Before (Synchronous with SharedPreferences)

```kotlin
// HabitTrackerFragment.kt (OLD)
class HabitTrackerFragment : Fragment() {
    private lateinit var prefsManager: PreferencesManager
    private var habits = mutableListOf<Habit>()
    
    // Load data - synchronous
    private fun loadHabits() {
        habits.clear()
        habits.addAll(prefsManager.getHabits())
        habitAdapter.updateHabits(habits)
        updateEmptyState()
    }
    
    // Save data - synchronous
    private fun saveHabits() {
        prefsManager.saveHabits(habits)
    }
    
    // Add habit
    private fun addHabit(habit: Habit) {
        habits.add(habit)
        saveHabits()
        habitAdapter.updateHabits(habits)
    }
    
    // Update habit
    private fun updateHabit(habit: Habit) {
        val index = habits.indexOfFirst { it.id == habit.id }
        if (index != -1) {
            habits[index] = habit
            saveHabits()
            habitAdapter.notifyItemChanged(index)
        }
    }
}
```

**Issues:**
- ❌ Synchronous I/O on main thread (acceptable only for small data)
- ❌ Manual list management
- ❌ No reactive updates
- ❌ Have to save entire list every time

### After (Asynchronous with Room + Coroutines)

```kotlin
// HabitTrackerFragment.kt (NEW)
class HabitTrackerFragment : Fragment() {
    private lateinit var prefsManager: PreferencesManager
    private var habits = mutableListOf<Habit>()
    
    // Load data - asynchronous with coroutines
    private fun loadHabits() {
        lifecycleScope.launch {
            habits.clear()
            habits.addAll(prefsManager.habitRepository.getAllHabits())
            habitAdapter.updateHabits(habits)
            updateEmptyState()
        }
    }
    
    // Add habit - asynchronous
    private fun addHabit(habit: Habit) {
        lifecycleScope.launch {
            prefsManager.habitRepository.insert(habit)
            loadHabits()  // Reload from database
        }
    }
    
    // Update habit - asynchronous
    private fun updateHabit(habit: Habit) {
        lifecycleScope.launch {
            prefsManager.habitRepository.update(habit)
            loadHabits()  // Reload from database
        }
    }
    
    // Delete habit - asynchronous
    private fun deleteHabit(habit: Habit) {
        lifecycleScope.launch {
            prefsManager.habitRepository.delete(habit)
            loadHabits()  // Reload from database
        }
    }
}
```

**Improvements:**
- ✅ Async I/O on background thread (no UI freezing)
- ✅ Automatic list management by database
- ✅ Can use Flow for reactive updates
- ✅ Only saves changed items

## Reactive Updates with Flow (Optional Enhancement)

### Using Flow for Automatic UI Updates

```kotlin
// HabitTrackerFragment.kt - With Flow
class HabitTrackerFragment : Fragment() {
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Observe data changes with Flow
        lifecycleScope.launch {
            prefsManager.habitRepository.allHabits.collect { habits ->
                // UI automatically updates when database changes
                this@HabitTrackerFragment.habits.clear()
                this@HabitTrackerFragment.habits.addAll(habits)
                habitAdapter.updateHabits(habits)
                updateEmptyState()
            }
        }
    }
    
    // Now we just modify the database, UI updates automatically
    private fun addHabit(habit: Habit) {
        lifecycleScope.launch {
            prefsManager.habitRepository.insert(habit)
            // No need to manually reload - Flow will trigger update
        }
    }
}
```

## Query Capabilities

### Before (SharedPreferences)

```kotlin
// Can only load all data
val allHabits = prefsManager.getHabits()

// Have to filter in memory
val completedHabits = allHabits.filter { it.isCompleted() }

// Have to sort in memory
val sortedHabits = allHabits.sortedBy { it.name }

// No support for complex queries
```

### After (Room Database)

```kotlin
// Can query specific data
@Query("SELECT * FROM habits WHERE currentCount >= targetCount")
suspend fun getCompletedHabits(): List<Habit>

// Database-level sorting
@Query("SELECT * FROM habits ORDER BY name ASC")
suspend fun getHabitsSortedByName(): List<Habit>

// Complex queries with multiple conditions
@Query("SELECT * FROM habits WHERE lastUpdated = :date AND currentCount > 0")
suspend fun getActiveHabitsForDate(date: String): List<Habit>

// Aggregation queries
@Query("SELECT COUNT(*) FROM habits WHERE currentCount >= targetCount")
suspend fun getCompletedHabitsCount(): Int

// Search queries
@Query("SELECT * FROM habits WHERE name LIKE '%' || :searchTerm || '%'")
suspend fun searchHabits(searchTerm: String): List<Habit>
```

## Data Model Comparison

### Before

```kotlin
// Habit.kt (OLD)
data class Habit(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val targetCount: Int,
    var currentCount: Int = 0,
    var lastUpdated: String = ""
) : Serializable
```

**Limitations:**
- ❌ No database mapping
- ❌ Serializable for Intent passing (not needed with database)

### After

```kotlin
// Habit.kt (NEW)
@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val targetCount: Int,
    var currentCount: Int = 0,
    var lastUpdated: String = ""
) : Serializable
```

**Improvements:**
- ✅ Database entity with `@Entity`
- ✅ Primary key with `@PrimaryKey`
- ✅ Custom table name
- ✅ Automatic type mapping
- ✅ Can add indexes, foreign keys, etc.

## Performance Comparison

| Operation | SharedPreferences | Room Database |
|-----------|------------------|---------------|
| Load All Data | O(n) - Parse entire JSON | O(n) - SQL query with indexes |
| Add Item | O(n) - Serialize entire list | O(1) - Single INSERT |
| Update Item | O(n) - Serialize entire list | O(1) - Single UPDATE |
| Delete Item | O(n) - Serialize entire list | O(1) - Single DELETE |
| Search | O(n) - Filter in memory | O(log n) - SQL with index |
| Reactive Updates | ❌ Manual polling | ✅ Flow-based automatic |
| Thread Safety | ⚠️ Must manage manually | ✅ Built-in |

## Migration Path

### Automatic Data Migration

```kotlin
// PreferencesManager.kt
private fun migrateToRoom() {
    if (!prefs.getBoolean(KEY_MIGRATED_TO_ROOM, false)) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Read from SharedPreferences
                val habitsJson = prefs.getString(KEY_HABITS, null)
                if (!habitsJson.isNullOrEmpty()) {
                    val habits: List<Habit> = gson.fromJson(habitsJson, type)
                    
                    // 2. Insert into Room
                    habits.forEach { habit ->
                        habitRepository.insert(habit)
                    }
                }
                
                // 3. Mark as migrated
                prefs.edit().putBoolean(KEY_MIGRATED_TO_ROOM, true).apply()
                
                // 4. Clean up old data
                prefs.edit().remove(KEY_HABITS).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
```

## Summary of Changes

### What Changed
1. ✅ **Data Storage**: SharedPreferences → SQLite via Room
2. ✅ **Serialization**: Manual JSON → Automatic object mapping
3. ✅ **Operations**: Synchronous → Asynchronous with Coroutines
4. ✅ **Architecture**: Direct access → Repository pattern
5. ✅ **Type Safety**: Runtime checks → Compile-time verification
6. ✅ **Queries**: Load all + filter → SQL queries
7. ✅ **Reactivity**: Manual updates → Flow-based reactive

### What Stayed the Same
- ✅ UI/UX experience
- ✅ Feature functionality
- ✅ SharedPreferences for settings
- ✅ All existing app features work identically

### What Improved
- ✅ Better performance for large datasets
- ✅ Type safety with compile-time checks
- ✅ Scalable architecture
- ✅ Support for complex queries
- ✅ Reactive data updates
- ✅ Thread safety
- ✅ Database versioning and migration support

## Conclusion

The migration from SharedPreferences to Room Database represents a significant architectural improvement while maintaining backward compatibility through automatic data migration. Users experience no disruption, and the app is now better positioned for future enhancements and scaling.
