package com.example.cognisync;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cognisync.del.ApiClient;
import com.example.cognisync.del.ApiService;
import com.example.cognisync.model.ScoreRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import retrofit2.Call;

public class SARTActivity extends AppCompatActivity {

    private TextView tvNumber, tvInstruction, tvProgress;
    private Button btnStart;

    private ApiService api;

    // ===============================
    // CONFIG (USER-FRIENDLY)
    // ===============================
    private static final int TOTAL_TRIALS = 30;   // ⬅ reduced
    private int currentTrial = 0;

    // forbidden numbers
    private int noGo1;
    private int noGo2;

    private boolean canTap = false;
    private long lastTime = 0;

    private final Handler handler = new Handler();
    private final Random random = new Random();

    // METRICS
    private int commissionErrors = 0;
    private int omissionErrors = 0;
    private int correctResponses = 0;
    private long rtSum = 0;
    private int streak = 0;
    private int maxStreak = 0;

    private List<Integer> sequence = new ArrayList<>();
    private Runnable currentTrialRunnable = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sart);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        api = ApiClient.getClient().create(ApiService.class);

        tvNumber = findViewById(R.id.tvNumber);
        tvInstruction = findViewById(R.id.tvInstruction);
        tvProgress = findViewById(R.id.tvProgress);
        btnStart = findViewById(R.id.btnStart);

        tvNumber.setClickable(true);
        tvNumber.setOnClickListener(v -> handleTap());
        btnStart.setOnClickListener(v -> startTask());
    }

    // ============================================================
    // START TASK
    // ============================================================
    private void startTask() {

        btnStart.setVisibility(View.GONE);

        noGo1 = random.nextInt(10);
        do {
            noGo2 = random.nextInt(10);
        } while (noGo2 == noGo1);

        tvInstruction.setText("Tap all numbers EXCEPT " + noGo1 + " & " + noGo2);

        commissionErrors = 0;
        omissionErrors = 0;
        correctResponses = 0;
        rtSum = 0;
        currentTrial = 0;
        streak = 0;
        maxStreak = 0;

        generateSequence();
        nextTrial();
    }

    // ============================================================
    // GENERATE SEQUENCE (EASIER)
    // ============================================================
    private void generateSequence() {
        sequence.clear();

        int noGoTarget = TOTAL_TRIALS / 7; // ~14% no-go

        for (int i = 0; i < noGoTarget; i++) {
            sequence.add(noGo1);
            sequence.add(noGo2);
        }

        while (sequence.size() < TOTAL_TRIALS) {
            int n = random.nextInt(10);
            if (n == noGo1 || n == noGo2) continue;
            sequence.add(n);
        }

        Collections.shuffle(sequence);
    }

    // ============================================================
    // RUN TRIAL
    // ============================================================
    private void nextTrial() {

        if (currentTrialRunnable != null) {
            handler.removeCallbacks(currentTrialRunnable);
            currentTrialRunnable = null;
        }

        if (currentTrial >= TOTAL_TRIALS) {
            endTask();
            return;
        }

        int number = sequence.get(currentTrial);
        currentTrial++;

        tvNumber.setText(String.valueOf(number));
        tvProgress.setText("Trial " + currentTrial + " / " + TOTAL_TRIALS);

        lastTime = System.currentTimeMillis();
        canTap = true;

        // ⬅ More time to react
        int delay = 1000 + random.nextInt(500);

        currentTrialRunnable = () -> {
            if (canTap && number != noGo1 && number != noGo2) {
                omissionErrors++;
                streak = 0;
            }
            canTap = false;
            currentTrialRunnable = null;
            handler.postDelayed(this::nextTrial, 200);
        };

        handler.postDelayed(currentTrialRunnable, delay);
    }

    // ============================================================
    // TAP ACTION
    // ============================================================
    private void handleTap() {
        if (!canTap) return;

        long rt = System.currentTimeMillis() - lastTime;
        int number = Integer.parseInt(tvNumber.getText().toString());

        if (currentTrialRunnable != null) {
            handler.removeCallbacks(currentTrialRunnable);
            currentTrialRunnable = null;
        }

        if (number == noGo1 || number == noGo2) {
            commissionErrors++;
            streak = 0;
        } else {
            correctResponses++;
            rtSum += rt;
            streak++;
            if (streak > maxStreak) maxStreak = streak;
        }

        canTap = false;
        handler.postDelayed(this::nextTrial, 150);
    }

    // ============================================================
    // END — SCORE (GENEROUS)
    // ============================================================
    private void endTask() {

        handler.removeCallbacksAndMessages(null);

        tvNumber.setText("-");
        tvProgress.setText("");
        tvInstruction.setText("Task complete ✓");

        long avgRT = correctResponses == 0 ? 0 : rtSum / correctResponses;

        double accuracy = correctResponses * 100.0 / TOTAL_TRIALS;

        // ⬅ Much lighter penalties
        double penalty = commissionErrors * 5 + omissionErrors * 2;

        // ⬅ Friendly speed bonus
        double speedBonus = avgRT == 0 ? 0 : Math.max(0, (500 - avgRT) / 5);

        double streakBonus = maxStreak * 2.5;

        double finalScore =
                (accuracy * 0.65) +
                        speedBonus +
                        streakBonus -
                        penalty;

        finalScore = Math.max(0, Math.min(100, finalScore));

        Toast.makeText(this,
                String.format(Locale.getDefault(),
                        "Correct: %d\nOmissions: %d\nCommissions: %d\nAvgRT: %dms\nMaxStreak: %d\nScore: %.1f /100",
                        correctResponses, omissionErrors, commissionErrors,
                        avgRT, maxStreak, finalScore),
                Toast.LENGTH_LONG).show();

        sendScore((float) finalScore);

        handler.postDelayed(this::finish, 1800);
    }

    // ============================================================
    // API
    // ============================================================
    private void sendScore(float score) {
        String email = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getString("email", "");

        if (email.isEmpty()) return;

        ScoreRequest req = new ScoreRequest(email, score);
        Call<Void> call = api.saveSartPost(req);

        ScoreUploader.uploadScore(this, email, score, "SART Post", call);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
