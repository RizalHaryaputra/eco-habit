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
    private String lastCompletedDate;

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
        this.isRepeating = true;
        this.isRandom = false;
        this.lastCompletedDate = "";
    }

    // --- LOGIC: Check if completed today ---
    public boolean isCompletedForToday() {
        String today = getCurrentDateString();
        return lastCompletedDate != null && lastCompletedDate.equals(today);
    }

    public void markAsCompletedToday() {
        this.lastCompletedDate = getCurrentDateString();
    }

    // Helper to get today's date "YYYY-MM-DD"
    private String getCurrentDateString() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) + "-" +
                (calendar.get(Calendar.MONTH) + 1) + "-" +
                calendar.get(Calendar.DAY_OF_MONTH);
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

    // Helper to format time for display
    public String getFormattedTime() {
        if (isRandom) return "Waktu Acak";
        return String.format("%02d:%02d", hour, minute);
    }
}