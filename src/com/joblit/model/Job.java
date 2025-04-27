package com.joblit.model;

import java.sql.Timestamp;

/**
 * Job.java
 * This class holds the information for a single job advert.
 * It's a simple class mainly for storing data, with hidden details (Encapsulation).
 */
public class Job {
    // CN 11: Private details are hidden (Encapsulation).
    private int jobId;
    private int employerId; // Which employer posted this job?
    private String title;
    private String description;
    private String location;
    private double salary; // Using double allows for decimals in salary.
    private String companyName; // Stored here to make showing it easier.
    private Timestamp postedAt; // When the job was put online.

    // CN 5: Constructor for making new Job objects.
    // CN 6: An object is made when we use 'new Job(...)'.
    public Job(int jobId, int employerId, String title, String description, String location, double salary, String companyName, Timestamp postedAt) {
        this.jobId = jobId;
        this.employerId = employerId;
        this.title = title;
        this.description = description;
        this.location = location;
        this.salary = salary;
        this.companyName = companyName;
        this.postedAt = postedAt;
    }

    // Another constructor for when we make a new job before saving it
    // (the database gives it the real jobId later).
    // CN 5: Another type of Constructor (Overloaded).
    public Job(int employerId, String title, String description, String location, double salary, String companyName) {
        this(0, employerId, title, description, location, salary, companyName, null); // Use the main constructor, setting jobId to 0 for now.
    }

    // Getters and Setters - Part of Encapsulation
    // CN 11: Public 'get'/'set' methods give safe access.
    // CN 7: These are 'get' and 'set' Methods.
    public int getJobId() {
        return jobId;
    }

    public int getEmployerId() {
        return employerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Timestamp getPostedAt() {
        return postedAt;
    }

    // toString method to show the job nicely in lists.
    // CN 7: Method (changing the standard toString).
    @Override
    public String toString() {
        // How it should look in a JList or similar GUI part.
        return title + " at " + companyName + " (" + location + ")" + (salary > 0 ? " - Salary: " + salary : ""); // CN 1: Ternary operator.
    }
} 