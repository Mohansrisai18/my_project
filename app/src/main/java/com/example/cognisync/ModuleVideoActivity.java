package com.example.cognisync;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ModuleVideoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_module_video);

        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvSubtitle = findViewById(R.id.tvSubtitle);
        TextView tvSessionInfo = findViewById(R.id.tvSessionInfo);
        TextView tvDuration = findViewById(R.id.tvDuration);
        TextView tvFocus = findViewById(R.id.tvFocus);
        TextView tvLevel = findViewById(R.id.tvLevel);
        Button startAssessmentButton = findViewById(R.id.startAssessmentButton);
        ImageButton backButton = findViewById(R.id.backButton);

        // Get selected module
        final String moduleType = getIntent().getStringExtra("module_type");
        final String module = moduleType != null ? moduleType.toLowerCase() : "focused_attention";

        // Set dynamic UI info
        setModuleDetails(module, tvTitle, tvSubtitle, tvSessionInfo, tvDuration, tvFocus, tvLevel, startAssessmentButton);

        // Start assessment / task
        startAssessmentButton.setOnClickListener(v -> startModuleAssessment(module));

        // Back to Home
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ModuleVideoActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setModuleDetails(String module,
                                  TextView tvTitle,
                                  TextView tvSubtitle,
                                  TextView tvSessionInfo,
                                  TextView tvDuration,
                                  TextView tvFocus,
                                  TextView tvLevel,
                                  Button startAssessmentButton) {

        switch (module) {
            case "focused_attention":
                tvTitle.setText("Focused Attention");
                tvSubtitle.setText("Enhance your ability to focus deeply");
                tvSessionInfo.setText("Learn to manage distractions and maintain focus for longer periods.");
                tvDuration.setText("Duration: 10 minutes");
                tvFocus.setText("Focus: Sustained Attention Training");
                tvLevel.setText("Level: Beginner");
                startAssessmentButton.setText("Start Focus Task");
                break;

            case "working_memory":
                tvTitle.setText("Working Memory");
                tvSubtitle.setText("Train your short-term memory capacity");
                tvSessionInfo.setText("Practice recalling and updating information in real time.");
                tvDuration.setText("Duration: 8 minutes");
                tvFocus.setText("Focus: Memory Sequencing");
                tvLevel.setText("Level: Intermediate");
                startAssessmentButton.setText("Start Memory Task");
                break;

            case "emotional_regulation":
                tvTitle.setText("Emotional Regulation");
                tvSubtitle.setText("Manage emotional responses effectively");
                tvSessionInfo.setText("Develop calmness and reduce emotional reactivity.");
                tvDuration.setText("Duration: 9 minutes");
                tvFocus.setText("Focus: Emotional Balance");
                tvLevel.setText("Level: Intermediate");
                startAssessmentButton.setText("Start Stroop Task");
                break;

            case "cognitive_flexibility":
                tvTitle.setText("Cognitive Flexibility");
                tvSubtitle.setText("Improve your ability to adapt to changes");
                tvSessionInfo.setText("Switch between tasks and mental sets efficiently.");
                tvDuration.setText("Duration: 12 minutes");
                tvFocus.setText("Focus: Task Switching");
                tvLevel.setText("Level: Advanced");
                startAssessmentButton.setText("Start Switch Task");
                break;

            case "present_moment":
            case "present_moment_awareness":
                tvTitle.setText("Present-Moment Awareness");
                tvSubtitle.setText("Strengthen mindfulness and awareness");
                tvSessionInfo.setText("Be aware of sensations, thoughts, and emotions in real time.");
                tvDuration.setText("Duration: 11 minutes");
                tvFocus.setText("Focus: Mindfulness Practice");
                tvLevel.setText("Level: Beginner Friendly");
                startAssessmentButton.setText("Start Reflection Task");
                break;

            default:
                tvTitle.setText("Cognitive Module");
                tvSubtitle.setText("Enhance your cognitive skills");
                tvSessionInfo.setText("Complete the session to strengthen your cognitive control.");
                tvDuration.setText("Duration: 10 minutes");
                tvFocus.setText("Focus: General Mindfulness");
                tvLevel.setText("Level: Beginner");
                startAssessmentButton.setText("Start Assessment");
                break;
        }
    }

    private void startModuleAssessment(String moduleType) {
        Intent intent;

        switch (moduleType) {
            case "focused_attention":
                intent = new Intent(this, SRTActivity.class);
                break;

            case "working_memory":
                intent = new Intent(this, NBackActivity.class);
                break;

            case "emotional_regulation":
                intent = new Intent(this, StroopTaskActivity.class);
                break;

            case "cognitive_flexibility":
                intent = new Intent(this, TaskSwitchActivity.class);
                break;

            case "present_moment":
            case "present_moment_awareness":
                intent = new Intent(this, SRTActivity.class);
                intent.putExtra("subtopic", "present_moment_awareness");
                break;

            default:
                Toast.makeText(this, "Unknown module type. Returning home.", Toast.LENGTH_SHORT).show();
                intent = new Intent(this, HomeActivity.class);
                break;
        }

        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
