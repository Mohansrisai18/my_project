package com.example.cognisync;

import android.os.Bundle;
import android.os.Handler;
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
    private int hits = 0;
    private int totalTaps = 0;
    private int totalTrials = 0;

    private final Handler handler = new Handler();
    private final Random random = new Random();

    private boolean respondedThisStimulus = false;
    private boolean canRespond = false;

    // ⬇️ EASIER CONFIG
    private static final int MAX_TRIALS = 18;
    private static final int STIMULUS_DURATION = 2000; // ms
    private static final int TRIAL_INTERVAL = 2500;    // ms

    // probability of intentionally creating a 2-back match when possible
    private static final float MATCH_PROBABILITY = 0.35f;

    private ApiService api;
    private String email;

    // runnable reference so we can cancel on destroy
    private final Runnable sequenceRunnable = new Runnable() {
        @Override
        public void run() {
            if (totalTrials >= MAX_TRIALS) {
                showResult();
                return;
            }

            String letter;

            // If we have at least two previous letters (needed for 2-back) we may force a match.
            if (shown.size() >= 2 && random.nextFloat() < MATCH_PROBABILITY) {
                // Intentionally create a 2-back match by repeating the letter from two steps ago
                letter = shown.get(shown.size() - 2);
            } else {
                // Pick a letter that is DIFFERENT from:
                // - the immediate previous letter (avoid accidental 1-back)
                // - the letter two steps back (avoid accidental 2-back)
                if (shown.size() >= 2) {
                    String prev = shown.get(shown.size() - 1);
                    String twoBack = shown.get(shown.size() - 2);
                    do {
                        letter = letters[random.nextInt(letters.length)];
                        // regenerate while accidentally matching immediate prev or two-back
                    } while (letter.equals(prev) || letter.equals(twoBack));
                } else if (shown.size() == 1) {
                    // only avoid immediate repeat
                    String prev = shown.get(shown.size() - 1);
                    do {
                        letter = letters[random.nextInt(letters.length)];
                    } while (letter.equals(prev));
                } else {
                    // first letter: any
                    letter = letters[random.nextInt(letters.length)];
                }
            }

            shown.add(letter);
            totalTrials++;

            tvLetter.setText(letter);
            tvInfo.setText("Tap MATCH if same as the letter shown two steps earlier (one in between).");

            canRespond = true;
            respondedThisStimulus = false;

            // response window
            handler.postDelayed(() -> canRespond = false, STIMULUS_DURATION);

            // schedule next trial
            handler.postDelayed(this, TRIAL_INTERVAL);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nback);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        api = ApiClient.getClient().create(ApiService.class);
        email = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getString("email", "");

        tvLetter = findViewById(R.id.tvLetter);
        tvScore  = findViewById(R.id.tvScore);
        tvInfo   = findViewById(R.id.tvInfo);
        btnMatch = findViewById(R.id.btnMatch);

        tvScore.setVisibility(TextView.GONE);

        btnMatch.setOnClickListener(v -> handleMatchTap());

        // start sequence (delayed start)
        handler.postDelayed(sequenceRunnable, 1200);
    }

    // ===============================================================
    // TAP HANDLER (2-BACK)
    // ===============================================================
    private void handleMatchTap() {

        if (!canRespond || respondedThisStimulus) return;

        totalTaps++;
        respondedThisStimulus = true;

        // For 2-back we need at least 3 shown items (indexes 0..n-1, current index is n-1,
        // compare current (n-1) to twoBack (n-3) BEFORE adding? But we add then check,
        // so current = shown.get(size-1), two-back is shown.get(size-3)
        if (shown.size() >= 3) {
            String cur = shown.get(shown.size() - 1);
            String twoBack = shown.get(shown.size() - 3);

            if (cur.equals(twoBack)) {
                hits++;
            }
        }
    }

    // ===============================================================
    // SCORING (2-BACK friendly, friendly weights)
    // ===============================================================
    private void showResult() {

        canRespond = false;

        int totalMatches = 0;
        // count real 2-back matches: positions where item equals item two steps earlier
        for (int i = 2; i < shown.size(); i++) {
            if (shown.get(i).equals(shown.get(i - 2))) {
                totalMatches++;
            }
        }

        int falseAlarms = Math.max(0, totalTaps - hits);
        int misses = Math.max(0, totalMatches - hits);

        // Friendly scoring weights (kept similar to previous tuning)
        double rawScore =
                (hits * 6)
                        - (misses * 1.5)
                        - (falseAlarms * 2);

        if (rawScore < 0) rawScore = 0;

        double maxScore = totalMatches * 6.0;
        double score = maxScore == 0 ? 0 : (rawScore / maxScore) * 100;

        score = Math.max(0, Math.min(100, score));

        tvScore.setVisibility(TextView.VISIBLE);
        tvScore.setText(String.format(Locale.getDefault(),
                "Score: %.1f /100\nHits: %d  Misses: %d  False: %d",
                score, hits, misses, falseAlarms));

        tvInfo.setText("2-Back task completed ✓");

        sendScore((float) score);

        handler.postDelayed(this::finish, 3000);
    }

    // ===============================================================
    // API
    // ===============================================================
    private void sendScore(float score) {

        if (email.isEmpty()) {
            Toast.makeText(this, "No user email stored!", Toast.LENGTH_LONG).show();
            return;
        }

        ScoreRequest req = new ScoreRequest(email, score);
        Call<Void> call = api.saveNbackPost(req);

        ScoreUploader.uploadScore(
                this,
                email,
                score,
                "N-Back Post",
                call
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // cancel pending runnables
        handler.removeCallbacks(sequenceRunnable);
        handler.removeCallbacksAndMessages(null);
    }
}
