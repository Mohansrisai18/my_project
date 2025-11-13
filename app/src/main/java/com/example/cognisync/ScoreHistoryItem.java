package com.example.cognisync;

public class ScoreHistoryItem {
    private final float score;
    private final String date;  // e.g., "Nov 12, 2025"
    private final String time;  // e.g., "1:42 PM"

    public ScoreHistoryItem(float score, String date, String time) {
        this.score = score;
        this.date = date;
        this.time = time;
    }

    public float getScore() {
        return score;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }
}
