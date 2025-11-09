package com.example.cognisync;

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
        TextView scoreText, dateText;

        ViewHolder(View itemView) {
            super(itemView);
            scoreText = itemView.findViewById(R.id.score_text);
            dateText  = itemView.findViewById(R.id.date_text);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_score_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScoreHistoryItem item = items.get(position);
        holder.scoreText.setText(String.format("%.1f/7", item.getScore()));
        holder.dateText.setText(item.getDate());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
