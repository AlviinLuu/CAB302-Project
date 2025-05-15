package com.example.cab302project.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Event {
    private String name;
    private String start_time;
    private String end_time;
    private String username;

    public Event(String name, String start_time, String end_time, String username) {
        this.name = name;
        this.start_time = start_time;
        this.end_time = end_time;
        this.username = username;
    }

    private String parseIcsDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            Date date = inputFormat.parse(dateString);

            SimpleDateFormat outputFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");


            return outputFormat.format(date);

        }catch (ParseException e){
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
                Date date = inputFormat.parse(dateString);
                SimpleDateFormat outputFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                return outputFormat.format(date);

            }catch (ParseException f){
                System.err.println("Error parsing date: " + dateString);
                e.printStackTrace();
                return "Invalid Date"; // Return a default string or handle error as needed
            }


        }catch (Exception e) {
            System.err.println("Unexpected error parsing date: " + dateString);
            e.printStackTrace();
            return "Error";
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

    public String getUsername() {
        return username;
    }

    public String toFormattedString() {
        return String.format("User: %s | Event: %s | Start: %s | End: %s",
                getUsername(), getName(), getStart_time(), getEnd_time());
    }

}