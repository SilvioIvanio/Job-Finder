package com.joblit.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import com.joblit.db.DatabaseManager;
import com.joblit.model.User;
import com.joblit.model.UserType;
import com.joblit.model.JobSeeker;
import com.joblit.model.Employer;

/**
 * RegistrationFrame.java
 * Lets new users sign up as a Job Seeker or an Employer.
 * Uses Swing parts and ActionListener for the buttons.
 * Talks to the DatabaseManager to save the new user's details.
 */
public class RegistrationFrame extends JFrame implements ActionListener {

    // GUI Parts
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField emailField;
    private JRadioButton seekerRadioButton;
    private JRadioButton employerRadioButton;
    private ButtonGroup userTypeGroup; // Makes sure only one radio button is picked.
    private JLabel specificLabel1; // Label changes (Full Name or Company Name).
    private JTextField specificField1; // Box changes (Full Name or Company Name).
    private JLabel specificLabel2; // Label for Skills (only shows for Seeker).
    private JTextField specificField2; // Box for Skills (only shows for Seeker).
    private JButton registerButton;
    private JButton cancelButton;

    // Panel for the fields that change depending on user type.
    private JPanel specificFieldsPanel;

    private DatabaseManager dbManager;
    private JFrame loginFrame; // We keep the login frame so we can show it again.

    public RegistrationFrame(DatabaseManager dbManager, JFrame loginFrame) {
        this.dbManager = dbManager;
        this.loginFrame = loginFrame;

        setTitle("JobLit - Register New User");
        // Make this window a bit bigger than the login one.
        setSize(new Dimension(450, 350));
        // Just close this window when the X is clicked, don't stop the whole app.
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(loginFrame); // Open this window near the login window.
        setLayout(new BorderLayout(10, 10));

        // --- Setting up the Input Panel ---
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add space around edges.

        // Panel for general user info (using GridLayout).
        JPanel generalPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        generalPanel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        generalPanel.add(usernameField);

        generalPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        generalPanel.add(passwordField);

        generalPanel.add(new JLabel("Confirm Password:"));
        confirmPasswordField = new JPasswordField();
        generalPanel.add(confirmPasswordField);

        generalPanel.add(new JLabel("Email:"));
        emailField = new JTextField();
        generalPanel.add(emailField);

        // Panel for picking User Type (using FlowLayout).
        JPanel userTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userTypePanel.add(new JLabel("Register as:"));
        seekerRadioButton = new JRadioButton("Job Seeker", true); // Start with Seeker selected.
        employerRadioButton = new JRadioButton("Employer");
        userTypeGroup = new ButtonGroup(); // Group the radio buttons.
        userTypeGroup.add(seekerRadioButton);
        userTypeGroup.add(employerRadioButton);
        userTypePanel.add(seekerRadioButton);
        userTypePanel.add(employerRadioButton);
        // Make radio buttons change the fields below when clicked.
        seekerRadioButton.addActionListener(this);
        employerRadioButton.addActionListener(this);

        // Using BorderLayout for the main content area.
        JPanel contentPanel = new JPanel(new BorderLayout(5,5));
        // Put general info and user type choice together at the top.
        JPanel topInputPanel = new JPanel();
        topInputPanel.setLayout(new BoxLayout(topInputPanel, BoxLayout.Y_AXIS)); // Stack vertically.
        topInputPanel.add(generalPanel);
        topInputPanel.add(userTypePanel);

        contentPanel.add(topInputPanel, BorderLayout.NORTH);

        // Panel for fields that change based on user type.
        specificFieldsPanel = new JPanel(new GridLayout(0, 2, 5, 5)); // Auto rows, 2 columns.
        specificLabel1 = new JLabel();
        specificField1 = new JTextField();
        specificLabel2 = new JLabel();
        specificField2 = new JTextField();
        // Add them now, they will be updated by updateSpecificFields.
        specificFieldsPanel.add(specificLabel1);
        specificFieldsPanel.add(specificField1);
        specificFieldsPanel.add(specificLabel2);
        specificFieldsPanel.add(specificField2);

        contentPanel.add(specificFieldsPanel, BorderLayout.CENTER); // Put changing fields in the middle.

        // Set up the first view of the specific fields (Job Seeker is chosen first).
        updateSpecificFields();

        // --- Panel for Buttons --- (FlowLayout centres them)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        registerButton = new JButton("Register");
        cancelButton = new JButton("Cancel");
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);

