package com.example.cognisync;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cognisync.del.ApiClient;
import com.example.cognisync.del.ApiService;
import com.example.cognisync.model.ScoreRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * PreAssessmentActivity — Option A (Server-only).
 *
 * - Computes pre-assessment scores locally (same as before)
 * - Sends only to backend via appropriate endpoint (e.g., save-maas-pre)
 * - Does NOT store scores locally in SharedPreferences
 */
public class PreAssessmentActivity extends AppCompatActivity {

    private LinearLayout questionContainer;
    private TextView titleText, taskLabel, navPath;
    private ImageButton backButton;
    private Button btnNext;

    private String moduleType;
    private final List<QuestionItem> questions = new ArrayList<>();

    private ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        titleText = findViewById(R.id.titleText);
        taskLabel = findViewById(R.id.taskLabel);
        questionContainer = findViewById(R.id.questionContainer);
        backButton = findViewById(R.id.backButton);
        btnNext = findViewById(R.id.btnNext);
        navPath = findViewById(R.id.navPath);

        moduleType = getIntent().getStringExtra("module_type");
        if (moduleType == null) moduleType = "focused_attention";

        titleText.setText(getReadableModuleTitle(moduleType));
        navPath.setText("Home > " + getReadableModuleTitle(moduleType));
        taskLabel.setText("Pre-Session Self-Assessment");

        api = ApiClient.getClient().create(ApiService.class);

        loadQuestions(moduleType);
        displayQuestions();

