# Developer Quick Reference Guide

## Project Structure

```
app/src/main/
├── java/com/example/labexam03/
│   ├── adapters/
│   │   ├── HabitAdapter.kt          # RecyclerView adapter for habits
│   │   └── MoodAdapter.kt           # RecyclerView adapter for mood entries
│   ├── fragments/
│   │   ├── HabitTrackerFragment.kt  # Habit management screen
│   │   ├── MoodJournalFragment.kt   # Mood logging screen
│   │   └── SettingsFragment.kt      # Settings screen
│   ├── models/
│   │   ├── Habit.kt                 # Habit data model
│   │   └── MoodEntry.kt             # Mood entry data model
│   ├── receivers/
│   │   └── HydrationReminderReceiver.kt  # Notification receiver
│   ├── utils/
│   │   └── PreferencesManager.kt    # SharedPreferences utility
│   └── MainActivity.kt              # Main activity with navigation
├── res/
│   ├── drawable/                    # Icons and graphics
│   ├── layout/                      # Portrait layouts
│   ├── layout-land/                 # Landscape layouts
│   ├── menu/                        # Navigation menus
│   └── values/                      # Strings, colors, themes
└── AndroidManifest.xml              # App configuration
```

## Key Classes and Methods

### MainActivity.kt
**Purpose**: Host activity with bottom navigation
**Key Methods**:
- `loadFragment(fragment: Fragment)`: Switch between fragments
- `requestNotificationPermission()`: Request notification permission for Android 13+

### HabitTrackerFragment.kt
**Purpose**: Manage daily habits
**Key Methods**:
- `showAddHabitDialog()`: Display dialog to add new habit
- `showEditHabitDialog(habit: Habit)`: Edit existing habit
- `incrementHabit(habit: Habit)`: Increase habit progress
- `checkAndResetDailyProgress()`: Reset progress for new day

### MoodJournalFragment.kt
**Purpose**: Log and display moods
**Key Methods**:
- `showAddMoodDialog()`: Display emoji selector dialog
- `updateChart()`: Refresh mood trend chart with latest data
- `shareMoodSummary()`: Share mood summary via implicit intent

### SettingsFragment.kt
**Purpose**: Configure hydration reminders
**Key Methods**:
- `scheduleHydrationReminders(intervalMinutes: Int)`: Set up AlarmManager
- `cancelHydrationReminders()`: Cancel all scheduled reminders
- `sendTestNotification()`: Send immediate test notification

### PreferencesManager.kt
**Purpose**: Centralized data persistence
**Key Methods**:
- `saveHabits(habits: List<Habit>)`: Persist habits to SharedPreferences
- `getHabits(): MutableList<Habit>`: Retrieve habits from SharedPreferences
- `saveMoodEntries(entries: List<MoodEntry>)`: Persist mood entries
- `getMoodEntries(): MutableList<MoodEntry>`: Retrieve mood entries
- `setReminderEnabled(enabled: Boolean)`: Save reminder on/off state
- `setReminderInterval(intervalMinutes: Int)`: Save reminder interval

## Common Tasks

### Adding a New Habit Type
1. Modify `Habit` data class if needed (add fields)
2. Update `PreferencesManager` serialization if structure changed
3. Modify `HabitAdapter` to display new fields
4. Update `dialog_add_habit.xml` layout for new inputs

### Adding a New Mood Emoji
1. Add emoji to `moodEmojis` list in `MoodJournalFragment`
2. Update `MoodEntry.getMoodValue()` to map emoji to value
3. Ensure emoji renders correctly in UI

### Changing Reminder Intervals
1. Modify `intervalSeekBar` max value in `fragment_settings.xml`
2. Update calculation in `SettingsFragment.setupListeners()`
3. Adjust display text in `updateIntervalText()`

### Customizing Chart Appearance
1. Modify `setupChart()` in `MoodJournalFragment`
2. Adjust colors, line width, circle radius in `updateChart()`
3. Update axis settings (min, max, granularity)

## Important Constants

### SharedPreferences Keys
```kotlin
private const val PREFS_NAME = "wellness_app_prefs"
private const val KEY_HABITS = "habits"
private const val KEY_MOOD_ENTRIES = "mood_entries"
private const val KEY_REMINDER_ENABLED = "reminder_enabled"
private const val KEY_REMINDER_INTERVAL = "reminder_interval"
```

### Notification
```kotlin
const val CHANNEL_ID = "hydration_reminder_channel"
const val NOTIFICATION_ID = 1001
```

### Date Formats
```kotlin
val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
```

## Build Configuration

### Minimum Requirements
- **minSdk**: 24 (Android 7.0)
- **targetSdk**: 36
- **compileSdk**: 36
- **Kotlin**: 2.0.21
- **Gradle**: 8.13

