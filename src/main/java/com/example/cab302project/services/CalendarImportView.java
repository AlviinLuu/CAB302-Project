package com.example.cab302project.services;

import com.example.cab302project.models.Event;
import com.example.cab302project.models.SqliteUserDAO;
import com.example.cab302project.models.User;
import com.example.cab302project.util.Session;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class CalendarImportView {

    public static void importCalendarFile(File selectedFile, int userId) {
        try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
            String line;
            String summary = null, dtStart = null, dtEnd = null;
            boolean insideEvent = false;

            // ✅ Get email from logged-in user
            User user = Session.getLoggedInUser();
            if (user == null) {
                System.out.println("❌ No user logged in.");
                return;
            }
            String userEmail = user.getEmail();

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.equals("BEGIN:VEVENT")) {
                    insideEvent = true;
                    summary = null;
                    dtStart = null;
                    dtEnd = null;
                } else if (line.startsWith("SUMMARY:") && insideEvent) {
                    summary = line.substring(8).trim();
                } else if (line.startsWith("DTSTART:") && insideEvent) {
                    dtStart = line.substring(8).trim();
                } else if (line.startsWith("DTEND:") && insideEvent) {
                    dtEnd = line.substring(6).trim();
                } else if (line.equals("END:VEVENT") && insideEvent) {
                    insideEvent = false;

                    if (summary != null && dtStart != null && dtEnd != null) {
                        Event event = new Event(summary, dtStart, dtEnd, userEmail); // ✅ now with email
                        saveEventToDatabase(event, userId);
                        System.out.println("✅ Event saved: " + summary);
                    } else {
                        System.out.println("⚠️ Skipped incomplete event block");
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveEventToDatabase(Event event, int userId) {
        SqliteUserDAO eventDAO = new SqliteUserDAO();
        eventDAO.insertEvent(userId, event.getUserEmail(), event.getSummary(), event.getStartTime(), event.getEndTime());
    }
}