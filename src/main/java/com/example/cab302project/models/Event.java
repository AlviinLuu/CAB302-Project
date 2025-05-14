package com.example.cab302project.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Event {
    private String name;
    private String start_time;
    private String end_time;
    private String user_email;

    public Event(String summary, String start_time, String end_time, String user_email) {
        this.name = summary;
        this.start_time = start_time;
        this.end_time = end_time;
        this.user_email = user_email;
    }

    private String parseIcsDate(String icsDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = inputFormat.parse(icsDate);
            SimpleDateFormat outputFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getName() {
        return name;
    }

    public String getStart_time() {
        return parseIcsDate(start_time); //Parse the start time to human-readable format
    }

    public String getEnd_time() {
        return parseIcsDate(end_time); //Parse the end time to human-readable format
    }

    public String getUser_email() {
        return user_email;
    }

    public String toFormattedString() {
        return String.format("User: %s | Event: %s | Start: %s | End: %s",
                getUser_email(), getName(), getStart_time(), getEnd_time());
    }

}