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

    private final List<ModuleSessionItem> sessionList;
    private final OnSessionClickListener listener;

    public interface OnSessionClickListener {
        void onSessionClick(ModuleSessionItem session);
    }

    public ModuleSessionAdapter(List<ModuleSessionItem> sessionList, OnSessionClickListener listener) {
        this.sessionList = sessionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_module_session, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModuleSessionItem session = sessionList.get(position);
        holder.tvSessionTitle.setText(session.getTitle());
        holder.tvSessionDesc.setText(session.getDescription());
        holder.tvSessionTest.setText("Linked Task: " + session.getModuleType());
        holder.cardSession.setOnClickListener(v -> listener.onSessionClick(session));
    }

    @Override
    public int getItemCount() {
        return sessionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSessionTitle, tvSessionDesc, tvSessionTest;
        CardView cardSession;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardSession = itemView.findViewById(R.id.cardSession);
            tvSessionTitle = itemView.findViewById(R.id.tvSessionTitle);
            tvSessionDesc = itemView.findViewById(R.id.tvSessionDesc);
            tvSessionTest = itemView.findViewById(R.id.tvSessionTest);
        }
    }
}
