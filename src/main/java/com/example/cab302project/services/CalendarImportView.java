package com.example.cab302project.services;

import java.io.File;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.mnode.ical4j.data.CalendarBuilder;
import org.mnode.ical4j.model.Calendar;
import org.mnode.ical4j.model.component.VEvent;
import com.example.cab302project.models.Event;  // Import the Event class

public class CalendarImportView {

    public static void show(File file) {
        try {
            FileReader reader = new FileReader(file);
            CalendarBuilder builder = new CalendarBuilder();
            Calendar calendar = builder.build(reader);

            // Loop through the calendar components
            for (Object obj : calendar.getComponents(VEvent.VEVENT)) {
                VEvent event = (VEvent) obj;

                // Retrieve event details
                String summary = event.getSummary().getValue();
                Date start = event.getStartDate().getDate();
                Date end = event.getEndDate().getDate();

                // Convert the event times to LocalDateTime for database compatibility
                LocalDateTime startDateTime = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                LocalDateTime endDateTime = end.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

                // Create a new Event object
                Event parsedEvent = new Event(summary, startDateTime, endDateTime);

                // Save to database
                saveEventToDatabase(parsedEvent);

                // Log the parsed event
                System.out.println("Parsed: " + parsedEvent.getTitle() + " from " + parsedEvent.getStartTime());
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Handle exceptions
        }
    }

    private static void saveEventToDatabase(Event event) {
        // Your database interaction logic goes here
    }
}
