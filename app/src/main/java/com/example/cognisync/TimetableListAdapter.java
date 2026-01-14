package com.example.cognisync;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cognisync.model.MLPredictResponse;

import java.util.List;

public class TimetableListAdapter
        extends RecyclerView.Adapter<TimetableListAdapter.ViewHolder> {

    private final List<MLPredictResponse.TimetableItem> items;

    public TimetableListAdapter(List<MLPredictResponse.TimetableItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_timetable_day, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position
    ) {
        MLPredictResponse.TimetableItem item = items.get(position);

        holder.day.setText("Day " + (position + 1));
        holder.title.setText(sanitizeTitle(item.title));
        holder.module.setText("Module: " + item.module);
        holder.description.setText(sanitizeDesc(item.description));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView day, title, module, description;

        ViewHolder(View v) {
            super(v);
            day = v.findViewById(R.id.tvDay);
            title = v.findViewById(R.id.tvTitle);
            module = v.findViewById(R.id.tvModule);
            description = v.findViewById(R.id.tvDescription);
        }
    }

    // --------------------------
    // Small sanitizers for UX
    // --------------------------
    private static String sanitizeTitle(String t) {
        if (t == null) return "";
        // Replace "Sub-video" or "Sub-video X" with "Audio Session X"
        String out = t.replaceAll("(?i)sub[- ]?video", "Audio Session");
        out = out.replaceAll("(?i)sub[- ]?audio", "Audio Session");
        return out;
    }

    private static String sanitizeDesc(String d) {
        if (d == null) return "";
        // Prefer audio wording
        String out = d.replaceAll("(?i)video", "audio");
        // Optional: map generic phrases to the standard audio description
        if (out.toLowerCase().contains("mindfulness")) {
            return "Guided mindfulness audio";
        }
        return out;
    }
}
