package com.example.cognisync;

import android.content.Intent;
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

    private int trial = 0;
    private static final int TOTAL_TRIALS = 12;

    private long startTime;
    private int prevColor = -1;

    private final List<Long> sameRuleTimes = new ArrayList<>();
    private final List<Long> switchRuleTimes = new ArrayList<>();

    private int currentNumber;
    private int currentColor;

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

        btnLeft.setOnClickListener(v -> handleResponse(true));
        btnRight.setOnClickListener(v -> handleResponse(false));

        nextTrial();
    }

    private void nextTrial() {
        if (trial >= TOTAL_TRIALS) {
            showResult();
            return;
        }

        currentNumber = random.nextInt(9) + 1; // 1–9
        currentColor = random.nextBoolean() ? 0 : 1; // Red=0, Blue=1

        tvNumber.setText(String.valueOf(currentNumber));
        tvNumber.setTextColor(currentColor == 0 ? 0xFFFF0000 : 0xFF2196F3);

        startTime = System.currentTimeMillis();
        trial++;
    }

    private void handleResponse(boolean leftPressed) {
        long rt = System.currentTimeMillis() - startTime;

        if (prevColor != -1) {
            if (prevColor == currentColor) sameRuleTimes.add(rt);
            else switchRuleTimes.add(rt);
        }

        prevColor = currentColor;

        boolean isCorrect;

        if (currentColor == 0) {  // Red → small/large
            isCorrect = (currentNumber < 5 && leftPressed) ||
                    (currentNumber > 5 && !leftPressed);
        } else {  // Blue → even/odd
            isCorrect = ((currentNumber % 2 == 0) && leftPressed) ||
                    ((currentNumber % 2 != 0) && !leftPressed);
        }

        tvResult.setText(isCorrect ? "" : "Incorrect!");

        handler.postDelayed(this::nextTrial, 700);
    }

    private void showResult() {
        long avgSame = average(sameRuleTimes);
        long avgSwitch = average(switchRuleTimes);

        long switchCost = Math.abs(avgSwitch - avgSame);

        double flexibilityScore = 100.0 * Math.exp(-((double) switchCost) / 180.0);
        flexibilityScore = Math.max(0, Math.min(100, flexibilityScore));

        String interpretation;
        if (flexibilityScore >= 75) interpretation = "Excellent flexibility";
        else if (flexibilityScore >= 45) interpretation = "Average";
        else interpretation = "Low flexibility";

        tvResult.setText(
                String.format(Locale.getDefault(),
                        "Switch Cost: %d ms\nScore: %.1f /100\n%s",
                        switchCost, flexibilityScore, interpretation)
        );

        // --------- 🔥 SEND TO BACKEND ONLY ---------
        String email = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getString("email", "");

        if (email.isEmpty()) {
            Toast.makeText(this, "Error: No user email saved!", Toast.LENGTH_LONG).show();
        } else {
            ScoreRequest req = new ScoreRequest(email, (float) flexibilityScore);
            Call<Void> call = api.saveTaskSwitchPost(req);

            ScoreUploader.uploadScore(
                    this,
                    email,
                    (float) flexibilityScore,
                    "TaskSwitch post",
                    call
            );
        }

        handler.postDelayed(this::finish, 2500);
    }

    private long average(List<Long> list) {
        if (list == null || list.isEmpty()) return 0;
        long sum = 0;
        for (Long v : list) sum += v;
        return sum / list.size();
    }
}
