package com.example.cognisync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ModuleHomeActivity extends AppCompatActivity {

    private RecyclerView sessionRecyclerView;
    private Button btnStartPre, btnStartPost;
    private ImageButton backButton;

    private String moduleType;
    private static final String PREF_STATE = "ModuleState";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_home);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        moduleType = getIntent().getStringExtra("module_type");
        if (moduleType == null) moduleType = "focused_attention";

        backButton = findViewById(R.id.backButton);
        btnStartPre = findViewById(R.id.btnStartPre);
        btnStartPost = findViewById(R.id.btnStartPost);

        TextView tv = findViewById(R.id.tvModuleTitle);
        tv.setText(pretty(moduleType));

        backButton.setOnClickListener(v -> finish());

        setupPreLogic();
        setupRecycler();

        btnStartPost.setOnClickListener(v -> openPostTask());
    }

    private void setupPreLogic() {
        SharedPreferences sp = getSharedPreferences(PREF_STATE, MODE_PRIVATE);
        boolean preDone = sp.getBoolean(moduleType + "_pre_completed", false);

        if (preDone) {
            btnStartPre.setEnabled(false);
            btnStartPre.setText("Pre-Assessment Completed ✓");
        } else {
            btnStartPost.setEnabled(false);
            btnStartPre.setOnClickListener(v -> {
                Intent i = new Intent(this, PreAssessmentActivity.class);
                i.putExtra("module_type", moduleType);
                startActivity(i);
            });
        }
    }

    private void setupRecycler() {
        sessionRecyclerView = findViewById(R.id.sessionRecyclerView);
        sessionRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<ModuleSessionItem> list = new ArrayList<>();
        list.add(new ModuleSessionItem("Video 1", "Introduction session", moduleType));
        list.add(new ModuleSessionItem("Video 2", "Guided practice", moduleType));
        list.add(new ModuleSessionItem("Video 3", "Deep training", moduleType));
        list.add(new ModuleSessionItem("Video 4", "Advanced practice", moduleType));
        list.add(new ModuleSessionItem("Video 5", "Final session", moduleType));

        ModuleSessionAdapter adapter = new ModuleSessionAdapter(list, session -> {
            // 🎯 OPEN VIDEO PLAYER HERE
            Intent i = new Intent(this, SessionActivity.class);
            i.putExtra("module_type", moduleType);
            i.putExtra("video_title", session.getTitle());
            i.putExtra("video_desc", session.getDescription());
            i.putExtra("video_uri", ""); // blank until backend provides real video URL
            startActivity(i);
        });

        sessionRecyclerView.setAdapter(adapter);
    }

    private void openPostTask() {
        Intent intent;
        switch (moduleType) {
            case "focused_attention": intent = new Intent(this, SRTActivity.class); break;
            case "working_memory": intent = new Intent(this, NBackActivity.class); break;
            case "emotional_regulation": intent = new Intent(this, StroopTaskActivity.class); break;
            case "cognitive_flexibility": intent = new Intent(this, TaskSwitchActivity.class); break;
            default: intent = new Intent(this, SARTActivity.class);
        }
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupPreLogic();
    }

    private String pretty(String type) {
        switch (type) {
            case "focused_attention": return "Focused Attention";
            case "working_memory": return "Working Memory";
            case "emotional_regulation": return "Emotional Regulation";
            case "cognitive_flexibility": return "Cognitive Flexibility";
            case "present_moment": return "Present-Moment Awareness";
            default: return "Mindfulness Module";
        }
    }
}
