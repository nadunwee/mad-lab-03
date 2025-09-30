package com.example.labexam03.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.labexam03.R
import com.example.labexam03.models.MoodEntry

/**
 * RecyclerView adapter for displaying list of mood entries
 * Shows past mood logs with emoji, date, time, and optional notes
 */
class MoodAdapter(
    private var moodEntries: MutableList<MoodEntry>,
    private val onItemClick: (MoodEntry) -> Unit
) : RecyclerView.Adapter<MoodAdapter.MoodViewHolder>() {
    
    inner class MoodViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val emojiText: TextView = view.findViewById(R.id.moodEmojiText)
        val dateText: TextView = view.findViewById(R.id.moodDateText)
        val timeText: TextView = view.findViewById(R.id.moodTimeText)
        val noteText: TextView = view.findViewById(R.id.moodNoteText)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood, parent, false)
        return MoodViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        val entry = moodEntries[position]
        
        holder.emojiText.text = entry.emoji
        holder.dateText.text = entry.dateString
        holder.timeText.text = entry.timeString
        
        // Show note if available, hide otherwise
        if (entry.note.isNotEmpty()) {
            holder.noteText.text = entry.note
            holder.noteText.visibility = View.VISIBLE
        } else {
            holder.noteText.visibility = View.GONE
        }
        
        holder.itemView.setOnClickListener {
            onItemClick(entry)
        }
    }
    
    override fun getItemCount(): Int = moodEntries.size
    
    /**
     * Update the mood entries list and refresh the RecyclerView
     */
    fun updateMoodEntries(newEntries: List<MoodEntry>) {
        moodEntries.clear()
        moodEntries.addAll(newEntries)
        notifyDataSetChanged()
    }
}
