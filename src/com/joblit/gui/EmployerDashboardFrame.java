package com.joblit.gui;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

import com.joblit.model.Employer;
import com.joblit.db.DatabaseManager;
import com.joblit.main.JobLitApp;
import com.joblit.model.Job;
import com.joblit.model.JobSeeker;

/**
 * EmployerDashboardFrame.java
 * The main window for users who post jobs (Employers).
 * Lets them post new jobs, look at the jobs they have posted,
 * and see who has applied for their jobs, including applicant details.
 * Uses Swing parts, layouts, ActionListener, and Lists/DefaultListModel.
 * Talks to the DatabaseManager.
 */
public class EmployerDashboardFrame extends JFrame implements ActionListener {

    private Employer currentEmployer; // The user who is logged in.
    private DatabaseManager dbManager;

    // GUI Parts
    // Panel for Posting Jobs
    private JTextField jobTitleField;
    private JTextArea jobDescriptionArea;
    private JTextField jobLocationField;
    private JTextField jobSalaryField; // Box for salary (numbers expected).
    private JButton postJobButton;

    // Panel for Looking at Own Jobs
    private JList<Job> postedJobsList; // Shows the list of jobs this employer posted.
    private DefaultListModel<Job> postedJobsListModel; // Holds the data for the list.
    private JButton editJobButton;
    private JButton deleteJobButton;

    // Panel for Looking at Applicants
    private JList<JobSeeker> applicantsList; // Shows people who applied for the selected job.
    private DefaultListModel<JobSeeker> applicantsListModel; // Holds data for the applicants list.
    private JLabel applicantsLabel; // Shows which job the applicants are for.
    private JTextArea applicantDetailsArea; // Shows details of the selected applicant.

    // Common Parts
    private JButton logoutButton;
    private JButton editProfileButton; // Button to edit employer profile.
    private JButton deleteProfileButton; // Button to delete employer profile.

