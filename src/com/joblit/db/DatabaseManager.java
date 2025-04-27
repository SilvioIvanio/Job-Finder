package com.joblit.db; // Updated package

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Need to import model classes now
import com.joblit.model.User;
import com.joblit.model.UserType;
import com.joblit.model.JobSeeker;
import com.joblit.model.Employer;
import com.joblit.model.Job;

/**
 * DatabaseManager.java
 * This class handles all the talking to the MySQL database using JDBC.
 * It uses PreparedStatement to make SQL queries safer.
 * It includes basic ways to handle SQLException errors.
 * CN 4: Shows how Java connects to the database (JDBC).
 * CN 8: How this class links with others fits the UML model (like getting Jobs for an Employer).
 */
public class DatabaseManager {

    // Database connection details
    // It's best to put these in a separate config file, but we'll keep it simple here.
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/joblit_db?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Ivanio29";

    // The object that holds the database connection.
    // CN 4: JDBC Connection object.
    private Connection connection;

    // Constructor: Makes the connection to the database when this object is created.
    // CN 5: Constructor.
    // CN 6: An object is made when we use 'new DatabaseManager()'.
    public DatabaseManager() {
        try { // CN 13: Starting try-catch for handling errors.
            // 2. Load and Register the Driver - Not really needed with new drivers.
            // Class.forName("com.mysql.jdbc.Driver"); // Old way, removed.

            // 3. Establish the Connection
            // CN 4: Using JDBC's DriverManager tool to connect.
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            // If the code reaches here, the connection worked.
        } catch (SQLException e) { // CN 13: Catching database errors (SQLException).
            // Handle database connection errors.
            System.err.println("Database connection failed!");
            e.printStackTrace();
            connection = null; // Make sure connection is null if it failed.
        }
    }

    // Method to get the connection object (might be useful elsewhere).
    // CN 7: Method ('get').
    public Connection getConnection() {
        return connection;
    }

