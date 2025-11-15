package com.example.cognisync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private TextView tvGreeting;
    private TextView tvMinutesFocused, tvMinutesWorking, tvMinutesPresent,
            tvMinutesCognitive, tvMinutesEmotional;

    private CardView cvFocusedAttention, cvWorkingMemory, cvPresentMoment,
            cvCognitiveIntegration, cvEmotionalRegulation,
            cvProgressDashboard, cvGraphSection;

    private LineChart lineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // ✅ White status bar with dark icons
        getWindow().setStatusBarColor(ContextCompat.getColor(this, android.R.color.white));
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        // Initialize and setup
        initViews();
        setGreeting();
        loadDynamicMinutes();
        setClickActions();
        showOverallGraph();
    }

    /** Refresh graph every time user returns to Home */
    @Override
    protected void onResume() {
        super.onResume();
        showOverallGraph();
    }

    /** Initialize all views */
    private void initViews() {
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
        cvProgressDashboard = findViewById(R.id.cvProgressDashboard);
        cvGraphSection = findViewById(R.id.cvGraphSection);

        lineChart = findViewById(R.id.lineChart);

        ImageButton btnMenu = findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class))
        );
    }

    /** Set greeting based on saved username */
    private void setGreeting() {
        tvGreeting = findViewById(R.id.tvGreeting);
        SharedPreferences userPref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String username = userPref.getString("username", "User");
        tvGreeting.setText("Hi, " + username);
    }

    /** Load minutes from SharedPreferences */
    private void loadDynamicMinutes() {
        SharedPreferences sp = getSharedPreferences("ModuleData", MODE_PRIVATE);
        tvMinutesFocused.setText(sp.getString("module_focused_minutes", "--"));
        tvMinutesWorking.setText(sp.getString("module_working_minutes", "--"));
        tvMinutesPresent.setText(sp.getString("module_present_minutes", "--"));
        tvMinutesCognitive.setText(sp.getString("module_cognitive_minutes", "--"));
        tvMinutesEmotional.setText(sp.getString("module_emotional_minutes", "--"));
    }

    /** Set navigation click actions */
    private void setClickActions() {
        cvFocusedAttention.setOnClickListener(v -> openModuleList("focused_attention"));
        cvWorkingMemory.setOnClickListener(v -> openModuleList("working_memory"));
        cvPresentMoment.setOnClickListener(v -> openModuleList("present_moment"));
        cvCognitiveIntegration.setOnClickListener(v -> openModuleList("cognitive_flexibility"));
        cvEmotionalRegulation.setOnClickListener(v -> openModuleList("emotional_regulation"));

        // ✅ Score Dashboard Navigation
        findViewById(R.id.cvAttentionScore).setOnClickListener(v -> openScoreDashboard("Attention"));
        findViewById(R.id.cvMemoryScore).setOnClickListener(v -> openScoreDashboard("Memory"));
        findViewById(R.id.cvPresentMomentScore).setOnClickListener(v -> openScoreDashboard("Awareness"));
        findViewById(R.id.cvCognitiveScore).setOnClickListener(v -> openScoreDashboard("Cognitive"));
        findViewById(R.id.cvEmotionScore).setOnClickListener(v -> openScoreDashboard("Emotional"));

        // Progress Dashboard stays static
        cvProgressDashboard.setOnClickListener(null);
    }

    private void openModuleList(String moduleType) {
        Intent intent = new Intent(this, ModuleIntroActivity.class);
        intent.putExtra("module_type", moduleType);
        startActivity(intent);
    }

    private void openScoreDashboard(String scoreType) {
        Intent intent = new Intent(this, ProgressDashboardActivity.class);
        intent.putExtra("score_type", scoreType);
        startActivity(intent);
    }

    /** ✅ Show Overall Mindfulness Graph */
    private void showOverallGraph() {
        if (lineChart == null) return;

        SharedPreferences sp = getSharedPreferences("CognitiveScores", MODE_PRIVATE);

        float attention = sp.getFloat("attention_post_score", 0f);
        float memory = sp.getFloat("memory_post_score", 0f);
        float emotion = sp.getFloat("emotion_post_score", 0f);
        float cognitive = sp.getFloat("flexibility_post_score", 0f);
        float awareness = sp.getFloat("awareness_post_score", 0f);

        // Log for debugging (optional)
        Log.d("GRAPH_DEBUG", "Scores → attention=" + attention +
                ", memory=" + memory +
                ", emotion=" + emotion +
                ", cognitive=" + cognitive +
                ", awareness=" + awareness);

        // If all zero → no data
        if (attention == 0f && memory == 0f && emotion == 0f && cognitive == 0f && awareness == 0f) {
            lineChart.clear();
            lineChart.setNoDataText("No data available yet");
            lineChart.setNoDataTextColor(Color.GRAY);
            return;
        }

        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, attention));
        entries.add(new Entry(1, memory));
        entries.add(new Entry(2, emotion));
        entries.add(new Entry(3, cognitive));
        entries.add(new Entry(4, awareness));

        LineDataSet dataSet = new LineDataSet(entries, "Mindfulness Progress");
        dataSet.setColor(Color.parseColor("#7C4DFF"));
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(6f);
        dataSet.setCircleHoleRadius(3f);
        dataSet.setCircleColor(Color.parseColor("#7C4DFF"));
        dataSet.setValueTextSize(11f);
        dataSet.setValueTextColor(Color.DKGRAY);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#BCA7FF"));
        dataSet.setFillAlpha(160);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // ✅ Customize X-axis
        String[] labels = {"Attention", "Memory", "Emotion", "Cognitive", "Awareness"};
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.DKGRAY);
        xAxis.setTextSize(11f);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (value >= 0 && value < labels.length) ? labels[(int) value] : "";
            }
        });

        // ✅ Customize Y-axis
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setTextColor(Color.DKGRAY);
        leftAxis.setTextSize(10f);
        leftAxis.setDrawGridLines(true);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        lineChart.getAxisRight().setEnabled(false);

        // ✅ Legend
        Legend legend = lineChart.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(Color.DKGRAY);
        legend.setTextSize(12f);
        legend.setForm(Legend.LegendForm.CIRCLE);

        // ✅ Chart styling
        lineChart.getDescription().setEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.animateX(1000);
        lineChart.setExtraBottomOffset(10f);
        lineChart.invalidate();
    }
}
