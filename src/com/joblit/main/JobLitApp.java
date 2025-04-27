package com.joblit.main;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane; // For error messages

import com.joblit.db.DatabaseManager;
import com.joblit.gui.LoginFrame;
import com.joblit.gui.SeekerDashboardFrame;
import com.joblit.gui.EmployerDashboardFrame;
import com.joblit.model.User;
import com.joblit.model.UserType;
import com.joblit.model.JobSeeker;
import com.joblit.model.Employer;

/**
 * JobLitApp.java
 * This is where the JobLit application starts running.
 * It sets up the database connection and shows the first login screen.
 * It also has static methods to help switch between different screens.
 * CN 8: The way the app is put together matches the UML plan.
 */
public class JobLitApp {

    // CN 11: Private static detail for the database manager (Encapsulation for the class).
    private static DatabaseManager dbManager;

    // Static getter method for the DatabaseManager
    // CN 7: Method (static 'get' method).
    // CN 11: Safe way to let other parts of the code get the dbManager.
    public static DatabaseManager getDbManager() {
        return dbManager;
    }

    /**
     * Main method - The application begins here.
     * Uses SwingUtilities.invokeLater to make sure the GUI starts safely.
     */
    // CN 7: Method (main - where the program starts).
    public static void main(String[] args) {
        // First, set up the Database Manager.
        // CN 6: Making the DatabaseManager object.
        dbManager = new DatabaseManager();

        // Check if we managed to connect to the database.
        // CN 1: Control Structure (if statement).
        if (dbManager.getConnection() == null) {
            // Show an error message using a pop-up box.
            // CN 3: Using a GUI part (JOptionPane) for messages.
            // CN 13: Handling errors (showing a message if database connection failed).
            JOptionPane.showMessageDialog(null,
                    "Serious Error: Cannot connect to the database. Please check the connection details and make sure the database server is running.",
                    "Database Connection Error",
                    JOptionPane.ERROR_MESSAGE);
            System.err.println("Stopping program because database connection failed.");
            return; // Stop the program if we can't connect.
        }

        // Tell Java Swing to create the GUI windows safely.
        // CN 3: The standard way to start Swing GUIs.
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // CN 7: Running a static method from this class.
                showLoginScreen();
            }
        });

        // Add a task to run when the program closes.
        // This makes sure the database connection is closed properly, even if the app stops unexpectedly.
        // CN 7: Running the addShutdownHook method.
        // CN 13: Makes the program more robust by cleaning up resources.
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                if (dbManager != null) { // CN 1: Control Structure (if statement).
                    System.out.println("Closing database connection...");
                    // CN 7: Running a method from the dbManager object.
                    dbManager.closeConnection();
                    System.out.println("Database connection closed.");
                }
            }
        }));
    }

    /**
     * Makes and shows the login screen.
     */
    // CN 7: Method (static helper method - can be called directly using JobLitApp.showLoginScreen()).
    public static void showLoginScreen() {
        // CN 6: Making the LoginFrame object.
        // CN 3: Making and showing a GUI window.
        LoginFrame loginFrame = new LoginFrame(dbManager);
        loginFrame.setVisible(true);
    }

    /**
     * Shows the correct dashboard (Seeker or Employer) after login.
     * Closes the previous window (e.g., the login window).
     *
     * @param user The user who just logged in. // CN 6: Takes a User object as input.
     * @param previousFrame The window we need to close. // CN 3: Takes a JFrame object as input.
     */
    // CN 7: Method (static helper method).
    public static void showDashboard(User user, JFrame previousFrame) {
        if (user == null) { // CN 1: Control Structure (if statement).
            System.err.println("Error: Cannot show dashboard. User is null.");
            return;
        }

        // Close the previous window (usually the login window).
        if (previousFrame != null) { // CN 1: Control Structure (if statement).
            // CN 3: Working with a GUI part (JFrame).
            previousFrame.dispose(); // Close the window.
        }

        // Show the right dashboard depending on the user type.
        // CN 1: Control Structure (if-else if-else statement).
        // CN 9: Checking the object type (related to Inheritance).
        // CN 12: Although we check the type here, the *call* to displayDashboard on the user object itself *is* polymorphic.
        if (user.getUserType() == UserType.SEEKER && user instanceof JobSeeker) {
            // CN 6: Making the SeekerDashboardFrame object.
            // CN 3: Making and showing a GUI window.
            SeekerDashboardFrame seekerDashboard = new SeekerDashboardFrame((JobSeeker) user);
            seekerDashboard.setVisible(true);
        } else if (user.getUserType() == UserType.EMPLOYER && user instanceof Employer) {
            // CN 6: Making the EmployerDashboardFrame object.
            // CN 3: Making and showing a GUI window.
            EmployerDashboardFrame employerDashboard = new EmployerDashboardFrame((Employer) user);
            employerDashboard.setVisible(true);
        } else {
            // This shouldn't happen if login/registration works properly.
            System.err.println("Error: Cannot recognise user type.");
            // Go back to login screen just in case.
            // CN 7: Running a static method.
            showLoginScreen();
        }
    }
}