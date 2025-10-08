package com.example.cognisync;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class HomeActivity extends AppCompatActivity {

    private CardView cvFocusedAttention;
    private CardView cvWorkingMemory;
    private CardView cvPresentMoment;
    private CardView cvCognitiveIntegration;
    private CardView cvEmotionalRegulation;

    private CardView cvProgressDashboard;
    private CardView cvMemoryScore;
    private CardView cvAttentionScore;
    private CardView cvEmotionScore;
    private CardView cvGraphSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // CardViews for mindfulness modules
        cvFocusedAttention     = findViewById(R.id.cvFocusedAttention);
        cvWorkingMemory        = findViewById(R.id.cvWorkingMemory);
        cvPresentMoment        = findViewById(R.id.cvPresentMoment);
        cvCognitiveIntegration = findViewById(R.id.cvCognitiveIntegration);
        cvEmotionalRegulation  = findViewById(R.id.cvEmotionalRegulation);

        // CardViews for scores and graph/buttons
        cvProgressDashboard    = findViewById(R.id.cvProgressDashboard);
        cvMemoryScore          = findViewById(R.id.cvMemoryScore);
        cvAttentionScore       = findViewById(R.id.cvAttentionScore);
        cvEmotionScore         = findViewById(R.id.cvEmotionScore);
        cvGraphSection         = findViewById(R.id.cvGraphSection);

        // Set click listeners for the 5 modules (all open the same VideoPlayerActivity with different module types)
        cvFocusedAttention.setOnClickListener(v -> openModuleVideo("focused_attention"));
        cvWorkingMemory.setOnClickListener(v -> openModuleVideo("working_memory"));
        cvPresentMoment.setOnClickListener(v -> openModuleVideo("present_moment"));
        cvCognitiveIntegration.setOnClickListener(v -> openModuleVideo("cognitive_integration"));
        cvEmotionalRegulation.setOnClickListener(v -> openModuleVideo("emotional_regulation"));

        // Set click listeners for score cards
        cvAttentionScore.setOnClickListener(v -> openScoreDetail("Attention"));
        cvMemoryScore.setOnClickListener(v -> openScoreDetail("Memory"));
        cvEmotionScore.setOnClickListener(v -> openScoreDetail("Emotion"));

        // Dashboard and Graph navigation
        cvProgressDashboard.setOnClickListener(v -> startActivity(new Intent(this, ProgressOverviewActivity.class)));
        cvGraphSection.setOnClickListener(v -> startActivity(new Intent(this, TrendChartActivity.class)));
    }

    // Open the video player with appropriate module type
    private void openModuleVideo(String moduleType) {
        Intent intent = new Intent(this, ModuleVideoActivity.class);
        intent.putExtra("module_type", moduleType);
        startActivity(intent);
    }

    private void openScoreDetail(String type) {
        Intent intent = new Intent(this, AssessmentDetailActivity.class);
        intent.putExtra("score_type", type);
        startActivity(intent);
    }
}
