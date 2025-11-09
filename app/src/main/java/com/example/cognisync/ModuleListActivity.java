package com.example.cognisync;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ModuleListActivity extends AppCompatActivity {

    private TextView tvModuleTitle, tvModuleDescription;
    private RecyclerView sessionRecyclerView;
    private ModuleSessionAdapter adapter;
    private List<ModuleSessionItem> sessionList = new ArrayList<>();
    private String currentModuleType = "focused_attention";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_list);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        tvModuleTitle = findViewById(R.id.tvModuleTitle);
        tvModuleDescription = findViewById(R.id.tvModuleDescription);
        sessionRecyclerView = findViewById(R.id.sessionRecyclerView);
        ImageButton backButton = findViewById(R.id.backButton);

        String moduleType = getIntent().getStringExtra("module_type");
        if (moduleType == null) moduleType = "focused_attention";

        currentModuleType = moduleType;
        setupModuleDetails(moduleType);
        setupRecycler();

        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void setupModuleDetails(String moduleType) {
        switch (moduleType) {
            case "focused_attention":
                tvModuleTitle.setText("Focused Attention");
                tvModuleDescription.setText("Train sustained focus using breath-based mindfulness.");
                sessionList = createSessionList("focused_attention");
                break;
            case "working_memory":
                tvModuleTitle.setText("Working Memory");
                tvModuleDescription.setText("Enhance memory and concentration through visualization.");
                sessionList = createSessionList("working_memory");
                break;
            case "emotional_regulation":
                tvModuleTitle.setText("Emotional Regulation");
                tvModuleDescription.setText("Improve emotional awareness and resilience.");
                sessionList = createSessionList("emotional_regulation");
                break;
            case "present_moment":
                tvModuleTitle.setText("Present-Moment Awareness");
                tvModuleDescription.setText("Cultivate awareness of the here and now.");
                sessionList = createSessionList("present_moment");
                break;
            case "cognitive_flexibility":
                tvModuleTitle.setText("Cognitive Flexibility");
                tvModuleDescription.setText("Develop adaptability and open awareness.");
                sessionList = createSessionList("cognitive_flexibility");
                break;
        }
    }

    private void setupRecycler() {
        adapter = new ModuleSessionAdapter(sessionList, session -> {
            // 👇 Navigate to PreAssessment before module video
            Intent intent = new Intent(ModuleListActivity.this, PreAssessmentActivity.class);
            intent.putExtra("module_type", currentModuleType);
            intent.putExtra("session_title", session.getTitle());
            startActivity(intent);
        });

        sessionRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        sessionRecyclerView.setAdapter(adapter);
    }

    private List<ModuleSessionItem> createSessionList(String moduleType) {
        List<ModuleSessionItem> list = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            list.add(new ModuleSessionItem(
                    "Session " + i,
                    "Guided practice for " + moduleType.replace("_", " "),
                    moduleType
            ));
        }
        return list;
    }
}