    public EmployerDashboardFrame(Employer employer) {
        this.currentEmployer = employer;
        this.dbManager = JobLitApp.getDbManager(); // Get the shared database manager.

        setTitle("JobLit - Employer Dashboard (" + currentEmployer.getCompanyName() + ")");
        setSize(new Dimension(950, 700)); // Making this window quite large.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centre on screen.
        setLayout(new BorderLayout(10, 10));

        // --- Main Split Pane (divides window left/right) ---
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setDividerLocation(350); // Initial position of the divider.

        // --- Left Panel: Post New Job Form ---
        JPanel postJobPanel = new JPanel(new BorderLayout(5, 5));
        postJobPanel.setBorder(BorderFactory.createTitledBorder("Post New Job")); // Adds a title border.

        JPanel postJobFormPanel = new JPanel(new GridLayout(0, 2, 5, 5)); // Grid for labels and fields.
        postJobFormPanel.add(new JLabel("Job Title:"));
        jobTitleField = new JTextField();
        postJobFormPanel.add(jobTitleField);

        postJobFormPanel.add(new JLabel("Location:"));
        jobLocationField = new JTextField();
        postJobFormPanel.add(jobLocationField);

        postJobFormPanel.add(new JLabel("Salary (Optional):"));
        jobSalaryField = new JTextField();
        postJobFormPanel.add(jobSalaryField);

        // Adding description label across both columns using an empty label placeholder.
        postJobFormPanel.add(new JLabel("Description:"));
        postJobFormPanel.add(new JLabel()); // Empty label takes up space.

        jobDescriptionArea = new JTextArea(10, 20); // Suggest size (10 rows, 20 columns).
        jobDescriptionArea.setWrapStyleWord(true);
        jobDescriptionArea.setLineWrap(true);
        JScrollPane descScrollPane = new JScrollPane(jobDescriptionArea); // Make it scrollable.

        postJobButton = new JButton("Post Job");

        // Arrange the form parts in the postJobPanel.
        postJobPanel.add(postJobFormPanel, BorderLayout.NORTH);
        postJobPanel.add(descScrollPane, BorderLayout.CENTER);
        postJobPanel.add(postJobButton, BorderLayout.SOUTH);

        mainSplitPane.setLeftComponent(postJobPanel); // Put the post job form on the left.

        // --- Right Panel: Shows Posted Jobs and Applicants (split top/bottom) ---
        JPanel rightPanel = new JPanel(new BorderLayout(5,5));
        JSplitPane rightSplitPaneJobsAndApplicants = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        rightSplitPaneJobsAndApplicants.setResizeWeight(0.4); // Give top part 40% height initially.

        // Top Right: List of Posted Jobs
        JPanel postedJobsPanel = new JPanel(new BorderLayout());
        postedJobsPanel.setBorder(BorderFactory.createTitledBorder("My Posted Jobs"));
        postedJobsListModel = new DefaultListModel<>();
        postedJobsList = new JList<>(postedJobsListModel);
        postedJobsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane postedListScrollPane = new JScrollPane(postedJobsList); // Make list scrollable.

        // Panel for the Edit and Delete Job buttons.
        JPanel jobButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        editJobButton = new JButton("Edit Job");
        deleteJobButton = new JButton("Delete Job");
        editJobButton.setEnabled(false); // Start disabled.
        deleteJobButton.setEnabled(false); // Start disabled.
        jobButtonsPanel.add(editJobButton);
        jobButtonsPanel.add(deleteJobButton);

        postedJobsPanel.add(postedListScrollPane, BorderLayout.CENTER);
        postedJobsPanel.add(jobButtonsPanel, BorderLayout.SOUTH);
        rightSplitPaneJobsAndApplicants.setTopComponent(postedJobsPanel);

        // Bottom Right: Area to Show Applicants
        JPanel applicantsAreaPanel = new JPanel(new BorderLayout());
        applicantsAreaPanel.setBorder(BorderFactory.createTitledBorder("Applicants for Selected Job"));
        applicantsLabel = new JLabel("Select a job above to see applicants", SwingConstants.CENTER); // Initial message.
        applicantsAreaPanel.add(applicantsLabel, BorderLayout.NORTH);

        // Another split pane inside here: Applicant list above, details below.
        JSplitPane applicantsSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        applicantsSplitPane.setResizeWeight(0.5); // Split 50/50 height.

        // Applicant List (Top part of the inner split pane).
        applicantsListModel = new DefaultListModel<>();
        applicantsList = new JList<>(applicantsListModel);
        applicantsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Use a custom renderer if needed to show more than just default toString.
        // applicantsList.setCellRenderer(new ApplicantListCellRenderer()); // Example
        JScrollPane applicantsScrollPane = new JScrollPane(applicantsList);
        applicantsSplitPane.setTopComponent(applicantsScrollPane);

        // Applicant Details Area (Bottom part of the inner split pane).
        applicantDetailsArea = new JTextArea();
        applicantDetailsArea.setEditable(false);
        applicantDetailsArea.setWrapStyleWord(true);
        applicantDetailsArea.setLineWrap(true);
        JScrollPane applicantDetailsScrollPane = new JScrollPane(applicantDetailsArea);
        applicantsSplitPane.setBottomComponent(applicantDetailsScrollPane);

        // Put the inner split pane (list + details) into the applicants area panel.
        applicantsAreaPanel.add(applicantsSplitPane, BorderLayout.CENTER);

        // Put the whole applicants area panel at the bottom of the right side.
        rightSplitPaneJobsAndApplicants.setBottomComponent(applicantsAreaPanel);

        // Put the combined right side into the main split pane.
        rightPanel.add(rightSplitPaneJobsAndApplicants, BorderLayout.CENTER);
        mainSplitPane.setRightComponent(rightPanel);

        // --- Bottom Panel (for Edit/Delete Profile & Logout) ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        editProfileButton = new JButton("Edit My Profile");
        deleteProfileButton = new JButton("Delete My Profile");
        logoutButton = new JButton("Logout");

        leftButtons.add(editProfileButton);
        leftButtons.add(deleteProfileButton);
        rightButtons.add(logoutButton);

        bottomPanel.add(leftButtons, BorderLayout.WEST);
        bottomPanel.add(rightButtons, BorderLayout.EAST);

        // Add the main parts to the window frame.
        add(mainSplitPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Make Buttons and Lists react (Add Listeners)
        postJobButton.addActionListener(this);
        logoutButton.addActionListener(this);
        editProfileButton.addActionListener(this);
        deleteProfileButton.addActionListener(this);
        editJobButton.addActionListener(this);
        deleteJobButton.addActionListener(this);

        // Listener for the Posted Jobs list.
        postedJobsList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    Job selectedJob = postedJobsList.getSelectedValue();
                    // When a job is selected, load its applicants.
                    loadApplicants(selectedJob);
                    // Enable/disable job action buttons based on selection.
                    boolean jobSelected = (selectedJob != null);
                    editJobButton.setEnabled(jobSelected);
                    deleteJobButton.setEnabled(jobSelected);
                }
            }
        });

        // Listener for the Applicants list.
        applicantsList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    // Show details of the selected applicant.
                    JobSeeker selectedSeeker = applicantsList.getSelectedValue();
                    displayApplicantDetails(selectedSeeker);
                }
            }
        });

        // Load the employer's jobs when the window first opens.
        loadPostedJobs();
    }

    // Gets the jobs posted by this employer and shows them in the list.
    private void loadPostedJobs() {
        List<Job> jobs = dbManager.getJobsByEmployer(currentEmployer.getUserId());
        postedJobsListModel.clear(); // Clear old list.
        if (jobs != null) {
            for (Job job : jobs) { // CN 1: For-each loop.
                postedJobsListModel.addElement(job); // CN 2: Add to list model.
            }
        }
        // Clear applicant info as no job is selected initially.
        applicantsListModel.clear();
        applicantsLabel.setText("Select a job above to see applicants");
        displayApplicantDetails(null);
        editJobButton.setEnabled(false);
        deleteJobButton.setEnabled(false);
        postedJobsList.clearSelection();
    }

    // Loads the list of people who applied for the selected job.
    private void loadApplicants(Job selectedJob) {
        applicantsListModel.clear(); // Clear previous applicants.
        applicantDetailsArea.setText(""); // Clear details area.
        if (selectedJob == null) {
            applicantsLabel.setText("Select a job above to see applicants");
        } else {
            List<JobSeeker> applicants = dbManager.getApplicantsForJob(selectedJob.getJobId());
            applicantsLabel.setText("Applicants for: " + selectedJob.getTitle());
            if (applicants != null && !applicants.isEmpty()) {
                for (JobSeeker seeker : applicants) { // CN 1: For-each loop.
                    applicantsListModel.addElement(seeker); // CN 2: Add to list model.
                }
            } else {
                applicantsLabel.setText("No applicants yet for: " + selectedJob.getTitle());
            }
        }
    }

    // Shows the details of the selected applicant in the text area.
    private void displayApplicantDetails(JobSeeker seeker) {
        if (seeker == null) {
            applicantDetailsArea.setText(""); // Clear if no applicant selected.
            return;
        }
        // Build the text with applicant's info.
        StringBuilder details = new StringBuilder();
        details.append("Applicant Name: ").append(seeker.getFullName()).append("\n");
        details.append("Email: ").append(seeker.getEmail()).append("\n");
        details.append("\nSkills:\n").append(seeker.getSkills() != null ? seeker.getSkills() : "Not provided").append("\n");
        details.append("\nResume/CV Info:\n").append(seeker.getResumeInfo() != null ? seeker.getResumeInfo() : "Not provided");

        applicantDetailsArea.setText(details.toString());
        applicantDetailsArea.setCaretPosition(0); // Scroll to top.
    }

    // --- Methods to Handle Button Clicks ---

    // Called when the 'Post Job' button is clicked.
    private void handlePostJob() {
        String title = jobTitleField.getText().trim();
        String description = jobDescriptionArea.getText().trim();
        String location = jobLocationField.getText().trim();
        String salaryStr = jobSalaryField.getText().trim();
        double salary = 0.0;

        // Basic checks.
        if (title.isEmpty() || description.isEmpty() || location.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in Job Title, Description, and Location.", "Post Job Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Try to read the salary, ignore if not a valid number.
        if (!salaryStr.isEmpty()) {
            try {
                salary = Double.parseDouble(salaryStr);
                if (salary < 0) salary = 0; // Salary can't be negative.
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Salary must be a valid number (or leave blank).", "Post Job Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // Make a new Job object.
        Job newJob = new Job(currentEmployer.getUserId(), title, description, location, salary, currentEmployer.getCompanyName());

        // Try to save it.
        if (dbManager.saveJob(newJob)) {
            JOptionPane.showMessageDialog(this, "Job posted successfully!", "Job Posted", JOptionPane.INFORMATION_MESSAGE);
            // Clear the form fields.
            jobTitleField.setText("");
            jobDescriptionArea.setText("");
            jobLocationField.setText("");
            jobSalaryField.setText("");
            // Refresh the list of posted jobs.
            loadPostedJobs();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to post job. Please try again.", "Post Job Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Called when the 'Logout' button is clicked.
    private void handleLogout() {
        // Show login screen again.
        JobLitApp.showLoginScreen();
        // Close this dashboard window.
        dispose();
    }

    // Called when the 'Edit Job' button is clicked.
    private void handleEditJob() {
        Job selectedJob = postedJobsList.getSelectedValue();
        if (selectedJob == null) {
            JOptionPane.showMessageDialog(this, "Please select a job from 'My Posted Jobs' list to edit.", "Edit Job Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Create a dialog box for editing.
        JDialog editDialog = new JDialog(this, "Edit Job Posting", true); // Modal dialog.
        editDialog.setSize(400, 350);
        editDialog.setLocationRelativeTo(this);
        editDialog.setLayout(new BorderLayout(5, 5));

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField titleField = new JTextField(selectedJob.getTitle());
        JTextField locationField = new JTextField(selectedJob.getLocation());
        JTextField salaryField = new JTextField(selectedJob.getSalary() > 0 ? String.valueOf(selectedJob.getSalary()) : "");
        JTextArea descriptionArea = new JTextArea(selectedJob.getDescription(), 8, 20);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setLineWrap(true);
        JScrollPane descScrollPane = new JScrollPane(descriptionArea);

        formPanel.add(new JLabel("Job Title:"));
        formPanel.add(titleField);
        formPanel.add(new JLabel("Location:"));
        formPanel.add(locationField);
        formPanel.add(new JLabel("Salary (Optional):"));
        formPanel.add(salaryField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save Changes");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        editDialog.add(formPanel, BorderLayout.NORTH);
        editDialog.add(descScrollPane, BorderLayout.CENTER);
        editDialog.add(buttonPanel, BorderLayout.SOUTH);

        // Action for the Save button.
        saveButton.addActionListener(e -> {
            String newTitle = titleField.getText().trim();
            String newLocation = locationField.getText().trim();
            String newSalaryStr = salaryField.getText().trim();
            String newDescription = descriptionArea.getText().trim();
            double newSalary = 0.0;

            if (newTitle.isEmpty() || newLocation.isEmpty() || newDescription.isEmpty()) {
                JOptionPane.showMessageDialog(editDialog, "Title, Location, and Description cannot be empty.", "Edit Job Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!newSalaryStr.isEmpty()) {
                try {
                    newSalary = Double.parseDouble(newSalaryStr);
                    if (newSalary < 0) newSalary = 0;
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(editDialog, "Salary must be a valid number.", "Edit Job Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // Update the selected Job object directly.
            selectedJob.setTitle(newTitle);
            selectedJob.setLocation(newLocation);
            selectedJob.setSalary(newSalary);
            selectedJob.setDescription(newDescription);

            // Try to save the updated job to the database.
            if (dbManager.updateJob(selectedJob)) {
                JOptionPane.showMessageDialog(editDialog, "Job updated successfully.", "Update Success", JOptionPane.INFORMATION_MESSAGE);
                loadPostedJobs(); // Refresh the list.
                editDialog.dispose(); // Close the dialog.
            } else {
                JOptionPane.showMessageDialog(editDialog, "Failed to update job.", "Update Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Action for the Cancel button.
        cancelButton.addActionListener(e -> editDialog.dispose());

        editDialog.setVisible(true);
    }

    // Called when the 'Delete Job' button is clicked.
    private void handleDeleteJob() {
        Job selectedJob = postedJobsList.getSelectedValue();
        if (selectedJob == null) {
            JOptionPane.showMessageDialog(this, "Please select a job from 'My Posted Jobs' list to delete.", "Delete Job Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the job posting:\n'" + selectedJob.getTitle() + "'?\nThis will also remove all applications for this job and cannot be undone.",
                "Confirm Job Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            // We need to delete applications first, then the job (or handle via DB constraints).
            // Assuming DatabaseManager.deleteJob handles related apps or DB does cascade delete.
            boolean success = dbManager.deleteJob(selectedJob.getJobId());

            if (success) {
                JOptionPane.showMessageDialog(this, "Job posting deleted successfully.", "Deletion Success", JOptionPane.INFORMATION_MESSAGE);
                loadPostedJobs(); // Refresh the list.
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete job posting.", "Deletion Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Called when the 'Edit My Profile' button is clicked.
    private void handleEditProfile() {
        // Create a simple panel for the dialog.
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        JPanel labels = new JPanel(new GridLayout(0, 1, 2, 2));
        labels.add(new JLabel("Email:", SwingConstants.RIGHT));
        labels.add(new JLabel("Password:", SwingConstants.RIGHT));
        labels.add(new JLabel("Confirm Pwd:", SwingConstants.RIGHT));
        labels.add(new JLabel("Company Name:", SwingConstants.RIGHT));
        panel.add(labels, BorderLayout.WEST);

        JPanel controls = new JPanel(new GridLayout(0, 1, 2, 2));
        JTextField email = new JTextField(currentEmployer.getEmail());
        controls.add(email);
        JPasswordField password = new JPasswordField();
        controls.add(password);
        JPasswordField confirmPassword = new JPasswordField();
        controls.add(confirmPassword);
        JTextField companyName = new JTextField(currentEmployer.getCompanyName());
        controls.add(companyName);
        panel.add(controls, BorderLayout.CENTER);

        panel.add(new JLabel("Leave password fields blank to keep current password."), BorderLayout.SOUTH);

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Employer Profile", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String newEmail = email.getText().trim();
            String newPassword = new String(password.getPassword());
            String newConfirmPassword = new String(confirmPassword.getPassword());
            String newCompanyName = companyName.getText().trim();

            // Validate
            if (newEmail.isEmpty() || newCompanyName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Email and Company Name cannot be empty.", "Update Error", JOptionPane.ERROR_MESSAGE);
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
                 currentEmployer.setPassword(newPassword);
            }
            // Update the employer object.
            currentEmployer.setEmail(newEmail);
            currentEmployer.setCompanyName(newCompanyName);

            // Try to save changes.
            boolean success = dbManager.updateUser(currentEmployer);

            if (success) {
                JOptionPane.showMessageDialog(this, "Profile updated successfully.", "Update Success", JOptionPane.INFORMATION_MESSAGE);
                // Update dashboard title if company name changed.
                setTitle("JobLit - Employer Dashboard (" + currentEmployer.getCompanyName() + ")");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update profile.", "Update Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Called when the 'Delete My Profile' button is clicked.
    private void handleDeleteProfile() {
         int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to permanently delete your profile?\nThis will remove ALL your job postings and their applications, and cannot be undone.",
                "Confirm Profile Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            boolean success = dbManager.deleteUser(currentEmployer);
            if (success) {
                JOptionPane.showMessageDialog(this, "Profile deleted successfully.", "Deletion Success", JOptionPane.INFORMATION_MESSAGE);
                // Log out and close dashboard.
                handleLogout();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete profile.", "Deletion Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // --- Main ActionListener Implementation ---
    // Handles clicks for the main buttons.
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == postJobButton) {
            handlePostJob();
        } else if (source == logoutButton) {
            handleLogout();
        } else if (source == editProfileButton) {
            handleEditProfile();
        } else if (source == deleteProfileButton) {
            handleDeleteProfile();
        } else if (source == editJobButton) {
            handleEditJob();
        } else if (source == deleteJobButton) {
            handleDeleteJob();
        }
    }

    // Optional: Custom renderer for the applicant list if needed.
    // private class ApplicantListCellRenderer extends DefaultListCellRenderer {
    //     @Override
    //     public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    //         Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    //         if (value instanceof JobSeeker) {
    //             JobSeeker seeker = (JobSeeker) value;
    //             setText(seeker.getFullName() + " (" + seeker.getEmail() + ")"); // Example: Show name and email
    //         }
    //         return comp;
    //     }
    // }
} 