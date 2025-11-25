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
import java.util.List;
import java.util.Locale;
import java.util.Random;

import retrofit2.Call;

public class TaskSwitchActivity extends AppCompatActivity {

    private TextView tvNumber, tvInstruction, tvResult;
    private Button btnLeft, btnRight;

    private final Random random = new Random();
    private final Handler handler = new Handler();

    private static final int TOTAL_TRIALS = 14;
    private int trial = 0;

    private long startTime;
    private int prevColor = -1;

    private int correctCount = 0;
    private int wrongCount = 0;

    private final List<Long> sameRuleTimes = new ArrayList<>();
    private final List<Long> switchRuleTimes = new ArrayList<>();

    private int currentNumber;
    private int currentColor; // 0 = RED, 1 = BLUE

    private ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_switch);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        api = ApiClient.getClient().create(ApiService.class);

        tvInstruction = findViewById(R.id.tvInstruction);
        tvNumber = findViewById(R.id.tvNumber);
        tvResult = findViewById(R.id.tvResult);

        btnLeft = findViewById(R.id.btnLeft);
        btnRight = findViewById(R.id.btnRight);

        // keep UI text same
        tvInstruction.setText(
                "RED → Left(<5), Right(≥5)\nBLUE → Left(Odd), Right(Even)"
        );

        btnLeft.setOnClickListener(v -> handleResponse(true));
        btnRight.setOnClickListener(v -> handleResponse(false));

        nextTrial();
    }

    private void nextTrial() {
        if (trial >= TOTAL_TRIALS) {
            showResult();
            return;
        }

        currentNumber = random.nextInt(9) + 1;
        currentColor = random.nextBoolean() ? 0 : 1; // 0=Red, 1=Blue

        tvNumber.setText(String.valueOf(currentNumber));
        tvNumber.setTextColor(currentColor == 0 ? 0xFFFF0000 : 0xFF2196F3);

        startTime = System.currentTimeMillis();
        trial++;
    }

    private void handleResponse(boolean leftPressed) {
        long rt = System.currentTimeMillis() - startTime;

        // ignore spam taps
        if (rt < 200) return;

        // store switch or same rule
        if (prevColor != -1) {
            if (prevColor == currentColor) sameRuleTimes.add(rt);
            else switchRuleTimes.add(rt);
        }
        prevColor = currentColor;

        boolean isCorrect;

        // 🔴 RED = quantity rule
        if (currentColor == 0) {
            isCorrect = (currentNumber < 5 && leftPressed) ||
                    (currentNumber >= 5 && !leftPressed);
        }
        // 🔵 BLUE = parity rule
        else {
            isCorrect = ((currentNumber % 2 != 0) && leftPressed) ||
                    ((currentNumber % 2 == 0) && !leftPressed);
        }

        if (isCorrect) correctCount++;
        else wrongCount++;

        tvResult.setText(isCorrect ? "" : "Incorrect!");
        handler.postDelayed(this::nextTrial, 500);
    }

    private void showResult() {

        long avgSame = average(sameRuleTimes);
        long avgSwitch = average(switchRuleTimes);

        long switchCost = Math.abs(avgSwitch - avgSame);

        // -----------------------------
        // 1️⃣ Accuracy (60%)
        // -----------------------------
        double accuracy = correctCount / (double) (correctCount + wrongCount);
        double accuracyScore = accuracy * 60.0;

        // -----------------------------
        // 2️⃣ Reaction Time (25%)
        // linear (mild penalty)
        // -----------------------------
        double avgRT = (avgSame + avgSwitch) / 2.0;
        avgRT = Math.min(avgRT, 1200);  // cap slow RT

        double reactionScore = 25.0 * (1.0 - avgRT / 1200.0);

        // -----------------------------
        // 3️⃣ Switch Cost (15%)
        // small penalty
        // -----------------------------
        switchCost = Math.min(switchCost, 600);  // cap switch cost
        double switchScore = 15.0 * (1.0 - switchCost / 600.0);

        double finalScore = accuracyScore + reactionScore + switchScore;
        finalScore = clamp(finalScore);

        tvResult.setText(String.format(Locale.getDefault(),
                "Correct: %d  Wrong: %d\nSwitch Cost: %d ms\nScore: %.1f /100",
                correctCount, wrongCount, switchCost, finalScore));

        sendScore((float) finalScore);
        handler.postDelayed(this::finish, 2500);
    }

    private void sendScore(float score) {
        String email = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getString("email", "");

        if (email.isEmpty()) {
            Toast.makeText(this, "Error: No user email saved!", Toast.LENGTH_LONG).show();
            return;
        }

        ScoreRequest req = new ScoreRequest(email, score);
        Call<Void> call = api.saveTaskSwitchPost(req);

        ScoreUploader.uploadScore(
                this,
                email,
                score,
                "TaskSwitch Post",
                call
        );
    }

    private long average(List<Long> list) {
        if (list.isEmpty()) return 0;
        long sum = 0;
        for (long value : list) sum += value;
        return sum / list.size();
    }

    private double clamp(double v) {
        return Math.max(0, Math.min(100, v));
    }
}
