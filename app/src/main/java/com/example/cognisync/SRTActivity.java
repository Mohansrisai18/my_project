package com.example.cognisync;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.widget.ImageButton;
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

public class SRTActivity extends AppCompatActivity {

    private TextView tvInstruction, tvDot, tvResult;
    private ImageButton backButton;
    private Handler handler = new Handler();
    private Random random = new Random();

    private long startTime;
    private int trialCount = 0;
    private static final int TOTAL_TRIALS = 10;
    private boolean dotVisible = false;

    private int screenWidth, screenHeight;
    private final List<Long> reactionTimes = new ArrayList<>();

    private String moduleType = "focused_attention";

    private ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_srt);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        api = ApiClient.getClient().create(ApiService.class);

        Intent i = getIntent();
        if (i != null && i.hasExtra("module_type"))
            moduleType = i.getStringExtra("module_type");

        tvInstruction = findViewById(R.id.tvInstruction);
        tvDot = findViewById(R.id.tvDot);
        tvResult = findViewById(R.id.tvResult);
        backButton = findViewById(R.id.backButton);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

        backButton.setOnClickListener(v -> finish());

        tvInstruction.setText("Tap as soon as you see the dot.");

        tvDot.setOnClickListener(v -> {
            if (dotVisible) {
                long reaction = System.currentTimeMillis() - startTime;
                reactionTimes.add(reaction);
                dotVisible = false;
                tvDot.setVisibility(TextView.INVISIBLE);
                nextTrial();
            }
        });

        nextTrial();
    }

    private void nextTrial() {
        if (trialCount >= TOTAL_TRIALS) {
            showResult();
            return;
        }

        trialCount++;
        tvInstruction.setText("Get ready...");

        int delay = random.nextInt(1800) + 700;

        handler.postDelayed(() -> {
            moveDotToRandomPosition();
            tvDot.setVisibility(TextView.VISIBLE);
            dotVisible = true;
            startTime = System.currentTimeMillis();
            tvInstruction.setText("Tap it!");
        }, delay);
    }

    private void moveDotToRandomPosition() {
        int dotSize = 200;
        int maxX = screenWidth - dotSize;
        int maxY = screenHeight - dotSize - 300;

        tvDot.setX(random.nextInt(Math.max(maxX, 1)));
        tvDot.setY(random.nextInt(Math.max(maxY, 1)) + 200);
    }

    private void showResult() {

        long avgRT = (long) reactionTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);

        // ============= NEW NORMALIZATION ==============
        double mu = 350.0;     // baseline human reaction
        double sigma = 180.0;  // allowed variance

        // Gaussian model
        double gaussian = Math.exp(
                -Math.pow((avgRT - mu), 2) / (2 * sigma * sigma)
        );

        double attentionScore = clamp100(gaussian * 100);

        // interpretation
        String performance;
        if (attentionScore >= 85) performance = "Elite Attention";
        else if (attentionScore >= 65) performance = "Strong Focus";
        else if (attentionScore >= 45) performance = "Normal Performance";
        else if (attentionScore >= 25) performance = "Reduced Vigilance";
        else performance = "Cognitive Fatigue";

        tvInstruction.setText("Task Complete!");

        tvResult.setText(String.format(Locale.getDefault(),
                "Avg RT: %d ms\nScore: %.1f/100\n%s",
                avgRT, attentionScore, performance));
        tvResult.setVisibility(TextView.VISIBLE);

        // -----------------------------------------
        // Backend upload (unchanged)
        // -----------------------------------------
        String email = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getString("email", "");

        if (email.isEmpty()) {
            Toast.makeText(this, "Error: No user email stored!", Toast.LENGTH_LONG).show();
        } else {
            ScoreRequest req = new ScoreRequest(email, (float) attentionScore);
            Call<Void> call = api.saveSrtPost(req);

            ScoreUploader.uploadScore(
                    this,
                    email,
                    (float) attentionScore,
                    "SRT Post",
                    call
            );
        }

        handler.postDelayed(this::finish, 2500);
    }

    private double clamp100(double v) {
        return Math.max(0, Math.min(100, v));
    }
}
