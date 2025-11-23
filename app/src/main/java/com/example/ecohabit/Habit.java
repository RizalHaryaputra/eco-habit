package com.example.ecohabit;

public class Habit {
    private String title;
    private String category;
    private String dateTime; // Gabungan Tanggal & Jam
    private boolean isCompleted;

    public Habit(String title, String category, String dateTime) {
        this.title = title;
        this.category = category;
        this.dateTime = dateTime;
        this.isCompleted = false;
    }

    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getDateTime() { return dateTime; } // Getter baru
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}