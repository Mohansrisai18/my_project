package com.example.cognisync;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProgressDashboardActivity extends AppCompatActivity {

    private TextView tvScoreTypeTitle, tvLatestScore, tvSummary;
    private RecyclerView recyclerScoreHistory;
    private ImageButton backButton;

    private String scoreType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide ActionBar and set light status bar
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        setContentView(R.layout.activity_progress_dashboard);

        // --- View Bindings ---
        tvScoreTypeTitle = findViewById(R.id.tvScoreTypeTitle);
        tvLatestScore = findViewById(R.id.tvLatestScore);
        tvSummary = findViewById(R.id.tvHistoryLabel); // reused as summary label
        recyclerScoreHistory = findViewById(R.id.recyclerScoreHistory);
        backButton = findViewById(R.id.backButton);

        // --- Get Domain Type ---
        scoreType = getIntent().getStringExtra("score_type");
        if (scoreType == null) scoreType = "Score";

        tvScoreTypeTitle.setText(scoreType + " Progress");

        // --- Load and Display Data ---
        loadScoreHistory();
        showProgressSummary();

        // --- Back Button ---
        backButton.setOnClickListener(v -> finish());
    }

    /** Load stored trial history for this domain */
    private void loadScoreHistory() {
        List<ScoreHistoryItem> history = ScoreHistoryStorage.getScoreHistory(this, scoreType);

        recyclerScoreHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerScoreHistory.setAdapter(new ScoreHistoryAdapter(history));

        if (history.isEmpty()) {
            tvLatestScore.setText("--");
        } else {
            ScoreHistoryItem latest = history.get(history.size() - 1);
            // Remove "/7" → display numeric or percentage cleanly
            tvLatestScore.setText(String.format("%.1f", latest.getScore()));
        }
    }

    /** Compare baseline vs post and display improvement */
    private void showProgressSummary() {
        SharedPreferences sp = getSharedPreferences("CognitiveScores", MODE_PRIVATE);

        float baseline = 0f, post = 0f;
        String keyBaseline = "", keyPost = "";

        // Map domain type to SharedPreferences keys
        switch (scoreType.toLowerCase()) {
            case "attention":
                keyBaseline = "maas_score";
                keyPost = "attention_post_score";
                break;
            case "working memory":
                keyBaseline = "cfq_score";
                keyPost = "memory_post_score";
                break;
            case "emotional regulation":
            case "emotional":
                keyBaseline = "panas_score";
                keyPost = "emotion_post_score";
                break;
            case "awareness":
                keyBaseline = "phlms_awareness";
                keyPost = "awareness_post_score";
                break;
            case "cognitive":
            case "flexibility":
                keyBaseline = "dass_stress_score";
                keyPost = "flexibility_post_score";
                break;
        }

        baseline = sp.getFloat(keyBaseline, 0f);
        post = sp.getFloat(keyPost, 0f);

        // --- No data yet ---
        if (baseline == 0f && post == 0f) {
            tvSummary.setText("No assessment data yet for this domain.");
            return;
        }

        float delta = post - baseline;
        String status;
        if (delta > 5)
            status = "↑ Improved";
        else if (delta < -5)
            status = "↓ Declined";
        else
            status = "→ Stable";

        // --- Display final summary line ---
        String summary = String.format(
                "%s Progress\nBaseline: %.1f   |   Post: %.1f   |   Δ Change: %.1f   %s",
                scoreType, baseline, post, delta, status
        );

        tvSummary.setText(summary);
    }
}
