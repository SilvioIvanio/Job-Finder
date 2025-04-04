import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.awt.Component;

public class LoginPage extends JFrame {
    private JTabbedPane tabbedPane;
    private JPanel loginPanel;
    private JPanel signupPanel;

    //login main components
    private JTextField loginEmailField;
    private JPasswordField loginPasswordField;
    private JComboBox<String> loginUserTypeCombo;
    private JButton loginButton;

    // stuff we are going to use for the login
    private JTextField signupNameField;
    private JTextField signupEmailField;
    private JPasswordField signupPasswordField;
    private JPasswordField signupConfirmPasswordField;
    private JButton signupButton;

    public LoginPage() {
        setTitle("Job Finder - Log in/Sign up");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);

        // Initialize database
        DatabaseConnection.initializeDatabase();

        initializeComponents();
        setupLayout();
        addActionListeners();

        setVisible(true);
    }

    private void initializeComponents() {
        tabbedPane = new JTabbedPane();

        // Login page
        loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        loginPanel.add(new JLabel("User Type:"), gbc);
        gbc.gridx = 1;
        loginUserTypeCombo = new JComboBox<>(new String[]{"Job Seeker", "Employer"});
        loginPanel.add(loginUserTypeCombo, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        loginPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        loginEmailField = new JTextField(20);
        loginPanel.add(loginEmailField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        loginPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        loginPasswordField = new JPasswordField(20);
        loginPanel.add(loginPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        loginButton = new JButton("Login");
        loginPanel.add(loginButton, gbc);

        // Signup Page
        signupPanel = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        signupPanel.add(new JLabel("User Type:"), gbc);

        gbc.gridx = 1;
        JComboBox<String> signupUserTypeCombo = new JComboBox<>(new String[]{"Job Seeker", "Employer"});
        signupPanel.add(signupUserTypeCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        signupPanel.add(new JLabel("Full Name:"), gbc);

        gbc.gridx = 1;
        signupNameField = new JTextField(20);
        signupPanel.add(signupNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        signupPanel.add(new JLabel("Email:"), gbc);

        gbc.gridx = 1;
        signupEmailField = new JTextField(20);
        signupPanel.add(signupEmailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        signupPanel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        signupPasswordField = new JPasswordField(20);
        signupPanel.add(signupPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        signupPanel.add(new JLabel("Confirm Password:"), gbc);

        gbc.gridx = 1;
        signupConfirmPasswordField = new JPasswordField(20);
        signupPanel.add(signupConfirmPasswordField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        signupButton = new JButton("Sign Up");
        signupPanel.add(signupButton, gbc);
    }

    private void setupLayout() {
        tabbedPane.addTab("Log in", loginPanel);
        tabbedPane.addTab("Sign Up", signupPanel);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);
    }

    private void addActionListeners() {
        loginButton.addActionListener(_ -> {
            String email = loginEmailField.getText();
            String password = new String(loginPasswordField.getPassword());
            String userType = (String) loginUserTypeCombo.getSelectedItem();

            if (email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(LoginPage.this,
                        "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Here you would typically validate credentials against database
            boolean isValid = validateLogin(email, password);

            if (isValid) {
                JOptionPane.showMessageDialog(LoginPage.this,
                        "Login successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                // Proceed to main application
                openMainApplication(userType);
            } else {
                JOptionPane.showMessageDialog(LoginPage.this,
                        "Invalid credentials", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        signupButton.addActionListener(_ -> {
            String name = signupNameField.getText();
            String email = signupEmailField.getText();
            String password = new String(signupPasswordField.getPassword());
            String confirmPassword = new String(signupConfirmPasswordField.getPassword());

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(LoginPage.this,
                        "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(LoginPage.this,
                        "Passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Here you would typically save the new user to database
            boolean isRegistered = registerUser();

            if (isRegistered) {
                JOptionPane.showMessageDialog(LoginPage.this,
                        "Registration successful! Please log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
                tabbedPane.setSelectedIndex(0); // Switch to log in tab
            } else {
                JOptionPane.showMessageDialog(LoginPage.this,
                        "Registration failed. Email may already be in use.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private boolean validateLogin(String email, String password) {
        return !email.isEmpty() && password.length() >= 4;
    }

    private boolean registerUser() {
        String name = signupNameField.getText();
        String email = signupEmailField.getText();
        String password = new String(signupPasswordField.getPassword());
        
        // Find the user type combo box in the signup panel
        String userType = "Job Seeker"; // Default value
        for (Component comp : signupPanel.getComponents()) {
            if (comp instanceof JComboBox<?>) {
                @SuppressWarnings("unchecked")
                JComboBox<String> combo = (JComboBox<String>) comp;
                userType = (String) combo.getSelectedItem();
                break;
            }
        }

        String insertSQL = "INSERT INTO users (name, email, password, user_type) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, password); // In a production environment, this should be hashed
            pstmt.setString(4, userType);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getMessage().contains("Duplicate entry") && e.getMessage().contains("email")) {
                JOptionPane.showMessageDialog(this,
                    "This email is already registered.", "Registration Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Error during registration: " + e.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
            }
            return false;
        }
    }

    private void openMainApplication(String userType) {
        // Close login window
        this.dispose();

        // Open the appropriate dashboard based on user type
        if ("Job Seeker".equals(userType)) {
            System.out.println("Opening Job Seeker Dashboard..."); // Placeholder - implement actual dashboard opening
            // Example: new JobSeekerDashboard().setVisible(true);
        } else if ("Employer".equals(userType)) {
            System.out.println("Opening Employer Dashboard..."); // Placeholder - implement actual dashboard opening
            // Example: new EmployerDashboard().setVisible(true);
        }
    }

    public static void main(String[] args) {
        // Use method reference for Runnable
        SwingUtilities.invokeLater(LoginPage::new);
    }
}