package com.joblit.gui;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
// import java.util.Vector; // Not needed anymore if using DefaultListModel

import com.joblit.model.JobSeeker;
import com.joblit.db.DatabaseManager;
import com.joblit.main.JobLitApp;
import com.joblit.model.Job;

/**
 * SeekerDashboardFrame.java
 * The main window for users who are looking for jobs.
 * Lets them look at jobs, search, see details, apply, change their profile,
 * and see or take back their applications.
 * Uses Swing parts, layouts, ActionListener, and Lists/DefaultListModel.
 * Talks to the DatabaseManager.
 */
public class SeekerDashboardFrame extends JFrame implements ActionListener {

    private JobSeeker currentSeeker; // The user who is logged in.
    private DatabaseManager dbManager;

    // GUI Parts - For Finding/Looking at Jobs
    private JList<Job> jobList; // Shows the list of jobs.
    private DefaultListModel<Job> jobListModel; // Holds the data for the job list.
    private JTextArea jobDetailsArea; // Shows details of the selected job.
    private JTextField searchField; // Box for typing search words.
    private JButton searchButton;   // Button to start the search.
    private JButton applyButton;
    private JButton viewAllButton; // Button to show all jobs again.

    // GUI Parts - For Profile
    private JTextArea resumeArea; // Box for CV/resume text.
    private JTextField skillsField; // Box for skills.
    private JButton editProfileButton; // Button to edit profile details.
    private JButton deleteProfileButton; // Button to delete profile.

    // GUI Parts - For My Applications
    private JList<Job> appliedJobsList; // Shows jobs the user applied for.
    private DefaultListModel<Job> appliedJobsListModel; // Holds data for the applied jobs list.
    private JButton withdrawButton; // Button to take back an application.

    // Common Parts
    private JButton logoutButton;

