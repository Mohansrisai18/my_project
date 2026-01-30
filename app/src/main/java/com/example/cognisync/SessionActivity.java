package com.example.cognisync;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;

import java.io.IOException;

public class SessionActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private Slider progressBar;
    private FloatingActionButton playPause;
    private MaterialButton btnBack;
    private ImageButton topBack;
    private TextView tvTitle, tvDesc;

    private final Handler handler = new Handler();
    private Runnable updateSlider;

    private boolean isReleased = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Fullscreen (as you had)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        // -----------------------
        // View bindings (MATCH XML)
        // -----------------------
        progressBar = findViewById(R.id.progressBar);
        playPause   = findViewById(R.id.playPauseButton);
        btnBack     = findViewById(R.id.btnBack);
        topBack     = findViewById(R.id.backButton);
        tvTitle     = findViewById(R.id.tvAudioTitle);
        tvDesc      = findViewById(R.id.tvAudioDesc);

        // Back buttons
        topBack.setOnClickListener(v -> finishWithPercent());
        btnBack.setOnClickListener(v -> finishWithPercent());

        // Intent data
        String audioUrl   = getIntent().getStringExtra("audio_url");
        String audioTitle = getIntent().getStringExtra("audio_title");
        String audioDesc  = getIntent().getStringExtra("audio_desc");

        audioTitle = sanitizeTitle(audioTitle,
                getIntent().getIntExtra("session_index", -1));
        audioDesc = sanitizeDesc(audioDesc);

        tvTitle.setText(audioTitle != null ? audioTitle : "Audio Session");
        tvDesc.setText(audioDesc != null ? audioDesc : "");

        if (audioUrl == null || audioUrl.isEmpty()) {
            return;
        }

        setupMedia(audioUrl);
        setupControls();
    }

    // --------------------------------------------------
    // SANITIZE HELPERS (unchanged)
    // --------------------------------------------------
    private String sanitizeTitle(String title, int indexHint) {
        if (title == null) return null;

        title = title.replaceAll("(?i)sub[- ]?video", "Session");
        title = title.replaceAll("(?i)sub[- ]?audio", "Session");

        if (!title.matches(".*\\d+$") && indexHint > 0) {
            title = title + " " + indexHint;
        }
        return title;
    }

    private String sanitizeDesc(String desc) {
        if (desc == null) return null;

        desc = desc.replaceAll("(?i)video", "audio");
        if (desc.toLowerCase().contains("mindfulness")) {
            return "Guided mindfulness audio";
        }
        return desc;
    }

    // --------------------------------------------------
    // MEDIA SETUP
    // --------------------------------------------------
    private void setupMedia(String url) {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(this, Uri.parse(url));
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(mp -> {
                progressBar.setValueFrom(0f);
                progressBar.setValueTo(mp.getDuration());
                progressBar.setValue(0f);

                mp.start();
                playPause.setImageResource(R.drawable.ic_pause);
                startProgressUpdates();
            });

            mediaPlayer.setOnCompletionListener(mp ->
                    playPause.setImageResource(R.drawable.ic_play)
            );

        } catch (IOException e) {
            Toast.makeText(this, "Audio playback error", Toast.LENGTH_SHORT).show();
        }
    }

    // --------------------------------------------------
    // CONTROLS
    // --------------------------------------------------
    private void setupControls() {

        playPause.setOnClickListener(v -> {
            if (mediaPlayer == null || isReleased) return;

            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                playPause.setImageResource(R.drawable.ic_play);
            } else {
                mediaPlayer.start();
                playPause.setImageResource(R.drawable.ic_pause);
            }
        });

        // Slider listener (Material Slider)
        progressBar.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser && mediaPlayer != null && !isReleased) {
                mediaPlayer.seekTo((int) value);
            }
        });
    }

    // --------------------------------------------------
    // PROGRESS UPDATE LOOP
    // --------------------------------------------------
    private void startProgressUpdates() {
        updateSlider = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying() && !isReleased) {
                    progressBar.setValue(mediaPlayer.getCurrentPosition());
                    handler.postDelayed(this, 400);
                }
            }
        };
        handler.post(updateSlider);
    }

    // --------------------------------------------------
    // FINISH HELPERS
    // --------------------------------------------------
    private void finishZero() {
        setResult(Activity.RESULT_OK);
        safeRelease();
        finish();
    }

    private void finishWithPercent() {
        if (mediaPlayer == null || isReleased) {
            finishZero();
            return;
        }

        int pos = mediaPlayer.getCurrentPosition();
        int dur = mediaPlayer.getDuration();
        float percent = dur > 0 ? (pos * 100f) / dur : 0f;

        getIntent().putExtra("listened_percent", percent);
        setResult(Activity.RESULT_OK, getIntent());

        safeRelease();
        finish();
    }

    // --------------------------------------------------
    // SAFE RELEASE
    // --------------------------------------------------
    private void safeRelease() {
        if (isReleased) return;
        isReleased = true;

        if (updateSlider != null) {
            handler.removeCallbacks(updateSlider);
        }

        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                mediaPlayer.release();
            }
        } catch (Exception ignored) {}

        mediaPlayer = null;
    }

    @Override
    protected void onStop() {
        super.onStop();
        safeRelease();
    }

    @Override
    protected void onDestroy() {
        safeRelease();
        super.onDestroy();
    }
}
