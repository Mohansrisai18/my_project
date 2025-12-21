package com.example.cognisync;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class SessionActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private SeekBar progressBar;
    private ImageButton playPause, btnPrev, btnNext;
    private Button btnFinish;
    private TextView tvTitle, tvDesc;

    private final Handler handler = new Handler();
    private Runnable updateSeekBar;
    private final int SEEK_STEP = 10000; // 10 seconds

    private boolean isReleased = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        progressBar = findViewById(R.id.progressBar);
        playPause = findViewById(R.id.playPauseButton);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        btnFinish = findViewById(R.id.btnFinishSave);
        tvTitle = findViewById(R.id.tvAudioTitle);
        tvDesc = findViewById(R.id.tvAudioDesc);

        ImageButton back = findViewById(R.id.backButton);
        back.setOnClickListener(v -> finishWithPercent());

        String audioUrl = getIntent().getStringExtra("audio_url");
        String audioTitle = getIntent().getStringExtra("audio_title");
        String audioDesc = getIntent().getStringExtra("audio_desc");

        tvTitle.setText(audioTitle != null ? audioTitle : "Session");
        tvDesc.setText(audioDesc != null ? audioDesc : "");

        if (audioUrl == null || audioUrl.isEmpty()) {
            btnFinish.setOnClickListener(v -> finishZero());
            return;
        }

        setupMedia(audioUrl);
        setupButtons();
    }

    private void setupMedia(String url) {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(this, Uri.parse(url));
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(mp -> {
                progressBar.setMax(mp.getDuration());
                mp.start();
                playPause.setImageResource(R.drawable.ic_pause);
                startSeekBarUpdate();
            });

            mediaPlayer.setOnCompletionListener(mp ->
                    playPause.setImageResource(R.drawable.ic_play));

        } catch (IOException e) {
            Toast.makeText(this, "Audio error", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupButtons() {

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

        btnPrev.setOnClickListener(v -> {
            if (mediaPlayer == null || isReleased) return;
            mediaPlayer.seekTo(Math.max(mediaPlayer.getCurrentPosition() - SEEK_STEP, 0));
        });

        btnNext.setOnClickListener(v -> {
            if (mediaPlayer == null || isReleased) return;
            mediaPlayer.seekTo(Math.min(
                    mediaPlayer.getCurrentPosition() + SEEK_STEP,
                    mediaPlayer.getDuration()
            ));
        });

        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null && !isReleased) {
                    mediaPlayer.seekTo(progress);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnFinish.setOnClickListener(v -> finishWithPercent());
    }

    private void startSeekBarUpdate() {
        updateSeekBar = () -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying() && !isReleased) {
                progressBar.setProgress(mediaPlayer.getCurrentPosition());
                handler.postDelayed(updateSeekBar, 400);
            }
        };
        handler.post(updateSeekBar);
    }

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

    private void safeRelease() {
        if (isReleased) return;
        isReleased = true;

        if (updateSeekBar != null) handler.removeCallbacks(updateSeekBar);

        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                mediaPlayer.release();
            }
        } catch (Exception ignored) {}

        mediaPlayer = null;
    }

    @Override
    protected void onDestroy() {
        safeRelease();
        super.onDestroy();
    }
}
