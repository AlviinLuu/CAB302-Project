package com.example.cab302project.models;

public class Event {
    private String summary;
    private String startTime;
    private String endTime;

    public Event(String summary, String startTime, String endTime) {
        this.summary = summary;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getSummary() {
        return summary;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }
}