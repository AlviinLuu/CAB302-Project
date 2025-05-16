package com.example.cab302project.models;

import com.example.cab302project.util.Session;

import java.sql.Connection;
import java.sql.Time;
import java.time.LocalTime;
import java.time.LocalDate;
import java.util.List;

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
     * @param date LocalDate value of events to retrieve
     * @return list of Event objects
     */
    public List<Event> getEventsByDate(LocalDate date) {
        return userDAO.getUserEventsByEmailAndDate(userEmail, date);
    }

    /**
     *
     * @param date LocalDate field of date
     * @param time
     * @return List of Events
     */
    public List<Event> getEventsByDate(LocalDate date, LocalTime time) {
        return userDAO.getUserEventsByEmailAndDateAndTime(userEmail, date, time);
    }

    /**
     * Overloaded convenience function which takes string input of date
     *
     * @param date string containing date in common date format
     * @return list of Event objects
     */
    public List<Event> getEventsByDate(String date) {
        LocalDate dateV = LocalDate.parse(date);
        return userDAO.getUserEventsByEmailAndDate(userEmail, dateV);
    }

    /**
     * Clears database
     */
    public void ClearEvents() {
        userDAO.clearAllEvents();
    }

    /**
     * Returns all
     * @return
     */
    public List<Event> getAllEvents() {
        return userDAO.getAllEvents();
    }

}