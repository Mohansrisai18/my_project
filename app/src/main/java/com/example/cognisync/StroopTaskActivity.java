package com.example.cognisync;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cognisync.del.ApiClient;
import com.example.cognisync.del.ApiService;
import com.example.cognisync.model.ScoreRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import retrofit2.Call;

public class StroopTaskActivity extends AppCompatActivity {

    private TextView tvWord, tvResult;
    private Button btnRed, btnBlue, btnGreen;
    private ImageButton backButton;

    // === WORD MAPPING ===
    // Emotional → Always RED
    private final String[] emotionalWords = {"ANGER", "FEAR"};

    // Neutral words use BLUE or GREEN
    private final String[] neutralWords = {"JOY", "LOVE", "CALM"};

    // UI colors → for interference only
    private final int[] colors = {
            0xFFF44336, // RED
            0xFF2196F3, // BLUE
            0xFF4CAF50  // GREEN
    };

    private final Random random = new Random();
    private final Handler handler = new Handler();

    // Task flow
    private static final int TOTAL_TRIALS = 12;
    private int trial = 0;

    // Stats
    private int correct = 0;
    private int wrong = 0;
    private long startTime;

    private final List<Long> neutralTimes = new ArrayList<>();
    private final List<Long> emotionalTimes = new ArrayList<>();

    private ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stroop_task);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        api = ApiClient.getClient().create(ApiService.class);

        tvWord = findViewById(R.id.tvWord);
        tvResult = findViewById(R.id.tvResult);
        btnRed = findViewById(R.id.btnRed);
        btnBlue = findViewById(R.id.btnBlue);
        btnGreen = findViewById(R.id.btnGreen);
        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> finish());

        // Player chooses the button according to the WORD category
        View.OnClickListener listener = v -> {
            long rt = System.currentTimeMillis() - startTime;
            String word = tvWord.getText().toString();

            int chosen;
            if (v == btnRed) chosen = 0;
            else if (v == btnBlue) chosen = 1;
            else chosen = 2;

            int correctColor = getCorrectColor(word);

            // Accuracy
            if (chosen == correctColor) {
                correct++;
                storeReaction(rt, word);
            } else {
                wrong++;
            }

            nextTrial();
        };

        btnRed.setOnClickListener(listener);
        btnBlue.setOnClickListener(listener);
        btnGreen.setOnClickListener(listener);

        nextTrial();
    }

    // =========================================
    // WORD → CORRECT BUTTON COLOR
    // =========================================
    private int getCorrectColor(String word) {
        if (word.equals("ANGER") || word.equals("FEAR"))
            return 0; // RED button
        if (word.equals("JOY"))
            return 2; // GREEN button
        return 1; // CALM / LOVE → BLUE
    }

    // =========================================
    // TRIAL FLOW
    // =========================================
    private void nextTrial() {
        if (trial >= TOTAL_TRIALS) {
            showResult();
            return;
        }

        trial++;

        boolean emo = random.nextBoolean();
        String word = emo ? emotionalWords[random.nextInt(emotionalWords.length)]
                : neutralWords[random.nextInt(neutralWords.length)];

        // UI color = random (not meaning)
        int colorIndex = random.nextInt(colors.length);
        tvWord.setText(word);
        tvWord.setTextColor(colors[colorIndex]);

        startTime = System.currentTimeMillis();
    }

    private void storeReaction(long rt, String word) {
        if (word.equals("ANGER") || word.equals("FEAR"))
            emotionalTimes.add(rt);
        else
            neutralTimes.add(rt);
    }

    // =========================================
    // SCORE CALCULATION
    // =========================================
    private void showResult() {

        int total = correct + wrong;

        // 1️⃣ Accuracy → 70%
        double accuracy = total == 0 ? 0 : (correct * 100.0 / total);

        // 2️⃣ Speed → AVG RT → 20%
        long avgNeutral = average(neutralTimes);
        long avgEmotional = average(emotionalTimes);
        long avgRT = (avgNeutral + avgEmotional) / 2;

        double speedScore = 100 - (avgRT - 450) / 3.0;
        speedScore = clamp(speedScore);

        // 3️⃣ Interference → delta → 10%
        long delta = Math.abs(avgEmotional - avgNeutral);
        double interferenceScore = 100 - (delta / 5.0);
        interferenceScore = clamp(interferenceScore);

        double finalScore =
                (accuracy * 0.70)
                        + (speedScore * 0.20)
                        + (interferenceScore * 0.10);

        finalScore = clamp(finalScore);

        tvResult.setVisibility(View.VISIBLE);
        tvResult.setText(String.format(Locale.getDefault(),
                "Correct: %d  Wrong: %d\nΔ: %d ms\nScore: %.1f /100",
                correct, wrong, delta, finalScore));

        sendScore((float) finalScore);

        handler.postDelayed(this::finish, 3000);
    }

    // =========================================
    // BACKEND UPLOAD
    // =========================================
    private void sendScore(float score) {

        String email = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getString("email", "");

        if (email.isEmpty()) {
            Toast.makeText(this, "No user email saved!", Toast.LENGTH_LONG).show();
            return;
        }

        ScoreRequest req = new ScoreRequest(email, score);
        Call<Void> call = api.saveStroopPost(req);

        ScoreUploader.uploadScore(
                this,
                email,
                score,
                "Stroop Post",
                call
        );
    }

    // =========================================
    // HELPERS
    // =========================================
    private long average(List<Long> list) {
        if (list.isEmpty()) return 0;
        long sum = 0;
        for (Long v : list) sum += v;
        return sum / list.size();
    }

    private double clamp(double v) {
        return Math.max(0, Math.min(100, v));
    }
}
