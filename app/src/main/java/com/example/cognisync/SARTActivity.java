package com.example.cognisync;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cognisync.del.ApiClient;
import com.example.cognisync.del.ApiService;
import com.example.cognisync.model.ScoreRequest;

import java.util.Locale;
import java.util.Random;

import retrofit2.Call;

public class SARTActivity extends AppCompatActivity {

    private TextView tvNumber, tvInstruction, tvProgress;
    private Button btnStart;

    private static final int TOTAL_TRIALS = 40;
    private int currentTrial = 0;

    private int commissionErrors = 0;   // tapped when should NOT tap (3)
    private int omissionErrors = 0;     // did NOT tap when should tap
    private int correctResponses = 0;   // correct taps
    private long reactionTimeSum = 0;

    private long lastShownTime = 0;
    private boolean canTap = false;

    private final Handler handler = new Handler();
    private final Random random = new Random();

    private String moduleType = "present_moment";

    private ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sart);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        api = ApiClient.getClient().create(ApiService.class);

        Intent i = getIntent();
        if (i != null && i.hasExtra("module_type"))
            moduleType = i.getStringExtra("module_type");

        tvNumber = findViewById(R.id.tvNumber);
        tvInstruction = findViewById(R.id.tvInstruction);
        tvProgress = findViewById(R.id.tvProgress);
        btnStart = findViewById(R.id.btnStart);

        btnStart.setOnClickListener(v -> startTask());

        tvNumber.setOnClickListener(v -> handleTap());
    }

    private void startTask() {
        btnStart.setVisibility(Button.GONE);

        tvInstruction.setText("Tap for every number EXCEPT 3!");

        currentTrial = 0;
        commissionErrors = 0;
        omissionErrors = 0;
        correctResponses = 0;
        reactionTimeSum = 0;

        runNextTrial();
    }

    private void runNextTrial() {
        if (currentTrial >= TOTAL_TRIALS) {
            endTask();
            return;
        }

        int number = random.nextInt(10);
        tvNumber.setText(String.valueOf(number));
        tvProgress.setText("Trial " + (currentTrial + 1) + " / " + TOTAL_TRIALS);

        lastShownTime = System.currentTimeMillis();
        canTap = true;

        currentTrial++;

        handler.postDelayed(() -> {
            if (canTap) {
                int n = Integer.parseInt(tvNumber.getText().toString());
                if (n != 3) {
                    omissionErrors++;  // should tap but didn't
                }
            }
            canTap = false;
            runNextTrial();

        }, 800);
    }

    private void handleTap() {
        if (!canTap) return;

        int n = Integer.parseInt(tvNumber.getText().toString());
        long rt = System.currentTimeMillis() - lastShownTime;

        if (n == 3) {
            commissionErrors++; // tapped but SHOULD NOT
        } else {
            correctResponses++; // tapped correctly
            reactionTimeSum += rt;
        }

        canTap = false;
    }

    private void endTask() {
        tvNumber.setText("-");
        tvInstruction.setText("Task Completed!");
        tvProgress.setText("");

        int totalResponses = commissionErrors + correctResponses;
        double accuracy = totalResponses == 0 ? 0 : (correctResponses * 100.0 / totalResponses);
        long avgRT = correctResponses == 0 ? 0 : reactionTimeSum / correctResponses;

        // Score calculations
        double errorScore = 100.0 - (commissionErrors * 10.0) - (omissionErrors * 5.0);
        errorScore = Math.max(0, Math.min(100, errorScore));

        double rtScore = 100.0 - ((avgRT - 300.0) * 0.15);
        rtScore = Math.max(0, Math.min(100, rtScore));

        double awarenessScore = (0.7 * errorScore) + (0.3 * rtScore);
        awarenessScore = Math.max(0, Math.min(100, awarenessScore));

        String text = "Accuracy: " + String.format(Locale.getDefault(), "%.1f", accuracy) + "%\n"
                + "Commission: " + commissionErrors + "\n"
                + "Omissions: " + omissionErrors + "\n"
                + "Avg RT: " + avgRT + " ms\n"
                + "Awareness Score: " + (int) awarenessScore + "/100";

        Toast.makeText(this, text, Toast.LENGTH_LONG).show();

        // ---------------------------
        // 🔥 SEND SCORE TO BACKEND
        // ---------------------------
        String email = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getString("email", "");

        if (email.isEmpty()) {
            Toast.makeText(this, "Error: No email saved!", Toast.LENGTH_LONG).show();
        } else {
            ScoreRequest req = new ScoreRequest(email, (float) awarenessScore);
            Call<Void> call = api.saveSartPost(req);

            ScoreUploader.uploadScore(
                    this,
                    email,
                    (float) awarenessScore,
                    "SART Post",
                    call
            );
        }

        handler.postDelayed(this::finish, 2500);
    }
}
