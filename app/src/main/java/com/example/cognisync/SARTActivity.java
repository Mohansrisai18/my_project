package com.example.cognisync;

import android.os.Bundle;
import android.os.Handler;
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
    // CONFIG
    // ===============================
    private static final int TOTAL_TRIALS = 50;
    private int currentTrial = 0;

    // two forbidden numbers
    private int noGo1;
    private int noGo2;

    private boolean canTap = false;
    private long lastTime = 0;

    private final Handler handler = new Handler();
    private final Random random = new Random();

    // METRICS
    private int commissionErrors = 0; // tapped forbidden
    private int omissionErrors = 0;   // failed to tap allowed
    private int correctResponses = 0;
    private long rtSum = 0;
    private int streak = 0;

    private List<Integer> sequence = new ArrayList<>();
    private List<Long> reactionTimes = new ArrayList<>();


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

        btnStart.setOnClickListener(v -> startTask());
        tvNumber.setOnClickListener(v -> handleTap());
    }

    // ============================================================
    // START TASK
    // ============================================================
    private void startTask() {

        btnStart.setVisibility(Button.GONE);

        // ------------------------
        // Generate random forbidden numbers
        // ------------------------
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
        reactionTimes.clear();

        generateSequence();
        nextTrial();
    }

    // ============================================================
    // GENERATE SEQUENCE
    // (Higher % of go trials for difficulty)
    // ============================================================
    private void generateSequence() {
        sequence.clear();

        int noGoTarget = TOTAL_TRIALS / 5;  // 20% forbidden trials

        // Insert forbidden
        for (int i = 0; i < noGoTarget; i++) {
            sequence.add(noGo1);
            sequence.add(noGo2);
        }

        // Insert allowed digits
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

        // HARDER: variable presentation
        int delay = 600 + random.nextInt(500);

        handler.postDelayed(() -> {

            if (canTap) {
                if (number != noGo1 && number != noGo2) {
                    // should tap
                    omissionErrors++;
                    streak = 0;
                }
            }

            canTap = false;
            nextTrial();
        }, delay);
    }

    // ============================================================
    // TAP ACTION
    // ============================================================
    private void handleTap() {
        if (!canTap) return;

        long rt = System.currentTimeMillis() - lastTime;
        int number = Integer.parseInt(tvNumber.getText().toString());

        if (number == noGo1 || number == noGo2) {
            commissionErrors++;
            streak = 0;
        } else {
            correctResponses++;
            rtSum += rt;
            reactionTimes.add(rt);
            streak++;
        }

        canTap = false;
    }

    // ============================================================
    // END — SCORE
    // ============================================================
    private void endTask() {

        tvNumber.setText("-");
        tvProgress.setText("");
        tvInstruction.setText("Task complete ✓");

        long avgRT = (correctResponses == 0) ? 0 : rtSum / correctResponses;

        int totalResp = correctResponses + omissionErrors + commissionErrors;
        double accuracy = totalResp == 0 ? 0 : (correctResponses * 100.0 / totalResp);

        // PENALTIES — stronger
        double penalty = commissionErrors * 15 + omissionErrors * 4;

        // SPEED BONUS
        double speedBonus = Math.max(0, (350 - avgRT) / 3);

        // STREAK BONUS
        double streakBonus = 0;
        if (streak >= 4) streakBonus += 5;
        if (streak >= 7) streakBonus += 12;
        if (streak >= 10) streakBonus += 20;

        double finalScore = (accuracy * 0.55) + speedBonus + streakBonus - penalty;
        if (finalScore < 0) finalScore = 0;
        if (finalScore > 100) finalScore = 100;

        Toast.makeText(this,
                String.format(Locale.getDefault(),
                        "NoGo: %d & %d\nCorrect: %d\nOmissions: %d\nCommissions: %d\nAvgRT: %dms\nScore: %.1f /100",
                        noGo1, noGo2,
                        correctResponses, omissionErrors, commissionErrors,
                        avgRT, finalScore),
                Toast.LENGTH_LONG).show();

        sendScore((float) finalScore);

        handler.postDelayed(this::finish, 3000);
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

}
