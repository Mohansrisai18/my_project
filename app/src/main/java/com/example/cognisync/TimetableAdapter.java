package com.example.cognisync;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cognisync.model.TimetableItem;

import java.util.List;

public class TimetableAdapter
        extends RecyclerView.Adapter<TimetableAdapter.ViewHolder> {

    private final List<TimetableItem> timetable;

    public TimetableAdapter(List<TimetableItem> timetable) {
        this.timetable = timetable;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_timetable, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        TimetableItem item = timetable.get(position);

        holder.tvDay.setText("Day " + item.getDay());
        holder.tvTitle.setText(item.getTitle());
        holder.tvModule.setText("Module: " + item.getModule());
        holder.tvDuration.setText(item.getDuration() + " mins");
    }

    @Override
    public int getItemCount() {
        return timetable.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvDay, tvTitle, tvModule, tvDuration;

        ViewHolder(View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvModule = itemView.findViewById(R.id.tvModule);
            tvDuration = itemView.findViewById(R.id.tvDuration);
        }
    }
}
