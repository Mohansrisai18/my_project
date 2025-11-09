package com.example.cognisync;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class SARTActivity extends AppCompatActivity {

    private TextView tvNumber, tvInstruction, tvProgress;
    private Button btnStart;

    private final int TOTAL_TRIALS = 40; // Total numbers shown
    private int currentTrial = 0;
    private int commissionErrors = 0; // Tap when '3' appears
    private int correctResponses = 0; // Correct tap on non-3 digits
    private long reactionTimeSum = 0;
    private long lastShownTime = 0;
    private boolean canTap = false;

    private Handler handler = new Handler();
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_sart);

        tvNumber = findViewById(R.id.tvNumber);
        tvInstruction = findViewById(R.id.tvInstruction);
        tvProgress = findViewById(R.id.tvProgress);
        btnStart = findViewById(R.id.btnStart);

        btnStart.setOnClickListener(v -> startTask());
        tvNumber.setOnClickListener(v -> handleTap());
    }

    private void startTask() {
        btnStart.setVisibility(View.GONE);
        tvInstruction.setText("Tap for every number EXCEPT 3!");
        currentTrial = 0;
        commissionErrors = 0;
        correctResponses = 0;
        reactionTimeSum = 0;
        runNextTrial();
    }

    private void runNextTrial() {
        if (currentTrial >= TOTAL_TRIALS) {
            endTask();
            return;
        }

        int number = random.nextInt(10); // 0–9
        tvNumber.setText(String.valueOf(number));
        tvProgress.setText("Trial " + (currentTrial + 1) + " / " + TOTAL_TRIALS);

        canTap = true;
        lastShownTime = System.currentTimeMillis();

        currentTrial++;

        handler.postDelayed(() -> {
            canTap = false;
            runNextTrial();
        }, 800); // Each number stays for 800ms
    }

    private void handleTap() {
        if (!canTap) return;

        int shownNumber = Integer.parseInt(tvNumber.getText().toString());
        long reactionTime = System.currentTimeMillis() - lastShownTime;

        if (shownNumber == 3) {
            commissionErrors++; // Wrong tap (commission error)
        } else {
            correctResponses++;
            reactionTimeSum += reactionTime;
        }

        canTap = false; // Prevent multiple taps per trial
    }

    private void endTask() {
        tvNumber.setText("-");
        tvInstruction.setText("Task Completed!");
        tvProgress.setText("");

        int totalResponses = correctResponses + commissionErrors;
        double accuracy = totalResponses == 0 ? 0 :
                (correctResponses * 100.0 / totalResponses);

        long avgReactionTime = correctResponses == 0 ? 0 :
                reactionTimeSum / correctResponses;

        float attentionScore = (float) (accuracy - commissionErrors * 2);
        if (attentionScore < 0) attentionScore = 0;
        if (attentionScore > 100) attentionScore = 100;

        String result = "✅ Accuracy: " + String.format("%.1f", accuracy) + "%\n"
                + "⚠️ Errors: " + commissionErrors + "\n"
                + "⏱ Avg Reaction: " + avgReactionTime + " ms\n"
                + "🎯 Attention Score: " + (int) attentionScore + "/100";

        Toast.makeText(this, result, Toast.LENGTH_LONG).show();

        // Save locally (can later sync with Firebase)
        getSharedPreferences("ModuleData", MODE_PRIVATE).edit()
                .putFloat("sart_accuracy", (float) accuracy)
                .putInt("sart_errors", commissionErrors)
                .putLong("sart_reaction_time", avgReactionTime)
                .putFloat("attention_score", attentionScore)
                .apply();
    }
}
