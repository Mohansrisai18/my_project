package com.example.cognisync;

public class ScoreHistoryItem {
    private final float score;
    private final String date;
    private final String time;
    private final String type; // pre / post label

    public ScoreHistoryItem(float score, String date, String time, String type) {
        this.score = score;
        this.date = date;
        this.time = time;
        this.type = type;
    }

    public float getScore() { return score; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getType() { return type; }
}
