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

    private Random random = new Random();
    private Handler handler = new Handler();

    private int trial = 0;
    private static final int TOTAL_TRIALS = 12;

    private long startTime;
    private long prevColor = -1;
    private List<Long> sameRuleTimes = new ArrayList<>();
    private List<Long> switchRuleTimes = new ArrayList<>();

    private int currentNumber;
    private int currentColor; // 0 = red (<5/>5), 1 = blue (odd/even)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_switch);

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

        currentNumber = random.nextInt(9) + 1; // 1-9
        currentColor = random.nextBoolean() ? 0 : 1; // 0 = red, 1 = blue

        tvNumber.setText(String.valueOf(currentNumber));
        tvNumber.setTextColor(currentColor == 0 ? 0xFFFF0000 : 0xFF2196F3);

        startTime = System.currentTimeMillis();
        trial++;
    }

    private void handleResponse(boolean leftPressed) {
        long rt = System.currentTimeMillis() - startTime;

        // Determine if this trial is a "switch" from the previous color
        if (prevColor != -1) {
            if (prevColor == currentColor) sameRuleTimes.add(rt);
            else switchRuleTimes.add(rt);
        }

        prevColor = currentColor;

        // Basic feedback rule validation
        boolean isCorrect;
        if (currentColor == 0) { // RED rule → less than 5 = left, greater than 5 = right
            isCorrect = (currentNumber < 5 && leftPressed) || (currentNumber > 5 && !leftPressed);
        } else { // BLUE rule → even = left, odd = right
            isCorrect = ((currentNumber % 2 == 0) && leftPressed) || ((currentNumber % 2 != 0) && !leftPressed);
        }

        tvResult.setText(isCorrect ? "" : "Incorrect!");

        handler.postDelayed(this::nextTrial, 700);
    }

    private void showResult() {
        long avgSame = average(sameRuleTimes);
        long avgSwitch = average(switchRuleTimes);
        long switchCost = Math.abs(avgSwitch - avgSame);

        String interpretation;
        float flexibilityScore;

        if (switchCost < 100) {
            interpretation = "Excellent flexibility";
            flexibilityScore = 100;
        } else if (switchCost <= 250) {
            interpretation = "Average";
            flexibilityScore = 70;
        } else {
            interpretation = "Low flexibility";
            flexibilityScore = 40;
        }

        tvResult.setText("Switch Cost: " + switchCost + " ms\n" + interpretation);

        // ✅ Save results to SharedPreferences for dashboard and tracking
        SharedPreferences sp = getSharedPreferences("CognitiveScores", MODE_PRIVATE);
        sp.edit()
                .putLong("switch_cost_ms", switchCost)
                .putFloat("flexibility_post_score", flexibilityScore)
                .putLong("timestamp_post", System.currentTimeMillis())
                .apply();

        // ✅ Store result for progress dashboard
        String date = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());
        ScoreHistoryStorage.addScoreHistory(this, "Cognitive", flexibilityScore / 14f * 7, date);

        // Auto-return to Home after 3s
        handler.postDelayed(() -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }, 3000);
    }

    // ✅ Helper method for average calculation
    private long average(List<Long> list) {
        if (list == null || list.isEmpty()) return 0;
        long sum = 0;
        for (Long value : list) {
            sum += value;
        }
        return sum / list.size();
    }
}
