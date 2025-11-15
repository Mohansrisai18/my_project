package com.example.cognisync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class SRTActivity extends AppCompatActivity {

    private TextView tvInstruction, tvDot, tvResult;
    private ImageButton backButton;
    private Handler handler = new Handler();
    private Random random = new Random();

    private long startTime;
    private int trialCount = 0;
    private static final int TOTAL_TRIALS = 10;
    private boolean dotVisible = false;

    private int screenWidth, screenHeight;
    private final List<Long> reactionTimes = new ArrayList<>();

    private String moduleType = "focused_attention";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_srt);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // read module_type if passed
        Intent i = getIntent();
        if (i != null && i.hasExtra("module_type")) moduleType = i.getStringExtra("module_type");

        tvInstruction = findViewById(R.id.tvInstruction);
        tvDot = findViewById(R.id.tvDot);
        tvResult = findViewById(R.id.tvResult);
        backButton = findViewById(R.id.backButton);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

        backButton.setOnClickListener(v -> finish());

        tvInstruction.setText("Tap as soon as you see the dot.");
        tvDot.setOnClickListener(v -> {
            if (dotVisible) {
                long reaction = System.currentTimeMillis() - startTime;
                reactionTimes.add(reaction);
                dotVisible = false;
                tvDot.setVisibility(View.INVISIBLE);
                nextTrial();
            }
        });

        nextTrial();
    }

    private void nextTrial() {
        if (trialCount >= TOTAL_TRIALS) {
            showResult();
            return;
        }

        trialCount++;
        tvInstruction.setText("Get ready...");

        int delay = random.nextInt(2000) + 1000;
        handler.postDelayed(() -> {
            moveDotToRandomPosition();
            tvDot.setVisibility(View.VISIBLE);
            dotVisible = true;
            startTime = System.currentTimeMillis();
            tvInstruction.setText("Tap it!");
        }, delay);
    }

    private void moveDotToRandomPosition() {
        int dotSize = 200;
        int maxX = screenWidth - dotSize;
        int maxY = screenHeight - dotSize - 300;
        tvDot.setX(random.nextInt(Math.max(maxX, 1)));
        tvDot.setY(random.nextInt(Math.max(maxY, 1)) + 200);
    }

    private void showResult() {
        long avgRT = (long) reactionTimes.stream().mapToLong(Long::longValue).average().orElse(0);

        // --- Exponential normalization ---
        double attentionScore = 100.0 * Math.exp(-((double) (avgRT - 250L)) / 250.0);
        attentionScore = Math.max(0, Math.min(100, attentionScore));

        String performance;
        if (attentionScore >= 80) performance = "Excellent Focus";
        else if (attentionScore >= 50) performance = "Normal Performance";
        else performance = "Reduced Vigilance";

        tvInstruction.setText("Task Complete!");
        tvResult.setText(String.format(Locale.getDefault(),
                "Avg RT: %d ms\nScore: %.1f /100\n%s", avgRT, attentionScore, performance));
        tvResult.setVisibility(View.VISIBLE);

        SharedPreferences sp = getSharedPreferences("CognitiveScores", MODE_PRIVATE);
        sp.edit()
                .putLong("srt_ms", avgRT)
                .putFloat("attention_post_score", (float) attentionScore)
                .putLong("timestamp_post", System.currentTimeMillis())
                .apply();

        // mark post completed for this module
        getSharedPreferences("ModuleState", MODE_PRIVATE)
                .edit()
                .putBoolean(moduleType + "_post_completed", true)
                .apply();

        // Save to history
        String date = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());
        ScoreHistoryStorage.addScoreHistory(this, "Attention", (float) attentionScore, date);

        // Auto close after short delay
        handler.postDelayed(this::finish, 2500);
    }
}
