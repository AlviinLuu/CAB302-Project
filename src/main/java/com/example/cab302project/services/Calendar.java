package com.example.cab302project.services;

import com.example.cab302project.models.Event;
import com.example.cab302project.models.SqliteConnection;
import com.example.cab302project.models.SqliteUserDAO;
import com.example.cab302project.models.User;
import com.example.cab302project.util.Session;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

public class Calendar {
    private Connection connection;
    private SqliteUserDAO userDAO;
    private User currentUser;
    private String userEmail;

    public Calendar(){
        connection = SqliteConnection.getInstance();
        userDAO    = new SqliteUserDAO();
        currentUser = Session.getLoggedInUser();
        userEmail = currentUser.getEmail();
    }


    /**
     *
     * @param date LocalDate value of events to retrieve
     * @return list of Event objects
     */
    public List<Event> getEventsByDate(LocalDate date){
        return userDAO.getUserEventsByEmailAndDate(userEmail,date);
    }

    /**
     * Overloaded convenience function which takes string input of date
     * @param date string containing date in common date format
     * @return list of Event objects
     */
    public List<Event> getEventsByDate(String date){
        LocalDate dateV = LocalDate.parse(date);
        return userDAO.getUserEventsByEmailAndDate(userEmail,dateV);
    }

}
