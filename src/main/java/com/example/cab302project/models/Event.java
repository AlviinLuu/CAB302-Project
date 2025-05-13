package com.example.cab302project.models;

public class Event {
    private String summary;
    private String startTime;
    private String endTime;
    private String userEmail;

    public Event(String summary, String startTime, String endTime, String userEmail) {
        this.summary = summary;
        this.startTime = startTime;
        this.endTime = endTime;
        this.userEmail = userEmail;
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

    public String getUserEmail() {
        return userEmail;
    }
}
