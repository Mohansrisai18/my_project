package com.example.cognisync.model;

public class ScoreRequest {
    private String email;
    private int score;

    public ScoreRequest(String email, int score) {
        this.email = email;
        this.score = score;
    }

    public String getEmail() { return email; }
    public int getScore() { return score; }
}
