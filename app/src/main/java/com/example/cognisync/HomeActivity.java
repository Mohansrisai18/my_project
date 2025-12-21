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

    private TextView tvGreeting;
    private TextView tvMinutesFocused, tvMinutesWorking, tvMinutesPresent,
            tvMinutesCognitive, tvMinutesEmotional;

    private CardView cvFocusedAttention, cvWorkingMemory, cvPresentMoment,
            cvCognitiveIntegration, cvEmotionalRegulation;

    private LineChart lineChart;

    private ApiService api;
    private String email;

    // POST scores only
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

        getWindow().setStatusBarColor(ContextCompat.getColor(this, android.R.color.white));
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        api = ApiClient.getClient().create(ApiService.class);

        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        email = sp.getString("email", "");

        initViews();
        setGreeting();          // ✅ updated
        loadEmptyMinutes();
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

    private void initViews() {

        tvGreeting = findViewById(R.id.tvGreeting);

        tvMinutesFocused = findViewById(R.id.tvMinutesFocused);
        tvMinutesWorking = findViewById(R.id.tvMinutesWorking);
        tvMinutesPresent = findViewById(R.id.tvMinutesPresent);
        tvMinutesCognitive = findViewById(R.id.tvMinutesCognitive);
        tvMinutesEmotional = findViewById(R.id.tvMinutesEmotional);

        cvFocusedAttention = findViewById(R.id.cvFocusedAttention);
        cvWorkingMemory = findViewById(R.id.cvWorkingMemory);
        cvPresentMoment = findViewById(R.id.cvPresentMoment);
        cvCognitiveIntegration = findViewById(R.id.cvCognitiveIntegration);
        cvEmotionalRegulation = findViewById(R.id.cvEmotionalRegulation);

        lineChart = findViewById(R.id.lineChart);

        ImageButton btnMenu = findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class))
        );
    }

    // ----------------------------------------------------
    // ✅ BACKWARD-COMPATIBLE GREETING
    // ----------------------------------------------------
    private void setGreeting() {

        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        String userId = sp.getString("user_id", null);

        if (userId == null || userId.isEmpty()) {
            userId = sp.getString("username", "User"); // fallback for old users
        }

        tvGreeting.setText("Hi " + userId);
    }

    private void loadEmptyMinutes() {
        tvMinutesFocused.setText("--");
        tvMinutesWorking.setText("--");
        tvMinutesPresent.setText("--");
        tvMinutesCognitive.setText("--");
        tvMinutesEmotional.setText("--");
    }

    private void setClickActions() {

        cvFocusedAttention.setOnClickListener(v -> openModule("focused_attention"));
        cvWorkingMemory.setOnClickListener(v -> openModule("working_memory"));
        cvPresentMoment.setOnClickListener(v -> openModule("present_moment"));
        cvCognitiveIntegration.setOnClickListener(v -> openModule("cognitive_flexibility"));
        cvEmotionalRegulation.setOnClickListener(v -> openModule("emotional_regulation"));

        findViewById(R.id.cvAttentionScore).setOnClickListener(v -> openScore("Attention"));
        findViewById(R.id.cvMemoryScore).setOnClickListener(v -> openScore("Memory"));
        findViewById(R.id.cvPresentMomentScore).setOnClickListener(v -> openScore("Awareness"));
        findViewById(R.id.cvCognitiveScore).setOnClickListener(v -> openScore("Cognitive"));
        findViewById(R.id.cvEmotionScore).setOnClickListener(v -> openScore("Emotional"));
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
    // 🔥 Fetch only POST scores
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
            public void onResponse(Call<List<ScoreResponse>> call, Response<List<ScoreResponse>> res) {

                if (!res.isSuccessful() || res.body() == null) return;

                for (ScoreResponse s : res.body()) {
                    if ("post".equalsIgnoreCase(s.getScore_type())) {
                        switch (domain) {
                            case "srt": attentionPost = s.getScore(); break;
                            case "nback": memoryPost = s.getScore(); break;
                            case "stroop": emotionPost = s.getScore(); break;
                            case "task_switch": cognitivePost = s.getScore(); break;
                            case "sart": awarenessPost = s.getScore(); break;
                        }
                        break;
                    }
                }

                drawGraph();
            }

            @Override
            public void onFailure(Call<List<ScoreResponse>> call, Throwable t) {}
        });
    }

    // ----------------------------------------------------
    // 🔥 Draw graph using POST scores
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

        LineDataSet ds = new LineDataSet(entries, "Your Mindfulness Growth");
        ds.setColor(Color.parseColor("#7C4DFF"));
        ds.setLineWidth(3f);
        ds.setCircleRadius(6f);
        ds.setCircleColor(Color.parseColor("#7C4DFF"));
        ds.setDrawFilled(true);
        ds.setFillColor(Color.parseColor("#BCA7FF"));
        ds.setValueTextSize(10f);

        LineData data = new LineData(ds);
        lineChart.setData(data);

        final String[] labels = {"Attention", "Memory", "Emotion", "Cognitive", "Awareness"};

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value >= 0 && value < labels.length) return labels[(int) value];
                return "";
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
