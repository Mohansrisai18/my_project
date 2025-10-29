package com.example.cognisync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class HomeActivity extends AppCompatActivity {

    private CardView cvFocusedAttention, cvWorkingMemory, cvPresentMoment,
            cvCognitiveIntegration, cvEmotionalRegulation,
            cvProgressDashboard, cvMemoryScore, cvAttentionScore,
            cvEmotionScore, cvGraphSection;

    private TextView tvGreeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tvGreeting = findViewById(R.id.tvGreeting);

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "User");
        if (username == null || username.trim().isEmpty()) {
            tvGreeting.setText("Hi!");
        } else {
            tvGreeting.setText("Hi, " + username);
        }

        ImageButton btnMenu = findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        cvFocusedAttention = findViewById(R.id.cvFocusedAttention);
        cvWorkingMemory = findViewById(R.id.cvWorkingMemory);
        cvPresentMoment = findViewById(R.id.cvPresentMoment);
        cvCognitiveIntegration = findViewById(R.id.cvCognitiveIntegration);
        cvEmotionalRegulation = findViewById(R.id.cvEmotionalRegulation);
        cvProgressDashboard = findViewById(R.id.cvProgressDashboard);
        cvMemoryScore = findViewById(R.id.cvMemoryScore);
        cvAttentionScore = findViewById(R.id.cvAttentionScore);
        cvEmotionScore = findViewById(R.id.cvEmotionScore);
        cvGraphSection = findViewById(R.id.cvGraphSection);

        cvFocusedAttention.setOnClickListener(v -> openModuleVideo("focused_attention"));
        cvWorkingMemory.setOnClickListener(v -> openModuleVideo("working_memory"));
        cvPresentMoment.setOnClickListener(v -> openModuleVideo("present_moment"));
        cvCognitiveIntegration.setOnClickListener(v -> openModuleVideo("cognitive_integration"));
        cvEmotionalRegulation.setOnClickListener(v -> openModuleVideo("emotional_regulation"));

        cvAttentionScore.setOnClickListener(v -> openScoreDetail("Attention"));
        cvMemoryScore.setOnClickListener(v -> openScoreDetail("Memory"));
        cvEmotionScore.setOnClickListener(v -> openScoreDetail("Emotion"));

        cvProgressDashboard.setOnClickListener(v -> startActivity(new Intent(this, ProgressDashboardActivity.class)));
        cvGraphSection.setOnClickListener(v -> startActivity(new Intent(this, TrendChartActivity.class)));
    }

    private void openModuleVideo(String moduleType) {
        Intent intent = new Intent(this, ModuleVideoActivity.class);
        intent.putExtra("module_type", moduleType);
        startActivity(intent);
    }

    private void openScoreDetail(String type) {
        Intent intent = new Intent(this, ProgressDashboardActivity.class);
        intent.putExtra("score_type", type);
        startActivity(intent);
    }
}
