package com.example.cognisync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.*;

public class PreAssessmentActivity extends AppCompatActivity {

    private LinearLayout questionContainer;
    private TextView titleText, taskLabel, navPath;
    private ImageButton backButton;
    private Button btnNext;

    private SharedPreferences sp;
    private static final String PREF_NAME = "ModulePreAssessment";
    private static final String PREF_MODULE_STATE = "ModuleState";

    private String moduleType;
    private final List<QuestionItem> questions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // Bind Views
        titleText = findViewById(R.id.titleText);
        taskLabel = findViewById(R.id.taskLabel);
        questionContainer = findViewById(R.id.questionContainer);
        backButton = findViewById(R.id.backButton);
        btnNext = findViewById(R.id.btnNext);
        navPath = findViewById(R.id.navPath);

        // Module Type
        moduleType = getIntent().getStringExtra("module_type");
        if (moduleType == null) moduleType = "focused_attention";

        // UI Text
        titleText.setText(getReadableModuleTitle(moduleType));
        navPath.setText("Home > " + getReadableModuleTitle(moduleType));
        taskLabel.setText("Pre-Session Self-Assessment");

        sp = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Load & Show Questions
        loadQuestions(moduleType);
        displayQuestions();

        backButton.setOnClickListener(v -> finish());

        btnNext.setOnClickListener(v -> saveAssessment(moduleType));
    }

    // ---------------------------------------------------------
    // Load questions
    // ---------------------------------------------------------
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

    // ---------------------------------------------------------
    // Display questions
    // ---------------------------------------------------------
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

    // ---------------------------------------------------------
    // Save Assessment + navigate to ModuleHome
    // ---------------------------------------------------------
    private void saveAssessment(String moduleType) {

        if (hasUnanswered()) {
            Toast.makeText(this, "Please answer all questions!", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences global = getSharedPreferences("CognitiveScores", MODE_PRIVATE);

        float scoreToSave = 0;

        switch (moduleType) {

            case "focused_attention":
                scoreToSave = computeMAASScore();
                global.edit().putFloat("MAAS_pre", scoreToSave).apply();
                break;

            case "emotional_regulation":
                float pos = computePANASPositive();
                float neg = computePANASNegative();
                float net = pos - neg;

                global.edit()
                        .putFloat("PANAS_positive_pre", pos)
                        .putFloat("PANAS_negative_pre", neg)
                        .putFloat("PANAS_pre", net)
                        .apply();

                scoreToSave = net;
                break;

            case "cognitive_flexibility":
                scoreToSave = computeDASSScore();
                global.edit().putFloat("DASS_pre", scoreToSave).apply();
                break;

            case "working_memory":
                scoreToSave = computeCFQScore();
                global.edit().putFloat("CFQ_pre", scoreToSave).apply();
                break;

            case "present_moment":
            case "present_moment_awareness":
                scoreToSave = computePHLMSScore();
                global.edit().putFloat("PHLMS_pre", scoreToSave).apply();
                break;
        }

        // Save local pre score
        sp.edit().putFloat(moduleType + "_pre_score", scoreToSave).apply();

        // Mark Pre Completed
        getSharedPreferences(PREF_MODULE_STATE, MODE_PRIVATE)
                .edit()
                .putBoolean(moduleType + "_pre_completed", true)
                .apply();

        Toast.makeText(this, "Saved! Score: " + scoreToSave, Toast.LENGTH_SHORT).show();

        // ---------------------------------------------------------
        // 🔥 Navigate to ModuleHomeActivity
        // ---------------------------------------------------------
        Intent intent = new Intent(this, ModuleHomeActivity.class);
        intent.putExtra("module_type", moduleType);
        startActivity(intent);
        finish();
    }

    // ---------------------------------------------------------
    private boolean hasUnanswered() {
        for (QuestionItem q : questions)
            if (q.getSelectedValue() == -1) return true;
        return false;
    }

    // ---------------------------------------------------------
    // Scoring logic
    // ---------------------------------------------------------
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

    // ---------------------------------------------------------
    // Question banks
    // ---------------------------------------------------------
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

    // ---------------------------------------------------------
    // Model
    // ---------------------------------------------------------
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
            return Integer.parseInt(spinner.getSelectedItem().toString());
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
