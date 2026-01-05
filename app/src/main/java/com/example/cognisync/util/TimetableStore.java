package com.example.cognisync.util;

import android.content.Context;
import android.content.SharedPreferences;

public class TimetableStore {

    private static final String PREF_NAME = "cognisync_timetable";
    private static final String KEY_TIMETABLE_JSON = "timetable_json";

    public static void save(Context context, String timetableJson) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        prefs.edit()
                .putString(KEY_TIMETABLE_JSON, timetableJson)
                .apply();
    }

    public static String load(Context context) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        return prefs.getString(KEY_TIMETABLE_JSON, null);
    }

    public static boolean exists(Context context) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        return prefs.contains(KEY_TIMETABLE_JSON);
    }

    public static void clear(Context context) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        prefs.edit().clear().apply();
    }
}
