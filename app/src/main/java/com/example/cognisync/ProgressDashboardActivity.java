package com.example.cognisync;

import android.content.Intent;
import android.content.SharedPreferences;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_dashboard);

        tvScoreTypeTitle = findViewById(R.id.tvScoreTypeTitle);
        tvLatestScore = findViewById(R.id.tvLatestScore);
        recyclerScoreHistory = findViewById(R.id.recyclerScoreHistory);
        backButton = findViewById(R.id.backButton);

        Intent intent = getIntent();
        String scoreType = intent.getStringExtra("score_type");

        // Setup Score Title
        if (scoreType != null) {
            if ("Attention".equalsIgnoreCase(scoreType)) {
                tvScoreTypeTitle.setText("Attention Score");
            } else if ("Memory".equalsIgnoreCase(scoreType)) {
                tvScoreTypeTitle.setText("Memory Score");
            } else if ("Emotion".equalsIgnoreCase(scoreType)) {
                tvScoreTypeTitle.setText("Emotional Regulation");
            } else {
                tvScoreTypeTitle.setText(scoreType + " Score");
            }
        } else {
            tvScoreTypeTitle.setText("Score Detail");
        }

        // Calculate and show the latest score
        SharedPreferences sp = getSharedPreferences("AssessmentScores", MODE_PRIVATE);
        float latestScore = 0f;
        if ("Attention".equalsIgnoreCase(scoreType)) {
            float focusedScore = sp.getFloat("focused_attention_score", 0f);
            float presentScore = sp.getFloat("present_moment_score", 0f);
            latestScore = avgNonZero(focusedScore, presentScore);
        } else if ("Memory".equalsIgnoreCase(scoreType)) {
            float workingScore = sp.getFloat("working_memory_score", 0f);
            float cognitiveScore = sp.getFloat("cognitive_integration_score", 0f);
            latestScore = avgNonZero(workingScore, cognitiveScore);
        } else if ("Emotion".equalsIgnoreCase(scoreType)) {
            latestScore = sp.getFloat("emotional_regulation_score", 0f);
        }

        if (latestScore > 0.01f) {
            tvLatestScore.setText(String.format("%.1f/7", latestScore));
        } else {
            tvLatestScore.setText("--/7");
        }

        // Load and show score history
        List<ScoreHistoryItem> scoreHistory = ScoreHistoryStorage.getScoreHistory(this, scoreType);
        recyclerScoreHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerScoreHistory.setAdapter(new ScoreHistoryAdapter(scoreHistory));

        backButton.setOnClickListener(v -> finish());
    }

    // Average helper
    private float avgNonZero(float... vals) {
        float sum = 0;
        int count = 0;
        for (float v : vals) {
            if (v > 0) {
                sum += v;
                count++;
            }
        }
        return count > 0 ? sum / count : 0;
    }
}
