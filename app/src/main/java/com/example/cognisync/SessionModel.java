package com.example.cognisync;

public class SessionModel {
    private final String title;
    private final String description;
    private final String testName;

    public SessionModel(String title, String description, String testName) {
        this.title = title;
        this.description = description;
        this.testName = testName;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getTestName() { return testName; }
}
