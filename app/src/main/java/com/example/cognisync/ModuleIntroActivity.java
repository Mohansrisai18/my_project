package com.example.cognisync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ModuleIntroActivity extends AppCompatActivity {

    private TextView tvTitle, tvSubtitle, tvDescription;
    private ImageButton backButton;
    private Button btnStartPre, btnContinue;

    private String moduleType;
    private static final String PREF_MODULE_STATE = "ModuleState";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_intro);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        moduleType = getIntent().getStringExtra("module_type");
        if (moduleType == null) moduleType = "focused_attention";

        initViews();
        loadIntroContent();
        applyButtonLogic();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvIntroTitle);
        tvSubtitle = findViewById(R.id.tvIntroSubtitle);
        tvDescription = findViewById(R.id.tvIntroDescription);
        btnStartPre = findViewById(R.id.btnStartPreAssessment);
        btnContinue = findViewById(R.id.btnContinueToModule);
        backButton = findViewById(R.id.backButtonIntro);

        backButton.setOnClickListener(v -> finish());
    }

    private void loadIntroContent() {
        switch (moduleType) {
            case "focused_attention":
                tvTitle.setText("Focused Attention");
                tvSubtitle.setText("Why Pre-Assessment?");
                tvDescription.setText(
                        "Before beginning this module, we measure your baseline focus ability.\n" +
                                "This helps personalize your training journey and shows improvement over time."
                );
                break;

            case "working_memory":
                tvTitle.setText("Working Memory");
                tvSubtitle.setText("Measure Your Starting Point");
                tvDescription.setText(
                        "This short assessment checks your memory lapses and cognitive load.\n" +
                                "It allows you to track improvements after completing the module."
                );
                break;

            case "emotional_regulation":
                tvTitle.setText("Emotional Regulation");
                tvSubtitle.setText("Assess Your Emotional Baseline");
                tvDescription.setText(
                        "The pre-assessment measures your emotional responses.\n" +
                                "Later, you will compare emotional resilience after training."
                );
                break;

            case "cognitive_flexibility":
                tvTitle.setText("Cognitive Flexibility");
                tvSubtitle.setText("Why It Matters");
                tvDescription.setText(
                        "This assessment identifies how smoothly you can switch between tasks.\n" +
                                "It helps track your adaptability and stress response."
                );
                break;

            case "present_moment":
            case "present_moment_awareness":
                tvTitle.setText("Present Moment Awareness");
                tvSubtitle.setText("Understanding Your Awareness Level");
                tvDescription.setText(
                        "This assessment measures mindfulness and awareness.\n" +
                                "It shows how often your mind wanders before training."
                );
                break;
        }
    }

    private void applyButtonLogic() {

        SharedPreferences sp = getSharedPreferences(PREF_MODULE_STATE, MODE_PRIVATE);
        boolean isPreDone = sp.getBoolean(moduleType + "_pre_completed", false);

        if (isPreDone) {

            // Disable pre button
            btnStartPre.setText("Pre-Assessment Completed ✓");
            btnStartPre.setEnabled(false);
            btnStartPre.setAlpha(0.6f);

            // Show continue button
            btnContinue.setVisibility(View.VISIBLE);

            btnContinue.setOnClickListener(v -> {
                Intent i = new Intent(this, ModuleHomeActivity.class);
                i.putExtra("module_type", moduleType);
                startActivity(i);
            });

        } else {

            // User has NOT completed pre-assessment
            btnContinue.setVisibility(View.GONE);

            btnStartPre.setOnClickListener(v -> {
                Intent intent = new Intent(this, PreAssessmentActivity.class);
                intent.putExtra("module_type", moduleType);
                intent.putExtra("session_title", "Pre Assessment");
                startActivity(intent);
            });
        }
    }
}
