package com.example.cognisync;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
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

public class NBackActivity extends AppCompatActivity {

    private TextView tvLetter, tvScore, tvInfo;
    private Button btnMatch;

    private final String[] letters = {"A","B","C","D","E","F","G","H"};

    private final List<String> shown = new ArrayList<>();
    private int hits = 0;            // taps when match
    private int totalTaps = 0;       // total button presses
    private int totalTrials = 0;     // number of letters shown

    private final Handler handler = new Handler();
    private final Random random = new Random();

    private boolean canRespond = false;
    private static final int MAX_TRIALS = 24;  // you can reduce/increase

    private ApiService api;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nback);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        api = ApiClient.getClient().create(ApiService.class);
        email = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getString("email", "");

        tvLetter = findViewById(R.id.tvLetter);
        tvScore = findViewById(R.id.tvScore);
        tvInfo  = findViewById(R.id.tvInfo);
        btnMatch = findViewById(R.id.btnMatch);

        tvScore.setVisibility(View.INVISIBLE);

        btnMatch.setOnClickListener(v -> {
            if (canRespond) {
                totalTaps++;

                if (shown.size() >= 3) {
                    String cur  = shown.get(shown.size()-1);
                    String prev = shown.get(shown.size()-3);

                    if (cur.equals(prev)) {
                        hits++;
                    }
                }
            }
        });

        startSequence();
    }

    // ===============================================================
    // MAIN SEQUENCE
    // ===============================================================
    private void startSequence() {

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (totalTrials >= MAX_TRIALS) {
                    showResult();
                    return;
                }

                String letter = letters[random.nextInt(letters.length)];
                shown.add(letter);
                totalTrials++;

                tvLetter.setText(letter);
                canRespond = true;

                handler.postDelayed(this, 1500);
            }
        }, 1000);
    }

    // ===============================================================
    // SCORING
    // ===============================================================
    private void showResult() {
        canRespond = false;

        int totalMatches = 0;
        int misses = 0;
        int falseAlarms = 0;

        // count real matches
        for (int i = 2; i < shown.size(); i++) {
            if (shown.get(i).equals(shown.get(i - 2))) {
                totalMatches++;
            }
        }

        // false alarms = taps that were wrong
        falseAlarms = Math.max(0, totalTaps - hits);

        // misses = match position but no tap
        misses = Math.max(0, totalMatches - hits);

        // ----------------------------
        // EASY FAIR SCORE
        // ----------------------------
        double rawScore =
                (hits * 5)       // good!
                        - (misses * 2)   // forgot / low WM
                        - (falseAlarms * 3);  // impulsive taps

        if (rawScore < 0) rawScore = 0;

        double maxScore = totalMatches * 5.0;
        double normalizedScore =
                (maxScore == 0) ? 0 : (rawScore / maxScore) * 100;

        // clamp to 0–100
        if (normalizedScore > 100) normalizedScore = 100;
        if (normalizedScore < 0) normalizedScore = 0;

        // UI
        tvScore.setVisibility(View.VISIBLE);
        tvScore.setText(String.format(Locale.getDefault(),
                "Score: %.1f /100\nHits=%d  Miss=%d  False=%d",
                normalizedScore, hits, misses, falseAlarms));

        tvInfo.setText("Task completed ✓");

        sendScore((float) normalizedScore);

        handler.postDelayed(this::finish, 2500);
    }

    // ===============================================================
    // API CALL
    // ===============================================================
    private void sendScore(float score) {

        if (email.isEmpty()) {
            Toast.makeText(this, "No user email stored!", Toast.LENGTH_LONG).show();
            return;
        }

        ScoreRequest req = new ScoreRequest(email, score);
        Call<Void> call  = api.saveNbackPost(req);

        ScoreUploader.uploadScore(
                this,
                email,
                score,
                "N-Back Post",
                call
        );
    }
}
