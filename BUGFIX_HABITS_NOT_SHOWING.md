# Bug Fix: Habits and Mood Entries Not Showing Up After Adding

## Problem Statement
When users added a new habit or mood entry, the item would not appear in the list immediately, even though the "Habit added!" or similar toast message was shown.

## Root Cause
The issue was caused by a shared mutable list reference between the Fragment and its Adapter. Here's what happened:

1. **Fragment initialization**: Both `HabitTrackerFragment` and `MoodJournalFragment` create a mutable list:
   ```kotlin
   private var habits = mutableListOf<Habit>()
   private var moodEntries = mutableListOf<MoodEntry>()
   ```

2. **Adapter initialization**: The adapter was created with a direct reference to this list:
   ```kotlin
   // BEFORE (buggy code)
   habitAdapter = HabitAdapter(
       habits = habits,  // Same reference as fragment's list
       ...
   )
   ```

3. **Adding a new item**: When a new habit is added:
   ```kotlin
   habits.add(habit)
   saveHabits()
   habitAdapter.updateHabits(habits)  // Pass the same list
   ```

4. **The bug**: Inside `updateHabits()`, the adapter does:
   ```kotlin
   fun updateHabits(newHabits: List<Habit>) {
       habits.clear()        // Clears the SHARED list
       habits.addAll(newHabits)  // Tries to add from the now-empty list!
   }
   ```

Since both the adapter and fragment share the same list object, clearing the adapter's list also clears the fragment's list. Then when trying to add items from `newHabits` (which is the same now-empty list), nothing gets added.

## Solution
Pass a **copy** of the list when initializing the adapter, so each has its own independent list:

```kotlin
// AFTER (fixed code)
habitAdapter = HabitAdapter(
    habits = habits.toMutableList(),  // Creates a copy
    ...
)
```

Now when `updateHabits()` is called:
- The adapter clears its own copy of the list
- Then adds items from the fragment's list (which still has the items)
- Result: Items appear correctly

## Files Changed
1. `app/src/main/java/com/example/labexam03/fragments/HabitTrackerFragment.kt`
   - Line 75: Changed `habits = habits` to `habits = habits.toMutableList()`

2. `app/src/main/java/com/example/labexam03/fragments/MoodJournalFragment.kt`
   - Line 92: Changed `moodEntries = moodEntries` to `moodEntries = moodEntries.toMutableList()`

## Testing
To verify the fix, a standalone Kotlin test was created demonstrating the bug and the fix:

```kotlin
// Bug demonstration
val habits = mutableListOf<Habit>()
val adapter = HabitAdapter(habits)  // Same reference
habits.add(Habit("Exercise"))
adapter.updateHabits(habits)
// Result: adapter.getCount() == 0 (BUG!)

// Fix demonstration
val habits = mutableListOf<Habit>()
val adapter = HabitAdapter(habits.toMutableList())  // Copy
habits.add(Habit("Exercise"))
adapter.updateHabits(habits)
// Result: adapter.getCount() == 1 (FIXED!)
```

## Impact
- ✅ Habits now appear immediately after being added
- ✅ Mood entries now appear immediately after being added
- ✅ No impact on existing functionality (edit, delete, increment still work)
- ✅ Minimal code change (only 2 lines changed)
