# JobLit - Job Finder System

## Description

JobLit is a simple Java desktop application designed to connect job seekers with employers. It provides a basic platform for users to register, log in, manage profiles (seekers), post jobs (employers), search for jobs, and manage applications. The system uses a MySQL database for data persistence, accessed via JDBC, and features a graphical user interface built with Java Swing.

This project was developed to demonstrate understanding and application of core Object-Oriented Programming (OOP) principles and fundamental Java concepts.

## Features

### Job Seekers
- Register for a new account.
- Login securely.
- View and search for available job postings (by title, description, or location).
- View detailed information about a selected job.
- Apply for jobs.
- Manage their profile (basic resume info, skills).
- View jobs they have applied for.
- Withdraw applications.
- Edit/Delete their profile.

### Employers
- Register for a new account.
- Login securely.
- Post new job openings with details (title, description, location, salary).
- View jobs they have posted.
- Edit or delete their job postings.
- View a list of applicants for their jobs.
- View applicant details (name, skills, resume info).
- Edit/Delete their profile.

## Technologies Used

- **Language:** Java
- **GUI:** Java Swing
- **Database:** MySQL
- **Connectivity:** JDBC (mysql-connector-j)
- **Core Concepts:** OOP (Inheritance, Encapsulation, Polymorphism, Abstraction), Control Structures, Data Structures (ArrayList, Enum), Exception Handling, Interfaces.

## Project Structure

The project follows a standard Java package structure:

-   `src/com/joblit/main`: Contains the main application class (`JobLitApp.java`).
-   `src/com/joblit/model`: Contains the data model classes (`User.java`, `JobSeeker.java`, `Employer.java`, `Job.java`, `UserType.java`).
-   `src/com/joblit/gui`: Contains the Swing-based GUI classes (`LoginFrame.java`, `RegistrationFrame.java`, etc.).
-   `src/com/joblit/db`: Contains the database interaction class (`DatabaseManager.java`).

## Implemented Concepts Checklist

-   [x] Control Structures (if, else, while, for)
-   [x] Data Structures (ArrayList, enum)
-   [x] Graphical User Interfaces (GUI - Swing)
-   [x] Java Database Connectivity (JDBC - MySQL)
-   [x] Constructors
-   [x] Objects & Instantiation
-   [x] Methods (Getters, Setters, Event Handlers, DB Ops, etc.)
-   [x] UML Modelling (Reflected in code structure/relationships)
-   [x] Inheritance
-   [x] Abstraction
-   [x] Encapsulation
-   [x] Polymorphism
-   [x] Exception Handling (SQLException, Basic Input Validation)
-   [x] Interfaces (ActionListener)


## Challenges Faced

*(This section describes some problems we found and how we solved them.)*

1.  **Making the Windows Look Tidy:**
    *   **Problem:** It was difficult to arrange all the parts (buttons, text boxes, lists) nicely in the windows, especially the dashboards. Using simple layouts sometimes made things look stretched or not lined up properly. It was also hard to make it look good if the user resized the window.
    *   **Solution:** We used *combined layouts*. We divided the window into smaller sections using `JPanel`s. Each panel then used the best layout for its parts (like `BorderLayout` for the main window shape, `GridLayout` for forms, and `FlowLayout` for buttons in a row). We used `JSplitPane` to make areas the user could resize, and `JTabbedPane` (in the Seeker Dashboard) to organise different parts into tabs. Adding borders and empty space around sections also helped.

2.  **Connecting to the Database and Handling Errors:**
    *   **Problem:** Sometimes, connecting to the MySQL database didn't work, or there were mistakes in the SQL commands. This could crash the program and show confusing error messages (`SQLException`).
    *   **Solution:** We put all the code for talking to the database into the `DatabaseManager` class. We used `try-catch` blocks around all database actions (connecting, running SQL commands). If an error happened, instead of crashing, the program now shows a simple message to the user using `JOptionPane` (like "Failed to save job. Please try again."). We also made sure to always close the database connections properly using `finally` blocks or try-with-resources.

3.  **Keeping the Window Updated:**
    *   **Problem:** When information changed in the database (like a new job was posted or someone applied), the lists in the window (`JList`) didn't show the new information automatically. The user had to close and open the program again.
    *   **Solution:** We used `DefaultListModel` for our lists. After any action that changed the data (like posting a job), we immediately re-ran the code to get the fresh data from the `DatabaseManager` (e.g., `loadPostedJobs()`). This code first clears the list (`jobListModel.clear()`) and then adds all the updated items back. This way, the window always shows the latest information.

4.  **Checking User Input:**
    *   **Problem:** Users might type the wrong thing, like leaving important boxes empty, typing letters instead of numbers (for salary), or making passwords not match when registering.
    *   **Solution:** Before trying to save anything to the database, we added checks in the button's code (like in `handlePostJob` or `handleRegistration`). We checked if required boxes were empty (`.trim().isEmpty()`), used `try-catch` when converting text to numbers (to catch `NumberFormatException`), and compared password boxes. If the input was wrong, we showed a message using `JOptionPane` telling the user what to fix, and we didn't save the incorrect data.

5.  **Showing Objects Nicely in Lists:**
    *   **Problem:** Putting `Job` or `JobSeeker` objects directly into a list (`JList`) showed strange text like `com.joblit.model.Job@1a2b3c4d`, not the actual job title or person's name.
    *   **Solution:** We changed the `toString()` method in the `Job` and `JobSeeker` classes. This method now returns text that is easy to read (like the job title or seeker's name). For lists where we wanted to show more detail, we made a custom `ListCellRenderer`.

6.  **Making Buttons Active/Inactive:**
    *   **Problem:** Buttons like "Apply" or "Withdraw" should only be clickable when a job is actually selected in the list next to it. Sometimes they were active when they shouldn't be.
    *   **Solution:** We added a `ListSelectionListener` to the lists. When the user clicks on an item in the list, this listener checks if something is selected (`jobList.getSelectedValue() != null`). Then, it makes the button active (`applyButton.setEnabled(true)`) or inactive (`applyButton.setEnabled(false)`) so it can only be clicked at the correct time. We also set the buttons correctly when the list was first loaded or cleared. 