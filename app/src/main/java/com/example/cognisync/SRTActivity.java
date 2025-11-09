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

import java.util.ArrayList;
import java.util.List;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_srt);

        tvInstruction = findViewById(R.id.tvInstruction);
        tvDot = findViewById(R.id.tvDot);
        tvResult = findViewById(R.id.tvResult);
        backButton = findViewById(R.id.backButton);

        // Get screen size for random positioning
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

        // Back button
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(SRTActivity.this, ModuleVideoActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        tvInstruction.setText("Tap as soon as you see the dot.");

        // Dot tap listener
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

        int delay = random.nextInt(2000) + 1000; // random 1–3 seconds delay
        handler.postDelayed(() -> {
            moveDotToRandomPosition();
            tvDot.setVisibility(View.VISIBLE);
            dotVisible = true;
            startTime = System.currentTimeMillis();
            tvInstruction.setText("Tap it!");
        }, delay);
    }

    /** 🔴 Move the dot to a random position within screen bounds */
    private void moveDotToRandomPosition() {
        int dotSize = 200; // approximate dp size converted to pixels

        int maxX = screenWidth - dotSize;
        int maxY = screenHeight - dotSize - 300; // subtract UI area at top

        int randomX = random.nextInt(Math.max(maxX, 1));
        int randomY = random.nextInt(Math.max(maxY, 1)) + 200; // avoid too top

        tvDot.setX(randomX);
        tvDot.setY(randomY);
    }

    private void showResult() {
        long avgRT = (long) reactionTimes.stream().mapToLong(Long::longValue).average().orElse(0);

        String performance;
        if (avgRT < 300) performance = "Excellent Focus";
        else if (avgRT <= 500) performance = "Normal Performance";
        else performance = "Reduced Vigilance";

        tvInstruction.setText("Task Complete!");
        tvResult.setText("Average Reaction Time: " + avgRT + " ms\n" + performance);
        tvResult.setVisibility(View.VISIBLE);

        SharedPreferences sp = getSharedPreferences("CognitiveScores", MODE_PRIVATE);
        sp.edit().putLong("srt_ms", avgRT).apply();

        // Auto return to home
        handler.postDelayed(() -> {
            Intent intent = new Intent(SRTActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }, 3000);
    }
}
