package com.example.cognisync.model;

import com.google.gson.annotations.SerializedName;

public class AudioResponse {
    private int id;
    private String title;

    @SerializedName("module_type")
    private String moduleType;

    private String url;

    @SerializedName("duration_seconds")
    private Integer durationSeconds;

    @SerializedName("created_at")
    private String createdAt;

    // getters / setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getModuleType() { return moduleType; }
    public void setModuleType(String moduleType) { this.moduleType = moduleType; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
