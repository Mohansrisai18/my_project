package com.example.cognisync;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ModuleSessionAdapter extends RecyclerView.Adapter<ModuleSessionAdapter.ViewHolder> {

    public interface OnSessionClickListener {
        void onSessionClick(ModuleSessionItem item);
    }

    private List<ModuleSessionItem> list;
    private final OnSessionClickListener listener;

    public ModuleSessionAdapter(List<ModuleSessionItem> list, OnSessionClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    public void updateList(List<ModuleSessionItem> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate the timetable-like card for module sessions
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_module_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModuleSessionItem s = list.get(position);

        holder.day.setText("Session " + (position + 1));
        holder.title.setText(s.getTitle());
        // put module short id or leave blank — module not provided per session. Hide if empty.
        holder.module.setText(""); // not applicable for module list; left blank
        holder.description.setText(s.getDescription());

        holder.card.setOnClickListener(v -> {
            if (listener != null) listener.onSessionClick(s);
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView card;
        TextView day, title, module, description;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardView); // card id inside item_module_card.xml
            day = itemView.findViewById(R.id.tvDay);
            title = itemView.findViewById(R.id.tvTitle);
            module = itemView.findViewById(R.id.tvModule);
            description = itemView.findViewById(R.id.tvDescription);
        }
    }
}
