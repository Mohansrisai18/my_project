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
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_session, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModuleSessionItem s = list.get(position);

        // Use the title & description provided by ModuleHomeActivity (already normalized)
        holder.title.setText(s.getTitle());
        holder.desc.setText(s.getDescription());

        holder.card.setOnClickListener(v -> listener.onSessionClick(s));
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView card;
        TextView title, desc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            card = itemView.findViewById(R.id.cardSession);
            title = itemView.findViewById(R.id.tvSessionTitle);
            desc = itemView.findViewById(R.id.tvSessionDesc);
        }
    }
}
