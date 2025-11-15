package com.example.cognisync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

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

    private String moduleType = "cognitive_flexibility";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_switch);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        Intent i = getIntent();
        if (i != null && i.hasExtra("module_type")) moduleType = i.getStringExtra("module_type");

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

        currentNumber = random.nextInt(9) + 1;
        currentColor = random.nextBoolean() ? 0 : 1;

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
        if (currentColor == 0) {
            isCorrect = (currentNumber < 5 && leftPressed) || (currentNumber > 5 && !leftPressed);
        } else {
            isCorrect = ((currentNumber % 2 == 0) && leftPressed) || ((currentNumber % 2 != 0) && !leftPressed);
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

        tvResult.setText(String.format(Locale.getDefault(),
                "Switch Cost: %d ms\nScore: %.1f /100\n%s", switchCost, flexibilityScore, interpretation));

        SharedPreferences sp = getSharedPreferences("CognitiveScores", MODE_PRIVATE);
        sp.edit()
                .putLong("switch_cost_ms", switchCost)
                .putFloat("flexibility_post_score", (float) flexibilityScore)
                .putLong("timestamp_post", System.currentTimeMillis())
                .apply();

        // mark post completed for this module
        getSharedPreferences("ModuleState", MODE_PRIVATE)
                .edit()
                .putBoolean(moduleType + "_post_completed", true)
                .apply();

        String date = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());
        ScoreHistoryStorage.addScoreHistory(this, "Cognitive", (float) flexibilityScore, date);

        handler.postDelayed(this::finish, 2500);
    }

    private long average(List<Long> list) {
        if (list == null || list.isEmpty()) return 0;
        long sum = 0;
        for (Long value : list) sum += value;
        return sum / list.size();
    }
}
