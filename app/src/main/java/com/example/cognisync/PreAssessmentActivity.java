package com.example.cognisync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PreAssessmentActivity extends AppCompatActivity {

    private LinearLayout questionContainer;
    private TextView titleText, taskLabel, navPath;
    private ImageButton backButton;
    private Button btnNext;

    private SharedPreferences sp;
    private static final String PREF_NAME = "ModulePreAssessment";

    private String moduleType;
    private List<QuestionItem> questions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_assessment);

        // Initialize views
        titleText = findViewById(R.id.titleText);
        taskLabel = findViewById(R.id.taskLabel);
        questionContainer = findViewById(R.id.questionContainer);
        backButton = findViewById(R.id.backButton);
        btnNext = findViewById(R.id.btnNext);
        navPath = findViewById(R.id.navPath);

        // Get module info
        moduleType = getIntent().getStringExtra("module_type");
        if (moduleType == null || moduleType.isEmpty()) moduleType = "focused_attention";
        String sessionTitle = getIntent().getStringExtra("session_title");
        if (sessionTitle == null) sessionTitle = "Session 1";

        // Setup preferences
        sp = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        String readableName = getReadableModuleTitle(moduleType);
        navPath.setText("Home > " + readableName + " > " + sessionTitle);
        titleText.setText(readableName);
        taskLabel.setText("Pre-Session Self-Assessment");

        // Load MCQ questions
        loadQuestions(moduleType);
        displayQuestions();

        // Back
        backButton.setOnClickListener(v -> finish());

        // Continue
        String finalSessionTitle = sessionTitle;
        btnNext.setOnClickListener(v -> {
            int score = collectResponses();
            if (score == -1) {
                Toast.makeText(this, "Please answer all questions!", Toast.LENGTH_SHORT).show();
                return;
            }

            sp.edit().putInt(moduleType + "_pre_score", score).apply();
            Toast.makeText(this, "Responses saved! Score: " + score, Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, ModuleVideoActivity.class);
            intent.putExtra("module_type", moduleType);
            intent.putExtra("session_title", finalSessionTitle);
            startActivity(intent);
            finish();
        });
    }

    /** Load only MCQ-based questions */
    private void loadQuestions(String moduleType) {
        switch (moduleType) {
            case "focused_attention": questions.addAll(getMAASQuestions()); break;
            case "working_memory": questions.addAll(getCFQQuestions()); break;
            case "emotional_regulation": questions.addAll(getPANASQuestions()); break;
            case "present_moment":
            case "present_moment_awareness": questions.addAll(getPHLMSQuestions()); break;
            case "cognitive_flexibility": questions.addAll(getDASSQuestions()); break;
            default:
                questions.add(new QuestionItem("How attentive did you feel today?", true));
                break;
        }
    }

    /** Display questions using Spinner dropdown */
    private void displayQuestions() {
        // Common options for all spinners
        String[] options = {"Select an option", "Very Poor", "Poor", "Average", "Good", "Very Good", "Excellent"};

        for (int i = 0; i < questions.size(); i++) {
            QuestionItem q = questions.get(i);

            TextView qText = new TextView(this);
            qText.setText((i + 1) + ". " + q.question);
            qText.setTextSize(16);
            qText.setPadding(0, 16, 0, 8);
            questionContainer.addView(qText);

            Spinner spinner = new Spinner(this);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, options);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            questionContainer.addView(spinner);

            q.setSpinner(spinner);
        }
    }

    /** Calculate total score */
    private int collectResponses() {
        int total = 0;
        for (QuestionItem q : questions) {
            int value = q.getSelectedValue();
            if (value == -1) return -1;
            total += value;
        }
        return total;
    }

    // ======== Question sets ======== //

    private List<QuestionItem> getMAASQuestions() {
        List<QuestionItem> list = new ArrayList<>();
        Collections.addAll(list,
                new QuestionItem("I found it hard to stay focused.", true),
                new QuestionItem("I felt distracted during simple tasks.", true),
                new QuestionItem("I noticed thoughts drifting frequently.", true),
                new QuestionItem("I rushed through activities without attention.", true),
                new QuestionItem("I was aware of my breathing while working.", true)
        );
        return list;
    }

    private List<QuestionItem> getCFQQuestions() {
        List<QuestionItem> list = new ArrayList<>();
        Collections.addAll(list,
                new QuestionItem("I forget why I entered a room.", true),
                new QuestionItem("I lose track of what I was doing mid-task.", true),
                new QuestionItem("I forget instructions quickly.", true),
                new QuestionItem("I misplace items I just used.", true),
                new QuestionItem("I find it hard to keep multiple things in mind.", true)
        );
        return list;
    }

    private List<QuestionItem> getPANASQuestions() {
        List<QuestionItem> list = new ArrayList<>();
        Collections.addAll(list,
                new QuestionItem("I felt calm and balanced.", true),
                new QuestionItem("I felt nervous or tense.", true),
                new QuestionItem("I felt optimistic about my day.", true),
                new QuestionItem("I felt easily irritated.", true),
                new QuestionItem("I felt in control of my emotions.", true)
        );
        return list;
    }

    private List<QuestionItem> getPHLMSQuestions() {
        List<QuestionItem> list = new ArrayList<>();
        Collections.addAll(list,
                new QuestionItem("I noticed sounds and sensations clearly.", true),
                new QuestionItem("I was aware of my body posture.", true),
                new QuestionItem("I observed my thoughts calmly.", true),
                new QuestionItem("I was present with what I was doing.", true),
                new QuestionItem("I noticed emotions as they appeared.", true)
        );
        return list;
    }

    private List<QuestionItem> getDASSQuestions() {
        List<QuestionItem> list = new ArrayList<>();
        Collections.addAll(list,
                new QuestionItem("I felt stressed easily.", true),
                new QuestionItem("I overreacted to small issues.", true),
                new QuestionItem("I found it hard to relax.", true),
                new QuestionItem("I got irritated by interruptions.", true),
                new QuestionItem("I used a lot of nervous energy.", true)
        );
        return list;
    }

    /** Human-readable titles */
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

    /** Inner class for MCQ item */
    private static class QuestionItem {
        String question;
        Spinner spinner;

        QuestionItem(String q, boolean mcq) { this.question = q; }

        void setSpinner(Spinner s) { this.spinner = s; }

        int getSelectedValue() {
            if (spinner == null) return -1;
            int pos = spinner.getSelectedItemPosition();
            return pos == 0 ? -1 : pos; // 1–6 values
        }
    }
}
