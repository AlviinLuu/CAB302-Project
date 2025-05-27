package com.example.cab302project.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

/**
 * Represents an event with a name, start and end times, and the associated user.
 * This class includes functionality to store time strings and parse them into
 * a standard human-readable format when requested.
 */
public class Event {
    /**
     * The name of the event.
     */
    private String name;
    /**
     * The start time of the event as a raw string.
     */
    private String start_time;
    /**
     * The end time of the event as a raw string.
     */
    private String end_time;
    /**
     * The username of the user associated with this event.
     */
    private String username;

    /**
     * Date format for use by parsers
     */
    final private DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");


    /**
     * Constructs a new Event object.
     * @param name The name of the event.
     * @param start_time The start time of the event as a string.
     * @param end_time The end time of the event as a string.
     * @param username The username associated with the event.
     */
    public Event(String name, String start_time, String end_time, String username) {
        this.name = name;
        this.start_time = start_time;
        this.end_time = end_time;
        this.username = username;
    }

    /**
     * Parse date string into a standardized "MM/dd/yyyy HH:mm:ss" string.
     * If the initial parsing ("MM/dd/yyyy HH:mm:ss") fails, it attempts to parse
     * using the "yyyyMMdd'T'HHmmss" format, and *adds 10 hours* to the result to account for timezones.
     * @param dateString The date string to parse.
     * @return A formatted date string ("MM/dd/yyyy HH:mm:ss"), or "Invalid Date" or "Error"
     * if parsing fails.
     */
    private String parseIcsDate(String dateString) {
        try {
            // Attempt to parse using the expected output format first
            SimpleDateFormat inputFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            Date date = inputFormat.parse(dateString);

            SimpleDateFormat outputFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

            return outputFormat.format(date);

        } catch (ParseException e) {
            // If initial parsing fails, try the ICS format and add 10 hours
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
                Date date = inputFormat.parse(dateString);
                // Note: Adding 10 hours might be specific to a timezone offset or data source issue.
                // This behavior is documented as per the code provided.
                date.setHours(date.getHours() + 10);

                SimpleDateFormat outputFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                return outputFormat.format(date);

            } catch (ParseException f) {
                // Handle the second parsing failure
                System.err.println("Error parsing date with ICS format: " + dateString);
                e.printStackTrace(); // Print stack trace for debugging
                return "Invalid Date"; // Indicate parsing failure
            }
        } catch (Exception e) {
            // Catch any other unexpected errors during parsing
            System.err.println("Unexpected error parsing date: " + dateString);
            e.printStackTrace(); // Print stack trace for debugging
            return "Error"; // Indicate unexpected error
        }
    }

    /**
     * Gets the name of the event.
     * @return The event name.
     */
    public String getName() {
        return name;
    }

    /**
     * Parses date, as a string, to <Code>LocalDateTime</Code>, using pattern defined in this class.
     * @param dateString    String containing date and time
     * @return              LocalDateTime value of input
     */
    private LocalDateTime parseToLocalDateTime(String dateString){
        return LocalDateTime.parse(dateString,dateFormat);
    }

    /**
     * Gets the start time of the event, parsed and formatted into "MM/dd/yyyy HH:mm:ss".
     * by using the parseICSdate method
     * @return The formatted start time string, or an error string if parsing fails.
     */
    public String getStart_time() {
        return parseIcsDate(start_time); // Parse the start time to readable format for ollama
    }

    /**
     * Gets the end time of the event, parsed and formatted into "MM/dd/yyyy HH:mm:ss".
     * Using parseICSdate method
     * @return The formatted end time string, or an error string if parsing fails.
     */
    public String getEnd_time() {
        return parseIcsDate(end_time); // Parse the end time to readable format for ollama
    }

    /**
     * Gets the start time of event, with LocalDateTime Type
     * @return Start Time of Event
     */
    public LocalDateTime getStart_Time_LocalDateTime(){
        return parseToLocalDateTime(start_time);
    }

    /**
     * Gets the end time of event, with LocalDateTime Type
     * @return End Time of Event
     */
    public LocalDateTime getEnd_Time_LocalDateTime(){
        return parseToLocalDateTime(end_time);
    }

    /**
     * Gets the username associated with the event.
     * @return The username.
     */
    public String getUsername() {
        return username;
    }

    /**
     *
     * @param currentTime Time to compare to event
     * @return            true if currentTime is between the startTime and endTime
     */
    public boolean IsEventInProgress(LocalDateTime currentTime){
        return  (currentTime.isAfter(getStart_Time_LocalDateTime())
                && currentTime.isBefore(getEnd_Time_LocalDateTime()) || currentTime.isEqual(getEnd_Time_LocalDateTime()))
                || currentTime.isEqual(getStart_Time_LocalDateTime());
    }
}