        // Add the main panels to the window.
        add(contentPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Make the buttons do something.
        registerButton.addActionListener(this);
        cancelButton.addActionListener(this);

        // Add a listener to make the login window reappear when this one closes.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (loginFrame != null) {
                    loginFrame.setVisible(true);
                }
            }
        });

        // pack(); // Using setSize instead.
    }

    /**
     * Changes the labels and shows/hides the fields depending on which radio button is picked.
     */
    private void updateSpecificFields() {
        if (seekerRadioButton.isSelected()) {
            specificLabel1.setText("Full Name:");
            specificLabel2.setText("Skills (comma-sep):");
            specificLabel2.setVisible(true); // Show the skills parts.
            specificField2.setVisible(true);
        } else { // Employer must be selected.
            specificLabel1.setText("Company Name:");
            specificLabel2.setText(""); // Clear and hide the skills parts.
            specificLabel2.setVisible(false);
            specificField2.setVisible(false);
        }
        // Tell the panel to update its layout and repaint itself.
        specificFieldsPanel.revalidate();
        specificFieldsPanel.repaint();
        // pack(); // Could call pack() here to resize window if needed.
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource(); // Find out what was clicked.

        if (source == registerButton) {
            handleRegistration(); // Try to register the user.
        } else if (source == cancelButton) {
            dispose(); // Just close this registration window.
        } else if (source == seekerRadioButton || source == employerRadioButton) {
            updateSpecificFields(); // Change the specific fields shown.
        }
    }

    /**
     * Does the registration when the user clicks the register button.
     * Checks the input and asks DatabaseManager to save the user.
     */
    private void handleRegistration() {
        // Get the text from the common fields.
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String email = emailField.getText().trim();

        // Simple Input Checks (using if statements - Control Structures)
        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username, Password, and Email must be filled in.", "Registration Error", JOptionPane.ERROR_MESSAGE);
            return; // Stop if required fields are empty.
        }
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "The passwords don't match.", "Registration Error", JOptionPane.ERROR_MESSAGE);
            passwordField.setText(""); // Clear password fields.
            confirmPasswordField.setText("");
            return; // Stop if passwords differ.
        }
        // Very basic email check.
        if (!email.contains("@") || !email.contains(".")) { // CN 1: Using OR (||)
             JOptionPane.showMessageDialog(this, "Please enter a proper email address.", "Registration Error", JOptionPane.ERROR_MESSAGE);
             return; // Stop if email looks wrong.
        }

        User user = null; // Will hold the new user object.
        // CN 1: Ternary operator to decide type based on radio button.
        UserType selectedType = seekerRadioButton.isSelected() ? UserType.SEEKER : UserType.EMPLOYER;
        String specField1 = specificField1.getText().trim(); // Full Name or Company Name.

        // Make the right type of User object based on selection.
        if (selectedType == UserType.SEEKER) {
            String skills = specificField2.getText().trim();
            if (specField1.isEmpty()) { // Check if Full Name is entered.
                JOptionPane.showMessageDialog(this, "Please enter your Full Name.", "Registration Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // CN 6: Making a JobSeeker object.
            user = new JobSeeker(0, username, password, email, specField1, skills, ""); // Start with ID 0.
        } else { // Must be Employer.
            if (specField1.isEmpty()) { // Check if Company Name is entered.
                 JOptionPane.showMessageDialog(this, "Please enter your Company Name.", "Registration Error", JOptionPane.ERROR_MESSAGE);
                 return;
            }
            // CN 6: Making an Employer object.
            user = new Employer(0, username, password, email, specField1); // Start with ID 0.
        }

        // Try to save the new user to the database.
        // CN 7: Calling a method on the dbManager object.
        boolean success = dbManager.registerUser(user);

        // Show message based on whether it worked.
        // CN 13: Feedback on success or failure.
        if (success) {
            JOptionPane.showMessageDialog(this, "Registration Successful! You can log in now.", "Registration Success", JOptionPane.INFORMATION_MESSAGE);
            dispose(); // Close this registration window.
        } else {
            // Usually fails if username or email is already used.
            JOptionPane.showMessageDialog(this, "Registration Failed. That username or email might already be taken.", "Registration Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
} 