### Dependencies
```kotlin
// Core Android
implementation("androidx.core:core-ktx:1.12.0")
implementation("androidx.appcompat:appcompat:1.7.1")
implementation("com.google.android.material:material:1.13.0")

// Fragments
implementation("androidx.fragment:fragment-ktx:1.6.2")

// WorkManager (for reminders)
implementation("androidx.work:work-runtime-ktx:2.9.0")

// Charts
implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

// JSON
implementation("com.google.code.gson:gson:2.10.1")
```

## Debugging Tips

### Enable Verbose Logging
Add to individual files:
```kotlin
private const val TAG = "YourFragmentName"
Log.d(TAG, "Debug message: $variable")
```

### Test SharedPreferences
```kotlin
// View stored data
val prefs = getSharedPreferences("wellness_app_prefs", Context.MODE_PRIVATE)
Log.d(TAG, "Habits: ${prefs.getString("habits", "none")}")

// Clear all data for testing
PreferencesManager(context).clearAll()
```

### Test Notifications Locally
```kotlin
// Send immediate notification
val intent = Intent(context, HydrationReminderReceiver::class.java)
context.sendBroadcast(intent)
```

### Check Alarm Status
```kotlin
val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
// Note: No direct way to list alarms, use logs in receiver
```

## UI Customization

### Change Theme Colors
Edit `res/values/colors.xml`:
```xml
<color name="primary">#6200EE</color>      <!-- Main brand color -->
<color name="primary_dark">#3700B3</color>  <!-- Darker variant -->
<color name="primary_light">#BB86FC</color> <!-- Lighter variant -->
<color name="accent">#03DAC5</color>        <!-- Accent color -->
```

### Modify Layouts
**Portrait**: `res/layout/`
**Landscape**: `res/layout-land/`
**Tablets**: `res/layout-sw600dp/` (create if needed)

### Update Strings
All user-facing text in `res/values/strings.xml`

## Testing Checklist

### Manual Testing
- [ ] Add habit and verify persistence
- [ ] Edit habit and verify changes saved
- [ ] Delete habit and verify removal
- [ ] Increment habit progress and check updates
- [ ] Log mood with emoji and note
- [ ] View mood history in list
- [ ] Check mood chart displays correctly
- [ ] Share mood summary via intent
- [ ] Enable hydration reminders
- [ ] Adjust reminder interval
- [ ] Test notification appears
- [ ] Verify app state persists after restart
- [ ] Test portrait orientation
- [ ] Test landscape orientation
- [ ] Check tablet layout (if available)

### Edge Cases
- [ ] No habits: Empty state shows
- [ ] No moods: Empty state shows  
- [ ] Day change: Habits reset correctly
- [ ] Permission denied: App handles gracefully
- [ ] Invalid input: Validation works
- [ ] Large data set: Performance acceptable

## Troubleshooting

### Build Errors
**Issue**: AGP version not found
**Solution**: Update `gradle/libs.versions.toml` with stable AGP version

**Issue**: MPAndroidChart not found
**Solution**: Verify JitPack repository in `settings.gradle.kts`

### Runtime Errors
**Issue**: Notification not showing
**Solution**: 
1. Check POST_NOTIFICATIONS permission granted
2. Verify notification channel created
3. Check receiver registered in manifest

**Issue**: Data not persisting
**Solution**:
1. Verify SharedPreferences key names match
2. Check Gson serialization working
3. Ensure `.apply()` called after edits

**Issue**: Chart not displaying
**Solution**:
1. Verify data exists (check empty state)
2. Check chart visibility not GONE
3. Ensure entries within last 7 days

## Code Style Guidelines

### Naming Conventions
- **Classes**: PascalCase (e.g., `HabitAdapter`)
- **Functions**: camelCase (e.g., `saveHabits()`)
- **Variables**: camelCase (e.g., `habitList`)
- **Constants**: SCREAMING_SNAKE_CASE (e.g., `CHANNEL_ID`)

### Comments
- Use KDoc for public classes and functions
- Add inline comments for complex logic
- Explain "why" not just "what"

### File Organization
- One class per file
- Group related functionality
- Keep files under 500 lines

## Resources

### Official Documentation
- [Android Developers](https://developer.android.com/)
- [Kotlin Language](https://kotlinlang.org/)
- [Material Design](https://material.io/)

### Libraries
- [MPAndroidChart GitHub](https://github.com/PhilJay/MPAndroidChart)
- [Gson Documentation](https://github.com/google/gson)

### Tutorials
- [Android Fragments Guide](https://developer.android.com/guide/fragments)
- [SharedPreferences Guide](https://developer.android.com/training/data-storage/shared-preferences)
- [AlarmManager Guide](https://developer.android.com/training/scheduling/alarms)
