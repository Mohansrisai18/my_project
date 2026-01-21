package com.example.cognisync;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cognisync.del.ApiClient;
import com.example.cognisync.del.ApiService;
import com.example.cognisync.model.AudioResponse;
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

    private List<ModuleSessionItem> audioList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_home);

        // If you're hiding the ActionBar elsewhere, you can keep this
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // ---------------------------
        // Window inset handling:
        // ensure top content (back button) is below status bar on all devices
        // ---------------------------
        View root = findViewById(android.R.id.content);
        if (root != null) {
            final int left = root.getPaddingLeft();
            final int right = root.getPaddingRight();
            final int bottom = root.getPaddingBottom();

            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());

                // Add top padding equal to status bar top inset to avoid overlap
                v.setPadding(
                        left,
                        bars.top,
                        right,
                        Math.max(bottom, ime.bottom)
                );
                return insets;
            });
            ViewCompat.requestApplyInsets(root);
        }

        api = ApiClient.getClient().create(ApiService.class);

        moduleType = getIntent().getStringExtra("module_type");
        if (moduleType == null) moduleType = "focused_attention";

        backButton = findViewById(R.id.backButton);
        btnStartPre = findViewById(R.id.btnStartPre);
        btnStartPost = findViewById(R.id.btnStartPost);

        TextView tv = findViewById(R.id.tvModuleTitle);
        if (tv != null) tv.setText(pretty(moduleType));

        // Safe back button handling (do nothing if view missing)
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        loadAudiosFromServer();
        checkPreStatusFromServer();
        checkPostStatusFromServer();

        if (btnStartPost != null) {
            btnStartPost.setOnClickListener(v -> openPostTask());
        }
    }

    // Convert Android module → backend module code (m-1, m-2...)
    private String toBackendModule(String t) {
        switch (t) {
            case "focused_attention": return "m-1";
            case "working_memory": return "m-2";
            case "emotional_regulation": return "m-3";
            case "present_moment":
            case "present_moment_awareness": return "m-4";
            case "cognitive_flexibility": return "m-5";
        }
        return "m-1";
    }

    // LOAD AUDIOS
    private void loadAudiosFromServer() {

        String backendModule = toBackendModule(moduleType);

        Call<List<AudioResponse>> call = api.getAudios(backendModule);

        call.enqueue(new Callback<List<AudioResponse>>() {
            @Override
            public void onResponse(Call<List<AudioResponse>> call, Response<List<AudioResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(ModuleHomeActivity.this, "Error loading audios", Toast.LENGTH_SHORT).show();
                    return;
                }

                audioList.clear();

                int index = 1;
                for (AudioResponse a : response.body()) {
                    String title = "Audio Session " + index;
                    String desc = "Guided mindfulness audio";

                    // keep audio URL so SessionActivity can play it
                    audioList.add(new ModuleSessionItem(title, desc, a.getUrl()));
                    index++;
                }

                setupRecycler(audioList);
            }

            @Override
            public void onFailure(Call<List<AudioResponse>> call, Throwable t) {
                Toast.makeText(ModuleHomeActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // RECYCLER
    private void setupRecycler(List<ModuleSessionItem> audioItems) {
        sessionRecyclerView = findViewById(R.id.sessionRecyclerView);
        if (sessionRecyclerView == null) return;

        sessionRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        ModuleSessionAdapter adapter = new ModuleSessionAdapter(
                audioItems, session -> {

            Intent i = new Intent(this, SessionActivity.class);
            i.putExtra("audio_url", session.getAudioUrl());
            i.putExtra("audio_title", session.getTitle());
            i.putExtra("audio_desc", session.getDescription());
            startActivity(i);
        });

        sessionRecyclerView.setAdapter(adapter);
    }

    // PRE STATUS
    private void checkPreStatusFromServer() {
        String email = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("email", "");
        String domain = getPreDomain();

        Call<List<ScoreResponse>> call = api.getScoreHistory(email, domain);

        call.enqueue(new Callback<List<ScoreResponse>>() {
            @Override
            public void onResponse(Call<List<ScoreResponse>> call, Response<List<ScoreResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    applyPreUI(false);
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
                applyPreUI(false);
            }
        });
    }

    private void applyPreUI(boolean preDone) {
        if (btnStartPre == null) return;

        btnStartPre.setText(preDone ? "Re-Attempt Pre-Assessment" : "Start Pre-Assessment");

        btnStartPre.setEnabled(true);
        btnStartPre.setOnClickListener(v -> {
            Intent i = new Intent(this, PreAssessmentActivity.class);
            i.putExtra("module_type", moduleType);
            startActivity(i);
        });
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

    // POST STATUS
    private void checkPostStatusFromServer() {

        String email = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("email", "");
        String domain = getPostDomain();

        Call<List<ScoreResponse>> call = api.getScoreHistory(email, domain);

        call.enqueue(new Callback<List<ScoreResponse>>() {
            @Override
            public void onResponse(Call<List<ScoreResponse>> call, Response<List<ScoreResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    applyPostUI(false);
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
            public void onFailure(Call<List<ScoreResponse>> call, Throwable t) {
                applyPostUI(false);
            }
        });
    }

    private void applyPostUI(boolean postDone) {
        if (btnStartPost == null) return;
        btnStartPost.setText(postDone ? "Re-Attempt Post-Assessment" : "Start Post-Assessment");
        btnStartPost.setEnabled(true);
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

    // OPEN TASK
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
