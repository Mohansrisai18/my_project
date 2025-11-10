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
import java.util.*;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_srt);

        tvInstruction = findViewById(R.id.tvInstruction);
        tvDot = findViewById(R.id.tvDot);
        tvResult = findViewById(R.id.tvResult);
        backButton = findViewById(R.id.backButton);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

        backButton.setOnClickListener(v -> {
            startActivity(new Intent(this, ModuleVideoActivity.class));
            finish();
        });

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

        String performance;
        float attentionScore;

        if (avgRT < 300) {
            performance = "Excellent Focus";
            attentionScore = 100;
        } else if (avgRT <= 500) {
            performance = "Normal Performance";
            attentionScore = 75;
        } else if (avgRT <= 700) {
            performance = "Below Average";
            attentionScore = 50;
        } else {
            performance = "Reduced Vigilance";
            attentionScore = 25;
        }

        tvInstruction.setText("Task Complete!");
        tvResult.setText("Avg RT: " + avgRT + " ms\n" + performance);
        tvResult.setVisibility(View.VISIBLE);

        SharedPreferences sp = getSharedPreferences("CognitiveScores", MODE_PRIVATE);
        sp.edit()
                .putLong("srt_ms", avgRT)
                .putFloat("attention_post_score", attentionScore)
                .putLong("timestamp_post", System.currentTimeMillis())
                .apply();

        // Save to score history for dashboard
        String date = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());
        ScoreHistoryStorage.addScoreHistory(this, "Attention", attentionScore / 14f * 7, date);

        handler.postDelayed(() -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }, 3000);
    }
}
