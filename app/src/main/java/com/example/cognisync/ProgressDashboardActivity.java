package com.example.cognisync;

import android.content.SharedPreferences;
import android.os.Bundle;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

    // temporary store for merged responses (may be appended multiple times)
    private final List<ScoreResponse> incoming = new ArrayList<>();

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

        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "No email found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load PRE + POST-like endpoints in parallel
        loadBothPreAndPost();

        backButton.setOnClickListener(v -> finish());
    }

    // ---------------- load both domains ----------------
    private void loadBothPreAndPost() {
        String[] domains = getDomainsForScoreType(scoreType);
        loadScoreFromServer(domains[0]); // PRE domain
        loadScoreFromServer(domains[1]); // POST domain
    }

    private void loadScoreFromServer(String domain) {
        Call<List<ScoreResponse>> call = api.getScoreHistory(email, domain);
        call.enqueue(new Callback<List<ScoreResponse>>() {
            @Override
            public void onResponse(Call<List<ScoreResponse>> call, Response<List<ScoreResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    // keep UI unchanged
                    return;
                }

                // Append new responses then rebuild unique merged list + UI
                synchronized (incoming) {
                    incoming.addAll(response.body());
                }
                rebuildAndShow();
            }

            @Override
            public void onFailure(Call<List<ScoreResponse>> call, Throwable t) {
                Toast.makeText(ProgressDashboardActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ---------------- merge, dedupe, sort, and update UI ----------------
    private void rebuildAndShow() {
        List<ScoreResponse> merged;
        synchronized (incoming) {
            // Deduplicate by (created_at | score | score_type) key to avoid duplicates on repeated loads
            Map<String, ScoreResponse> map = new LinkedHashMap<>();
            for (ScoreResponse s : incoming) {
                if (s == null) continue;
                String key = safe(s.getCreated_at()) + "|" + s.getScore() + "|" + safe(s.getScore_type());
                map.put(key, s);
            }
            merged = new ArrayList<>(map.values());
        }

        if (merged.isEmpty()) {
            runOnUiThread(() -> {
                tvLatestScore.setText("--");
                tvSummary.setText("No assessment data yet.");
                recyclerScoreHistory.setAdapter(null);
            });
            return;
        }

        // Sort newest -> oldest by ISO timestamp string (lexicographic for ISO works)
        Collections.sort(merged, (a, b) -> safe(b.getCreated_at()).compareTo(safe(a.getCreated_at())));

        // Determine earliest PRE and latest POST-like values
        Float earliestPre = findEarliestPre(merged);
        Float latestPostLike = findLatestPostLike(merged);

        // Build ScoreHistoryItem list (with baseline / latestPost flags)
        List<ScoreHistoryItem> items = new ArrayList<>();
        for (ScoreResponse s : merged) {
            String[] local = convertToLocal(s.getCreated_at());
            boolean isBaseline = false;
            boolean isLatestPost = false;

            String t = safe(s.getScore_type()).toLowerCase(Locale.ROOT);

            if (isPreLike(t) && earliestPre != null) {
                if (Math.abs(s.getScore() - earliestPre) < 0.001f) isBaseline = true;
            }

            if (isPostLike(t) && latestPostLike != null) {
                if (Math.abs(s.getScore() - latestPostLike) < 0.001f) isLatestPost = true;
            }

            items.add(new ScoreHistoryItem(
                    s.getScore(),
                    local[0],
                    local[1],
                    s.getScore_type(),
                    isBaseline,
                    isLatestPost
            ));
        }

        // Update UI on main thread
        runOnUiThread(() -> {
            recyclerScoreHistory.setLayoutManager(new LinearLayoutManager(ProgressDashboardActivity.this));
            recyclerScoreHistory.setAdapter(new ScoreHistoryAdapter(items));

            // Latest overall score is the first entry (newest)
            tvLatestScore.setText(String.format(Locale.getDefault(), "%.1f", merged.get(0).getScore()));

            // Update summary text using earliestPre and latestPostLike
            updateSummaryText(earliestPre, latestPostLike, merged);
        });
    }

    private void updateSummaryText(Float earliestPre, Float latestPostLike, List<ScoreResponse> merged) {
        if (earliestPre == null && latestPostLike == null) {
            tvSummary.setText("No assessment data yet.");
            return;
        }

        float preVal = earliestPre == null ? 0f : earliestPre;
        float postVal = latestPostLike == null ? 0f : latestPostLike;
        float delta = postVal - preVal;

        String status;
        if (earliestPre == null) {
            status = "No baseline (PRE) found";
        } else if (latestPostLike == null) {
            status = "No POST found yet";
        } else {
            if (delta > 3) status = "↑ Improved";
            else if (delta < -3) status = "↓ Declined";
            else status = "→ Stable";
        }

        String baselineText = earliestPre == null ? "--" : String.format(Locale.getDefault(), "%.1f", earliestPre);
        String postText = latestPostLike == null ? "--" : String.format(Locale.getDefault(), "%.1f", latestPostLike);
        String deltaText = String.format(Locale.getDefault(), "%.1f", delta);

        tvSummary.setText(String.format(Locale.getDefault(),
                "%s Progress\nBaseline: %s   |   Post: %s   |   Δ: %s   %s",
                scoreType, baselineText, postText, deltaText, status
        ));
    }

    // Find earliest (oldest) PRE-like score
    private Float findEarliestPre(List<ScoreResponse> list) {
        String earliestIso = null;
        Float val = null;
        for (ScoreResponse s : list) {
            String t = safe(s.getScore_type()).toLowerCase(Locale.ROOT);
            if (!isPreLike(t)) continue;
            String iso = safe(s.getCreated_at());
            if (iso.isEmpty()) continue;
            if (earliestIso == null || iso.compareTo(earliestIso) < 0) {
                earliestIso = iso;
                val = s.getScore();
            }
        }
        return val;
    }

    // Find newest POST-like score
    private Float findLatestPostLike(List<ScoreResponse> list) {
        String latestIso = null;
        Float val = null;
        for (ScoreResponse s : list) {
            String t = safe(s.getScore_type()).toLowerCase(Locale.ROOT);
            if (!isPostLike(t)) continue;
            String iso = safe(s.getCreated_at());
            if (iso.isEmpty()) continue;
            if (latestIso == null || iso.compareTo(latestIso) > 0) {
                latestIso = iso;
                val = s.getScore();
            }
        }
        return val;
    }

    // --------- heuristics to accept different server score_type names ----------
    private boolean isPreLike(String typeLower) {
        if (typeLower == null) return false;
        return typeLower.contains("pre");
    }

    private boolean isPostLike(String typeLower) {
        if (typeLower == null) return false;
        // accept "post" explicitly, and also "task" because your screenshots show TASK as the immediate result.
        return typeLower.contains("post") || typeLower.contains("task");
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    // -----------------------------
    // Convert backend ISO → local timezone (keeps your existing logic)
    // -----------------------------
    private String[] convertToLocal(String iso) {
        String date = "--";
        String time = "--";

        if (iso == null || iso.isEmpty()) return new String[]{date, time};

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

    // PRE & POST domain mapping (unchanged)
    private String[] getDomainsForScoreType(String type) {
        type = type.toLowerCase(Locale.ROOT);
        if (type.contains("attention")) return new String[]{"maas", "srt"};
        if (type.contains("memory")) return new String[]{"cfq", "nback"};
        if (type.contains("emotion")) return new String[]{"panas", "stroop"};
        if (type.contains("awareness")) return new String[]{"phlms", "sart"};
        if (type.contains("cognitive")) return new String[]{"dass", "task_switch"};
        return new String[]{"maas", "srt"};
    }
}
