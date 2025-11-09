package com.example.cognisync;

public class SessionItem {
    private String title;
    private String description;
    private String moduleType;

    public SessionItem(String title, String description, String moduleType) {
        this.title = title;
        this.description = description;
        this.moduleType = moduleType;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getModuleType() { return moduleType; }
}
