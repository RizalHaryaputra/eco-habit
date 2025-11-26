package com.example.ecohabit;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

public class Habit {
    private int id; // ID Unik untuk Alarm
    private String title;
    private String category;

    // Time Management
    private int hour;
    private int minute;
    private boolean isRandom; // If true, time is randomized daily
    
    // Logic Settings
    private boolean isActionRequired; // If true, notification shows "Done" button
    private boolean isRepeating;      // If true, alarm reschedules for tomorrow
    
    // State
    private boolean isActive; // Used to toggle off single-use reminders without deleting
    private boolean isCompletedForToday;

    public Habit(String title, String category, int hour, int minute, boolean isActionRequired) {
        // Generate ID acak saat dibuat
        this.id = new Random().nextInt(100000);
        this.title = title;
        this.category = category;
        this.hour = hour;
        this.minute = minute;
        this.isActionRequired = isActionRequired;
        
        // Defaults
        this.isActive = true;
        this.isRepeating = false; 
        this.isRandom = false;
        this.isCompletedForToday = false;
    }

    // Getters & Setters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; } // For Editing
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getHour() { return hour; }
    public void setHour(int hour) { this.hour = hour; }
    
    public int getMinute() { return minute; }
    public void setMinute(int minute) { this.minute = minute; }

    public boolean isRandom() { return isRandom; }
    public void setRandom(boolean random) { isRandom = random; }

    public boolean isActionRequired() { return isActionRequired; }
    public void setActionRequired(boolean actionRequired) { isActionRequired = actionRequired; }

    public boolean isRepeating() { return isRepeating; }
    public void setRepeating(boolean repeating) { isRepeating = repeating; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public boolean isCompletedForToday() { return isCompletedForToday; }
    public void setCompletedForToday(boolean completed) { isCompletedForToday = completed; }
    
    // Helper to format time for display
    public String getFormattedTime() {
        if (isRandom) return "Waktu Acak";
        return String.format("%02d:%02d", hour, minute);
    }
}