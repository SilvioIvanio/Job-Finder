package com.joblit.model;

import javax.swing.*;
import com.joblit.gui.SeekerDashboardFrame;

/**
 * JobSeeker.java
 * This class is for a user looking for a job.
 * It gets shared details from the main User class (Inheritance).
 * It has its own version of the displayDashboard method (Polymorphism).
 * CN 9: JobSeeker gets things from User (Inheritance).
 */
public class JobSeeker extends User {

    // Special details just for a Job Seeker (Encapsulation)
    // CN 11: Private details are hidden (Encapsulation).
    private String fullName;
    private String skills;
    private String resumeInfo; // Using a simple text area for CV details.

    // Constructor - Uses the main User constructor first
    // CN 5: This constructor makes a new JobSeeker.
    // CN 6: We make an object when we use 'new JobSeeker(...)'.
    public JobSeeker(int userId, String username, String password, String email, String fullName, String skills, String resumeInfo) {
        // CN 9: Calling the constructor from the User class.
        super(userId, username, password, email, UserType.SEEKER);
        this.fullName = fullName;
        this.skills = skills; // Keeping skills simple for now.
        this.resumeInfo = resumeInfo;
    }

    // Getters and Setters for the JobSeeker's own details (Encapsulation)
    // CN 11: Public 'get'/'set' methods give safe access.
    // CN 7: These are 'get' and 'set' Methods.
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getResumeInfo() {
        return resumeInfo;
    }

    // Also allowing email and password changes via setters from User
    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setResumeInfo(String resumeInfo) {
        this.resumeInfo = resumeInfo;
    }

    /**
     * displayDashboard (Polymorphism)
     * This is JobSeeker's own version of the abstract method from User.
     * It makes and shows the special dashboard window for a Job Seeker.
     * It needs the parent window (like the login screen) so it can close it.
     */
    @Override // CN 12: Making our own version of the User's abstract method shows Polymorphism.
    // CN 7: Writing the code for the method.
    public void displayDashboard(JFrame parentFrame) {
        // Close the parent window (probably the login screen).
        if (parentFrame != null) { // CN 1: Control Structure (if statement).
            parentFrame.dispose(); // Close the old window.
        }
        // Now, make and show the seeker's dashboard window.
        // We need a SeekerDashboardFrame class to do this.
        // Just checking the method works for now.
        // System.out.println("Displaying Job Seeker Dashboard for: " + username);
        // CN 6: Making a SeekerDashboardFrame object.
        // CN 3: Using a GUI window (which is a type of JFrame).
        SeekerDashboardFrame seekerDashboard = new SeekerDashboardFrame(this);
        seekerDashboard.setVisible(true);

        // Note: We give 'this' (the JobSeeker object itself) to the dashboard
        // so the dashboard can see the seeker's details and use its methods.
    }

    // Changing toString to show something useful
    // CN 7: Method (changing the standard toString).
    @Override
    public String toString() {
        // Use the full name in lists if we have it, otherwise the username.
        String namePart = (fullName != null && !fullName.isEmpty()) ? fullName : username; // CN 1: Ternary operator (like a short if/else).
        return namePart + " (Seeker)";
    }
} 