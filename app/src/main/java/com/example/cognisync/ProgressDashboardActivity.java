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
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        setContentView(R.layout.activity_progress_dashboard);

        // Bind views
        tvScoreTypeTitle = findViewById(R.id.tvScoreTypeTitle);
        tvLatestScore = findViewById(R.id.tvLatestScore);
        tvSummary = findViewById(R.id.tvHistoryLabel);
        recyclerScoreHistory = findViewById(R.id.recyclerScoreHistory);
        backButton = findViewById(R.id.backButton);

        // Get score type
        scoreType = getIntent().getStringExtra("score_type");
        if (scoreType == null) scoreType = "Score";
        tvScoreTypeTitle.setText(scoreType + " Progress");

        loadScoreHistory();
        showProgressSummary();

        backButton.setOnClickListener(v -> finish());
    }

    /** Load score history & show latest score */
    private void loadScoreHistory() {
        String normalized = normalizeType(scoreType);
        List<ScoreHistoryItem> history = ScoreHistoryStorage.getScoreHistory(this, normalized);

        recyclerScoreHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerScoreHistory.setAdapter(new ScoreHistoryAdapter(history));

        if (history.isEmpty()) {
            tvLatestScore.setText("--");
        } else {
            ScoreHistoryItem latest = history.get(history.size() - 1);
            tvLatestScore.setText(String.format("%.1f", latest.getScore()));
        }
    }

    /** Compare baseline vs post scores (new model) */
    private void showProgressSummary() {
        SharedPreferences sp = getSharedPreferences("CognitiveScores", MODE_PRIVATE);

        String domain = normalizeType(scoreType);
        float baseline = 0f, post = 0f;

        switch (domain) {
            case "attention":     // SRT → attention score
                baseline = sp.getFloat("maas_score", 0f);
                post = sp.getFloat("attention_post_score", 0f);
                break;

            case "memory":        // N-Back → new memory formula
                baseline = sp.getFloat("cfq_score", 0f);
                post = sp.getFloat("nback_memory_score", 0f);
                break;

            case "emotional":     // Stroop → exponential
                baseline = sp.getFloat("panas_score", 0f);
                post = sp.getFloat("emotion_post_score", 0f);
                break;

            case "awareness":     // SART → weighted exponential
                baseline = sp.getFloat("phlms_awareness", 0f);
                post = sp.getFloat("awareness_post_score", 0f);
                break;

            case "cognitive":     // Task Switching → flexibility score
                baseline = sp.getFloat("dass_stress_score", 0f);
                post = sp.getFloat("flexibility_post_score", 0f);
                break;
        }

        if (baseline == 0f && post == 0f) {
            tvSummary.setText("No assessment data yet for this domain.");
            return;
        }

        float delta = post - baseline;
        String status;

        if (delta > 3) status = "↑ Improved";
        else if (delta < -3) status = "↓ Declined";
        else status = "→ Stable";

        String summary = String.format(
                "%s Progress\nBaseline: %.1f   |   Post: %.1f   |   Δ: %.1f   %s",
                capitalize(scoreType), baseline, post, delta, status
        );

        tvSummary.setText(summary);
    }

    /** Normalize keys for consistent history + SharedPrefs */
    private String normalizeType(String input) {
        if (input == null) return "";
        input = input.trim().toLowerCase();

        if (input.contains("attention")) return "attention";
        if (input.contains("memory") || input.contains("working")) return "memory";
        if (input.contains("emotion") || input.contains("emotional")) return "emotional";
        if (input.contains("awareness")) return "awareness";
        if (input.contains("cognitive") || input.contains("flexibility")) return "cognitive";

        return input;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
