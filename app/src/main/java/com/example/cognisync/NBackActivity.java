package com.example.cognisync;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View; // ✅ FIXED: added missing import
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

    private final String[] letters = {"A", "B", "C", "D", "E", "F", "G", "H"};
    private final List<String> shown = new ArrayList<>();
    private int correct = 0, total = 0;

    private final Handler handler = new Handler();
    private final Random random = new Random();
    private boolean canRespond = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ✅ Hide action bar and set light status bar
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nback);

        // --- View Bindings ---
        tvLetter = findViewById(R.id.tvLetter);
        tvScore = findViewById(R.id.tvScore);
        btnMatch = findViewById(R.id.btnMatch);

        // --- Button Logic ---
        btnMatch.setOnClickListener(v -> {
            if (canRespond && shown.size() > 2) {
                String current = shown.get(shown.size() - 1);
                String twoBack = shown.get(shown.size() - 3);
                total++;
                if (current.equals(twoBack)) correct++;
            }
        });

        // --- Start Task ---
        startSequence();
    }

    /** Start the N-Back sequence */
    private void startSequence() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (shown.size() >= 20) { // after 20 trials
                    showResult();
                    return;
                }

                // Display random letter
                String letter = letters[random.nextInt(letters.length)];
                shown.add(letter);
                tvLetter.setText(letter);

                canRespond = true;

                // Show next letter after 1.5 seconds
                handler.postDelayed(this, 1500);
            }
        }, 1000);
    }

    /** Display final result */
    private void showResult() {
        canRespond = false;
        double accuracy = (total == 0) ? 0 : (correct * 100.0 / total);

        tvScore.setVisibility(View.VISIBLE);
        tvScore.setText(String.format(Locale.getDefault(), "Accuracy: %.1f%%", accuracy));

        // ✅ Save data to SharedPreferences for dashboard
        SharedPreferences sp = getSharedPreferences("CognitiveScores", MODE_PRIVATE);
        sp.edit()
                .putFloat("nback_accuracy", (float) accuracy)
                .putFloat("memory_post_score", (float) accuracy)
                .putLong("timestamp_post", System.currentTimeMillis())
                .apply();

        // ✅ Save to score history (normalized key handled automatically)
        String date = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());
        ScoreHistoryStorage.addScoreHistory(this, "Memory", (float) accuracy, date);

        // ✅ Auto close after 3 seconds
        handler.postDelayed(this::finish, 3000);
    }
}
