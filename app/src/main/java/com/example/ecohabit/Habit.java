package com.example.ecohabit;

import java.util.Random;

public class Habit {
    private int id; // ID Unik untuk Alarm
    private String title;
    private String category;
    private String dateTime;
    private boolean isCompleted;

    public Habit(String title, String category, String dateTime) {
        // Generate ID acak saat dibuat
        this.id = new Random().nextInt(100000);
        this.title = title;
        this.category = category;
        this.dateTime = dateTime;
        this.isCompleted = false;
    }

    public int getId() { return id; } // Getter ID
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getDateTime() { return dateTime; }
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}