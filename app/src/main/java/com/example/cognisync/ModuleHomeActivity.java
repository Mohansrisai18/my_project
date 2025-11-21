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

        loadAudiosFromServer();   // 🔥 load audio list dynamically
        checkPreStatusFromServer();
        checkPostStatusFromServer();

        btnStartPost.setOnClickListener(v -> openPostTask());
    }

    // ---------------------- LOAD AUDIOS ----------------------
    private void loadAudiosFromServer() {

        Call<List<AudioResponse>> call = api.getAudios(moduleType);

        call.enqueue(new Callback<List<AudioResponse>>() {
            @Override
            public void onResponse(Call<List<AudioResponse>> call, Response<List<AudioResponse>> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(ModuleHomeActivity.this, "Error loading audios", Toast.LENGTH_SHORT).show();
                    return;
                }

                audioList.clear();

                for (AudioResponse a : response.body()) {
                    audioList.add(new ModuleSessionItem(
                            a.getTitle(),
                            "Mindfulness Audio Session",
                            a.getUrl()
                    ));
                }

                setupRecycler(audioList);
            }

            @Override
            public void onFailure(Call<List<AudioResponse>> call, Throwable t) {
                Toast.makeText(ModuleHomeActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    // ---------------------- RECYCLER ----------------------
    private void setupRecycler(List<ModuleSessionItem> audioItems) {
        sessionRecyclerView = findViewById(R.id.sessionRecyclerView);
        sessionRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        ModuleSessionAdapter adapter = new ModuleSessionAdapter(
                audioItems, session -> {

            Intent i = new Intent(this, SessionActivity.class);
            i.putExtra("audio_url", session.getAudioUrl());
            i.putExtra("video_title", session.getTitle());
            i.putExtra("video_desc", session.getDescription());
            startActivity(i);
        });

        sessionRecyclerView.setAdapter(adapter);
    }


    // ---------------------- CHECK PRE STATUS ----------------------
    private void checkPreStatusFromServer() {
        String email = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("email", "");
        String domain = getPreDomain();

        Call<List<ScoreResponse>> call = api.getScoreHistory(email, domain);

        call.enqueue(new Callback<List<ScoreResponse>>() {
            @Override
            public void onResponse(Call<List<ScoreResponse>> call, Response<List<ScoreResponse>> response) {
                if (!response.isSuccessful()) {
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
        if (preDone) {
            btnStartPre.setText("Re-Attempt Pre-Assessment");
        } else {
            btnStartPre.setText("Start Pre-Assessment");
        }

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

    // ---------------------- CHECK POST STATUS ----------------------
    private void checkPostStatusFromServer() {

        String email = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("email", "");
        String domain = getPostDomain();

        Call<List<ScoreResponse>> call = api.getScoreHistory(email, domain);

        call.enqueue(new Callback<List<ScoreResponse>>() {
            @Override
            public void onResponse(Call<List<ScoreResponse>> call, Response<List<ScoreResponse>> response) {
                if (!response.isSuccessful()) {
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
        if (postDone) {
            btnStartPost.setText("Re-Attempt Post-Assessment");
        } else {
            btnStartPost.setText("Start Post-Assessment");
        }

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
