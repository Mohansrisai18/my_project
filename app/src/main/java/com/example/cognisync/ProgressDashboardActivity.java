package com.example.cognisync;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProgressDashboardActivity extends AppCompatActivity {

    private TextView tvScoreTypeTitle, tvLatestScore;
    private RecyclerView recyclerScoreHistory;
    private ImageButton backButton;

    String scoreType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_dashboard);

        tvScoreTypeTitle = findViewById(R.id.tvScoreTypeTitle);
        tvLatestScore = findViewById(R.id.tvLatestScore);
        recyclerScoreHistory = findViewById(R.id.recyclerScoreHistory);
        backButton = findViewById(R.id.backButton);

        scoreType = getIntent().getStringExtra("score_type");
        if (scoreType == null) scoreType = "Score";

        tvScoreTypeTitle.setText(scoreType + " Score");

        loadScoreHistory();
        backButton.setOnClickListener(v -> finish());
    }

    private void loadScoreHistory() {
        List<ScoreHistoryItem> history = ScoreHistoryStorage.getScoreHistory(this, scoreType);

        if (history.isEmpty()) {
            tvLatestScore.setText("--/7");
            recyclerScoreHistory.setLayoutManager(new LinearLayoutManager(this));
            recyclerScoreHistory.setAdapter(new ScoreHistoryAdapter(history));
            return;
        }

        // latest score
        ScoreHistoryItem latestItem = history.get(history.size() - 1);
        tvLatestScore.setText(String.format("%.1f/7", latestItem.getScore()));

        recyclerScoreHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerScoreHistory.setAdapter(new ScoreHistoryAdapter(history));
    }
}
