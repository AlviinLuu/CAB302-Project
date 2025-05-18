package com.example.cab302project.models;

import com.example.cab302project.util.Session;

import java.sql.Connection;
import java.sql.Time;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.LocalDate;
import java.util.List;

/**
 * Mostly a convenience class that automatically uses the username of the currently logged-in user for requests to the
 * SqliteUserDAO
 */
public class CalendarDAO {
    private Connection connection;
    private SqliteUserDAO userDAO;
    private User currentUser;
    private String userEmail;

    public CalendarDAO() {
        connection = SqliteConnection.getInstance();
        userDAO = new SqliteUserDAO();
        currentUser = Session.getLoggedInUser();
        userEmail = currentUser.getEmail();
    }

    /**
     * Checks if given time is between start and end time
     * @param startTime Start time of event
     * @param endTime End time of event
     * @param currentTime time to compare to range
     * @return true if given time is between startTime and endTime
     */
    public static boolean IsEventInProgress(LocalDateTime startTime, LocalDateTime endTime, LocalDateTime currentTime){
        return (currentTime.isAfter(startTime)||currentTime.isEqual(startTime))
                && currentTime.isBefore(endTime);
    }

    /**
     * Overload of function which takes seperate date and time as input
     * @param startDate
     * @param startTime
     * @param endDate
     * @param endTime
     * @param currentDate
     * @param currentTime
     * @return
     */
    public static boolean IsEventInProgress(LocalDate startDate, LocalTime startTime, LocalDate endDate, LocalTime endTime, LocalDate currentDate, LocalTime currentTime){
        return IsEventInProgress(zipDateAndTime(startDate,startTime),zipDateAndTime(endDate, endTime),zipDateAndTime(currentDate,currentTime));
    }

    public static boolean IsEventInProgress(LocalDateTime start, LocalDateTime end, LocalDate currentDate, LocalTime currentTime){
        return IsEventInProgress(start,end,zipDateAndTime(currentDate,currentTime));
    }

    /**
     * Combines a LocalDate and LocalTime to create a LocalDateTime value;
     * @param date
     * @param time
     * @return
     */
    public static LocalDateTime zipDateAndTime(LocalDate date, LocalTime time){
        return date.atTime(time);
    }


    /**
     * Returns events from the currently logged-in user that are on date day
     * @param date  LocalDate value of events to retrieve
     * @return      list of Event objects
     */
    public List<Event> getEventsByDate(LocalDate date) {
        return userDAO.getUserEventsByEmailAndDate(userEmail, date);
    }

    /**
     * Returns events from the currently logged-in user that are on 'date' day and at 'time' time
     * @param date  LocalDate field of date
     * @param time  LocalTime of event start time
     * @return      List of Event objects that start at the given date and time
     */
    public List<Event> getEventsByDate(LocalDate date, LocalTime time) {
        return userDAO.getUserEventsByEmailAndDateAndTime(userEmail, date, time);
    }

    /**
     * Overloaded convenience function which takes string input of date
     * @param date  string containing date in common date format
     * @return      list of Event objects
     */
    public List<Event> getEventsByDate(String date) {
        LocalDate dateV = LocalDate.parse(date);
        return userDAO.getUserEventsByEmailAndDate(userEmail, dateV);
    }

    /**
     * Deletes ALL events in database
     */
    public void ClearEvents() {
        userDAO.executeQuery("DELETE * from events");
    }

    /**
     * Returns all events from database
     * @return      List of Event objects
     */
    public List<Event> getAllEvents() {
        return userDAO.executeEventQuery(userEmail,"SELECT * FROM events");
    }



}