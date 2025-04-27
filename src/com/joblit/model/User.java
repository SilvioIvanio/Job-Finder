package com.joblit.model;

import javax.swing.JFrame;

/**
 * User.java
 * This is the main template for all users (JobSeekers and Employers).
 * It holds the shared details like ID, login info, and what type of user they are.
 * We keep details private and use get/set methods for safety (Encapsulation).
 * CN 10: This abstract class is a general idea (Abstraction).
 * CN 9: It's the starting point for JobSeeker and Employer classes (Inheritance).
 * CN 8: The setup matches the UML plan in the documents.
 */
public abstract class User {
    // Attributes - these are protected so child classes can see them,
    // but usually we use the get/set methods (Encapsulation).
    // CN 11: Keeping details protected/private like this is Encapsulation.
    protected int userId;
    protected String username;
    protected String password; // Keeping password simple (plain text) for this project.
    protected String email;
    protected UserType userType; // This uses the UserType Enum // CN 2: Using the UserType Enum (a Data Structure).

    // Constructor
    // Child classes use this to set up the common details.
    // CN 5: This constructor makes a new User.
    public User(int userId, String username, String password, String email, UserType userType) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.email = email;
        this.userType = userType;
    }

    // Getters and Setters - Needed for Encapsulation
    // CN 11: Public 'get' methods let us safely read the details hidden inside.
    // CN 7: These are 'Get' Methods.
    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    // Usually, we wouldn't let others get the password directly like this.
    // But it's simpler for this project's login check.
    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    // CN 7: These are 'Set' Methods.
    // CN 11: Public 'set' methods let us safely change the details hidden inside.
    public void setEmail(String email) {
        this.email = email;
    }

    public UserType getUserType() {
        return userType;
    }

    // Abstract Method
    // JobSeeker and Employer classes *must* have their own version of this method.
    // This is part of Polymorphism.
    // CN 10: An abstract method is a rule for child classes (Abstraction).
    // CN 12: This setup is needed for Polymorphism to work.
    // CN 7: Defining what the method looks like.
    public abstract void displayDashboard(JFrame parentFrame);

    // toString method - handy for checking things during development
    // CN 7: Method (changing how the basic toString works).
    @Override
    public String toString() {
        // Just showing some basic user info.
        return "User{" +
               "ID=" + userId +
               ", Name='" + username + '\'' +
               ", Type=" + userType +
               '}';
    }
} 