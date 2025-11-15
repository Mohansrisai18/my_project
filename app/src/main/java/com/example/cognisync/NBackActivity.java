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

public class NBackActivity extends AppCompatActivity {

    private TextView tvLetter, tvScore;
    private Button btnMatch;

    private final String[] letters = {"A","B","C","D","E","F","G","H"};
    private final List<String> shown = new ArrayList<>();
    private int correct = 0, total = 0;

    private final Handler handler = new Handler();
    private final Random random = new Random();
    private boolean canRespond = false;

    private String moduleType = "working_memory";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nback);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        Intent i = getIntent();
        if (i != null && i.hasExtra("module_type")) moduleType = i.getStringExtra("module_type");

        tvLetter = findViewById(R.id.tvLetter);
        tvScore = findViewById(R.id.tvScore);
        btnMatch = findViewById(R.id.btnMatch);

        btnMatch.setOnClickListener(v -> {
            if (canRespond && shown.size() > 2) {
                String current = shown.get(shown.size() - 1);
                String twoBack = shown.get(shown.size() - 3);
                total++;
                if (current.equals(twoBack)) correct++;
            }
        });

        startSequence();
    }

    private void startSequence() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (shown.size() >= 20) {
                    showResult();
                    return;
                }
                String letter = letters[random.nextInt(letters.length)];
                shown.add(letter);
                tvLetter.setText(letter);
                canRespond = true;
                handler.postDelayed(this, 1500);
            }
        }, 1000);
    }

    private void showResult() {
        canRespond = false;
        double accuracy = (total == 0) ? 0 : (correct * 100.0 / total);

        tvScore.setVisibility(View.VISIBLE);
        tvScore.setText(String.format(Locale.getDefault(), "Accuracy: %.1f%%", accuracy));

        SharedPreferences sp = getSharedPreferences("CognitiveScores", MODE_PRIVATE);
        sp.edit()
                .putFloat("nback_accuracy", (float) accuracy)
                .putFloat("memory_post_score", (float) accuracy)
                .putLong("timestamp_post", System.currentTimeMillis())
                .apply();

        // mark post completed for this module
        getSharedPreferences("ModuleState", MODE_PRIVATE)
                .edit()
                .putBoolean(moduleType + "_post_completed", true)
                .apply();

        String date = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());
        ScoreHistoryStorage.addScoreHistory(this, "Memory", (float) accuracy, date);

        handler.postDelayed(this::finish, 2500);
    }
}