        backButton.setOnClickListener(v -> finish());
        btnNext.setOnClickListener(v -> saveAssessment(moduleType));
    }

    // ---------------- Load questions ----------------
    private void loadQuestions(String type) {
        questions.clear();
        switch (type) {
            case "focused_attention":
                questions.addAll(getRandomQuestions(getMAASPool(), 5));
                break;
            case "emotional_regulation":
                questions.addAll(getRandomQuestions(getPANASPool(), 10));
                break;
            case "cognitive_flexibility":
                questions.addAll(getDASSPool());
                break;
            case "working_memory":
                questions.addAll(getRandomQuestions(getCFQPool(), 5));
                break;
            case "present_moment":
            case "present_moment_awareness":
                questions.addAll(getRandomQuestions(getPHLMSPool(), 5));
                break;
            default:
                questions.add(new QuestionItem("How are you feeling today?", 1, 6, true));
        }
    }

    // ---------------- Display questions ----------------
    private void displayQuestions() {
        questionContainer.removeAllViews();
        for (int i = 0; i < questions.size(); i++) {
            QuestionItem q = questions.get(i);

            TextView qText = new TextView(this);
            qText.setText((i + 1) + ". " + q.question);
            qText.setTextSize(16);
            qText.setPadding(0, 16, 0, 8);
            questionContainer.addView(qText);

            Spinner spinner = new Spinner(this);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    q.getOptionsArray()
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

            questionContainer.addView(spinner);
            q.spinner = spinner;
        }
    }

    // ---------------- Save assessment (server-only) ----------------
    private void saveAssessment(String moduleType) {
        if (hasUnanswered()) {
            Toast.makeText(this, "Please answer all questions!", Toast.LENGTH_SHORT).show();
            return;
        }

        float scoreToSave = 0f;

        switch (moduleType) {
            case "focused_attention":
                scoreToSave = computeMAASScore();
                callSaveMaasPre(scoreToSave);
                break;
            case "emotional_regulation":
                float pos = computePANASPositive();
                float neg = computePANASNegative();
                scoreToSave = pos - neg;
                callSavePanasPre(scoreToSave);
                break;
            case "cognitive_flexibility":
                scoreToSave = computeDASSScore();
                callSaveDassPre(scoreToSave);
                break;
            case "working_memory":
                scoreToSave = computeCFQScore();
                callSaveCfqPre(scoreToSave);
                break;
            case "present_moment":
            case "present_moment_awareness":
                scoreToSave = computePHLMSScore();
                callSavePhlmsPre(scoreToSave);
                break;
        }

        // notify user locally (server call will also notify on success/failure)
        Toast.makeText(this, "Submitting score to server... (" + String.format(Locale.getDefault(), "%.2f", scoreToSave) + ")", Toast.LENGTH_SHORT).show();

        // navigate back to module home (we still navigate; server call runs async)
        Intent intent = new Intent(this, ModuleHomeActivity.class);
        intent.putExtra("module_type", moduleType);
        startActivity(intent);
        finish();
    }

    // ---------------- Network calls (use ScoreRequest(email, score) constructor) ----------------
    private void callSaveMaasPre(float score) {
        String email = getUserEmail();
        if (email.isEmpty()) { showNoEmailError(); return; }

        ScoreRequest req = new ScoreRequest(email, score);
        Call<Void> call = api.saveMaasPre(req);
        enqueueScoreCall(call, "MAAS pre");
    }

    private void callSavePanasPre(float score) {
        String email = getUserEmail();
        if (email.isEmpty()) { showNoEmailError(); return; }

        ScoreRequest req = new ScoreRequest(email, score);
        Call<Void> call = api.savePanasPre(req);
        enqueueScoreCall(call, "PANAS pre");
    }

    private void callSaveDassPre(float score) {
        String email = getUserEmail();
        if (email.isEmpty()) { showNoEmailError(); return; }

        ScoreRequest req = new ScoreRequest(email, score);
        Call<Void> call = api.saveDassPre(req);
        enqueueScoreCall(call, "DASS pre");
    }

    private void callSaveCfqPre(float score) {
        String email = getUserEmail();
        if (email.isEmpty()) { showNoEmailError(); return; }

        ScoreRequest req = new ScoreRequest(email, score);
        Call<Void> call = api.saveCfqPre(req);
        enqueueScoreCall(call, "CFQ pre");
    }

    private void callSavePhlmsPre(float score) {
        String email = getUserEmail();
        if (email.isEmpty()) { showNoEmailError(); return; }

        ScoreRequest req = new ScoreRequest(email, score);
        Call<Void> call = api.savePhlmsPre(req);
        enqueueScoreCall(call, "PHLMS pre");
    }

    private void enqueueScoreCall(Call<Void> call, final String label) {
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("PreAssessment", label + " saved. HTTP " + response.code());
                    // Small success toast (non-blocking)
                    Toast.makeText(PreAssessmentActivity.this, label + " saved on server", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("PreAssessment", label + " server error: HTTP " + response.code());
                    Toast.makeText(PreAssessmentActivity.this, label + " failed: server " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("PreAssessment", label + " network failure: " + t.getMessage(), t);
                Toast.makeText(PreAssessmentActivity.this, label + " failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showNoEmailError() {
        Toast.makeText(this, "No user email found (login required). Score not sent.", Toast.LENGTH_LONG).show();
    }

    private String getUserEmail() {
        // We need user's email to associate scores on server. This expects you stored it at login.
        // If you don't store email on login, store it in UserPrefs with key "email".
        return getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("email", "").trim();
    }

    // ---------------- Validation ----------------
    private boolean hasUnanswered() {
        for (QuestionItem q : questions)
            if (q.getSelectedValue() == -1) return true;
        return false;
    }

    // ---------------- Scoring logic (same as your previous) ----------------
    private float computeMAASScore() {
        float total = 0;
        for (QuestionItem q : questions) total += q.getSelectedValue();
        return total / questions.size();
    }

    private float computePANASPositive() {
        float sum = 0; int c = 0;
        for (QuestionItem q : questions) if (q.isPositive) { sum += q.getSelectedValue(); c++; }
        return (c == 0) ? 0 : sum / c;
    }

    private float computePANASNegative() {
        float sum = 0; int c = 0;
        for (QuestionItem q : questions) if (!q.isPositive) { sum += q.getSelectedValue(); c++; }
        return (c == 0) ? 0 : sum / c;
    }

    private float computeDASSScore() {
        float total = 0;
        for (QuestionItem q : questions) total += q.getSelectedValue();
        return total * 2;
    }

    private float computeCFQScore() {
        float total = 0;
        for (QuestionItem q : questions) total += q.getSelectedValue();
        return total;
    }

    private float computePHLMSScore() {
        float total = 0;
        for (QuestionItem q : questions) total += q.getSelectedValue();
        return total / questions.size();
    }

    // ---------------- Question pools ----------------
    private List<QuestionItem> getMAASPool() {
        return Arrays.asList(
                new QuestionItem("I find it difficult to stay focused on what’s happening.", 1, 6),
                new QuestionItem("I rush through activities without paying attention.", 1, 6),
                new QuestionItem("I get easily distracted.", 1, 6),
                new QuestionItem("I do things automatically without awareness.", 1, 6),
                new QuestionItem("I fail to notice small details.", 1, 6)
        );
    }

    private List<QuestionItem> getPANASPool() {
        return Arrays.asList(
                new QuestionItem("Interested", 1, 5, true),
                new QuestionItem("Excited", 1, 5, true),
                new QuestionItem("Strong", 1, 5, true),
                new QuestionItem("Enthusiastic", 1, 5, true),
                new QuestionItem("Alert", 1, 5, true),
                new QuestionItem("Distressed", 1, 5, false),
                new QuestionItem("Upset", 1, 5, false),
                new QuestionItem("Guilty", 1, 5, false),
                new QuestionItem("Scared", 1, 5, false),
                new QuestionItem("Hostile", 1, 5, false)
        );
    }

    private List<QuestionItem> getCFQPool() {
        return Arrays.asList(
                new QuestionItem("Do you forget why you entered a room?", 1, 5),
                new QuestionItem("Do you lose track mid-task?", 1, 5),
                new QuestionItem("Do you misplace items you just used?", 1, 5),
                new QuestionItem("Do you forget instructions quickly?", 1, 5),
                new QuestionItem("Do you forget daily routines?", 1, 5)
        );
    }

    private List<QuestionItem> getPHLMSPool() {
        return Arrays.asList(
                new QuestionItem("I noticed sensations in my body.", 1, 5),
                new QuestionItem("I was aware of my breathing.", 1, 5),
                new QuestionItem("I observed my thoughts calmly.", 1, 5),
                new QuestionItem("I was present with my actions.", 1, 5),
                new QuestionItem("I noticed emotions as they appeared.", 1, 5)
        );
    }

    private List<QuestionItem> getDASSPool() {
        return Arrays.asList(
                new QuestionItem("I found it difficult to relax.", 0, 3),
                new QuestionItem("I felt stressed over small things.", 0, 3),
                new QuestionItem("I felt tense for no reason.", 0, 3)
        );
    }

    private List<QuestionItem> getRandomQuestions(List<QuestionItem> pool, int count) {
        List<QuestionItem> shuffled = new ArrayList<>(pool);
        Collections.shuffle(shuffled);
        return shuffled.subList(0, Math.min(count, shuffled.size()));
    }

    // ---------------- Model ----------------
    private static class QuestionItem {
        String question;
        Spinner spinner;
        int min, max;
        boolean isPositive;

        QuestionItem(String q, int min, int max) { this(q, min, max, true); }

        QuestionItem(String q, int min, int max, boolean isPositive) {
            this.question = q;
            this.min = min;
            this.max = max;
            this.isPositive = isPositive;
        }

        String[] getOptionsArray() {
            List<String> list = new ArrayList<>();
            list.add("Select");
            for (int i = min; i <= max; i++) list.add(String.valueOf(i));
            return list.toArray(new String[0]);
        }

        int getSelectedValue() {
            if (spinner == null) return -1;
            int pos = spinner.getSelectedItemPosition();
            if (pos == 0) return -1;
            try {
                return Integer.parseInt(spinner.getSelectedItem().toString());
            } catch (Exception e) {
                return -1;
            }
        }
    }

    private String getReadableModuleTitle(String moduleType) {
        switch (moduleType) {
            case "focused_attention": return "Focused Attention";
            case "working_memory": return "Working Memory";
            case "emotional_regulation": return "Emotional Regulation";
            case "cognitive_flexibility": return "Cognitive Flexibility";
            case "present_moment":
            case "present_moment_awareness": return "Present-Moment Awareness";
            default: return "Mindfulness Module";
        }
    }
}
