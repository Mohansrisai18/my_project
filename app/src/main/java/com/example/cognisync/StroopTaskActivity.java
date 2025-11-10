package com.example.cognisync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class StroopTaskActivity extends AppCompatActivity {

    private TextView tvWord, tvResult;
    private Button btnRed, btnBlue, btnGreen;
    private ImageButton backButton;

    private final String[] words = {"ANGER", "CALM", "JOY", "FEAR", "LOVE"};
    private final int[] colors = {0xFFF44336, 0xFF2196F3, 0xFF4CAF50}; // red, blue, green
    private final String[] colorNames = {"RED", "BLUE", "GREEN"};

    private int trial = 0;
    private long startTime;
    private final List<Long> neutralTimes = new ArrayList<>();
    private final List<Long> emotionalTimes = new ArrayList<>();
    private final Random random = new Random();
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stroop_task);

        // UI elements
        tvWord = findViewById(R.id.tvWord);
        tvResult = findViewById(R.id.tvResult);
        btnRed = findViewById(R.id.btnRed);
        btnBlue = findViewById(R.id.btnBlue);
        btnGreen = findViewById(R.id.btnGreen);
        backButton = findViewById(R.id.backButton);

        // Back button → go to ModuleVideoActivity
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(StroopTaskActivity.this, ModuleVideoActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // Common button listener
        View.OnClickListener listener = v -> {
            long rt = System.currentTimeMillis() - startTime;
            if (isEmotional(tvWord.getText().toString())) emotionalTimes.add(rt);
            else neutralTimes.add(rt);
            nextTrial();
        };

        btnRed.setOnClickListener(listener);
        btnBlue.setOnClickListener(listener);
        btnGreen.setOnClickListener(listener);

        // Start task
        nextTrial();
    }

    private void nextTrial() {
        if (trial >= 10) {
            showResult();
            return;
        }

        String word = words[random.nextInt(words.length)];
        int color = colors[random.nextInt(colors.length)];

        tvWord.setText(word);
        tvWord.setTextColor(color);

        startTime = System.currentTimeMillis();
        trial++;
    }

    private boolean isEmotional(String word) {
        return word.equals("ANGER") || word.equals("FEAR");
    }

    // ... truncated for brevity ...
// Replace showResult() with:

    private void showResult() {
        long avgNeutral = average(neutralTimes);
        long avgEmotional = average(emotionalTimes);
        long delta = Math.abs(avgEmotional - avgNeutral);

        String interpretation;
        float emotionScore;
        if (delta < 50) {
            interpretation = "Good emotion regulation";
            emotionScore = 100;
        } else if (delta <= 150) {
            interpretation = "Moderate interference";
            emotionScore = 70;
        } else {
            interpretation = "High reactivity";
            emotionScore = 40;
        }

        tvResult.setText("ΔRT = " + delta + " ms\n" + interpretation);

        SharedPreferences sp = getSharedPreferences("CognitiveScores", MODE_PRIVATE);
        sp.edit()
                .putLong("stroop_delta", delta)
                .putFloat("emotion_post_score", emotionScore)
                .putLong("timestamp_post", System.currentTimeMillis())
                .apply();

        String date = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());
        ScoreHistoryStorage.addScoreHistory(this, "Emotional", emotionScore / 14f * 7, date);

        handler.postDelayed(() -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }, 3000);
    }

    // ✅ Add this helper method below:
    private long average(List<Long> list) {
        if (list == null || list.isEmpty()) return 0;
        long sum = 0;
        for (Long value : list) {
            sum += value;
        }
        return sum / list.size();
    }
}