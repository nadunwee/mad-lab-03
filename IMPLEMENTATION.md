# Implementation Details - Personal Wellness App

## Architecture Overview

### Data Layer
The app uses SharedPreferences for persistent storage, managed through the `PreferencesManager` utility class. This provides a centralized approach to data storage and retrieval.

**Key Design Decision**: SharedPreferences was chosen over SQLite/Room database as per requirements. While this limits complex querying capabilities, it's suitable for the app's data volume and simplifies implementation.

### Presentation Layer
The app follows a Fragment-based architecture with three main screens:

1. **HabitTrackerFragment**: Manages habit creation, editing, deletion, and progress tracking
2. **MoodJournalFragment**: Handles mood logging, history display, and trend visualization
3. **SettingsFragment**: Controls hydration reminder configuration

### Navigation
BottomNavigationView provides intuitive tab-based navigation between fragments. The MainActivity hosts the fragment container and handles fragment transactions.

## Key Features Implementation

### 1. Daily Habit Tracker

**Data Model**:
```kotlin
data class Habit(
    val id: String,           // Unique identifier (UUID)
    val name: String,         // Habit name
    val targetCount: Int,     // Daily target
    var currentCount: Int,    // Current progress
    var lastUpdated: String   // Last update date (YYYY-MM-DD)
)
```

**Progress Tracking**:
- Progress is tracked per day using `lastUpdated` field
- If date changes, progress auto-resets to 0
- Visual feedback via ProgressBar (0-100%)
- Completion status indicated by color changes

**User Actions**:
- **Add**: Dialog with name and target input
- **Edit**: Pre-filled dialog to modify existing habit
- **Delete**: Confirmation dialog before removal
- **Increment**: + button to increase progress

### 2. Mood Journal

**Data Model**:
```kotlin
data class MoodEntry(
    val id: String,          // Unique identifier
    val emoji: String,       // Selected mood emoji
    val note: String,        // Optional text note
    val timestamp: Long,     // Unix timestamp
    val dateString: String,  // Formatted date
    val timeString: String   // Formatted time
)
```

**Emoji Selection**:
Five mood options: 😢 (Very Sad), 😞 (Sad), 😐 (Neutral), 😊 (Happy), 😄 (Very Happy)

**Mood Value Mapping** (for charting):
- 😢 = 1.0
- 😞 = 2.0
- 😐 = 3.0
- 😊 = 4.0
- 😄 = 5.0

**Features**:
- Emoji selector dialog with optional note
- Chronological list of past entries (newest first)
- Tap entry to view details
- Share mood summary via implicit intent

### 3. Hydration Reminders

**Implementation Components**:

1. **SettingsFragment**: UI for configuration
   - Switch to enable/disable reminders
   - SeekBar for interval selection (15 min to 4 hours)
   - Test notification button

2. **HydrationReminderReceiver**: BroadcastReceiver
   - Creates notification channel (Android O+)
   - Displays notification with PendingIntent
   - Tapping notification opens MainActivity

3. **AlarmManager Scheduling**:
   ```kotlin
   alarmManager.setRepeating(
       AlarmManager.RTC_WAKEUP,
       triggerTime,
       intervalMillis,
       pendingIntent
   )
   ```

**Notification Channel**:
- Channel ID: "hydration_reminder_channel"
- Importance: DEFAULT
- Description: "Reminders to drink water throughout the day"

### 4. Advanced Feature: Mood Trend Chart

**Library**: MPAndroidChart v3.1.0

**Features**:
- Line chart showing last 7 days of moods
- X-axis: Dates (formatted as MM-DD)
- Y-axis: Mood values (1-5 scale)
- Filled area under line for visual appeal
- Touch-enabled with zoom and pan

**Data Processing**:
1. Filter entries from last 7 days
2. Sort by timestamp ascending
3. Map mood emojis to numeric values
4. Create Entry objects for chart
5. Apply styling and formatting

## Data Persistence Strategy

### SharedPreferences Keys
- `habits`: JSON array of Habit objects
- `mood_entries`: JSON array of MoodEntry objects
- `reminder_enabled`: Boolean
- `reminder_interval`: Integer (minutes)
- `last_reminder_time`: Long (timestamp)

### Serialization
Gson library handles JSON serialization/deserialization:
```kotlin
// Save
val json = gson.toJson(habits)
prefs.edit().putString("habits", json).apply()

// Load
val json = prefs.getString("habits", null)
val type = object : TypeToken<MutableList<Habit>>() {}.type
val habits = gson.fromJson<MutableList<Habit>>(json, type)
```

