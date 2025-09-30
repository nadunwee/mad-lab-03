package com.example.labexam03.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.labexam03.R
import com.example.labexam03.models.Habit

/**
 * RecyclerView adapter for displaying list of habits
 * Handles habit item display with progress tracking and action buttons
 */
class HabitAdapter(
    private var habits: MutableList<Habit>,
    private val onIncrement: (Habit) -> Unit,
    private val onEdit: (Habit) -> Unit,
    private val onDelete: (Habit) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {
    
    inner class HabitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.habitNameText)
        val progressText: TextView = view.findViewById(R.id.habitProgressText)
        val progressBar: ProgressBar = view.findViewById(R.id.habitProgressBar)
        val incrementButton: Button = view.findViewById(R.id.incrementButton)
        val editButton: Button = view.findViewById(R.id.editButton)
        val deleteButton: Button = view.findViewById(R.id.deleteButton)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]
        
        // Display habit information
        holder.nameText.text = habit.name
        holder.progressText.text = "${habit.currentCount} / ${habit.targetCount}"
        holder.progressBar.progress = habit.getCompletionPercentage()
        
        // Set up button click listeners
        holder.incrementButton.setOnClickListener {
            onIncrement(habit)
        }
        
        holder.editButton.setOnClickListener {
            onEdit(habit)
        }
        
        holder.deleteButton.setOnClickListener {
            onDelete(habit)
        }
        
        // Change appearance if completed
        if (habit.isCompleted()) {
            holder.progressText.setTextColor(holder.itemView.context.getColor(android.R.color.holo_green_dark))
        } else {
            holder.progressText.setTextColor(holder.itemView.context.getColor(android.R.color.darker_gray))
        }
    }
    
    override fun getItemCount(): Int = habits.size
    
    /**
     * Update the habit list and refresh the RecyclerView
     */
    fun updateHabits(newHabits: List<Habit>) {
        habits.clear()
        habits.addAll(newHabits)
        notifyDataSetChanged()
    }
}
