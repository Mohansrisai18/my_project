package com.example.cognisync;

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
    private final Random random = new Random();
    private final Handler handler = new Handler();

    private int trial = 0;
    private long startTime;
    private final List<Long> neutralTimes = new ArrayList<>();
    private final List<Long> emotionalTimes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Hide ActionBar and set status bar light mode
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stroop_task);

        // --- Bind Views ---
        tvWord = findViewById(R.id.tvWord);
        tvResult = findViewById(R.id.tvResult);
        btnRed = findViewById(R.id.btnRed);
        btnBlue = findViewById(R.id.btnBlue);
        btnGreen = findViewById(R.id.btnGreen);
        backButton = findViewById(R.id.backButton);

        // Back button closes the activity
        backButton.setOnClickListener(v -> finish());

        // Common button listener for color choices
        View.OnClickListener listener = v -> {
            long rt = System.currentTimeMillis() - startTime;
            String word = tvWord.getText().toString();
            if (isEmotional(word)) emotionalTimes.add(rt);
            else neutralTimes.add(rt);
            nextTrial();
        };

        btnRed.setOnClickListener(listener);
        btnBlue.setOnClickListener(listener);
        btnGreen.setOnClickListener(listener);

        nextTrial();
    }

    /** Show next word trial */
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

    /** Check if word is emotionally charged */
    private boolean isEmotional(String word) {
        return word.equals("ANGER") || word.equals("FEAR");
    }

    /** Show result summary and save data */
    private void showResult() {
        long avgNeutral = average(neutralTimes);
        long avgEmotional = average(emotionalTimes);
        long delta = Math.abs(avgEmotional - avgNeutral);

        String interpretation;
        float emotionScore;

        if (delta < 50) {
            interpretation = "Excellent emotional regulation";
            emotionScore = 100f;
        } else if (delta <= 150) {
            interpretation = "Moderate emotion control";
            emotionScore = 70f;
        } else {
            interpretation = "High emotional reactivity";
            emotionScore = 40f;
        }

        tvResult.setVisibility(View.VISIBLE);
        tvResult.setText(String.format(Locale.getDefault(),
                "Δ Reaction Time = %d ms\n%s", delta, interpretation));

        // ✅ Save score to main shared preferences (used by graph)
        SharedPreferences sp = getSharedPreferences("CognitiveScores", MODE_PRIVATE);
        sp.edit()
                .putLong("stroop_delta", delta)
                .putFloat("emotion_post_score", emotionScore)
                .putLong("timestamp_post", System.currentTimeMillis())
                .apply();

        // ✅ Save in history with date + time
        String date = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());
        String time = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
        ScoreHistoryStorage.addScoreHistory(this, "Emotional", emotionScore, date + " • " + time);

        // ✅ Auto close after short delay
        handler.postDelayed(this::finish, 3000);
    }

    /** Compute average reaction time */
    private long average(List<Long> list) {
        if (list == null || list.isEmpty()) return 0;
        long sum = 0;
        for (Long value : list) sum += value;
        return sum / list.size();
    }
}
