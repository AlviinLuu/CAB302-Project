package com.example.cab302project.services;

import com.example.cab302project.models.Event;
import com.example.cab302project.models.SqliteUserDAO;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.DateTime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class CalendarImportView {

    public static void importCalendarFile(File selectedFile, int userId) {
        try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
            String line;
            String summary = null, dtStart = null, dtEnd = null;

            while ((line = reader.readLine()) != null) {
                // Look for the relevant fields
                if (line.startsWith("SUMMARY:")) {
                    summary = line.substring(8); // Extract the value after "SUMMARY:"
                }
                if (line.startsWith("DTSTART:")) {
                    dtStart = line.substring(8); // Extract the value after "DTSTART:"
                }
                if (line.startsWith("DTEND:")) {
                    dtEnd = line.substring(6); // Extract the value after "DTEND:"
                }
            }

            if (summary != null && dtStart != null && dtEnd != null) {
                // Once we have all the fields, create an Event object
                Event event = new Event(summary, dtStart, dtEnd);
                saveEventToDatabase(event, userId); // Save to the database
            } else {
                System.out.println("Error: Missing required fields in the .ics file");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveEventToDatabase(Event event, int userId) {
        SqliteUserDAO eventDAO = new SqliteUserDAO();
        eventDAO.insertEvent(userId, event.getSummary(), event.getStartTime(), event.getEndTime());
    }
}
