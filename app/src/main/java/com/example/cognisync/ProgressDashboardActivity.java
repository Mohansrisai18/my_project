package com.example.cognisync;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cognisync.del.ApiClient;
import com.example.cognisync.del.ApiService;
import com.example.cognisync.model.ScoreResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProgressDashboardActivity extends AppCompatActivity {

    private TextView tvScoreTypeTitle, tvLatestScore, tvSummary;
    private RecyclerView recyclerScoreHistory;
    private ImageButton backButton;

    private String scoreType;
    private ApiService api;
    private String email;

    // Merged PRE + POST scores
    private final List<ScoreResponse> allScores = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_progress_dashboard);

        api = ApiClient.getClient().create(ApiService.class);

        tvScoreTypeTitle = findViewById(R.id.tvScoreTypeTitle);
        tvLatestScore = findViewById(R.id.tvLatestScore);
        tvSummary = findViewById(R.id.tvHistoryLabel);
        recyclerScoreHistory = findViewById(R.id.recyclerScoreHistory);
        backButton = findViewById(R.id.backButton);

        scoreType = getIntent().getStringExtra("score_type");
        if (scoreType == null) scoreType = "Score";

        tvScoreTypeTitle.setText(scoreType + " Progress");

        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        email = sp.getString("email", "");

        if (email.isEmpty()) {
            Toast.makeText(this, "No email found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadBothPreAndPost();
        backButton.setOnClickListener(v -> finish());
    }

    // Load PRE + POST based on module
    private void loadBothPreAndPost() {
        String[] domains = getDomainsForScoreType(scoreType);

        loadScoreFromServer(domains[0]); // PRE
        loadScoreFromServer(domains[1]); // POST
    }

    private void loadScoreFromServer(String domain) {
        Call<List<ScoreResponse>> call = api.getScoreHistory(email, domain);

        call.enqueue(new Callback<List<ScoreResponse>>() {
            @Override
            public void onResponse(Call<List<ScoreResponse>> call, Response<List<ScoreResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) return;

                allScores.addAll(response.body());
                updateMergedUI();
            }

            @Override
            public void onFailure(Call<List<ScoreResponse>> call, Throwable t) {
                Toast.makeText(ProgressDashboardActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMergedUI() {

        if (allScores.isEmpty()) {
            tvLatestScore.setText("--");
            tvSummary.setText("No assessment data yet.");
            return;
        }

        // Sort newest -> oldest
        allScores.sort((a, b) -> safe(b.getCreated_at()).compareTo(safe(a.getCreated_at())));

        List<ScoreHistoryItem> items = new ArrayList<>();

        for (ScoreResponse s : allScores) {
            String[] local = convertToLocal(s.getCreated_at());
            items.add(new ScoreHistoryItem(
                    s.getScore(),
                    local[0],
                    local[1],
                    s.getScore_type()   // 🔥 add this
            ));
        }

        recyclerScoreHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerScoreHistory.setAdapter(new ScoreHistoryAdapter(items));

        tvLatestScore.setText(String.format(Locale.getDefault(), "%.1f", allScores.get(0).getScore()));

        computeProgress(allScores);
    }

    private void computeProgress(List<ScoreResponse> list) {
        Float pre = null, post = null;

        for (ScoreResponse s : list) {
            if ("pre".equalsIgnoreCase(s.getScore_type()) && pre == null)
                pre = s.getScore();

            if ("post".equalsIgnoreCase(s.getScore_type()) && post == null)
                post = s.getScore();
        }

        if (pre == null && post == null) {
            tvSummary.setText("No assessment data yet.");
            return;
        }

        if (pre == null) pre = 0f;
        if (post == null) post = 0f;

        float delta = post - pre;

        String status;
        if (delta > 3) status = "↑ Improved";
        else if (delta < -3) status = "↓ Declined";
        else status = "→ Stable";

        tvSummary.setText(String.format(
                "%s Progress\nBaseline: %.1f   |   Post: %.1f   |   Δ: %.1f   %s",
                scoreType, pre, post, delta, status
        ));
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    // -----------------------------
    // Convert backend ISO → local timezone
    // -----------------------------
    private String[] convertToLocal(String iso) {
        String date = "--";
        String time = "--";

        if (iso == null || iso.isEmpty()) return new String[]{date, time};

        // ---- Modern java.time for API 26+ ----
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Instant instant = Instant.parse(iso);
                ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());

                date = zdt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                time = zdt.format(DateTimeFormatter.ofPattern("HH:mm"));

                return new String[]{date, time};
            } catch (Exception ignore) {}
        }

        // ---- Fallback for older devices ----
        try {
            SimpleDateFormat parser1 =
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            parser1.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date d;
            try {
                d = parser1.parse(iso);
            } catch (ParseException ex) {
                SimpleDateFormat parser2 =
                        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                parser2.setTimeZone(TimeZone.getTimeZone("UTC"));
                d = parser2.parse(iso);
            }

            SimpleDateFormat outDate =
                    new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outTime =
                    new SimpleDateFormat("HH:mm", Locale.getDefault());

            outDate.setTimeZone(TimeZone.getDefault());
            outTime.setTimeZone(TimeZone.getDefault());

            date = outDate.format(d);
            time = outTime.format(d);

        } catch (Exception ignore2) {}

        return new String[]{date, time};
    }

    // PRE & POST domain mapping
    private String[] getDomainsForScoreType(String type) {
        type = type.toLowerCase();

        if (type.contains("attention")) return new String[]{"maas", "srt"};
        if (type.contains("memory")) return new String[]{"cfq", "nback"};
        if (type.contains("emotion")) return new String[]{"panas", "stroop"};
        if (type.contains("awareness")) return new String[]{"phlms", "sart"};
        if (type.contains("cognitive")) return new String[]{"dass", "task_switch"};

        return new String[]{"maas", "srt"};
    }
}