## UI/UX Design

### Material Design 3
- MaterialCardView for list items
- FloatingActionButton for primary actions
- TextInputLayout for form inputs
- Material color scheme with primary/accent colors

### Responsive Layouts
**Portrait**:
- Vertical scrolling
- Full-width components
- Bottom-aligned FABs

**Landscape**:
- Two-column layout
- Side-by-side panels
- Optimized horizontal space usage

### Adaptive Features
- Different layouts for portrait/landscape
- Responsive to screen size changes
- Configuration change handling
- State preservation

## Permission Handling

### Runtime Permissions
**POST_NOTIFICATIONS** (Android 13+):
```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    if (checkSelfPermission(POST_NOTIFICATIONS) != GRANTED) {
        requestPermissions(arrayOf(POST_NOTIFICATIONS), CODE)
    }
}
```

### Manifest Permissions
- POST_NOTIFICATIONS: For showing notifications
- SCHEDULE_EXACT_ALARM: For precise alarm scheduling
- USE_EXACT_ALARM: Companion permission for alarms

## Intent Usage

### Explicit Intents
**MainActivity Launch**:
Used in notification PendingIntent to return to app:
```kotlin
val intent = Intent(context, MainActivity::class.java)
val pendingIntent = PendingIntent.getActivity(
    context, 0, intent, 
    FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
)
```

### Implicit Intents
**Share Mood Summary**:
```kotlin
val shareIntent = Intent(Intent.ACTION_SEND).apply {
    type = "text/plain"
    putExtra(Intent.EXTRA_SUBJECT, "My Mood Journal Summary")
    putExtra(Intent.EXTRA_TEXT, summary)
}
startActivity(Intent.createChooser(shareIntent, "Share via"))
```

## State Management

### Fragment Lifecycle
- Data loaded in `onViewCreated()`
- No need for `onSaveInstanceState()` due to SharedPreferences
- Fragments recreated with fresh data on configuration changes

### Data Synchronization
- Immediate saves after any data modification
- No caching or delayed writes
- Consistent state across app restarts

## Error Handling

### Input Validation
- Empty field checks
- Numeric range validation
- Null safety with Kotlin's type system

### Edge Cases
- No habits/moods: Empty state messages
- Date changes: Auto-reset habit progress
- First-time use: Default settings

### Graceful Degradation
- Notification permission denied: App still functional
- Chart with no data: Hidden until data available
- Alarm scheduling fails: User notified via Toast

## Performance Considerations

### Efficient Data Loading
- Lazy loading of data only when needed
- Direct deserialization from JSON
- No unnecessary database queries

### Memory Management
- RecyclerView for efficient list rendering
- ViewHolder pattern for view recycling
- Minimal object creation in loops

### UI Responsiveness
- Data operations on main thread (acceptable for SharedPreferences)
- Immediate UI updates after actions
- Smooth animations and transitions

## Testing Strategy

### Unit Tests
- Model methods (Habit.getCompletionPercentage(), etc.)
- Utility functions (date formatting, JSON parsing)
- Business logic validation

### Instrumented Tests
- UI interactions
- Fragment navigation
- SharedPreferences operations
- Notification display

## Future Enhancements

Potential improvements (beyond scope):
1. **Database Migration**: Move to Room for complex queries
2. **Cloud Sync**: Firebase integration for multi-device support
3. **Advanced Charts**: More visualization options
4. **Habit Streaks**: Track consecutive completion days
5. **Custom Reminders**: Per-habit reminders
6. **Export Data**: CSV/PDF export functionality
7. **Themes**: Dark mode and custom themes
8. **Widgets**: Home screen widget for quick access
9. **Backup/Restore**: Data backup functionality
10. **Social Features**: Share achievements with friends

## Known Limitations

1. **No Data Backup**: Data lost if app uninstalled
2. **Single Device**: No multi-device synchronization
3. **Limited Queries**: SharedPreferences doesn't support complex queries
4. **No Offline Charts**: Charts require data in memory
5. **Fixed Interval**: Reminders at fixed intervals only

## Conclusion

This Personal Wellness App demonstrates:
- Complete Android app development lifecycle
- Modern Kotlin best practices
- Material Design principles
- Effective use of Android SDK components
- Clean, maintainable code architecture
- Comprehensive documentation

The app successfully meets all requirements while providing an intuitive, feature-rich user experience for promoting personal wellness.
