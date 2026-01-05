package com.example.cognisync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.cognisync.del.ApiClient;
import com.example.cognisync.del.ApiService;
import com.example.cognisync.model.ScoreResponse;
import com.example.cognisync.util.TimetableStore;
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

    // ✅ Timetable card
    private CardView cvTimetable;

    // Graph
    private LineChart lineChart;

    private ApiService api;
    private String email;

    // TASK scores only
    private Float attentionPost = null;
    private Float memoryPost = null;
    private Float emotionPost = null;
    private Float cognitivePost = null;
    private Float awarenessPost = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (getSupportActionBar() != null) getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Status bar styling
        getWindow().setStatusBarColor(
                ContextCompat.getColor(this, android.R.color.white)
        );
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            getWindow().getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        api = ApiClient.getClient().create(ApiService.class);

        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        email = sp.getString("email", "");

        initViews();
        setGreeting();
        setClickActions();

        if (!email.isEmpty()) {
            fetchPostScoresFromBackend();
        } else {
            Toast.makeText(this, "Email missing!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchPostScoresFromBackend();
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

        // ✅ Bind timetable card
        cvTimetable = findViewById(R.id.cvTimetable);

        lineChart = findViewById(R.id.lineChart);

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

        // ✅ Timetable card click
        cvTimetable.setOnClickListener(v -> {

            // 1️⃣ Check if timetable exists
            if (TimetableStore.exists(this)) {

                // 2️⃣ Open timetable
                startActivity(
                        new Intent(this, TimetableActivity.class)
                );

            } else {

                // 3️⃣ Not generated yet
                Toast.makeText(
                        this,
                        "Complete assessment to generate your timetable",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void openModule(String type) {
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
        fetchScoreForDomain("srt");
        fetchScoreForDomain("nback");
        fetchScoreForDomain("stroop");
        fetchScoreForDomain("task_switch");
        fetchScoreForDomain("sart");
    }

    private void fetchScoreForDomain(String domain) {

        api.getScoreHistory(email, domain).enqueue(new Callback<List<ScoreResponse>>() {
            @Override
            public void onResponse(Call<List<ScoreResponse>> call,
                                   Response<List<ScoreResponse>> response) {

                if (!response.isSuccessful() || response.body() == null) return;

                for (ScoreResponse s : response.body()) {

                    if ("task".equalsIgnoreCase(s.getScore_type())) {

                        switch (domain) {
                            case "srt":
                                attentionPost = s.getScore();
                                break;
                            case "nback":
                                memoryPost = s.getScore();
                                break;
                            case "stroop":
                                emotionPost = s.getScore();
                                break;
                            case "task_switch":
                                cognitivePost = s.getScore();
                                break;
                            case "sart":
                                awarenessPost = s.getScore();
                                break;
                        }
                        break;
                    }
                }
                drawGraph();
            }

            @Override
            public void onFailure(Call<List<ScoreResponse>> call, Throwable t) {
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

        if (a == 0 && m == 0 && e == 0 && c == 0 && w == 0) {
            lineChart.clear();
            lineChart.setNoDataText("No progress data yet");
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
}
