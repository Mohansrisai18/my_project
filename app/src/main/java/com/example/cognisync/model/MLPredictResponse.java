package com.example.cognisync.model;

import java.util.List;
import java.util.Map;

public class MLPredictResponse {

    public Map<String, Float> probabilities;
    public List<SelectedLabel> selected_labels;
    public List<TimetableItem> timetable;

    public static class SelectedLabel {
        public String label;
        public float probability;
        public String level;
    }

    public static class TimetableItem {
        public String audio_code;
        public String module;
        public String title;
        public String description;
    }
}
