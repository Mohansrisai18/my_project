package com.example.cognisync;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ScoreHistoryStorage {

    private static final String PREFS_NAME = "ScoreHistoryPrefs";

    public static void addScoreHistory(Context context, String scoreType, float score, String date) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String key = "history_" + scoreType.toLowerCase();
        String historyJson = prefs.getString(key, "[]");

        try {
            JSONArray array = new JSONArray(historyJson);
            JSONObject obj = new JSONObject();
            obj.put("score", score);
            obj.put("date", date);
            array.put(obj);

            prefs.edit().putString(key, array.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static List<ScoreHistoryItem> getScoreHistory(Context context, String scoreType) {
        List<ScoreHistoryItem> history = new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String key = "history_" + scoreType.toLowerCase();
        String historyJson = prefs.getString(key, "[]");

        try {
            JSONArray array = new JSONArray(historyJson);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                float score = (float) obj.getDouble("score");
                String date = obj.getString("date");
                history.add(new ScoreHistoryItem(score, date));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return history;
    }
}
