package com.example.cognisync;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
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

public class StroopTaskActivity extends AppCompatActivity {

    private TextView tvWord, tvResult;
    private Button btnRed, btnBlue, btnGreen;
    private ImageButton backButton;

    private final String[] words = {"ANGER","CALM","JOY","FEAR","LOVE"};
    private final int[] colors = {0xFFF44336, 0xFF2196F3, 0xFF4CAF50};

    private final Random random = new Random();
    private final Handler handler = new Handler();

    private int trial = 0;
    private long startTime;

    private final List<Long> neutralTimes = new ArrayList<>();
    private final List<Long> emotionalTimes = new ArrayList<>();

    private ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stroop_task);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        api = ApiClient.getClient().create(ApiService.class);

        tvWord = findViewById(R.id.tvWord);
        tvResult = findViewById(R.id.tvResult);
        btnRed = findViewById(R.id.btnRed);
        btnBlue = findViewById(R.id.btnBlue);
        btnGreen = findViewById(R.id.btnGreen);
        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> finish());

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
        return "ANGER".equals(word) || "FEAR".equals(word);
    }

    private void showResult() {
        long avgNeutral = average(neutralTimes);
        long avgEmotional = average(emotionalTimes);
        long delta = Math.abs(avgEmotional - avgNeutral);

        double emotionScore = 100.0 * Math.exp(-((double) delta) / 150.0);
        emotionScore = Math.max(0, Math.min(100, emotionScore));

        String interpretation;
        if (emotionScore >= 75) interpretation = "Excellent emotional regulation";
        else if (emotionScore >= 40) interpretation = "Moderate emotion control";
        else interpretation = "High emotional reactivity";

        tvResult.setVisibility(View.VISIBLE);
        tvResult.setText(String.format(Locale.getDefault(),
                "Δ Reaction Time = %d ms\nScore: %.1f /100\n%s",
                delta, emotionScore, interpretation));

        // 🔥 Backend only — no SharedPreferences
        String email = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getString("email", "");

        if (email.isEmpty()) {
            Toast.makeText(this, "Error: No user email saved!", Toast.LENGTH_LONG).show();
        } else {
            ScoreRequest req = new ScoreRequest(email, (float) emotionScore);
            Call<Void> call = api.saveStroopPost(req);

            ScoreUploader.uploadScore(
                    this,
                    email,
                    (float) emotionScore,
                    "Stroop post",
                    call
            );
        }

        handler.postDelayed(this::finish, 2500);
    }

    private long average(List<Long> list) {
        if (list == null || list.isEmpty()) return 0;
        long sum = 0;
        for (Long v : list) sum += v;
        return sum / list.size();
    }
}
