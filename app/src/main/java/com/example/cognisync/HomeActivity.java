package com.example.cognisync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.cognisync.del.ApiClient;
import com.example.cognisync.del.ApiService;
import com.example.cognisync.model.ScoreResponse;
// Removed TimetableStore import as requested
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    // Header
    private TextView tvGreeting;

    // Module cards
    private CardView cvFocusedAttention, cvWorkingMemory, cvPresentMoment,
            cvCognitiveIntegration, cvEmotionalRegulation;

    // Score cards
    private CardView cvAttentionScore, cvMemoryScore,
            cvEmotionScore, cvCognitiveScore, cvPresentMomentScore;

    // (Timetable removed)

    // Graph
    private LineChart lineChart;

    private ApiService api;
    private String email;

    // 🔒 Prevent score API spam
    private boolean scoresLoaded = false;

    // track pending requests so we mark scoresLoaded only after all responses
    private int pendingScoreRequests = 0;
    private final String[] SCORE_DOMAINS = {"srt", "nback", "stroop", "task_switch", "sart"};

    // TASK scores only (Float wrappers so we can test null)
    private Float attentionPost = null;
    private Float memoryPost = null;
    private Float emotionPost = null;
    private Float cognitivePost = null;
    private Float awarenessPost = null;

    // top container so we can add status bar padding
    private LinearLayout rootContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (getSupportActionBar() != null) getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Status bar styling (white background + dark icons where supported)
        getWindow().setStatusBarColor(
                ContextCompat.getColor(this, android.R.color.white)
        );
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // Light status bar: dark icons on light background
            getWindow().getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        api = ApiClient.getClient().create(ApiService.class);

        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        email = sp.getString("email", "");

        initViews();

        // ---- STATUS BAR OVERLAP FIX ----
        // Add top padding equal to status bar height if needed (prevents content from being under status bar)
        // We guard so we don't double-pad if XML or other code already added padding.
        if (rootContainer != null && rootContainer.getPaddingTop() == 0) {
            int statusBarHeight = getStatusBarHeight();
            if (statusBarHeight > 0) {
                rootContainer.setPadding(
                        rootContainer.getPaddingLeft(),
                        statusBarHeight,
                        rootContainer.getPaddingRight(),
                        rootContainer.getPaddingBottom()
                );
            }
        }

        setGreeting();
        setClickActions();

        if (email.isEmpty()) {
            Toast.makeText(this, "Email missing!", Toast.LENGTH_SHORT).show();
        }

        // NOTE: We do NOT trigger fetch here to avoid double-fetch (onCreate -> onResume).
        // Fetch will be handled in onResume if needed.
    }

    @Override
    protected void onResume() {
        super.onResume();

        // fetch fresh scores when needed (scoresLoaded will be set to true only after all requests finish)
        if (!scoresLoaded) {
            if (!email.isEmpty()) {
                fetchPostScoresFromBackend();
            }
        }
    }

    // ----------------------------------------------------
    // INIT VIEWS
    // ----------------------------------------------------
    private void initViews() {

        tvGreeting = findViewById(R.id.tvGreeting);

        cvFocusedAttention = findViewById(R.id.cvFocusedAttention);
        cvWorkingMemory = findViewById(R.id.cvWorkingMemory);
        cvPresentMoment = findViewById(R.id.cvPresentMoment);
        cvCognitiveIntegration = findViewById(R.id.cvCognitiveIntegration);
        cvEmotionalRegulation = findViewById(R.id.cvEmotionalRegulation);

        cvAttentionScore = findViewById(R.id.cvAttentionScore);
        cvMemoryScore = findViewById(R.id.cvMemoryScore);
        cvEmotionScore = findViewById(R.id.cvEmotionScore);
        cvCognitiveScore = findViewById(R.id.cvCognitiveScore);
        cvPresentMomentScore = findViewById(R.id.cvPresentMomentScore);

        // timetable view binding removed:
        // cvTimetable = findViewById(R.id.cvTimetable);

        lineChart = findViewById(R.id.lineChart);

        // root container inside NestedScrollView (used for status bar padding)
        rootContainer = findViewById(R.id.rootContainer);

        ImageButton btnMenu = findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class))
        );
    }

    // ----------------------------------------------------
    // GREETING
    // ----------------------------------------------------
    private void setGreeting() {
        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        String userId = sp.getString("user_id", null);
        if (userId == null || userId.isEmpty()) {
            userId = sp.getString("username", "User");
        }

        tvGreeting.setText("Hi " + userId);
    }

    // ----------------------------------------------------
    // CLICK ACTIONS
    // ----------------------------------------------------
    private void setClickActions() {

        // Modules
        cvFocusedAttention.setOnClickListener(v -> openModule("focused_attention"));
        cvWorkingMemory.setOnClickListener(v -> openModule("working_memory"));
        cvPresentMoment.setOnClickListener(v -> openModule("present_moment"));
        cvCognitiveIntegration.setOnClickListener(v -> openModule("cognitive_flexibility"));
        cvEmotionalRegulation.setOnClickListener(v -> openModule("emotional_regulation"));

        // Scores
        cvAttentionScore.setOnClickListener(v -> openScore("Attention"));
        cvMemoryScore.setOnClickListener(v -> openScore("Memory"));
        cvEmotionScore.setOnClickListener(v -> openScore("Emotional"));
        cvCognitiveScore.setOnClickListener(v -> openScore("Cognitive"));
        cvPresentMomentScore.setOnClickListener(v -> openScore("Awareness"));

        // Timetable click handler removed (XML no longer contains timetable)
    }

    private void openModule(String type) {
        // When opening a module the user may complete assessments and post-scores will change.
        // Mark scoresLoaded=false so that when HomeActivity resumes we fetch fresh results.
        scoresLoaded = false;

        Intent i = new Intent(this, ModuleIntroActivity.class);
        i.putExtra("module_type", type);
        startActivity(i);
    }

    private void openScore(String type) {
        Intent i = new Intent(this, ProgressDashboardActivity.class);
        i.putExtra("score_type", type);
        startActivity(i);
    }

    // ----------------------------------------------------
    // FETCH TASK SCORES
    // ----------------------------------------------------
    private void fetchPostScoresFromBackend() {
        // reset posts so drawGraph sees "no data" until responses arrive (optional)
        attentionPost = null;
        memoryPost = null;
        emotionPost = null;
        cognitivePost = null;
        awarenessPost = null;

        // set pending counter and call each domain
        pendingScoreRequests = SCORE_DOMAINS.length;

        for (String domain : SCORE_DOMAINS) {
            fetchScoreForDomain(domain);
        }
    }

    private void fetchScoreForDomain(String domain) {

        api.getScoreHistory(email, domain).enqueue(new Callback<List<ScoreResponse>>() {
            @Override
            public void onResponse(Call<List<ScoreResponse>> call,
                                   Response<List<ScoreResponse>> response) {

                try {
                    if (response != null && response.isSuccessful() && response.body() != null
                            && !response.body().isEmpty()) {

                        List<ScoreResponse> list = response.body();

                        // Try to find the most recent "task" score:
                        // iterate through list and pick the LAST element that has score_type == "task"
                        // (this ensures we pick the most recent task entry if list is ordered older->newer or vice-versa)
                        Float latestTaskScore = null;
                        ScoreResponse fallback = null;

                        for (ScoreResponse s : list) {
                            if (s == null) continue;
                            // keep fallback as last non-null element
                            fallback = s;
                            String st = s.getScore_type();
                            if (st != null && st.equalsIgnoreCase("task")) {
                                // assign/overwrite so final value is the last 'task' seen
                                latestTaskScore = Float.valueOf(s.getScore());
                            }
                        }

                        // If we found a task score, use it; otherwise use fallback's score (most recent)
                        Float chosenScore = latestTaskScore;
                        if (chosenScore == null && fallback != null) {
                            chosenScore = Float.valueOf(fallback.getScore());
                        }

                        if (chosenScore != null) {
                            switch (domain) {
                                case "srt":
                                    attentionPost = chosenScore;
                                    break;
                                case "nback":
                                    memoryPost = chosenScore;
                                    break;
                                case "stroop":
                                    emotionPost = chosenScore;
                                    break;
                                case "task_switch":
                                    cognitivePost = chosenScore;
                                    break;
                                case "sart":
                                    awarenessPost = chosenScore;
                                    break;
                            }
                        }

                        // redraw graph after processing the domain response
                        drawGraph();
                    }
                } finally {
                    // Always decrement pending and mark loaded if finished
                    pendingScoreRequests = Math.max(0, pendingScoreRequests - 1);
                    if (pendingScoreRequests == 0) {
                        scoresLoaded = true;
                    }
                }
            }

            @Override
            public void onFailure(Call<List<ScoreResponse>> call, Throwable t) {
                // Even on failure we decrement the pending counter to avoid locking state.
                pendingScoreRequests = Math.max(0, pendingScoreRequests - 1);
                if (pendingScoreRequests == 0) {
                    scoresLoaded = true;
                }
            }
        });
    }

    // ----------------------------------------------------
    // DRAW GRAPH
    // ----------------------------------------------------
    private void drawGraph() {

        if (lineChart == null) return;

        float a = attentionPost == null ? 0 : attentionPost;
        float m = memoryPost == null ? 0 : memoryPost;
        float e = emotionPost == null ? 0 : emotionPost;
        float c = cognitivePost == null ? 0 : cognitivePost;
        float w = awarenessPost == null ? 0 : awarenessPost;

        // If everything is zero (no data) show no-data text
        if (a == 0 && m == 0 && e == 0 && c == 0 && w == 0) {
            lineChart.clear();
            lineChart.setNoDataText("No progress data yet");
            lineChart.invalidate();
            return;
        }

        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, a));
        entries.add(new Entry(1, m));
        entries.add(new Entry(2, e));
        entries.add(new Entry(3, c));
        entries.add(new Entry(4, w));

        LineDataSet dataSet = new LineDataSet(entries, "Your Mindfulness Growth");
        dataSet.setColor(Color.parseColor("#7C4DFF"));
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(6f);
        dataSet.setCircleColor(Color.parseColor("#7C4DFF"));
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#BCA7FF"));
        dataSet.setValueTextSize(10f);

        lineChart.setData(new LineData(dataSet));

        final String[] labels =
                {"Attention", "Memory", "Emotion", "Cognitive", "Awareness"};

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (value >= 0 && value < labels.length)
                        ? labels[(int) value] : "";
            }
        });

        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setAxisMinimum(0);
        yAxis.setAxisMaximum(100);
        lineChart.getAxisRight().setEnabled(false);

        lineChart.getDescription().setEnabled(false);
        lineChart.animateX(800);
        lineChart.invalidate();
    }

    // Helper: get status bar height in pixels
    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
