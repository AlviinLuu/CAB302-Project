package com.example.cab302project.models;

public class User {
    private String username;
    private String password;
    private String email;
    private String bio;

    // Constructor
    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {this.email = email;}

    public void setBio(String bio) {this.bio = bio;}

    public String getBio() {return bio;}
}
