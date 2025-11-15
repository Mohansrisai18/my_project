package com.example.cognisync;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class VideoItemAdapter extends RecyclerView.Adapter<VideoItemAdapter.VH> {

    private final List<ModuleVideoListActivity.VideoItem> list;
    private final OnVideoClickListener listener;

    public interface OnVideoClickListener { void onVideoClick(int position); }

    public VideoItemAdapter(List<ModuleVideoListActivity.VideoItem> list, OnVideoClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ModuleVideoListActivity.VideoItem it = list.get(position);
        holder.title.setText(it.title);
        holder.desc.setText(it.desc);
        holder.card.setOnClickListener(v -> listener.onVideoClick(position));
    }

    @Override public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        CardView card; TextView title, desc;
        VH(View v) {
            super(v);
            card = v.findViewById(R.id.cardVideo);
            title = v.findViewById(R.id.tvVideoTitle);
            desc = v.findViewById(R.id.tvVideoDesc);
        }
    }
}
