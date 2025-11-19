package com.example.cognisync;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cognisync.del.ApiClient;
import com.example.cognisync.del.ApiService;
import com.example.cognisync.model.ScoreResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ModuleHomeActivity extends AppCompatActivity {

    private RecyclerView sessionRecyclerView;
    private Button btnStartPre, btnStartPost;
    private ImageButton backButton;

    private String moduleType;

    private ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_home);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        api = ApiClient.getClient().create(ApiService.class);

        moduleType = getIntent().getStringExtra("module_type");
        if (moduleType == null) moduleType = "focused_attention";

        backButton = findViewById(R.id.backButton);
        btnStartPre = findViewById(R.id.btnStartPre);
        btnStartPost = findViewById(R.id.btnStartPost);

        TextView tv = findViewById(R.id.tvModuleTitle);
        tv.setText(pretty(moduleType));

        backButton.setOnClickListener(v -> finish());

        setupRecycler();
        checkPreStatusFromServer();
        checkPostStatusFromServer();

        btnStartPost.setOnClickListener(v -> openPostTask());
    }

    // ---------------------- BACKEND CHECK: PRE STATUS ----------------------
    private void checkPreStatusFromServer() {
        String email = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("email", "");
        String domain = getPreDomain();

        Call<List<ScoreResponse>> call = api.getScoreHistory(email, domain);

        call.enqueue(new Callback<List<ScoreResponse>>() {
            @Override
            public void onResponse(Call<List<ScoreResponse>> call, Response<List<ScoreResponse>> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(ModuleHomeActivity.this, "Error loading pre status", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean preDone = false;
                for (ScoreResponse s : response.body()) {
                    if ("pre".equals(s.getScore_type())) {
                        preDone = true;
                        break;
                    }
                }

                applyPreUI(preDone);
            }

            @Override
            public void onFailure(Call<List<ScoreResponse>> call, Throwable t) {
                Toast.makeText(ModuleHomeActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyPreUI(boolean preDone) {
        if (preDone) {
            btnStartPre.setEnabled(false);
            btnStartPre.setText("Pre-Assessment Completed ✓");

            btnStartPost.setEnabled(true);

        } else {
            btnStartPre.setEnabled(true);
            btnStartPre.setOnClickListener(v -> {
                Intent i = new Intent(this, PreAssessmentActivity.class);
                i.putExtra("module_type", moduleType);
                startActivity(i);
            });

            btnStartPost.setEnabled(false);
        }
    }

    private String getPreDomain() {
        switch (moduleType) {
            case "focused_attention": return "maas";
            case "working_memory": return "cfq";
            case "emotional_regulation": return "panas";
            case "cognitive_flexibility": return "dass";
            case "present_moment":
            case "present_moment_awareness": return "phlms";
        }
        return "maas";
    }

    // ---------------------- BACKEND CHECK: POST STATUS ----------------------
    private void checkPostStatusFromServer() {

        String email = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("email", "");
        String domain = getPostDomain();

        Call<List<ScoreResponse>> call = api.getScoreHistory(email, domain);

        call.enqueue(new Callback<List<ScoreResponse>>() {
            @Override
            public void onResponse(Call<List<ScoreResponse>> call, Response<List<ScoreResponse>> response) {
                if (!response.isSuccessful()) {
                    return;
                }

                boolean postDone = false;

                for (ScoreResponse s : response.body()) {
                    if ("post".equals(s.getScore_type())) {
                        postDone = true;
                        break;
                    }
                }

                applyPostUI(postDone);
            }

            @Override
            public void onFailure(Call<List<ScoreResponse>> call, Throwable t) {}
        });
    }

    private void applyPostUI(boolean postDone) {
        if (postDone) {
            btnStartPost.setEnabled(false);
            btnStartPost.setText("Post-Assessment Completed ✓");
        }
    }

    private String getPostDomain() {
        switch (moduleType) {
            case "focused_attention": return "srt";
            case "working_memory": return "nback";
            case "emotional_regulation": return "stroop";
            case "cognitive_flexibility": return "task_switch";
            case "present_moment":
            case "present_moment_awareness": return "sart";
        }
        return "srt";
    }

    // ---------------------- RECYCLER ----------------------
    private void setupRecycler() {
        sessionRecyclerView = findViewById(R.id.sessionRecyclerView);
        sessionRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<ModuleSessionItem> sessions = new ArrayList<>();
        sessions.add(new ModuleSessionItem("Video 1", "Introduction session"));
        sessions.add(new ModuleSessionItem("Video 2", "Guided practice"));
        sessions.add(new ModuleSessionItem("Video 3", "Deep training"));
        sessions.add(new ModuleSessionItem("Video 4", "Advanced practice"));
        sessions.add(new ModuleSessionItem("Video 5", "Final session"));

        ModuleSessionAdapter adapter = new ModuleSessionAdapter(
                sessions, session -> {
            Intent i = new Intent(this, SessionActivity.class);
            i.putExtra("module_type", moduleType);
            i.putExtra("video_title", session.getTitle());
            i.putExtra("video_desc", session.getDescription());
            startActivity(i);
        });

        sessionRecyclerView.setAdapter(adapter);
    }

    // ---------------------- POST TASK ----------------------
    private void openPostTask() {
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
            default:
                intent = new Intent(this, SARTActivity.class);
        }
        startActivity(intent);
    }

    private String pretty(String type) {
        switch (type) {
            case "focused_attention": return "Focused Attention";
            case "working_memory": return "Working Memory";
            case "emotional_regulation": return "Emotional Regulation";
            case "cognitive_flexibility": return "Cognitive Flexibility";
            case "present_moment": return "Present-Moment Awareness";
        }
        return "Mindfulness Module";
    }
}
