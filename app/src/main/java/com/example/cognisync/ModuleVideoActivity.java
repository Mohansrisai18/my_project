package com.example.cognisync;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class ModuleVideoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_video);

        VideoView video = findViewById(R.id.backgroundVideo);
        ImageButton playPauseButton = findViewById(R.id.playPauseButton);
        SeekBar progressBar = findViewById(R.id.progressBar);

        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvSubtitle = findViewById(R.id.tvSubtitle);
        TextView tvSessionInfo = findViewById(R.id.tvSessionInfo);
        TextView tvDuration = findViewById(R.id.tvDuration);
        TextView tvFocus = findViewById(R.id.tvFocus);
        TextView tvLevel = findViewById(R.id.tvLevel);
        Button startAssessmentButton = findViewById(R.id.startAssessmentButton);

        // Initially hide start assessment button until video finishes
        startAssessmentButton.setVisibility(View.GONE);

        final String moduleType = getIntent().getStringExtra("module_type") == null
                ? "focused_attention"
                : getIntent().getStringExtra("module_type");

        String videoUrl = "";

        switch (moduleType) {
            case "focused_attention":
                tvTitle.setText(getString(R.string.focused_attention));
                tvSubtitle.setText(getString(R.string.mindfulness_session));
                videoUrl = "https://your-backend.com/videos/focused_attention.mp4";
                tvSessionInfo.setText(getString(R.string.focused_attention_desc));
                tvDuration.setText(getString(R.string.duration_12_minutes));
                tvFocus.setText(getString(R.string.focus_attention_training));
                tvLevel.setText(getString(R.string.level_beginner_friendly));
                break;
            case "working_memory":
                tvTitle.setText(getString(R.string.working_memory));
                tvSubtitle.setText(getString(R.string.mindfulness_session));
                videoUrl = "https://your-backend.com/videos/working_memory.mp4";
                tvSessionInfo.setText(getString(R.string.working_memory_desc));
                tvDuration.setText(getString(R.string.duration_13_minutes));
                tvFocus.setText(getString(R.string.focus_memory_exercises));
                tvLevel.setText(getString(R.string.level_beginner_friendly));
                break;
            case "present_moment":
                tvTitle.setText(getString(R.string.present_moment));
                tvSubtitle.setText(getString(R.string.mindfulness_session));
                videoUrl = "https://your-backend.com/videos/present_moment.mp4";
                tvSessionInfo.setText(getString(R.string.present_moment_desc));
                tvDuration.setText(getString(R.string.duration_20_minutes));
                tvFocus.setText(getString(R.string.focus_awareness_practices));
                tvLevel.setText(getString(R.string.level_beginner_friendly));
                break;
            case "cognitive_integration":
                tvTitle.setText(getString(R.string.cognitive_integration));
                tvSubtitle.setText(getString(R.string.mindfulness_session));
                videoUrl = "https://your-backend.com/videos/cognitive_integration.mp4";
                tvSessionInfo.setText(getString(R.string.cognitive_integration_desc));
                tvDuration.setText(getString(R.string.duration_20_minutes));
                tvFocus.setText(getString(R.string.focus_integration_techniques));
                tvLevel.setText(getString(R.string.level_beginner_friendly));
                break;
            case "emotional_regulation":
                tvTitle.setText(getString(R.string.emotional_regulation));
                tvSubtitle.setText(getString(R.string.mindfulness_session));
                videoUrl = "https://your-backend.com/videos/emotional_regulation.mp4";
                tvSessionInfo.setText(getString(R.string.emotional_regulation_desc));
                tvDuration.setText(getString(R.string.duration_15_minutes));
                tvFocus.setText(getString(R.string.focus_emotional_control));
                tvLevel.setText(getString(R.string.level_beginner_friendly));
                break;
        }

        video.setVideoURI(Uri.parse(videoUrl));

        final boolean[] isPlaying = {true};
        video.setOnPreparedListener(mp -> {
            progressBar.setMax(video.getDuration());
            video.start();
            playPauseButton.setImageResource(R.drawable.ic_pause);
        });

        // Show startAssessmentButton only after video finishes
        video.setOnCompletionListener(mp -> {
            startAssessmentButton.setVisibility(View.VISIBLE);
        });

        playPauseButton.setOnClickListener(v -> {
            if (isPlaying[0]) {
                video.pause();
                playPauseButton.setImageResource(R.drawable.ic_play_arrow);
            } else {
                video.start();
                playPauseButton.setImageResource(R.drawable.ic_pause);
            }
            isPlaying[0] = !isPlaying[0];
        });

        startAssessmentButton.setText(getString(R.string.start_assessment));
        startAssessmentButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AssessmentDetailActivity.class);
            intent.putExtra("module_type", moduleType);
            startActivity(intent);
            finish();
        });

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
    }
}
