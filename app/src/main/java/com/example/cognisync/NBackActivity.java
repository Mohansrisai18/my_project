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

    private TextView tvLetter, tvScore;
    private Button btnMatch;

    private final String[] letters = {"A","B","C","D","E","F","G","H"};
    private final List<String> shown = new ArrayList<>();
    private int correct = 0, total = 0;

    private final Handler handler = new Handler();
    private final Random random = new Random();
    private boolean canRespond = false;

    private String moduleType = "working_memory";

    private ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nback);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        api = ApiClient.getClient().create(ApiService.class);

        Intent i = getIntent();
        if (i != null && i.hasExtra("module_type"))
            moduleType = i.getStringExtra("module_type");

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

        // ---------------------------
        // 🔥 SEND SCORE TO BACKEND ONLY
        // ---------------------------
        String email = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getString("email", "");

        if (email.isEmpty()) {
            Toast.makeText(this, "Error: No user email stored!", Toast.LENGTH_LONG).show();
        } else {
            // Create request
            ScoreRequest req = new ScoreRequest(email, (float) accuracy);

            // API call
            Call<Void> call = api.saveNbackPost(req);

            // Upload helper (shows toast if success/fail)
            ScoreUploader.uploadScore(
                    this,
                    email,
                    (float) accuracy,
                    "N-Back Post",
                    call
            );
        }

        handler.postDelayed(this::finish, 2500);
    }
}
