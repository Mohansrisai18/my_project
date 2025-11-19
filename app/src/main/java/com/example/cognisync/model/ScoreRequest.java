package com.example.cognisync.model;

import java.util.Map;

public class ScoreRequest {

    private String email;
    private float score;
    private String domain;       // optional
    private String score_type;   // optional
    private Map<String, Object> details; // optional

    // Simple constructor (used in PreAssessment)
    public ScoreRequest(String email, float score) {
        this.email = email;
        this.score = score;
    }

    // Full constructor (used in tasks with details)
    public ScoreRequest(String email, float score, String domain, String score_type, Map<String, Object> details) {
        this.email = email;
        this.score = score;
        this.domain = domain;
        this.score_type = score_type;
        this.details = details;
    }

    public String getEmail() { return email; }
    public float getScore() { return score; }
    public String getDomain() { return domain; }
    public String getScore_type() { return score_type; }
    public Map<String, Object> getDetails() { return details; }
}
