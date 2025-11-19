package com.example.cognisync;

public class ModuleSessionItem {
    private final String title;
    private final String description;

    public ModuleSessionItem(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() { return title; }

    public String getDescription() { return description; }
}
