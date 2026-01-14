package com.example.cognisync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cognisync.del.ApiClient;
import com.example.cognisync.del.ApiService;
import com.example.cognisync.model.ScoreResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ModuleIntroActivity extends AppCompatActivity {

    private TextView tvTitle, tvSubtitle, tvDescription;
    private ImageButton backButton;
    private Button btnStartPre, btnContinue;

    private String moduleType;
    private ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_intro);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        moduleType = getIntent().getStringExtra("module_type");
        if (moduleType == null) moduleType = "focused_attention";

        api = ApiClient.getClient().create(ApiService.class);

        initViews();
        loadIntroContent();

        // initial server check (will be followed by onResume local check)
        checkPreAssessmentStatusFromServer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Fast local refresh so UI updates immediately after returning
        updateUIFromLocal();
        // Also refresh from server in background to remain authoritative
        checkPreAssessmentStatusFromServer();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvIntroTitle);
        tvSubtitle = findViewById(R.id.tvIntroSubtitle);
        tvDescription = findViewById(R.id.tvIntroDescription);
        btnStartPre = findViewById(R.id.btnStartPreAssessment);
        btnContinue = findViewById(R.id.btnContinueToModule);
        backButton = findViewById(R.id.backButtonIntro);

        // default state
        btnContinue.setVisibility(View.GONE);

        backButton.setOnClickListener(v -> finish());
    }

    private void loadIntroContent() {
        switch (moduleType) {
            case "focused_attention":
                tvTitle.setText("Focused Attention");
                tvSubtitle.setText("Why Pre-Assessment?");
                tvDescription.setText("This measures your baseline focus ability.");
                break;
            case "working_memory":
                tvTitle.setText("Working Memory");
                tvSubtitle.setText("Measure Your Starting Point");
                tvDescription.setText("Tracks improvement after training.");
                break;
            case "emotional_regulation":
                tvTitle.setText("Emotional Regulation");
                tvSubtitle.setText("Assess Emotional Baseline");
                tvDescription.setText("Measure emotional response patterns.");
                break;
            case "cognitive_flexibility":
                tvTitle.setText("Cognitive Flexibility");
                tvSubtitle.setText("Why It Matters");
                tvDescription.setText("Shows how quickly you adapt to changes.");
                break;
            case "present_moment":
            case "present_moment_awareness":
                tvTitle.setText("Present Moment Awareness");
                tvSubtitle.setText("Understanding Awareness Level");
                tvDescription.setText("Shows how mindful you are before training.");
                break;
        }
    }

    private void updateUIFromLocal() {
        SharedPreferences sp = getSharedPreferences("AssessmentStatus", MODE_PRIVATE);
        boolean preDone = sp.getBoolean(moduleType + "_pre_done", false);
        applyButtons(preDone);
    }

    private void checkPreAssessmentStatusFromServer() {

        String email = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("email", "");
        String domain = getDomainForModule();

        Call<List<ScoreResponse>> call = api.getScoreHistory(email, domain);

        call.enqueue(new Callback<List<ScoreResponse>>() {
            @Override
            public void onResponse(Call<List<ScoreResponse>> call, Response<List<ScoreResponse>> response) {

                if (!response.isSuccessful() || response.body() == null) {
                    // don't override good local state — if server fails, keep local UI
                    return;
                }

                boolean isPreDone = false;

                for (ScoreResponse s : response.body()) {
                    if ("pre".equals(s.getScore_type())) {
                        isPreDone = true;
                        break;
                    }
                }

                // Update local cache (optional but keeps local & server in sync)
                getSharedPreferences("AssessmentStatus", MODE_PRIVATE)
                        .edit()
                        .putBoolean(moduleType + "_pre_done", isPreDone)
                        .apply();

                applyButtons(isPreDone);
            }

            @Override
            public void onFailure(Call<List<ScoreResponse>> call, Throwable t) {
                // keep local UI; network failure should not clear it
            }
        });
    }

    private String getDomainForModule() {
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

    private void applyButtons(boolean preCompleted) {

        if (preCompleted) {
            // Exclusive state: show re-attempt + continue; do not hide or duplicate other controls
            btnStartPre.setVisibility(View.VISIBLE);
            btnStartPre.setText("Re-Attempt Pre-Assessment");
            btnStartPre.setEnabled(true);
            btnStartPre.setAlpha(1f);
            btnStartPre.setOnClickListener(v -> {
                Intent intent = new Intent(this, PreAssessmentActivity.class);
                intent.putExtra("module_type", moduleType);
                startActivity(intent);
            });

            btnContinue.setVisibility(View.VISIBLE);
            btnContinue.setOnClickListener(v -> {
                Intent i = new Intent(this, ModuleHomeActivity.class);
                i.putExtra("module_type", moduleType);
                startActivity(i);
            });

        } else {
            // Exclusive state: show only start pre-assessment
            btnStartPre.setVisibility(View.VISIBLE);
            btnStartPre.setText("Start Pre-Assessment");
            btnStartPre.setEnabled(true);
            btnStartPre.setAlpha(1f);
            btnStartPre.setOnClickListener(v -> {
                Intent intent = new Intent(this, PreAssessmentActivity.class);
                intent.putExtra("module_type", moduleType);
                startActivity(intent);
            });

            btnContinue.setVisibility(View.GONE);
            btnContinue.setOnClickListener(null);
        }
    }
}
