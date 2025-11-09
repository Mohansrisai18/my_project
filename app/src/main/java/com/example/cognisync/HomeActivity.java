package com.example.cognisync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class HomeActivity extends AppCompatActivity {

    private TextView tvGreeting;
    TextView tvMinutesFocused, tvMinutesWorking, tvMinutesPresent, tvMinutesCognitive, tvMinutesEmotional;

    CardView cvFocusedAttention, cvWorkingMemory, cvPresentMoment,
            cvCognitiveIntegration, cvEmotionalRegulation,
            cvProgressDashboard, cvGraphSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Greeting
        tvGreeting = findViewById(R.id.tvGreeting);
        SharedPreferences userPref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String username = userPref.getString("username", "User");
        tvGreeting.setText("Hi, " + username);

        initViews();
        loadDynamicMinutes();
        setClickActions();
    }

    private void initViews() {
        // Module Minutes TextViews
        tvMinutesFocused = findViewById(R.id.tvMinutesFocused);
        tvMinutesWorking = findViewById(R.id.tvMinutesWorking);
        tvMinutesPresent = findViewById(R.id.tvMinutesPresent);
        tvMinutesCognitive = findViewById(R.id.tvMinutesCognitive);
        tvMinutesEmotional = findViewById(R.id.tvMinutesEmotional);

        // Module Cards
        cvFocusedAttention = findViewById(R.id.cvFocusedAttention);
        cvWorkingMemory = findViewById(R.id.cvWorkingMemory);
        cvPresentMoment = findViewById(R.id.cvPresentMoment);
        cvCognitiveIntegration = findViewById(R.id.cvCognitiveIntegration);
        cvEmotionalRegulation = findViewById(R.id.cvEmotionalRegulation);

        // Navigation Cards
        cvProgressDashboard = findViewById(R.id.cvProgressDashboard);
        cvGraphSection = findViewById(R.id.cvGraphSection);

        // Menu Button
        ImageButton btnMenu = findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
    }

    private void loadDynamicMinutes() {
        SharedPreferences sp = getSharedPreferences("ModuleData", MODE_PRIVATE);
        tvMinutesFocused.setText(sp.getString("module_focused_minutes", "--"));
        tvMinutesWorking.setText(sp.getString("module_working_minutes", "--"));
        tvMinutesPresent.setText(sp.getString("module_present_minutes", "--"));
        tvMinutesCognitive.setText(sp.getString("module_cognitive_minutes", "--"));
        tvMinutesEmotional.setText(sp.getString("module_emotional_minutes", "--"));
    }

    private void setClickActions() {
        // Open the list of sessions for each module
        cvFocusedAttention.setOnClickListener(v -> openModuleList("focused_attention"));
        cvWorkingMemory.setOnClickListener(v -> openModuleList("working_memory"));
        cvPresentMoment.setOnClickListener(v -> openModuleList("present_moment"));
        cvCognitiveIntegration.setOnClickListener(v -> openModuleList("cognitive_flexibility"));
        cvEmotionalRegulation.setOnClickListener(v -> openModuleList("emotional_regulation"));

        // Open Dashboard & Graph
        cvProgressDashboard.setOnClickListener(v ->
                startActivity(new Intent(this, ProgressDashboardActivity.class)));
        cvGraphSection.setOnClickListener(v ->
                startActivity(new Intent(this, TrendChartActivity.class)));
    }

    private void openModuleList(String moduleType) {
        Intent intent = new Intent(this, ModuleListActivity.class);
        intent.putExtra("module_type", moduleType);
        startActivity(intent);
    }
}
