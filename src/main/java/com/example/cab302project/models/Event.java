package com.example.cab302project.models;

import java.time.LocalDateTime;

public class Event {
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Constructor
    public Event(String title, LocalDateTime startTime, LocalDateTime endTime) {
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    // Optionally, you can add setters, toString, etc., as needed
}
