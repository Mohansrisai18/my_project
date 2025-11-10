package com.example.cognisync;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.*;

public class NBackActivity extends AppCompatActivity {

    private TextView tvLetter, tvScore;
    private Button btnMatch;

    private final String[] letters = {"A","B","C","D","E","F","G","H"};
    private final List<String> shown = new ArrayList<>();
    private int correct = 0, total = 0;

    private Handler handler = new Handler();
    private Random random = new Random();
    private boolean canRespond = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getSupportActionBar()!=null) getSupportActionBar().hide();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nback);

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
        tvScore.setText(String.format("Accuracy: %.1f%%", accuracy));

        SharedPreferences sp = getSharedPreferences("CognitiveScores", MODE_PRIVATE);
        sp.edit()
                .putFloat("nback_accuracy", (float) accuracy)
                .putLong("timestamp_post", System.currentTimeMillis())
                .apply();

        String date = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());
        float wmScore = (float) accuracy;
        ScoreHistoryStorage.addScoreHistory(this, "Memory", wmScore / 14f * 7, date);
    }
}
