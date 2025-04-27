package com.joblit.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import com.joblit.db.DatabaseManager;
import com.joblit.model.User;
import com.joblit.main.JobLitApp;
import com.joblit.gui.RegistrationFrame;
import com.joblit.model.JobSeeker;
import com.joblit.model.Employer;

/**
 * LoginFrame.java
 * Makes the first login window the user sees.
 * Uses Swing parts (buttons, text boxes) and layout tools.
 * Implements ActionListener to make the buttons work.
 * CN 3: Shows how we make the GUI using JFrame and Swing parts.
 * CN 14: Uses the ActionListener interface.
 */
public class LoginFrame extends JFrame implements ActionListener {

    // GUI Parts (declared here so actionPerformed can use them)
    // CN 3: GUI parts like text box, password box, button.
    // CN 11: Keeping GUI parts private is good practice (Encapsulation).
    private JTextField usernameField;
    private JPasswordField passwordField; // Special box for passwords.
    private JButton loginButton;
    private JButton registerButton;
    // CN 6: A place to hold the DatabaseManager object.
    private DatabaseManager dbManager;

    // Constructor
    // CN 5: Constructor for the Login window.
    // CN 6: An object is made when we use 'new LoginFrame(...)'.
    public LoginFrame(DatabaseManager dbManager) {
        this.dbManager = dbManager;

        // Setting up the window
        // CN 3: Setting up the main window (JFrame).
        setTitle("JobLit - Login");
        setSize(new Dimension(350, 200)); // Setting a fixed size.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close the whole app if this window closes.
        setLocationRelativeTo(null); // Open window in the centre of the screen.
        setLayout(new BorderLayout(10, 10)); // CN 3: Using BorderLayout to arrange main sections.

        // Input Panel (using GridLayout for rows and columns)
        // CN 6: Making a JPanel object (a panel for grouping things).
        // CN 3: Using a JPanel with GridLayout.
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5)); // 2 rows, 2 columns, with gaps.
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add some space around the edges.

        // CN 6: Making JLabel and JTextField objects (text label, text box).
        JLabel userLabel = new JLabel("Username:");
        usernameField = new JTextField();
        JLabel passLabel = new JLabel("Password:");
        passwordField = new JPasswordField();

        // Adding components to the grid panel
        // CN 3: Putting parts into a container (the JPanel).
        inputPanel.add(userLabel);
        inputPanel.add(usernameField);
        inputPanel.add(passLabel);
        inputPanel.add(passwordField);

        // Button Panel (using FlowLayout to put buttons in a row)
        // CN 6: Making JPanel and JButton objects (panel, button).
        // CN 3: Using a JPanel with FlowLayout.
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10)); // CN 3: FlowLayout centres the buttons.
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");

        // Add buttons to the button panel
        // CN 3: Putting parts into a container (the JPanel).
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        // Add panels to the main frame (using BorderLayout positions)
        // CN 3: Putting panels into the main window (JFrame).
        add(inputPanel, BorderLayout.CENTER); // Input fields in the middle.
        add(buttonPanel, BorderLayout.SOUTH); // Buttons at the bottom.

        // Add Action Listeners (make buttons do something when clicked)
        // CN 14: Connecting the ActionListener to the buttons.
        // CN 3: This is how the GUI reacts to clicks.
        loginButton.addActionListener(this);
        registerButton.addActionListener(this);

        // Making the window visible is done by whoever creates this LoginFrame.
        // setVisible(true);
        // pack(); // We used setSize instead for a fixed size.
    }

    /**
     * actionPerformed Method
     * This runs when the user clicks the Login or Register button.
     * Uses if/else to see which button was clicked.
     * CN 14: This is the code for the ActionListener interface method.
     * CN 7: Method (actionPerformed - runs on button click).
     * CN 3: This handles the main button clicks for the GUI.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // CN 1: Control Structure (if-else if statement).
        if (e.getSource() == loginButton) {
            // Handle the login button click.
            handleLogin(); // CN 7: Running another method in this class.
        } else if (e.getSource() == registerButton) {
            // Handle the register button click - open the registration window.
            handleRegister(); // CN 7: Running another method in this class.
        }
    }

    /**
     * Handles the login button click.
     * Gets the username/password, asks DatabaseManager to check them,
     * and shows the right dashboard or an error message.
     * CN 7: Method (private helper method - only used inside this class).
     */
    private void handleLogin() {
        String username = usernameField.getText().trim(); // CN 3: Reading text from the username text box.
        String password = new String(passwordField.getPassword()); // CN 3: Reading text from the password box.

        // CN 1: Control Structure (if statement).
        // CN 13: Simple check to make sure boxes aren't empty, gives feedback.
        if (username.isEmpty() || password.isEmpty()) {
            // CN 3: Using JOptionPane to show pop-up messages.
            JOptionPane.showMessageDialog(this,
                "Please enter your username and password.",
                "Login Error",
                JOptionPane.ERROR_MESSAGE);
            return; // Stop here if boxes are empty.
        }

        // CN 6: Using the DatabaseManager object we have.
        // CN 7: Running a method from the DatabaseManager object.
        User user = dbManager.authenticateUser(username, password);

        // CN 1: Control Structure (if-else statement) - did we find a user?
        if (user != null) {
            // CN 6: Got a User object back from the database check.
            String displayName;
            // CN 9: Check if it's a JobSeeker or Employer (related to Inheritance).
            // CN 1: Control Structure (if-else statement).
            if (user instanceof JobSeeker) {
                displayName = ((JobSeeker) user).getFullName(); // CN 11: Using a 'get' method.
            } else { // Assume it's an Employer if not a Seeker.
                displayName = ((Employer) user).getCompanyName(); // CN 11: Using a 'get' method.
            }

            // CN 3: Using JOptionPane for a success message.
            JOptionPane.showMessageDialog(this,
                "Login Successful! Welcome " + displayName,
                "Login Success",
                JOptionPane.INFORMATION_MESSAGE);

            // CN 12: Calling the displayDashboard method (Polymorphism).
            // The right dashboard (Seeker or Employer) will show because of Polymorphism.
            // CN 7: Running a method from the User object.
            user.displayDashboard(this); // Give it this login window so it can close it.
        } else {
            // CN 13: Showing an error message if login failed.
            // CN 3: Using JOptionPane.
            JOptionPane.showMessageDialog(this,
                "Sorry, incorrect username or password.",
                "Login Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Handles the register button click.
     * Opens the RegistrationFrame window.
     * CN 7: Method (private helper method).
     */
    private void handleRegister() {
        // Open the Registration window.
        // We need the RegistrationFrame class for this.
        // CN 6: Making a RegistrationFrame object.
        // CN 3: Making and showing another GUI window.
        RegistrationFrame registrationFrame = new RegistrationFrame(dbManager, this); // Give it the DB manager and this login window.
        registrationFrame.setVisible(true);
        // We could hide the login window now, but let's leave it open.
        // setVisible(false);
    }

    // This window doesn't need a main method; JobLitApp has it.
} 