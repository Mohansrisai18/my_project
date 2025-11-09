package com.example.cognisync;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.function.Consumer;

public class SessionListAdapter extends RecyclerView.Adapter<SessionListAdapter.ViewHolder> {

    private final List<SessionModel> sessions;
    private final Consumer<SessionModel> onClick;

    public SessionListAdapter(List<SessionModel> sessions, Consumer<SessionModel> onClick) {
        this.sessions = sessions;
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_session_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SessionModel session = sessions.get(position);
        holder.tvTitle.setText(session.getTitle());
        holder.tvDescription.setText(session.getDescription());
        holder.tvTestName.setText(session.getTestName());
        holder.itemView.setOnClickListener(v -> onClick.accept(session));
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvTestName;
        CardView cardView;
        ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardSession);
            tvTitle = itemView.findViewById(R.id.tvSessionTitle);
            tvDescription = itemView.findViewById(R.id.tvSessionDesc);
            tvTestName = itemView.findViewById(R.id.tvSessionTest);
        }
    }
}
