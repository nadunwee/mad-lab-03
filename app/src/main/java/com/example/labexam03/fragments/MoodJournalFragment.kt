package com.example.labexam03.fragments

import android.app.AlertDialog
import android.content.Intent
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
import com.example.labexam03.adapters.MoodAdapter
import com.example.labexam03.models.MoodEntry
import com.example.labexam03.utils.PreferencesManager
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fragment for logging and viewing mood entries
 * Features emoji selector, mood history list, and weekly mood trend chart
 */
class MoodJournalFragment : Fragment() {
    
    private lateinit var prefsManager: PreferencesManager
    private lateinit var moodAdapter: MoodAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var addMoodButton: FloatingActionButton
    private lateinit var shareButton: FloatingActionButton
    private lateinit var moodChart: LineChart
    private lateinit var emptyStateText: TextView
    private var moodEntries = mutableListOf<MoodEntry>()
    
    private val moodEmojis = listOf("😢", "😞", "😐", "😊", "😄")
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mood_journal, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize preferences manager
        prefsManager = PreferencesManager(requireContext())
        
        // Initialize views
        recyclerView = view.findViewById(R.id.moodRecyclerView)
        addMoodButton = view.findViewById(R.id.addMoodButton)
        shareButton = view.findViewById(R.id.shareMoodButton)
        moodChart = view.findViewById(R.id.moodChart)
        emptyStateText = view.findViewById(R.id.moodEmptyStateText)
        
        // Set up RecyclerView
        setupRecyclerView()
        
        // Load mood entries from Room Database
        loadMoodEntries()
        
        // Set up chart
        setupChart()
        
        // Set up button click listeners
        addMoodButton.setOnClickListener {
            showAddMoodDialog()
        }
        
        shareButton.setOnClickListener {
            shareMoodSummary()
        }
    }
    
    /**
     * Set up RecyclerView with adapter
     */
    private fun setupRecyclerView() {
        moodAdapter = MoodAdapter(
            moodEntries = moodEntries.toMutableList(),
            onItemClick = { entry -> showMoodDetails(entry) }
        )
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = moodAdapter
        }
    }
    
    /**
     * Load mood entries from Room Database
     */
    private fun loadMoodEntries() {
        lifecycleScope.launch {
            moodEntries.clear()
            moodEntries.addAll(prefsManager.moodEntryRepository.getAllMoodEntries())
            // Already sorted by timestamp descending in query
            moodAdapter.updateMoodEntries(moodEntries)
            updateEmptyState()
            updateChart()
        }
    }
    
    /**
     * Show/hide empty state message based on mood entries list
     */
    private fun updateEmptyState() {
        if (moodEntries.isEmpty()) {
            emptyStateText.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            moodChart.visibility = View.GONE
        } else {
            emptyStateText.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            moodChart.visibility = View.VISIBLE
        }
    }
    
    /**
     * Show dialog with emoji selector to add a new mood entry
     */
    private fun showAddMoodDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_mood, null)
        
        val noteInput = dialogView.findViewById<EditText>(R.id.moodNoteInput)
        val emojiContainer = dialogView.findViewById<ViewGroup>(R.id.emojiContainer)
        
        var selectedEmoji = "😐"  // Default to neutral
        
        // Create emoji buttons
        moodEmojis.forEach { emoji ->
            val emojiButton = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_emoji_button, emojiContainer, false) as TextView
            
            emojiButton.text = emoji
            emojiButton.setOnClickListener {
                selectedEmoji = emoji
                // Update selection appearance
                for (i in 0 until emojiContainer.childCount) {
                    val child = emojiContainer.getChildAt(i)
                    child.isSelected = child == emojiButton
                }
            }
            
            // Set default selection
            if (emoji == selectedEmoji) {
                emojiButton.isSelected = true
            }
            
            emojiContainer.addView(emojiButton)
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("How are you feeling?")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val note = noteInput.text.toString().trim()
                val currentTime = System.currentTimeMillis()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val date = Date(currentTime)
                
                val entry = MoodEntry(
                    emoji = selectedEmoji,
                    note = note,
                    timestamp = currentTime,
                    dateString = dateFormat.format(date),
                    timeString = timeFormat.format(date)
                )
                
                lifecycleScope.launch {
                    prefsManager.moodEntryRepository.insert(entry)
                    loadMoodEntries()
                    Toast.makeText(requireContext(), "Mood logged!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    /**
     * Show details of a mood entry
     */
    private fun showMoodDetails(entry: MoodEntry) {
        val message = buildString {
            append("Emoji: ${entry.emoji}\n")
            append("Date: ${entry.dateString}\n")
            append("Time: ${entry.timeString}\n")
            if (entry.note.isNotEmpty()) {
                append("\nNote: ${entry.note}")
            }
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Mood Entry")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
    
    /**
     * Set up mood trend chart
     */
    private fun setupChart() {
        moodChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            setDrawGridBackground(false)
            setPinchZoom(true)
            
            // X-axis settings
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
            }
            
            // Y-axis settings
            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = 6f
                setDrawGridLines(true)
            }
            
            axisRight.isEnabled = false
            legend.isEnabled = true
        }
    }
    
    /**
     * Update chart with weekly mood data
     */
    private fun updateChart() {
        if (moodEntries.isEmpty()) {
            return
        }
        
        // Get last 7 days of mood entries
        val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
        val recentEntries = moodEntries.filter { it.timestamp >= sevenDaysAgo }
            .sortedBy { it.timestamp }
        
        if (recentEntries.isEmpty()) {
            return
        }
        
        // Create chart entries
        val entries = recentEntries.mapIndexed { index, moodEntry ->
            Entry(index.toFloat(), moodEntry.getMoodValue())
        }
        
        // Create dataset
        val dataSet = LineDataSet(entries, "Mood Trend (Last 7 Days)").apply {
            color = requireContext().getColor(R.color.primary)
            valueTextColor = requireContext().getColor(android.R.color.black)
            lineWidth = 2f
            circleRadius = 4f
            setCircleColor(requireContext().getColor(R.color.primary))
            setDrawFilled(true)
            fillColor = requireContext().getColor(R.color.primary_light)
        }
        
        // Set data to chart
        moodChart.data = LineData(dataSet)
        
        // Custom x-axis formatter to show dates
        moodChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return if (index >= 0 && index < recentEntries.size) {
                    recentEntries[index].dateString.substring(5)  // Show MM-DD
                } else ""
            }
        }
        
        moodChart.invalidate()
    }
    
    /**
     * Share mood summary using implicit intent
     */
    private fun shareMoodSummary() {
        if (moodEntries.isEmpty()) {
            Toast.makeText(requireContext(), "No mood entries to share", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Calculate mood statistics
        val avgMood = moodEntries.map { it.getMoodValue() }.average()
        val moodText = when {
            avgMood >= 4.5f -> "very positive"
            avgMood >= 3.5f -> "positive"
            avgMood >= 2.5f -> "neutral"
            avgMood >= 1.5f -> "somewhat negative"
            else -> "negative"
        }
        
        val summary = buildString {
            append("My Wellness Mood Summary\n\n")
            append("Total entries: ${moodEntries.size}\n")
            append("Average mood: $moodText\n")
            append("Latest mood: ${moodEntries.first().emoji}\n")
        }
        
        // Create share intent
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "My Mood Journal Summary")
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        
        startActivity(Intent.createChooser(shareIntent, "Share mood summary via"))
    }
}
