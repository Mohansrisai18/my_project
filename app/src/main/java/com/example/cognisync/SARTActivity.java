package com.example.cognisync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class SARTActivity extends AppCompatActivity {

    private TextView tvNumber, tvInstruction, tvProgress;
    private Button btnStart;

    private static final int TOTAL_TRIALS = 40;
    private int currentTrial = 0;
    private int commissionErrors = 0;
    private int omissionErrors = 0;
    private int correctResponses = 0;
    private long reactionTimeSum = 0;
    private long lastShownTime = 0;
    private boolean canTap = false;

    private final Handler handler = new Handler();
    private final Random random = new Random();

    private String moduleType = "present_moment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sart);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        Intent i = getIntent();
        if (i != null && i.hasExtra("module_type")) moduleType = i.getStringExtra("module_type");

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
        omissionErrors = 0;
        correctResponses = 0;
        reactionTimeSum = 0;
        runNextTrial();
    }

    private void runNextTrial() {
        if (currentTrial >= TOTAL_TRIALS) {
            endTask();
            return;
        }

        int number = random.nextInt(10);
        tvNumber.setText(String.valueOf(number));
        tvProgress.setText("Trial " + (currentTrial + 1) + " / " + TOTAL_TRIALS);

        canTap = true;
        lastShownTime = System.currentTimeMillis();

        currentTrial++;

        handler.postDelayed(() -> {
            if (canTap) {
                int shownNumber = Integer.parseInt(tvNumber.getText().toString());
                if (shownNumber != 3) omissionErrors++;
            }
            canTap = false;
            runNextTrial();
        }, 800);
    }

    private void handleTap() {
        if (!canTap) return;

        int shownNumber = Integer.parseInt(tvNumber.getText().toString());
        long reactionTime = System.currentTimeMillis() - lastShownTime;

        if (shownNumber == 3) {
            commissionErrors++;
        } else {
            correctResponses++;
            reactionTimeSum += reactionTime;
        }

        canTap = false;
    }

    private void endTask() {
        tvNumber.setText("-");
        tvInstruction.setText("Task Completed!");
        tvProgress.setText("");

        int totalResponses = correctResponses + commissionErrors;
        double accuracy = totalResponses == 0 ? 0 : (correctResponses * 100.0 / Math.max(1, totalResponses));
        long avgReactionTime = correctResponses == 0 ? 0 : (reactionTimeSum / Math.max(1, correctResponses));

        double errorScore = 100.0 - (commissionErrors * 10.0) - (omissionErrors * 5.0);
        errorScore = Math.max(0, Math.min(100, errorScore));

        double rtScore = 100.0 - ((avgReactionTime - 300.0) * 0.15);
        rtScore = Math.max(0, Math.min(100, rtScore));

        double awarenessScore = (0.7 * errorScore) + (0.3 * rtScore);
        awarenessScore = Math.max(0, Math.min(100, awarenessScore));

        String result = "Accuracy: " + String.format(Locale.getDefault(), "%.1f", accuracy) + "%\n"
                + "Errors (commission): " + commissionErrors + "\n"
                + "Omissions: " + omissionErrors + "\n"
                + "Avg RT: " + avgReactionTime + " ms\n"
                + "Awareness Score: " + (int) Math.round(awarenessScore) + "/100";

        Toast.makeText(this, result, Toast.LENGTH_LONG).show();

        SharedPreferences sp = getSharedPreferences("CognitiveScores", MODE_PRIVATE);
        sp.edit()
                .putFloat("awareness_post_score", (float) awarenessScore)
                .putFloat("sart_accuracy", (float) accuracy)
                .putInt("sart_errors", commissionErrors)
                .putInt("sart_omissions", omissionErrors)
                .putLong("sart_reaction_time", avgReactionTime)
                .putLong("timestamp_post", System.currentTimeMillis())
                .apply();

        // mark post completed for this module
        getSharedPreferences("ModuleState", MODE_PRIVATE)
                .edit()
                .putBoolean(moduleType + "_post_completed", true)
                .apply();

        String date = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());
        ScoreHistoryStorage.addScoreHistory(this, "Awareness", (float) awarenessScore, date);

        handler.postDelayed(this::finish, 2500);
    }
}
