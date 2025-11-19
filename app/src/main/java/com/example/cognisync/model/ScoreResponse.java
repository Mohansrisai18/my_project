package com.example.cognisync.model;

import java.util.Map;

public class ScoreResponse {

    private int id;
    private int user;
    private String domain;
    private float score;
    private String score_type;
    private Map<String, Object> details;
    private String created_at;

    public int getId() {
        return id;
    }

    public int getUser() {
        return user;
    }

    public String getDomain() {
        return domain;
    }

    public float getScore() {
        return score;
    }

    public String getScore_type() {
        return score_type;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public String getCreated_at() {
        return created_at;
    }
}
