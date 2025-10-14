# Personal Wellness Android App

A complete Android application built with Kotlin that promotes personal wellness through habit tracking, mood journaling, and hydration reminders.

## Features

### 1. Daily Habit Tracker
- **Add, Edit, and Delete Habits**: Manage your daily habits with an intuitive interface
- **Progress Tracking**: Visual progress bars showing completion percentage
- **Daily Targets**: Set custom targets for each habit
- **Auto-Reset**: Progress automatically resets at the start of each day
- **Persistent Storage**: All habits saved using Room Database (SQLite)

### 2. Mood Journal with Emoji Selector
- **Emoji-Based Mood Logging**: Select from 5 different mood emojis (😢, 😞, 😐, 😊, 😄)
- **Optional Notes**: Add text notes to mood entries
- **Date/Time Stamps**: Automatic timestamp for each entry
- **Mood History**: View all past mood entries in a scrollable list
- **Share Functionality**: Share mood summaries via implicit intents

### 3. Hydration Reminders
- **Customizable Intervals**: Set reminder intervals from 15 minutes to 4 hours
- **AlarmManager Integration**: Uses AlarmManager for reliable scheduling
- **Notification Support**: Push notifications with NotificationManager
- **Test Notification**: Test button to verify notification setup
- **Persistent Settings**: Reminder preferences saved across sessions

### 4. Advanced Feature: Mood Trend Chart
- **MPAndroidChart Integration**: Beautiful line chart visualization
- **Weekly Mood Trends**: Displays mood patterns over the last 7 days
- **Interactive Chart**: Touch-enabled with zoom and pan capabilities
- **Mood Value Mapping**: Converts emojis to numeric values (1-5 scale)

## Technical Implementation

### Architecture
- **MVVM Pattern**: Separation of concerns with Models, Views, and data management
- **Fragments**: Modular UI with HabitTrackerFragment, MoodJournalFragment, and SettingsFragment
- **Navigation**: BottomNavigationView for seamless fragment switching
- **Data Persistence**: Room Database with SQLite for habits and mood entries, SharedPreferences for settings
- **Repository Pattern**: Clean separation between data access and business logic

### Key Technologies
- **Language**: Kotlin
- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 36
- **UI Framework**: Material Design 3
- **Database**: Room ORM with SQLite
- **Async Operations**: Kotlin Coroutines
- **Charts**: MPAndroidChart library
- **JSON Parsing**: Gson for data serialization

### Components

#### Data Models
- `Habit.kt`: Room Entity representing a daily habit with progress tracking
- `MoodEntry.kt`: Room Entity representing a mood journal entry with emoji and notes

#### Database Layer
- `AppDatabase.kt`: Room database instance managing SQLite database
- `HabitDao.kt`: Data Access Object for habit operations
- `MoodEntryDao.kt`: Data Access Object for mood entry operations

#### Repository Layer
- `HabitRepository.kt`: Repository for habit data access
- `MoodEntryRepository.kt`: Repository for mood entry data access

#### Utilities
- `PreferencesManager.kt`: Manages Room database repositories and SharedPreferences for settings

#### Adapters
- `HabitAdapter.kt`: RecyclerView adapter for displaying habits
- `MoodAdapter.kt`: RecyclerView adapter for displaying mood entries

#### Receivers
- `HydrationReminderReceiver.kt`: BroadcastReceiver for handling reminder notifications

### Permissions
```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.USE_EXACT_ALARM" />
```

## User Interface

### Layouts
- **Portrait Mode**: Vertical scrolling optimized for phone use
- **Landscape Mode**: Two-column layout utilizing horizontal space
- **Responsive Design**: Adapts to different screen sizes (phones and tablets)
- **Material Design**: Follows Material Design 3 guidelines

### Navigation
Three main screens accessible via bottom navigation:
1. **Habits**: Daily habit tracker with progress monitoring
2. **Mood**: Mood journal with chart and history
3. **Settings**: Hydration reminder configuration

## Data Persistence

The app uses a hybrid persistence strategy:

### Room Database (SQLite)
- **Habits**: Stored in `habits` table with full CRUD operations
- **Mood Entries**: Stored in `mood_entries` table ordered by timestamp
- **Benefits**: Type-safe queries, automatic migration, reactive updates via Flow
- **Migration**: Automatic migration from old SharedPreferences data on first launch

### SharedPreferences
- **Reminder Settings**: Enabled state and interval configuration
- **App Settings**: Last reminder time and other preferences
- **Lightweight**: Used only for simple key-value settings

For detailed information about the Room database implementation, see [ROOM_INTEGRATION.md](ROOM_INTEGRATION.md)

## Code Quality

### Comments
- Comprehensive KDoc comments for all classes and functions
- Inline comments explaining complex logic
- Parameter descriptions and return value documentation

### Organization
- Logical package structure (models, fragments, adapters, utils, receivers)
- Separated concerns (UI, data, business logic)
- Reusable components and utilities

### Best Practices
- Proper resource management (closing dialogs, stopping services)
- Error handling for edge cases
- Null safety with Kotlin's type system
- State preservation across configuration changes

## Building and Running

### Requirements
- Android Studio Arctic Fox or newer
- Gradle 8.13
- Kotlin 2.0.21
- Android SDK API 36

### Build Instructions
```bash
# Clone the repository
git clone https://github.com/nadunwee/mad-lab-03.git

# Open in Android Studio
# File > Open > Select the project directory

# Sync Gradle files
# File > Sync Project with Gradle Files

# Run on emulator or device
# Run > Run 'app'
```

### Testing
The app includes:
- Unit test examples in `ExampleUnitTest.kt`
- Instrumented test examples in `ExampleInstrumentedTest.kt`

## Usage Guide

### Adding a Habit
1. Navigate to the Habits tab
2. Tap the + button
3. Enter habit name and daily target
4. Tap "Add"

### Logging Mood
1. Navigate to the Mood tab
2. Tap the + button
3. Select an emoji representing your mood
4. Optionally add a note
5. Tap "Save"

### Setting Up Reminders
1. Navigate to the Settings tab
2. Enable "Enable Reminders" switch
3. Adjust the interval slider
4. Tap "Save Settings"
5. Use "Test Notification" to verify

### Sharing Mood Summary
1. Navigate to the Mood tab
2. Tap the share button (📤)
3. Select app to share via (Messages, Email, etc.)

## Screenshots

(Screenshots would be inserted here showing the app in action)

## License

This project is for educational purposes as part of MAD Lab 03.

## Author

Created as part of Mobile Application Development coursework.

## Acknowledgments

- Material Design Icons from Google
- MPAndroidChart library by PhilJay
- Android documentation and best practices
