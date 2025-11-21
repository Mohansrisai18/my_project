package com.example.cognisync;

public class ModuleSessionItem {
    private final String title;
    private final String description;
    private final String audioUrl; // may be null
    private final String videoUrl; // may be null

    public ModuleSessionItem(String title, String description) {
        this(title, description, null, null);
    }

    public ModuleSessionItem(String title, String description, String audioUrl) {
        this(title, description, audioUrl, null);
    }

    public ModuleSessionItem(String title, String description, String audioUrl, String videoUrl) {
        this.title = title;
        this.description = description;
        this.audioUrl = audioUrl;
        this.videoUrl = videoUrl;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getAudioUrl() { return audioUrl; }
    public String getVideoUrl() { return videoUrl; }
}
