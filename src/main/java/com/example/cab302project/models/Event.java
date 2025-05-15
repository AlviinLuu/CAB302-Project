package com.example.cab302project.models;

import java.time.LocalDateTime; // Import LocalDateTime
import java.time.format.DateTimeFormatter; // Import DateTimeFormatter
import java.time.format.DateTimeParseException; // Import DateTimeParseException


public class Event {
    private String name; // Using 'name' for event name
    private String start_time;
    private String end_time;
    private String username;

    private static final DateTimeFormatter DATABASE_DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");



    public Event(String name, String start_time, String end_time, String username) {
        this.name = name;
        this.start_time = start_time;
        this.end_time = end_time;
        this.username = username;
    }

    private LocalDateTime parseDatabaseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateString, DATABASE_DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            System.err.println("Error parsing database date string: '" + dateString + "' - Expected format: MM/dd/yyyy HH:mm:ss - " + e.getMessage());
            // Return null to indicate parsing failed
            return null;
        } catch (Exception e) {
            System.err.println("Unexpected error parsing database date string: '" + dateString + "' - " + e.getMessage());
            return null;
        }
    }

    public String getName() {
        return name;
    }


    public String getStart_time() {
        if (start_time != null && start_time.matches("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}:\\d{2}")) {
            return start_time;
        }

        LocalDateTime dateTime = parseDatabaseDate(start_time);
        if (dateTime != null) {
            return dateTime.format(DATABASE_DATE_FORMATTER);
        }
        System.err.println("Stored start_time is not in expected format and could not be parsed: " + start_time);
        return "Invalid Date";
    }

    public String getEnd_time() {
        if (end_time != null && end_time.matches("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}:\\d{2}")) {
            return end_time;
        }

        LocalDateTime dateTime = parseDatabaseDate(end_time);
        if (dateTime != null) {
            return dateTime.format(DATABASE_DATE_FORMATTER);
        }
        System.err.println("Stored end_time is not in expected format and could not be parsed: " + end_time);
        return "Invalid Date";
    }

    public String getUsername() {
        return username;
    }
}
