package com.example.cognisync;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ModuleVideoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_video);

        // Bind UI elements
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvSubtitle = findViewById(R.id.tvSubtitle);
        TextView tvSessionInfo = findViewById(R.id.tvSessionInfo);
        TextView tvDuration = findViewById(R.id.tvDuration);
        TextView tvFocus = findViewById(R.id.tvFocus);
        TextView tvLevel = findViewById(R.id.tvLevel);
        Button startAssessmentButton = findViewById(R.id.startAssessmentButton);
        ImageButton backButton = findViewById(R.id.backButton);

        final String moduleType = getIntent().getStringExtra("module_type") == null
                ? "focused_attention"
                : getIntent().getStringExtra("module_type");

        // Set module info based on type
        switch (moduleType) {
            case "focused_attention":
                tvTitle.setText(getString(R.string.focused_attention));
                tvSubtitle.setText(getString(R.string.mindfulness_session));
                tvSessionInfo.setText(getString(R.string.focused_attention_desc));
                tvDuration.setText(getString(R.string.duration_12_minutes));
                tvFocus.setText(getString(R.string.focus_attention_training));
                tvLevel.setText(getString(R.string.level_beginner_friendly));
                break;
            case "working_memory":
                tvTitle.setText(getString(R.string.working_memory));
                tvSubtitle.setText(getString(R.string.mindfulness_session));
                tvSessionInfo.setText(getString(R.string.working_memory_desc));
                tvDuration.setText(getString(R.string.duration_13_minutes));
                tvFocus.setText(getString(R.string.focus_memory_exercises));
                tvLevel.setText(getString(R.string.level_beginner_friendly));
                break;
            case "present_moment":
                tvTitle.setText(getString(R.string.present_moment));
                tvSubtitle.setText(getString(R.string.mindfulness_session));
                tvSessionInfo.setText(getString(R.string.present_moment_desc));
                tvDuration.setText(getString(R.string.duration_20_minutes));
                tvFocus.setText(getString(R.string.focus_awareness_practices));
                tvLevel.setText(getString(R.string.level_beginner_friendly));
                break;
            case "cognitive_integration":
                tvTitle.setText(getString(R.string.cognitive_integration));
                tvSubtitle.setText(getString(R.string.mindfulness_session));
                tvSessionInfo.setText(getString(R.string.cognitive_integration_desc));
                tvDuration.setText(getString(R.string.duration_20_minutes));
                tvFocus.setText(getString(R.string.focus_integration_techniques));
                tvLevel.setText(getString(R.string.level_beginner_friendly));
                break;
            case "emotional_regulation":
                tvTitle.setText(getString(R.string.emotional_regulation));
                tvSubtitle.setText(getString(R.string.mindfulness_session));
                tvSessionInfo.setText(getString(R.string.emotional_regulation_desc));
                tvDuration.setText(getString(R.string.duration_15_minutes));
                tvFocus.setText(getString(R.string.focus_emotional_control));
                tvLevel.setText(getString(R.string.level_beginner_friendly));
                break;
        }

        // Show the assessment button and handle click
        startAssessmentButton.setText(getString(R.string.start_assessment));
        startAssessmentButton.setOnClickListener(v -> startAssessment(moduleType));

        // Back button - navigate to Home page
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ModuleVideoActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void startAssessment(String moduleType) {
        Intent intent = new Intent(this, AssessmentDetailActivity.class);
        intent.putExtra("module_type", moduleType);
        startActivity(intent);
    }
}
