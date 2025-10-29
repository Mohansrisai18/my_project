package com.example.cognisync;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ModuleVideoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_video);

        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvSubtitle = findViewById(R.id.tvSubtitle);
        TextView tvSessionInfo = findViewById(R.id.tvSessionInfo);
        TextView tvDuration = findViewById(R.id.tvDuration);
        TextView tvFocus = findViewById(R.id.tvFocus);
        TextView tvLevel = findViewById(R.id.tvLevel);
        Button startAssessmentButton = findViewById(R.id.startAssessmentButton);
        ImageButton backButton = findViewById(R.id.backButton);

        final String moduleType = getIntent().getStringExtra("module_type") == null
                ? "focused_attention"
                : getIntent().getStringExtra("module_type");

        // Optionally set UI info here based on moduleType...

        startAssessmentButton.setOnClickListener(v -> startAssessment(moduleType));

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ModuleVideoActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void startAssessment(String moduleType) {
        Intent intent = new Intent(ModuleVideoActivity.this, AssessmentDetailActivity.class);
        intent.putExtra("subtopic", moduleType);
        startActivity(intent);
    }
}
