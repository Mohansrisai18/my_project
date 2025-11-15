package com.example.cognisync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Session detail screen that controls the module flow:
 * - Start Pre (once)
 * - Watch Videos (open list)
 * - Start Post (enabled when preDone && watchedPercent > 0)
 * - Restart Module (clears pre & watched progress but keeps saved scores)
 */
public class SessionDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvSession;
    private Button btnStartPre, btnWatchVideos, btnStartPost, btnRestart;
    private ImageButton backButton;
    private String moduleType;
    private String sessionTitle;

    private SharedPreferences pref;
    private static final String PREF_MODULE_STATE = "ModuleState";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_detail);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        tvTitle = findViewById(R.id.tvModuleTitle);
        tvSession = findViewById(R.id.tvSessionTitle);
        btnStartPre = findViewById(R.id.btnStartPre);
        btnWatchVideos = findViewById(R.id.btnWatchVideos);
        btnStartPost = findViewById(R.id.btnStartPost);
        btnRestart = findViewById(R.id.btnRestart);
        backButton = findViewById(R.id.backButton);

        moduleType = getIntent().getStringExtra("module_type");
        sessionTitle = getIntent().getStringExtra("session_title");
        if (moduleType == null) moduleType = "focused_attention";
        if (sessionTitle == null) sessionTitle = "Session 1";

        tvTitle.setText(getReadableModuleTitle(moduleType));
        tvSession.setText(sessionTitle);

        pref = getSharedPreferences(PREF_MODULE_STATE, MODE_PRIVATE);

        updateButtons();

        backButton.setOnClickListener(v -> finish());

        btnStartPre.setOnClickListener(v -> {
            // PreAssessmentActivity will set pre flag when saved (we do it locally here too)
            Intent intent = new Intent(this, PreAssessmentActivity.class);
            intent.putExtra("module_type", moduleType);
            intent.putExtra("session_title", sessionTitle);
            startActivity(intent);
        });

        btnWatchVideos.setOnClickListener(v -> {
            Intent intent = new Intent(this, ModuleVideoListActivity.class);
            intent.putExtra("module_type", moduleType);
            intent.putExtra("session_title", sessionTitle);
            startActivity(intent);
        });

        btnStartPost.setOnClickListener(v -> {
            // Map module type → post task activity
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
                    intent = new Intent(this, SARTActivity.class);
                    break;
                default:
                    intent = new Intent(this, HomeActivity.class);
                    break;
            }
            startActivity(intent);
        });

        btnRestart.setOnClickListener(v -> {
            // clear module flags (but do NOT delete post scores)
            pref.edit()
                    .remove(moduleType + "_pre_done")
                    .remove(moduleType + "_watched_percent")
                    .apply();
            updateButtons();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateButtons();
    }

    private void updateButtons() {
        boolean preDone = pref.getBoolean(moduleType + "_pre_done", false);
        float watched = pref.getFloat(moduleType + "_watched_percent", 0f);

        btnStartPre.setEnabled(!preDone);
        btnStartPre.setText(preDone ? "Pre-Assessment Completed" : "Start Pre-Assessment");

        btnWatchVideos.setEnabled(true);
        btnWatchVideos.setText("Watch Videos");

        // Start Post enabled only if pre done and watched > 0
        boolean canStartPost = preDone && watched > 0f;
        btnStartPost.setEnabled(canStartPost);
        btnStartPost.setText(canStartPost ? "Start Post-Assessment" : "Post Locked (Do Pre and Watch video)");

        btnRestart.setEnabled(preDone || watched > 0f);
    }

    private String getReadableModuleTitle(String moduleType) {
        switch (moduleType) {
            case "focused_attention": return "Focused Attention";
            case "working_memory": return "Working Memory";
            case "emotional_regulation": return "Emotional Regulation";
            case "cognitive_flexibility": return "Cognitive Flexibility";
            case "present_moment":
            case "present_moment_awareness": return "Present-Moment Awareness";
            default: return "Module";
        }
    }
}