    /**
     * Checks a user's login details (username and password).
     * Gives back the User object if they are correct, or null if not.
     * Shows how to use SELECT with PreparedStatement and read results.
     */
    // CN 7: Method (to check user login).
    public User authenticateUser(String username, String password) {
        if (connection == null) { // CN 1: Control Structure (if statement).
            System.err.println("Database not connected. Cannot check login.");
            return null;
        }

        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        User user = null; // CN 6: Will hold the found user object later.
        PreparedStatement pstmt = null; // CN 4: JDBC PreparedStatement (for safe SQL queries).
        ResultSet rs = null; // CN 4: JDBC ResultSet (holds results from database).

        try { // CN 13: Handling errors during database work.
            // 4. Prepare the SQL Statement
            pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password); // Comparing simple text password.

            // 5. Run the Query
            rs = pstmt.executeQuery();

            // 6. Look at the Results
            if (rs.next()) { // CN 1: Control Structure (if statement) - did we find a user?
                int userId = rs.getInt("user_id");
                String email = rs.getString("email");
                // CN 2: Using the UserType Enum (Data Structure).
                UserType userType = UserType.valueOf(rs.getString("user_type"));

                if (userType == UserType.SEEKER) { // CN 1: Control Structure (if-else statement).
                    String fullName = rs.getString("full_name");
                    String skills = rs.getString("skills");
                    String resumeInfo = rs.getString("resume_info");
                    // CN 6: Making a JobSeeker object.
                    // CN 9: Related to Inheritance (making a child class object).
                    user = new JobSeeker(userId, username, password, email, fullName, skills, resumeInfo);
                } else { // It must be an EMPLOYER
                    String companyName = rs.getString("company_name");
                    // CN 6: Making an Employer object.
                    // CN 9: Related to Inheritance (making a child class object).
                    user = new Employer(userId, username, password, email, companyName);
                }
            }
        } catch (SQLException e) { // CN 13: Catching database errors (SQLException).
            System.err.println("Problem checking login: " + e.getMessage());
            e.printStackTrace();
        } finally { // CN 13: 'finally' block ensures we always close things.
            // 7. Close the database things
            closeResources(rs, pstmt); // Use our helper method to close.
        }
        return user; // Send back the user found, or null.
    }

    /**
     * Adds a new user (JobSeeker or Employer) to the database.
     * Returns true if it worked, false if not.
     * Shows how to use INSERT with PreparedStatement.
     */
    // CN 7: Method (to add a new user).
    public boolean registerUser(User user) { // CN 6: Needs a User object with the details.
        if (connection == null) { // CN 1: Control Structure (if statement).
            System.err.println("Database not connected. Cannot add user.");
            return false;
        }

        String sql = "INSERT INTO users (username, password, email, user_type, full_name, skills, resume_info, company_name) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pstmt = null; // CN 4: JDBC PreparedStatement (for safe SQL insert).
        boolean success = false;

        try { // CN 13: Handling errors.
            pstmt = connection.prepareStatement(sql);
            // CN 11: Getting User details using 'get' methods (Encapsulation).
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword()); // Simple text password.
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getUserType().name()); // Change Enum to text for DB.

            // CN 1: Control Structure (if-else if statement).
            // CN 9: Check what type of user it is (related to Inheritance).
            if (user instanceof JobSeeker) {
                JobSeeker seeker = (JobSeeker) user;
                pstmt.setString(5, seeker.getFullName());
                pstmt.setString(6, seeker.getSkills());
                pstmt.setString(7, seeker.getResumeInfo());
                pstmt.setNull(8, Types.VARCHAR); // Employer field is empty for seeker.
            } else if (user instanceof Employer) {
                Employer employer = (Employer) user;
                pstmt.setNull(5, Types.VARCHAR); // Seeker fields are empty for employer.
                pstmt.setNull(6, Types.VARCHAR);
                pstmt.setNull(7, Types.VARCHAR);
                pstmt.setString(8, employer.getCompanyName());
            }

            // 5. Run the Insert Command
            // CN 4: Running an SQL INSERT command.
            int rowsAffected = pstmt.executeUpdate(); // Gives back how many rows were added (should be 1).
            success = rowsAffected > 0; // It worked if we added 1 row.

        } catch (SQLException e) { // CN 13: Catching database errors (SQLException).
            System.err.println("Problem adding user: " + e.getMessage());
            e.printStackTrace();
            success = false;
        } finally { // CN 13: 'finally' block.
            // 7. Close database things
            closeResources(null, pstmt); // Use helper method.
        }
        return success;
    }

     /**
     * Saves a new job advert to the database.
     * Returns true if it worked, false if not.
     */
    // CN 7: Method (to save a job).
    public boolean saveJob(Job job) { // CN 6: Needs a Job object with the details.
        if (connection == null) return false; // CN 1: Control Structure (if statement).
        String sql = "INSERT INTO jobs (employer_id, title, description, location, salary, company_name) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement pstmt = null; // CN 4: JDBC PreparedStatement (for safe SQL insert).
        boolean success = false;
        try { // CN 13: Handling errors.
            pstmt = connection.prepareStatement(sql);
            // CN 11: Getting Job details using 'get' methods (Encapsulation).
            pstmt.setInt(1, job.getEmployerId());
            pstmt.setString(2, job.getTitle());
            pstmt.setString(3, job.getDescription());
            pstmt.setString(4, job.getLocation());
            pstmt.setDouble(5, job.getSalary());
            pstmt.setString(6, job.getCompanyName());

            // CN 4: Running an SQL INSERT command.
            int rowsAffected = pstmt.executeUpdate();
            success = rowsAffected > 0;
        } catch (SQLException e) { // CN 13: Catching database errors (SQLException).
            System.err.println("Problem saving job: " + e.getMessage());
            e.printStackTrace();
        } finally { // CN 13: 'finally' block.
            closeResources(null, pstmt);
        }
        return success;
    }

    // CN 7: Method (to change job details).
    public boolean updateJob(Job job) {
        String query = "UPDATE jobs SET title=?, description=?, location=?, salary=? WHERE job_id=?";
        // Using try-with-resources automatically closes 'stmt' after we finish.
        try (PreparedStatement stmt = connection.prepareStatement(query)) { // CN 13: try-with-resources.
            // CN 11: Using 'get' methods.
            stmt.setString(1, job.getTitle());
            stmt.setString(2, job.getDescription());
            stmt.setString(3, job.getLocation());
            stmt.setDouble(4, job.getSalary());
            stmt.setInt(5, job.getJobId());

            // CN 4: Running an SQL UPDATE command.
            return stmt.executeUpdate() > 0; // Returns true if 1 row was changed.
        } catch (SQLException e) { // CN 13: Catching database errors (SQLException).
            e.printStackTrace();
            return false;
        }
    }

    // CN 7: Method (to remove a job).
    public boolean deleteJob(int jobId) {
        String query = "DELETE FROM jobs WHERE job_id=?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) { // CN 13: try-with-resources.
            stmt.setInt(1, jobId);
            // CN 4: Running an SQL DELETE command.
            return stmt.executeUpdate() > 0; // Returns true if 1 row was removed.
        } catch (SQLException e) { // CN 13: Catching database errors (SQLException).
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets all the jobs from the database.
     * Gives back a List containing Job objects.
     * Shows using a Data Structure (ArrayList).
     */
    // CN 7: Method (to get all jobs).
    // CN 2: Gives back a List (made using ArrayList) Data Structure.
    public List<Job> getAllJobs() {
        if (connection == null) return new ArrayList<>(); // CN 1: If // CN 6: Making a new, empty ArrayList object.
        List<Job> jobs = new ArrayList<>(); // CN 2: ArrayList Data Structure. // CN 6: Making a new ArrayList object.
        String sql = "SELECT * FROM jobs ORDER BY posted_at DESC";
        Statement stmt = null; // CN 4: JDBC Statement (for simple SQL queries).
        ResultSet rs = null; // CN 4: JDBC ResultSet (holds results from database).

        try { // CN 13: Handling errors.
            // 4. Create Statement
            stmt = connection.createStatement();
            // 5. Run the Query
            // CN 4: Running an SQL query.
            rs = stmt.executeQuery(sql);

            // 6. Look at the Results
            while (rs.next()) { // CN 1: Control Structure (while loop) - go through each result.
                // CN 6: Making a Job object for each result row.
                Job job = new Job(
                        rs.getInt("job_id"),
                        rs.getInt("employer_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("location"),
                        rs.getDouble("salary"),
                        rs.getString("company_name"),
                        rs.getTimestamp("posted_at")
                );
                jobs.add(job); // CN 2: Adding the Job object to the ArrayList.
            }
        } catch (SQLException e) { // CN 13: Catching database errors (SQLException).
            System.err.println("Problem getting all jobs: " + e.getMessage());
            e.printStackTrace();
        } finally { // CN 13: 'finally' block.
            closeResources(rs, stmt);
        }
        return jobs; // CN 2: Send back the ArrayList of jobs.
    }

    /**
     * Gets jobs put online by one specific employer.
     */
     // CN 7: Method (to get jobs from one employer).
     // CN 2: Gives back an ArrayList Data Structure.
    public List<Job> getJobsByEmployer(int employerId) {
         if (connection == null) return new ArrayList<>(); // CN 1: If // CN 6: Making a new, empty ArrayList object.
         List<Job> jobs = new ArrayList<>(); // CN 2: ArrayList. // CN 6: Making a new ArrayList object.
         String sql = "SELECT * FROM jobs WHERE employer_id = ? ORDER BY posted_at DESC";
         PreparedStatement pstmt = null; // CN 4: JDBC PreparedStatement (for safe SQL queries).
         ResultSet rs = null; // CN 4: JDBC ResultSet (holds results from database).
         try { // CN 13: Handling errors.
            pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, employerId);
            rs = pstmt.executeQuery(); // CN 4: Running the query.
            while (rs.next()) { // CN 1: While loop - go through each result.
                // CN 6: Making Job object.
                Job job = new Job(
                        rs.getInt("job_id"),
                        rs.getInt("employer_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("location"),
                        rs.getDouble("salary"),
                        rs.getString("company_name"),
                        rs.getTimestamp("posted_at")
                );
                jobs.add(job); // CN 2: Adding the Job to the ArrayList.
            }
         } catch (SQLException e) { // CN 13: Catching database errors (SQLException).
             System.err.println("Problem getting jobs for employer: " + e.getMessage());
             e.printStackTrace();
         } finally { // CN 13: 'finally' block.
            closeResources(rs, pstmt);
        }
        return jobs; // CN 2: Send back the ArrayList.
    }

    /**
     * Finds jobs by looking for a word in the title, description, OR location.
     * It uses LIKE to find parts of words.
     */
     // CN 7: Method (to search for jobs).
     // CN 2: Gives back an ArrayList Data Structure.
    public List<Job> searchJobsByKeywordOrLocation(String searchTerm) {
        if (connection == null) return new ArrayList<>(); // CN 1: If // CN 6: Making a new, empty ArrayList object.
        List<Job> jobs = new ArrayList<>(); // CN 2: ArrayList. // CN 6: Making a new ArrayList object.
        // Simple search using LIKE. Whether it ignores capital letters depends on the database settings.
        String sql = "SELECT * FROM jobs WHERE title LIKE ? OR description LIKE ? OR location LIKE ? ORDER BY posted_at DESC";
        PreparedStatement pstmt = null; // CN 4: JDBC PreparedStatement (for safe SQL queries).
        ResultSet rs = null; // CN 4: JDBC ResultSet (holds results from database).
        String searchPattern = "%" + searchTerm + "%"; // Add % to search for the term anywhere inside.

        try { // CN 13: Handling errors.
            pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, searchPattern); // Look in title.
            pstmt.setString(2, searchPattern); // Look in description.
            pstmt.setString(3, searchPattern); // Look in location.
            rs = pstmt.executeQuery(); // CN 4: Running the query.
            while (rs.next()) { // CN 1: While loop - go through each result.
                 // CN 6: Making Job object.
                 Job job = new Job(
                        rs.getInt("job_id"),
                        rs.getInt("employer_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("location"),
                        rs.getDouble("salary"),
                        rs.getString("company_name"),
                        rs.getTimestamp("posted_at")
                );
                jobs.add(job); // CN 2: Adding the Job to the ArrayList.
            }
        } catch (SQLException e) { // CN 13: Catching database errors (SQLException).
            System.err.println("Problem searching jobs: " + e.getMessage());
            e.printStackTrace();
        } finally { // CN 13: 'finally' block.
            closeResources(rs, pstmt);
        }
        return jobs; // CN 2: Send back the ArrayList.
    }

    /**
     * Saves or updates the CV information for a job seeker.
     * Shows how to use an UPDATE query.
     */
     // CN 7: Method (to save CV details).
    public boolean saveResumeInfo(int seekerId, String resumeInfo, String skills) {
        if (connection == null) return false; // CN 1: If
        String sql = "UPDATE users SET resume_info = ?, skills = ? WHERE user_id = ? AND user_type = 'SEEKER'";
        PreparedStatement pstmt = null; // CN 4: JDBC PreparedStatement (for safe SQL queries).
        boolean success = false;
        try { // CN 13: Handling errors.
            pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, resumeInfo);
            pstmt.setString(2, skills);
            pstmt.setInt(3, seekerId);

            int rowsAffected = pstmt.executeUpdate(); // CN 4: Running the SQL UPDATE.
            success = rowsAffected > 0;
        } catch (SQLException e) { // CN 13: Catching database errors (SQLException).
             System.err.println("Problem saving CV info: " + e.getMessage());
             e.printStackTrace();
        } finally { // CN 13: 'finally' block.
            closeResources(null, pstmt);
        }
        return success;
    }

    /**
     * Saves a record showing a seeker applied for a job.
     */
     // CN 7: Method (to apply for a job).
    public boolean applyForJob(int seekerId, int jobId) {
        if (connection == null) return false; // CN 1: If
        // Check if they already applied, stop them applying twice.
        if (hasApplied(seekerId, jobId)) { // CN 1: If
             System.out.println("User " + seekerId + " has already applied for job " + jobId);
             return false; // Could also give an error message.
        }

        String sql = "INSERT INTO applications (seeker_id, job_id) VALUES (?, ?)";
        PreparedStatement pstmt = null; // CN 4: JDBC PreparedStatement (for safe SQL queries).
        boolean success = false;
        try { // CN 13: Handling errors.
            pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, seekerId);
            pstmt.setInt(2, jobId);
            int rowsAffected = pstmt.executeUpdate(); // CN 4: Running the SQL INSERT.
            success = rowsAffected > 0;
        } catch (SQLException e) { // CN 13: Catching database errors (SQLException).
            if (e.getErrorCode() == 1062) { // Error code 1062 often means duplicate entry. // CN 1: If
                System.out.println("User already applied for this job (database check)." );
                // We might count this as success because the application exists.
                success = true; // Or maybe return a specific status?
            } else {
                System.err.println("Problem applying for job: " + e.getMessage());
                e.printStackTrace();
            }
        } finally { // CN 13: 'finally' block.
            closeResources(null, pstmt);
        }
        return success;
    }

     // Helper method to check if someone has already applied.
     // CN 7: Method (private helper method - only used inside this class).
    private boolean hasApplied(int seekerId, int jobId) {
        if (connection == null) return true; // Safer to assume they applied if we can't check DB?
        String sql = "SELECT 1 FROM applications WHERE seeker_id = ? AND job_id = ? LIMIT 1";
        PreparedStatement pstmt = null; // CN 4: JDBC PreparedStatement (for safe SQL queries).
        ResultSet rs = null; // CN 4: JDBC ResultSet (holds results from database).
        boolean applied = false;
        try { // CN 13: Handling errors.
            pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, seekerId);
            pstmt.setInt(2, jobId);
            rs = pstmt.executeQuery(); // CN 4: Running the query.
            applied = rs.next(); // Is true if we found at least one row.
        } catch (SQLException e) { // CN 13: Catching database errors (SQLException).
            System.err.println("Problem checking if already applied: " + e.getMessage());
            e.printStackTrace();
            // Decide what to do on error - maybe return true to stop trying again?
            applied = true;
        } finally { // CN 13: 'finally' block.
            closeResources(rs, pstmt);
        }
        return applied;
    }


     /**
     * Gets a list of JobSeekers who applied for a certain job.
     */
     // CN 7: Method (to get people who applied).
     // CN 2: Gives back an ArrayList Data Structure.
    public List<JobSeeker> getApplicantsForJob(int jobId) {
        if (connection == null) return new ArrayList<>(); // CN 1: If // CN 6: Making a new, empty ArrayList object.
        List<JobSeeker> applicants = new ArrayList<>(); // CN 2: ArrayList. // CN 6: Making a new ArrayList object.
        // We need to join users and applications tables to get seeker details.
        String sql = "SELECT u.* FROM users u JOIN applications a ON u.user_id = a.seeker_id WHERE a.job_id = ? AND u.user_type = 'SEEKER'";
        PreparedStatement pstmt = null; // CN 4: JDBC PreparedStatement (for safe SQL queries).
        ResultSet rs = null; // CN 4: JDBC ResultSet (holds results from database).
        try { // CN 13: Handling errors.
            pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, jobId);
            rs = pstmt.executeQuery(); // CN 4: Running the query.
            while (rs.next()) { // CN 1: While loop - go through each applicant.
                // CN 6: Making JobSeeker object for each applicant found.
                // CN 9: Related to Inheritance.
                JobSeeker seeker = new JobSeeker(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password"), // Maybe don't need password here?
                        rs.getString("email"),
                        rs.getString("full_name"),
                        rs.getString("skills"),
                        rs.getString("resume_info")
                );
                applicants.add(seeker); // CN 2: Adding the seeker to the ArrayList.
            }
        } catch (SQLException e) { // CN 13: Catching database errors (SQLException).
             System.err.println("Problem getting applicants: " + e.getMessage());
             e.printStackTrace();
        } finally { // CN 13: 'finally' block.
            closeResources(rs, pstmt);
        }
        return applicants; // CN 2: Send back the ArrayList of applicants.
    }

    /**
     * Gets a list of jobs that a specific seeker has applied for.
     */
     // CN 7: Method (to get jobs someone applied for).
     // CN 2: Gives back an ArrayList Data Structure.
    public List<Job> getAppliedJobs(int seekerId) {
        if (connection == null) return new ArrayList<>(); // CN 1: If // CN 6: Making a new, empty ArrayList object.
        List<Job> appliedJobs = new ArrayList<>(); // CN 2: ArrayList. // CN 6: Making a new ArrayList object.
        // Join jobs and applications tables to get job details for the seeker's applications.
        String sql = "SELECT j.* FROM jobs j " +
                     "JOIN applications a ON j.job_id = a.job_id " +
                     "WHERE a.seeker_id = ? " +
                     "ORDER BY a.application_date DESC";
        PreparedStatement pstmt = null; // CN 4: JDBC PreparedStatement (for safe SQL queries).
        ResultSet rs = null; // CN 4: JDBC ResultSet (holds results from database).

        try { // CN 13: Handling errors.
            pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, seekerId);
            rs = pstmt.executeQuery(); // CN 4: Running the query.

            while (rs.next()) { // CN 1: While loop - go through each applied job.
                 // CN 6: Making Job object.
                 Job job = new Job(
                        rs.getInt("job_id"),
                        rs.getInt("employer_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("location"),
                        rs.getDouble("salary"),
                        rs.getString("company_name"),
                        rs.getTimestamp("posted_at") // Get the time the job was posted.
                );
                appliedJobs.add(job); // CN 2: Adding the job to the ArrayList.
            }
        } catch (SQLException e) { // CN 13: Catching database errors (SQLException).
            System.err.println("Problem getting applied jobs: " + e.getMessage());
            e.printStackTrace();
        } finally { // CN 13: 'finally' block.
           closeResources(rs, pstmt); // Use helper method.
        }
        return appliedJobs; // CN 2: Send back the ArrayList of jobs.
    }

    /**
     * Deletes a job application record.
     * Returns true if it worked, false if not.
     */
     // CN 7: Method (to remove an application).
     public boolean deleteApplication(int seekerId, int jobId) {
        if (connection == null) return false; // CN 1: If
        String sql = "DELETE FROM applications WHERE seeker_id = ? AND job_id = ?";
        PreparedStatement pstmt = null; // CN 4: JDBC PreparedStatement (for safe SQL queries).
        boolean success = false;
        try { // CN 13: Handling errors.
            pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, seekerId);
            pstmt.setInt(2, jobId);

            int rowsAffected = pstmt.executeUpdate(); // CN 4: Running the SQL DELETE.
            // Check if any row was actually removed.
            success = rowsAffected > 0;

        } catch (SQLException e) { // CN 13: Catching database errors (SQLException).
            System.err.println("Problem deleting application: " + e.getMessage());
            e.printStackTrace();
            success = false;
        } finally { // CN 13: 'finally' block.
            closeResources(null, pstmt); // Only the statement needs closing here.
        }
        return success;
    }

    /**
     * Changes a user's profile information in the database.
     * Returns true if it worked, false if not.
     */
     // CN 7: Method (to change user details).
    public boolean updateUser(User user) { // CN 6: Takes User object with new details.
        if (connection == null) return false; // CN 1: If
        String sql = "UPDATE users SET email=?, password=?, full_name=?, skills=?, resume_info=?, company_name=? WHERE user_id=?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) { // CN 13: try-with-resources.
             // CN 11: Using 'get' methods.
            pstmt.setString(1, user.getEmail());
            pstmt.setString(2, user.getPassword()); // Plain text password.

            if (user instanceof JobSeeker) { // CN 1: If // CN 9: Check object type
                JobSeeker seeker = (JobSeeker) user;
                pstmt.setString(3, seeker.getFullName());
                pstmt.setString(4, seeker.getSkills());
                pstmt.setString(5, seeker.getResumeInfo());
                pstmt.setNull(6, Types.VARCHAR); // company_name is empty for seeker.
            } else if (user instanceof Employer) { // CN 1: Else If
                Employer employer = (Employer) user;
                pstmt.setNull(3, Types.VARCHAR); // seeker fields are empty for employer.
                pstmt.setNull(4, Types.VARCHAR);
                pstmt.setNull(5, Types.VARCHAR);
                pstmt.setString(6, employer.getCompanyName());
            }

            pstmt.setInt(7, user.getUserId());

            // CN 4: Running the SQL UPDATE command.
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { // CN 13: Catching database errors (SQLException).
            System.err.println("Problem updating user profile: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a user's profile and related things (like applications or jobs).
     * For JobSeekers: Removes their applications.
     * For Employers: Removes their jobs and any applications for those jobs.
     * Returns true if it worked, false if not.
     */
     // CN 7: Method (to remove a user).
    public boolean deleteUser(User user) { // CN 6: Takes User object to remove.
        if (connection == null) return false; // CN 1: If

        try { // CN 13: Handling errors.
            // Start a transaction - means all steps must work, or none do.
            connection.setAutoCommit(false);

            // Remove related things first, depending on user type.
            if (user instanceof JobSeeker) { // CN 1: If // CN 9: Check object type
                // Remove all applications made by this seeker.
                String deleteApps = "DELETE FROM applications WHERE seeker_id = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(deleteApps)) { // CN 13: Try-with-resources.
                    pstmt.setInt(1, user.getUserId());
                    pstmt.executeUpdate(); // CN 4: Running update.
                }
            } else if (user instanceof Employer) { // CN 1: Else if
                // Remove all applications for jobs posted by this employer.
                String deleteApps = "DELETE FROM applications WHERE job_id IN (SELECT job_id FROM jobs WHERE employer_id = ?)";
                try (PreparedStatement pstmt = connection.prepareStatement(deleteApps)) {
                    pstmt.setInt(1, user.getUserId());
                    pstmt.executeUpdate();
                }

                // Remove all jobs posted by this employer.
                String deleteJobs = "DELETE FROM jobs WHERE employer_id = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(deleteJobs)) {
                    pstmt.setInt(1, user.getUserId());
                    pstmt.executeUpdate();
                }
            }

            // Now, remove the user themselves.
            String deleteUser = "DELETE FROM users WHERE user_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteUser)) {
                pstmt.setInt(1, user.getUserId());
                int result = pstmt.executeUpdate(); // CN 4: Running update.

                if (result > 0) { // CN 1: If - did the user get removed?
                    connection.commit(); // CN 4: Make all changes permanent.
                    return true;
                } else {
                    connection.rollback(); // CN 4: Undo all changes from this transaction.
                    return false;
                }
            }

        } catch (SQLException e) { // CN 13: Catching database errors (SQLException).
            try { // Nested try-catch for rollback error.
                connection.rollback(); // Try to undo changes if anything went wrong.
            } catch (SQLException rollbackEx) {
                System.err.println("Problem undoing changes: " + rollbackEx.getMessage());
            }
            System.err.println("Problem deleting user profile: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally { // CN 13: 'finally' block - always runs.
            try {
                connection.setAutoCommit(true); // Put database back to normal auto-commit mode.
            } catch (SQLException e) {
                System.err.println("Problem resetting auto-commit: " + e.getMessage());
            }
        }
    }

    /**
     * Closes the database connection when the program finishes.
     * Should be called when the application exits.
     */
    // CN 7: Method (to close the database connection).
    public void closeConnection() {
        try { // CN 13: Handling errors.
            if (connection != null && !connection.isClosed()) { // CN 1: If - check if connected.
                connection.close(); // CN 4: Closing the JDBC connection.
                System.out.println("Database connection closed successfully.");
            }
        } catch (SQLException e) { // CN 13: Catching database errors (SQLException).
            System.err.println("Problem closing database connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper method to close ResultSet and Statement/PreparedStatement cleanly.
    // CN 7: Method (private helper method - only used inside this class).
    private void closeResources(ResultSet rs, Statement stmt) {
        try { // CN 13: Handling errors.
            if (rs != null) { // CN 1: If
                rs.close(); // CN 4: Closing the JDBC ResultSet.
            }
        } catch (SQLException e) { // CN 13: Catching database errors (SQLException).
            System.err.println("Problem closing ResultSet: " + e.getMessage());
        }
        try { // CN 13: Handling errors.
            if (stmt != null) { // CN 1: If
                stmt.close(); // CN 4: Closing the JDBC Statement.
            }
        } catch (SQLException e) { // CN 13: Catching database errors (SQLException).
            System.err.println("Problem closing Statement: " + e.getMessage());
        }
    }
} 