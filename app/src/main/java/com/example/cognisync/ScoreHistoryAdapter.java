package com.example.cognisync;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ScoreHistoryAdapter extends RecyclerView.Adapter<ScoreHistoryAdapter.ViewHolder> {

    private final List<ScoreHistoryItem> items;

    public ScoreHistoryAdapter(List<ScoreHistoryItem> items) {
        this.items = items;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView scoreText, dateText, typeText;

        ViewHolder(View itemView) {
            super(itemView);
            scoreText = itemView.findViewById(R.id.score_text);
            dateText = itemView.findViewById(R.id.date_text);
            typeText = itemView.findViewById(R.id.type_text);
        }
    }

    @NonNull
    @Override
    public ScoreHistoryAdapter.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_score_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ScoreHistoryAdapter.ViewHolder holder,
            int position
    ) {
        ScoreHistoryItem item = items.get(position);

        holder.scoreText.setText(String.format("%.1f", item.getScore()));
        holder.dateText.setText(item.getDate() + " • " + item.getTime());

        // PRE or POST label
        if (item.getType() != null) {
            holder.typeText.setText(item.getType().toUpperCase());

            if (item.getType().equalsIgnoreCase("pre")) {
                holder.typeText.setTextColor(Color.parseColor("#6C63FF")); // purple
            } else {
                holder.typeText.setTextColor(Color.parseColor("#00A878")); // green
            }
        } else {
            holder.typeText.setText(""); // hidden if unknown
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
