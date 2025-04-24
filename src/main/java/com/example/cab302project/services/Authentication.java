package com.example.cab302project.services;

import com.example.cab302project.models.User;
import java.util.HashMap;

public class Authentication {
    private HashMap<String, User> users = new HashMap<>();

    public void register(User user) {
        users.put(user.getUsername(), user);
    }

    public boolean login(String username, String password) {
        User user = users.get(username);
        return user != null && user.getPassword().equals(password);
    }
}
