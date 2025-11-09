package com.example.cognisync;

public class ModuleSessionItem {

    private final String title;
    private final String description;
    private final String moduleType;

    public ModuleSessionItem(String title, String description, String moduleType) {
        this.title = title;
        this.description = description;
        this.moduleType = moduleType;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getModuleType() {
        return moduleType;
    }
}
