package com.example.labexam03.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.labexam03.R
import com.example.labexam03.adapters.HabitAdapter
import com.example.labexam03.models.Habit
import com.example.labexam03.utils.PreferencesManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fragment for managing daily habits
 * Displays habit list with progress tracking and provides add/edit/delete functionality
 */
class HabitTrackerFragment : Fragment() {
    
    private lateinit var prefsManager: PreferencesManager
    private lateinit var habitAdapter: HabitAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var addHabitButton: FloatingActionButton
    private lateinit var emptyStateText: TextView
    private var habits = mutableListOf<Habit>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_habit_tracker, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize preferences manager
        prefsManager = PreferencesManager(requireContext())
        
        // Initialize views
        recyclerView = view.findViewById(R.id.habitRecyclerView)
        addHabitButton = view.findViewById(R.id.addHabitButton)
        emptyStateText = view.findViewById(R.id.emptyStateText)
        
        // Set up RecyclerView
        setupRecyclerView()
        
        // Load habits from Room Database
        loadHabits()
        
        // Set up add button click listener
        addHabitButton.setOnClickListener {
            showAddHabitDialog()
        }
        
        // Check and reset daily progress if it's a new day
        checkAndResetDailyProgress()
    }
    
    /**
     * Set up RecyclerView with adapter
     */
    private fun setupRecyclerView() {
        habitAdapter = HabitAdapter(
            habits = habits.toMutableList(),
            onIncrement = { habit -> incrementHabit(habit) },
            onEdit = { habit -> showEditHabitDialog(habit) },
            onDelete = { habit -> showDeleteConfirmation(habit) }
        )
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = habitAdapter
        }
    }
    
    /**
     * Load habits from Room Database
     */
    private fun loadHabits() {
        lifecycleScope.launch {
            habits.clear()
            habits.addAll(prefsManager.habitRepository.getAllHabits())
            habitAdapter.updateHabits(habits)
            updateEmptyState()
        }
    }
    
    /**
     * Show/hide empty state message based on habits list
     */
    private fun updateEmptyState() {
        if (habits.isEmpty()) {
            emptyStateText.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyStateText.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
    
    /**
     * Show dialog to add a new habit
     */
    private fun showAddHabitDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_habit, null)
        
        val nameInput = dialogView.findViewById<EditText>(R.id.habitNameInput)
        val targetInput = dialogView.findViewById<EditText>(R.id.habitTargetInput)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Add New Habit")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = nameInput.text.toString().trim()
                val targetText = targetInput.text.toString().trim()
                
                if (name.isEmpty() || targetText.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                val target = targetText.toIntOrNull()
                if (target == null || target <= 0) {
                    Toast.makeText(requireContext(), "Please enter a valid target", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                val habit = Habit(
                    name = name,
                    targetCount = target,
                    lastUpdated = getCurrentDate()
                )
                
                lifecycleScope.launch {
                    prefsManager.habitRepository.insert(habit)
                    loadHabits()
                    Toast.makeText(requireContext(), "Habit added!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    /**
     * Show dialog to edit an existing habit
     */
    private fun showEditHabitDialog(habit: Habit) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_habit, null)
        
        val nameInput = dialogView.findViewById<EditText>(R.id.habitNameInput)
        val targetInput = dialogView.findViewById<EditText>(R.id.habitTargetInput)
        
        // Pre-fill with existing values
        nameInput.setText(habit.name)
        targetInput.setText(habit.targetCount.toString())
        
        AlertDialog.Builder(requireContext())
            .setTitle("Edit Habit")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = nameInput.text.toString().trim()
                val targetText = targetInput.text.toString().trim()
                
                if (name.isEmpty() || targetText.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                val target = targetText.toIntOrNull()
                if (target == null || target <= 0) {
                    Toast.makeText(requireContext(), "Please enter a valid target", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                // Update habit
                lifecycleScope.launch {
                    val updatedHabit = habit.copy(name = name, targetCount = target)
                    prefsManager.habitRepository.update(updatedHabit)
                    loadHabits()
                    Toast.makeText(requireContext(), "Habit updated!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    /**
     * Show confirmation dialog before deleting a habit
     */
    private fun showDeleteConfirmation(habit: Habit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete \"${habit.name}\"?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    prefsManager.habitRepository.delete(habit)
                    loadHabits()
                    Toast.makeText(requireContext(), "Habit deleted", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    /**
     * Increment habit progress
     */
    private fun incrementHabit(habit: Habit) {
        val currentDate = getCurrentDate()
        
        // Reset progress if it's a new day
        if (habit.lastUpdated != currentDate) {
            habit.currentCount = 0
            habit.lastUpdated = currentDate
        }
        
        if (habit.currentCount < habit.targetCount) {
            habit.currentCount++
        }
        
        lifecycleScope.launch {
            prefsManager.habitRepository.update(habit)
            loadHabits()
            
            if (habit.isCompleted()) {
                Toast.makeText(requireContext(), "Habit completed! 🎉", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * Check all habits and reset progress if it's a new day
     */
    private fun checkAndResetDailyProgress() {
        val currentDate = getCurrentDate()
        
        lifecycleScope.launch {
            var updated = false
            
            habits.forEach { habit ->
                if (habit.lastUpdated != currentDate) {
                    habit.currentCount = 0
                    habit.lastUpdated = currentDate
                    prefsManager.habitRepository.update(habit)
                    updated = true
                }
            }
            
            if (updated) {
                loadHabits()
            }
        }
    }
    
    /**
     * Get current date in YYYY-MM-DD format
     */
    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}
