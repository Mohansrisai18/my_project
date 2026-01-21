package com.example.cognisync;

public class ScoreHistoryItem {
    private final float score;
    private final String date;
    private final String time;
    private final String type; // pre / post label
    private final boolean isBaseline;
    private final boolean isLatestPost;

    public ScoreHistoryItem(float score, String date, String time, String type,
                            boolean isBaseline, boolean isLatestPost) {
        this.score = score;
        this.date = date;
        this.time = time;
        this.type = type;
        this.isBaseline = isBaseline;
        this.isLatestPost = isLatestPost;
    }

    public float getScore() { return score; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getType() { return type; }
    public boolean isBaseline() { return isBaseline; }
    public boolean isLatestPost() { return isLatestPost; }
}
