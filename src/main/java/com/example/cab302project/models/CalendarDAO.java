package com.example.cab302project.models;

import com.example.cab302project.util.Session;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Mostly a convenience class that automatically uses the username of the currently logged-in user for requests to the
 * SqliteUserDAO
 */
public class CalendarDAO {
    private final Connection connection;
    private final SqliteUserDAO userDAO;
    private final String userEmail;
    private List<Event> events;
    private final LocalDateTime startDateTime;
    private final LocalDateTime endDateTime;

    /**
     * Object which holds events which occur in the after a certain time period from the start time (Such as all events
     * for a week or all events for an hour).
     * <p>
     * This class is intended to be instantiated by a calendar view to hold values
     * to display in that view (for example, a week view would make a class to display all events in the currently selected
     * week, a day view all the events in the day and so on)
     * @param startDate     Date which is the first one shown by the view instantiating this class
     * @param timePeriod    Length of time of events to display on view
     * @param interval      Level of detail in time increment
     */
    public CalendarDAO(LocalDate startDate, Period timePeriod, TimeUnit interval) {

        connection = SqliteConnection.getInstance();
        //TODO: consider getting rid of this
        userDAO = new SqliteUserDAO();
        User currentUser = Session.getLoggedInUser();
        userEmail = currentUser.getEmail();

        this.startDateTime = startDate.atTime(1,0);
        endDateTime = startDateTime.plus(timePeriod);

        events = getAllUserEvents();
        events = filterToTimePeriod();
    }


    /**
     * Iterates list of events, looking for the first event during the date
     * @param dateTime The date to search for an event
     * @param interval The TimeUnit size of the time interval
     * @return         {@code Event} object containing data of first event occurrence, null otherwise
     */
    public Event getFirstEventForInterval(LocalDateTime dateTime, TimeUnit interval){
        for (final Event e: events){
            LocalDateTime eventStartDateTime = e.getStart_Time_LocalDateTime();
            switch (interval){
                case DAYS:
                    if (eventStartDateTime.toLocalDate() == dateTime.toLocalDate()){return e;}

                case HOURS:
                    if (dateTime.isEqual(eventStartDateTime)){return e;}

                default:
                    break;
            }
        }
        return null;
    }
    public Event getFirstEventForInterval(LocalDate date,int timeHour, TimeUnit interval){
        return getFirstEventForInterval(date.atTime(LocalTime.of((timeHour),0,0)), interval);
    }

    /**
     * Filters {@code event} class field list based on parameters set in constructor
     * @return a {@code List<Event>} of events between start and end dates
     */
    private List<Event> filterToTimePeriod(){
        List<Event> eventsFiltered = new ArrayList<>();
        for (final Event e : events){
            //Checks that event either starts or is during the time period, then does the same for the end time
            if (
                    (e.getStart_Time_LocalDateTime().isAfter(startDateTime) || e.getStart_Time_LocalDateTime().isEqual(startDateTime)) &&
                    (e.getEnd_Time_LocalDateTime().isBefore(endDateTime) || e.getEnd_Time_LocalDateTime().isEqual(endDateTime))
                )
            {
                eventsFiltered.add(e);
            }
        }
        return eventsFiltered;
    }


    /**
     * Combines a {@code LocalDate} and {@code LocalTime} to create a {@code LocalDateTime} value.
     * @param date  {@code LocalDate} value containing date
     * @param time  {@code LocalTime} value containing time
     * @return      {@code LocalDateTime} value containing date and time
     */
    public static LocalDateTime zipDateAndTime(LocalDate date, LocalTime time){
        return date.atTime(time);
    }


    /**
     * @return a {@code List<Event>} for the currently logged-in user
     */
    private List<Event> getAllUserEvents() {
        List<Event> events = new ArrayList<>();


        String query = "SELECT e.name, e.start_time, e.end_time, u.username " +
                "FROM events e JOIN users u ON e.user_email = u.email " +
                "WHERE e.user_email = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, userEmail);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String startTime = resultSet.getString("start_time");
                String endTime = resultSet.getString("end_time");
                String username = resultSet.getString("username");

                Event event = new Event(name, startTime, endTime, username);
                events.add(event);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving events by email and date: " + e.getMessage());
        }

        return events;
    }

    /**
     * @return {@code List<Event>} from database, regardless of the email
     */
    private List<Event> getAllEvents() {
        List<Event> events = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

        String query = "SELECT * FROM events";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String startTime = resultSet.getString("start_time");
                String endTime = resultSet.getString("end_time");

                Event event = new Event(name, startTime, endTime, "username");
                events.add(event);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all events: " + e.getMessage());
        }

        return events;
    }

    /**
     * Deletes <b>ALL</b> events in database. Here for debugging purposes and should not be used in normal code
     */
    private void ClearEvents() {
        userDAO.executeQuery("DELETE * from events");
    }
}