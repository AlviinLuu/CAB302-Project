package com.example.cab302project.services;

import java.io.File;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.DateTime;

import com.example.cab302project.models.Event;

public class CalendarImportView {

    public static void show(File file) {
        try (FileReader reader = new FileReader(file)) {
            CalendarBuilder builder = new CalendarBuilder();
            Calendar calendar = builder.build(reader);

            for (Object obj : calendar.getComponents(VEvent.VEVENT)) {
                VEvent event = (VEvent) obj;

                // Retrieve values
                String summary = event.getSummary() != null ? event.getSummary().getValue() : "Untitled Event";

                String rawStart = event.getStartDate()
                        .orElseThrow(() -> new RuntimeException("Missing start date"))
                        .getValue().toString();

                String rawEnd = event.getEndDate()
                        .orElseThrow(() -> new RuntimeException("Missing end date"))
                        .getValue().toString();

                // Parse into Date
                Date start = new DateTime(rawStart);
                Date end = new DateTime(rawEnd);

                // Convert to LocalDateTime
                LocalDateTime startDateTime = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                LocalDateTime endDateTime = end.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

                // Build and save event
                Event parsedEvent = new Event(summary, startDateTime, endDateTime);
                saveEventToDatabase(parsedEvent);

                System.out.println("Parsed: " + parsedEvent.getTitle() + " from " + parsedEvent.getStartTime());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveEventToDatabase(Event event) {
        // TODO: Implement saving to DB
    }
}
