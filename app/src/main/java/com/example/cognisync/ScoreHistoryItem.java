package com.example.cognisync;

public class ScoreHistoryItem {
    private final float score;
    private final String date;

    public ScoreHistoryItem(float score, String date) {
        this.score = score;
        this.date = date;
    }

    public float getScore() {
        return score;
    }

    public String getDate() {
        return date;
    }
}
