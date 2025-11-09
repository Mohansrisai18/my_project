package com.example.cognisync;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.ViewHolder> {

    public interface OnSessionClickListener {
        void onSessionClick(SessionItem session);
    }

    private List<SessionItem> sessionList;
    private OnSessionClickListener listener;

    public SessionAdapter(List<SessionItem> sessionList, OnSessionClickListener listener) {
        this.sessionList = sessionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SessionItem session = sessionList.get(position);
        holder.title.setText(session.getTitle());
        holder.description.setText(session.getDescription());
        holder.itemView.setOnClickListener(v -> listener.onSessionClick(session));
    }

    @Override
    public int getItemCount() {
        return sessionList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, description;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(android.R.id.text1);
            description = itemView.findViewById(android.R.id.text2);
        }
    }
}