    public SeekerDashboardFrame(JobSeeker seeker) {
        this.currentSeeker = seeker;
        this.dbManager = JobLitApp.getDbManager(); // Get the shared database manager.

        setTitle("JobLit - Seeker Dashboard (" + currentSeeker.getUsername() + ")");
        setSize(new Dimension(850, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centre on screen.
        setLayout(new BorderLayout(10, 10));

        // --- Top Panel (for search controls) ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Align to the left.
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Padding.
        // Search Box and Button
        topPanel.add(new JLabel("Search (Title/Desc/Location):"));
        searchField = new JTextField(25); // Make the search box wider.
        topPanel.add(searchField);
        searchButton = new JButton("Search Jobs");
        topPanel.add(searchButton);
        // View All Button
        viewAllButton = new JButton("View All Jobs");
        topPanel.add(viewAllButton);

        // --- Main Content Panel (Left: Job List, Right: Tabs) ---
        jobListModel = new DefaultListModel<>(); // Prepare the model for the list.
        jobList = new JList<>(jobListModel); // Create the list using the model.
        jobList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Only allow selecting one job.
        JScrollPane listScrollPane = new JScrollPane(jobList); // Make the list scrollable.
        listScrollPane.setPreferredSize(new Dimension(300, 400)); // Suggest a size.

        // --- Right Side Panel (Using Tabs) ---
        JTabbedPane rightTabbedPane = new JTabbedPane();

        // Tab 1: Job Details
        JPanel detailsPanel = new JPanel(new BorderLayout(5,5));
        jobDetailsArea = new JTextArea();
        jobDetailsArea.setEditable(false); // User can't type here.
        jobDetailsArea.setWrapStyleWord(true); // Wrap long lines nicely.
        jobDetailsArea.setLineWrap(true);
        JScrollPane detailsScrollPane = new JScrollPane(jobDetailsArea); // Make details scrollable.
        applyButton = new JButton("Apply for Selected Job");
        applyButton.setEnabled(false); // Start disabled, enable when a job is picked.
        detailsPanel.add(detailsScrollPane, BorderLayout.CENTER);
        detailsPanel.add(applyButton, BorderLayout.SOUTH);
        rightTabbedPane.addTab("Job Details", detailsPanel);

        // Tab 2: My Profile/Resume
        JPanel profilePanel = new JPanel(new BorderLayout(5, 5));
        profilePanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10)); // Add space.
        // Show current skills and resume info if they exist.
        skillsField = new JTextField(currentSeeker.getSkills() != null ? currentSeeker.getSkills() : "");
        resumeArea = new JTextArea(currentSeeker.getResumeInfo() != null ? currentSeeker.getResumeInfo() : "");
        resumeArea.setWrapStyleWord(true);
        resumeArea.setLineWrap(true);
        JScrollPane resumeScrollPane = new JScrollPane(resumeArea);
        // Panel for the skills input field and its label.
        JPanel profileInputPanel = new JPanel(new BorderLayout(5,5));
        profileInputPanel.add(new JLabel("Skills (comma-separated):"), BorderLayout.NORTH);
        profileInputPanel.add(skillsField, BorderLayout.CENTER);
        profileInputPanel.add(new JLabel("Resume/CV Information:"), BorderLayout.SOUTH);
        profilePanel.add(profileInputPanel, BorderLayout.NORTH);
        profilePanel.add(resumeScrollPane, BorderLayout.CENTER);
        // Note: Save button is added later with its listener.
        rightTabbedPane.addTab("My Profile/CV", profilePanel);

        // Tab 3: My Applications
        JPanel applicationsPanel = new JPanel(new BorderLayout(5,5));
        applicationsPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10)); // Add space.
        appliedJobsListModel = new DefaultListModel<>();
        appliedJobsList = new JList<>(appliedJobsListModel);
        appliedJobsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane appliedListScrollPane = new JScrollPane(appliedJobsList);
        withdrawButton = new JButton("Withdraw Application");
        withdrawButton.setEnabled(false); // Start disabled.
        applicationsPanel.add(new JLabel("Jobs you have applied for:"), BorderLayout.NORTH);
        applicationsPanel.add(appliedListScrollPane, BorderLayout.CENTER);
        applicationsPanel.add(withdrawButton, BorderLayout.SOUTH);
        rightTabbedPane.addTab("My Applications", applicationsPanel);

        // --- Split Pane to divide Job List and Tabs ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScrollPane, rightTabbedPane);
        splitPane.setDividerLocation(310); // Initial position of the divider.

        // --- Bottom Panel (Edit/Delete Profile & Logout) ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Buttons on the left.
        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // Button on the right.

        editProfileButton = new JButton("Edit My Profile"); // More descriptive text.
        deleteProfileButton = new JButton("Delete My Profile");
        logoutButton = new JButton("Logout");

        leftButtons.add(editProfileButton);
        leftButtons.add(deleteProfileButton);
        rightButtons.add(logoutButton);

        bottomPanel.add(leftButtons, BorderLayout.WEST);
        bottomPanel.add(rightButtons, BorderLayout.EAST);

        // Add the main parts to the window.
        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // --- Make Buttons Work (Add Listeners) ---
        searchButton.addActionListener(this);
        viewAllButton.addActionListener(this);
        applyButton.addActionListener(this);
        editProfileButton.addActionListener(this);
        deleteProfileButton.addActionListener(this);
        withdrawButton.addActionListener(this);
        logoutButton.addActionListener(this);

        // Add Save Profile/Resume button and its listener separately.
        JButton saveProfileButton = new JButton("Save Profile/CV");
        // Using a lambda expression for this simple action.
        saveProfileButton.addActionListener(e -> handleSaveProfile());
        profilePanel.add(saveProfileButton, BorderLayout.SOUTH);

        // Listener for the main Job List selection.
        jobList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                // Only do something when the user finishes selecting.
                if (!e.getValueIsAdjusting()) {
                    Job selectedJob = jobList.getSelectedValue();
                    if (selectedJob != null) {
                        displayJobDetails(selectedJob); // Show details.
                        applyButton.setEnabled(true); // Allow applying.
                    } else {
                        jobDetailsArea.setText(""); // Clear details.
                        applyButton.setEnabled(false); // Don't allow applying.
                    }
                }
            }
        });

        // Listener for the Applied Jobs List selection.
        appliedJobsList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    // Allow withdrawing only if an applied job is selected.
                    withdrawButton.setEnabled(appliedJobsList.getSelectedValue() != null);
                }
            }
        });

        // Load initial data when the window opens.
        loadAllJobs();
        loadAppliedJobs();
    }

    // --- Methods to Load Data into Lists ---

    // Gets all jobs from database and shows them in the list.
    private void loadAllJobs() {
        List<Job> jobs = dbManager.getAllJobs(); // Get jobs from DB manager.
        jobListModel.clear(); // Clear the list first.
        if (jobs != null) {
            // Go through each job and add it to the list model.
            for (Job job : jobs) { // CN 1: For-each loop.
                jobListModel.addElement(job); // CN 2: Add to DefaultListModel.
            }
        }
        // Show a message if the list is empty.
        jobDetailsArea.setText(jobListModel.isEmpty() ? "No jobs available." : "Select a job to view details.");
        jobList.clearSelection(); // Make sure nothing is selected.
        applyButton.setEnabled(false); // Disable apply button.
    }

    // Searches jobs using the combined search term and updates the list.
    private void searchJobsCombined(String searchTerm) {
        List<Job> jobs = dbManager.searchJobsByKeywordOrLocation(searchTerm);
        updateJobList(jobs, "No jobs found matching: '" + searchTerm + "'."); // Use helper method.
    }

    // Helper method to update the job list display.
    private void updateJobList(List<Job> jobs, String messageIfEmpty) {
        jobListModel.clear(); // Clear old list items.
        if (jobs != null && !jobs.isEmpty()) {
            for (Job job : jobs) { // CN 1: For-each loop.
                jobListModel.addElement(job); // CN 2: Add new items.
            }
            jobDetailsArea.setText("Select a job to view details.");
        } else {
            jobDetailsArea.setText(messageIfEmpty); // Show message if no jobs found.
        }
        jobList.clearSelection();
        applyButton.setEnabled(false);
    }

    // Gets the jobs the current user applied for and shows them.
    private void loadAppliedJobs() {
        List<Job> jobs = dbManager.getAppliedJobs(currentSeeker.getUserId());
        appliedJobsListModel.clear();
        if (jobs != null) {
            for (Job job : jobs) { // CN 1: For-each loop.
                appliedJobsListModel.addElement(job); // CN 2: Add to DefaultListModel.
            }
        }
        appliedJobsList.clearSelection();
        withdrawButton.setEnabled(false); // Disable withdraw button.
    }

    // --- Method to Show Job Details ---

    // Puts the details of the selected job into the text area.
    private void displayJobDetails(Job job) {
        if (job == null) {
            jobDetailsArea.setText(""); // Clear if no job selected.
            return;
        }
        // Build the text to show.
        StringBuilder details = new StringBuilder();
        details.append("Job Title: ").append(job.getTitle()).append("\n");
        details.append("Company: ").append(job.getCompanyName()).append("\n");
        details.append("Location: ").append(job.getLocation()).append("\n");
        if (job.getSalary() > 0) {
            details.append("Salary: ").append(String.format("%.2f", job.getSalary())).append("\n");
        }
        details.append("Posted: ").append(job.getPostedAt() != null ? job.getPostedAt().toString().substring(0, 10) : "N/A").append("\n\n"); // Show date only.
        details.append("Description:\n");
        details.append(job.getDescription());

        jobDetailsArea.setText(details.toString());
        jobDetailsArea.setCaretPosition(0); // Scroll to the top.
    }

    // --- Methods to Handle Button Clicks ---

    // Called when the 'Apply' button is clicked.
    private void handleApply() {
        Job selectedJob = jobList.getSelectedValue();
        if (selectedJob == null) {
            JOptionPane.showMessageDialog(this, "Please select a job from the list first.", "Apply Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Ask for confirmation.
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to apply for:\n" + selectedJob.getTitle() + " at " + selectedJob.getCompanyName() + "?",
                "Confirm Application",
                JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            boolean success = dbManager.applyForJob(currentSeeker.getUserId(), selectedJob.getJobId());
            if (success) {
                JOptionPane.showMessageDialog(this, "Application submitted successfully!", "Application Success", JOptionPane.INFORMATION_MESSAGE);
                loadAppliedJobs(); // Refresh the 'My Applications' list.
            } else {
                JOptionPane.showMessageDialog(this, "Could not submit application. You might have already applied.", "Application Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Called when the 'Edit My Profile' button is clicked.
    private void handleEditProfile() {
        // Create a panel for the dialog box.
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        JPanel labels = new JPanel(new GridLayout(0, 1, 2, 2));
        labels.add(new JLabel("Email:", SwingConstants.RIGHT));
        labels.add(new JLabel("Password:", SwingConstants.RIGHT));
        labels.add(new JLabel("Confirm Pwd:", SwingConstants.RIGHT));
        labels.add(new JLabel("Full Name:", SwingConstants.RIGHT));
        labels.add(new JLabel("Skills:", SwingConstants.RIGHT));
        panel.add(labels, BorderLayout.WEST);

        JPanel controls = new JPanel(new GridLayout(0, 1, 2, 2));
        JTextField email = new JTextField(currentSeeker.getEmail());
        controls.add(email);
        JPasswordField password = new JPasswordField();
        controls.add(password);
        JPasswordField confirmPassword = new JPasswordField();
        controls.add(confirmPassword);
        JTextField fullName = new JTextField(currentSeeker.getFullName());
        controls.add(fullName);
        JTextField skills = new JTextField(currentSeeker.getSkills());
        controls.add(skills);
        panel.add(controls, BorderLayout.CENTER);

        // Add a note about leaving password blank.
        panel.add(new JLabel("Leave password fields blank to keep current password."), BorderLayout.SOUTH);

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Profile", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String newEmail = email.getText().trim();
            String newPassword = new String(password.getPassword());
            String newConfirmPassword = new String(confirmPassword.getPassword());
            String newFullName = fullName.getText().trim();
            String newSkills = skills.getText().trim();

            // Validate
            if (newEmail.isEmpty() || newFullName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Email and Full Name cannot be empty.", "Update Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!newEmail.contains("@") || !newEmail.contains(".")) {
                JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Update Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!newPassword.isEmpty()) {
                if (!newPassword.equals(newConfirmPassword)) {
                    JOptionPane.showMessageDialog(this, "Passwords do not match.", "Update Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                 // Only set password if a new one was entered.
                 currentSeeker.setPassword(newPassword);
            }
            // Update the seeker object's fields.
            currentSeeker.setEmail(newEmail);
            currentSeeker.setFullName(newFullName);
            currentSeeker.setSkills(newSkills);
            // Resume info is updated from the main tab.

            // Try to save changes to the database.
            boolean success = dbManager.updateUser(currentSeeker);

            if (success) {
                JOptionPane.showMessageDialog(this, "Profile updated successfully.", "Update Success", JOptionPane.INFORMATION_MESSAGE);
                // Update title bar if username changed? (Username not editable here)
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update profile.", "Update Failed", JOptionPane.ERROR_MESSAGE);
                // Maybe reload seeker data from DB if failed?
            }
        }
    }

    // Called when the 'Delete My Profile' button is clicked.
    private void handleDeleteProfile() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to permanently delete your profile?\nThis will remove your applications and cannot be undone.",
                "Confirm Profile Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            boolean success = dbManager.deleteUser(currentSeeker);
            if (success) {
                JOptionPane.showMessageDialog(this, "Profile deleted successfully.", "Deletion Success", JOptionPane.INFORMATION_MESSAGE);
                // Log out and close dashboard.
                handleLogout();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete profile.", "Deletion Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Called when the 'Logout' button is clicked.
    private void handleLogout() {
        // Show login screen again.
        JobLitApp.showLoginScreen();
        // Close this dashboard window.
        dispose();
    }

    // Called when the 'Withdraw Application' button is clicked.
    private void handleWithdrawApplication() {
        Job selectedAppliedJob = appliedJobsList.getSelectedValue();
        if (selectedAppliedJob == null) {
            JOptionPane.showMessageDialog(this,
                "Please select an application from the 'My Applications' list first.",
                "Withdraw Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to withdraw your application for:\n" + selectedAppliedJob.getTitle() + "?",
                "Confirm Withdrawal",
                JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            boolean success = dbManager.deleteApplication(currentSeeker.getUserId(), selectedAppliedJob.getJobId());
            if (success) {
                JOptionPane.showMessageDialog(this, "Application withdrawn successfully.", "Withdrawal Success", JOptionPane.INFORMATION_MESSAGE);
                loadAppliedJobs(); // Refresh the list.
            } else {
                JOptionPane.showMessageDialog(this, "Failed to withdraw application.", "Withdrawal Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Called when the 'Save Profile/Resume' button on the profile tab is clicked.
    private void handleSaveProfile() {
        String skills = skillsField.getText().trim();
        String resume = resumeArea.getText().trim();

        // Update the current seeker object first.
        currentSeeker.setSkills(skills);
        currentSeeker.setResumeInfo(resume);

        // Then, try to save only these fields to the database.
        // We could reuse updateUser, or have a more specific method.
        // Let's try using a specific method for just resume/skills.
        boolean success = dbManager.saveResumeInfo(currentSeeker.getUserId(), resume, skills);

        if (success) {
            JOptionPane.showMessageDialog(this,
                "Skills and Resume/CV information saved.",
                "Profile Saved",
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                "Could not save profile information.",
                "Save Error",
                JOptionPane.ERROR_MESSAGE);
            // Maybe reload fields from DB if save failed?
            skillsField.setText(currentSeeker.getSkills());
            resumeArea.setText(currentSeeker.getResumeInfo());
        }
    }

    // --- Main ActionListener Implementation ---
    // Handles clicks for buttons that weren't handled by specific listeners.
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == searchButton) {
            String searchTerm = searchField.getText().trim();
            if (!searchTerm.isEmpty()) {
                searchJobsCombined(searchTerm);
            } else {
                // Maybe show a message asking for search term?
                JOptionPane.showMessageDialog(this, "Please enter something to search for.", "Search", JOptionPane.INFORMATION_MESSAGE);
            }
        } else if (source == viewAllButton) {
            loadAllJobs();
            searchField.setText(""); // Clear search field when viewing all.
        } else if (source == applyButton) {
            handleApply();
        } else if (source == editProfileButton) {
            handleEditProfile();
        } else if (source == deleteProfileButton) {
            handleDeleteProfile();
        } else if (source == withdrawButton) {
            handleWithdrawApplication();
        } else if (source == logoutButton) {
            handleLogout();
        }
        // Save profile/resume button is handled by its own lambda listener.
    }
} 