package com.example.cognisync;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScoreHistoryStorage {

    private static final String PREFS_NAME = "ScoreHistoryPrefs";

    /** Save new score entry with date + time */
    public static void addScoreHistory(Context context, String scoreType, float score, String date) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String key = "history_" + normalizeType(scoreType);

        String historyJson = prefs.getString(key, "[]");
        String time = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());

        try {
            JSONArray array = new JSONArray(historyJson);
            JSONObject obj = new JSONObject();
            obj.put("score", score);
            obj.put("date", date);
            obj.put("time", time);
            array.put(obj);

            // Limit to last 10 records
            if (array.length() > 10) {
                JSONArray trimmed = new JSONArray();
                for (int i = array.length() - 10; i < array.length(); i++) {
                    trimmed.put(array.get(i));
                }
                prefs.edit().putString(key, trimmed.toString()).apply();
            } else {
                prefs.edit().putString(key, array.toString()).apply();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /** Fetch stored history */
    public static List<ScoreHistoryItem> getScoreHistory(Context context, String scoreType) {
        List<ScoreHistoryItem> history = new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String key = "history_" + normalizeType(scoreType);
        String historyJson = prefs.getString(key, "[]");

        try {
            JSONArray array = new JSONArray(historyJson);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                float score = (float) obj.getDouble("score");
                String date = obj.getString("date");
                String time = obj.has("time") ? obj.getString("time") : "";
                history.add(new ScoreHistoryItem(score, date, time));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return history;
    }

    /** Normalize domain type */
    private static String normalizeType(String input) {
        if (input == null) return "";
        input = input.trim().toLowerCase();
        if (input.contains("working")) return "memory";
        if (input.contains("emotional") || input.contains("emotion")) return "emotional";
        if (input.contains("flexibility") || input.contains("cognitive")) return "cognitive";
        return input.replace(" ", "_");
    }
}
