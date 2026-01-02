package com.example.cognisync.model;

import java.util.List;
import java.util.Map;

public class TimetableResponse {

    private Map<String, Float> probabilities;
    private List<String> selected_labels;
    private List<TimetableItem> timetable;

    public Map<String, Float> getProbabilities() {
        return probabilities;
    }

    public List<String> getSelected_labels() {
        return selected_labels;
    }

    public List<TimetableItem> getTimetable() {
        return timetable;
    }
}
