package com.joblit.model;

import javax.swing.*;
import com.joblit.gui.EmployerDashboardFrame;

/**
 * Employer.java
 * This class is for a user who posts jobs.
 * It gets shared details from the main User class (Inheritance).
 * It has its own version of the displayDashboard method (Polymorphism).
 * CN 9: Employer gets things from User (Inheritance).
 */
public class Employer extends User {

    // Special detail just for an Employer (Encapsulation)
    // CN 11: Private detail is hidden (Encapsulation).
    private String companyName;

    // Constructor - Uses the main User constructor first
    // CN 5: This constructor makes a new Employer.
    // CN 6: We make an object when we use 'new Employer(...)'.
    public Employer(int userId, String username, String password, String email, String companyName) {
        // CN 9: Calling the constructor from the User class.
        super(userId, username, password, email, UserType.EMPLOYER);
        this.companyName = companyName;
    }

    // Getter and Setter for the Employer's own detail (Encapsulation)
    // CN 11: Public 'get'/'set' methods give safe access.
    // CN 7: These are 'get' and 'set' Methods.
    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    // Also allowing email and password changes via setters from User
    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * displayDashboard (Polymorphism)
     * This is Employer's own version of the abstract method from User.
     * It makes and shows the special dashboard window for an Employer.
     */
    @Override // CN 12: Making our own version of the User's abstract method shows Polymorphism.
    // CN 7: Writing the code for the method.
    public void displayDashboard(JFrame parentFrame) {
        // Close the parent window (e.g., the login screen).
        if (parentFrame != null) { // CN 1: Control Structure (if statement).
            parentFrame.dispose();
        }

        // Make and show the employer's dashboard window.
        // We need an EmployerDashboardFrame class for this.
        // CN 6: Making an EmployerDashboardFrame object.
        // CN 3: Using a GUI window (which is a type of JFrame).
        EmployerDashboardFrame employerDashboard = new EmployerDashboardFrame(this);
        employerDashboard.setVisible(true);
    }

    // Changing toString to show something useful
    // CN 7: Method (changing the standard toString).
    @Override
    public String toString() {
        // Use the company name in lists if we have it, otherwise the username.
        String namePart = (companyName != null && !companyName.isEmpty()) ? companyName : username; // CN 1: Ternary operator (like a short if/else).
        return namePart + " (Employer)";
    }
} 