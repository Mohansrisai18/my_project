package com.example.cognisync;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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

    private Handler handler = new Handler();
    private Runnable updateSeekBar;
    private int SEEK_STEP = 10000; // 10 seconds skip

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        // UI refs
        progressBar = findViewById(R.id.progressBar);
        playPause = findViewById(R.id.playPauseButton);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        btnFinish = findViewById(R.id.btnFinishSave);
        tvTitle = findViewById(R.id.tvAudioTitle);
        tvDesc = findViewById(R.id.tvAudioDesc);

        // Back button
        ImageButton back = findViewById(R.id.backButton);
        back.setOnClickListener(v -> finishSession());

        // Get extras
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
                progressBar.setMax(mediaPlayer.getDuration());
                mediaPlayer.start();
                playPause.setImageResource(R.drawable.ic_pause);
                startSeekBarUpdate();
            });

            mediaPlayer.setOnCompletionListener(mp ->
                    playPause.setImageResource(R.drawable.ic_play));

        } catch (IOException e) {
            Toast.makeText(this, "Audio error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupButtons() {

        // Play pause toggle
        playPause.setOnClickListener(v -> {
            if (mediaPlayer == null) return;

            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                playPause.setImageResource(R.drawable.ic_play);
            } else {
                mediaPlayer.start();
                playPause.setImageResource(R.drawable.ic_pause);
            }
        });

        // 10 sec rewind
        btnPrev.setOnClickListener(v -> {
            if (mediaPlayer == null) return;
            int newPos = mediaPlayer.getCurrentPosition() - SEEK_STEP;
            mediaPlayer.seekTo(Math.max(newPos, 0));
        });

        // 10 sec forward
        btnNext.setOnClickListener(v -> {
            if (mediaPlayer == null) return;
            int newPos = mediaPlayer.getCurrentPosition() + SEEK_STEP;
            mediaPlayer.seekTo(Math.min(newPos, mediaPlayer.getDuration()));
        });

        // SeekBar drag
        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Finish action
        btnFinish.setOnClickListener(v -> finishWithPercent());
    }

    private void startSeekBarUpdate() {
        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    progressBar.setProgress(mediaPlayer.getCurrentPosition());
                }
                handler.postDelayed(this, 400);
            }
        };
        handler.post(updateSeekBar);
    }

    private void finishZero() {
        Intent out = new Intent();
        out.putExtra("listened_percent", 0f);
        setResult(Activity.RESULT_OK, out);
        finishSession();
    }

    private void finishWithPercent() {
        if (mediaPlayer == null) {
            finishZero();
            return;
        }

        int pos = mediaPlayer.getCurrentPosition();
        int dur = mediaPlayer.getDuration();
        float percent = dur > 0 ? (pos * 100f) / dur : 0f;

        Intent out = new Intent();
        out.putExtra("listened_percent", percent);
        setResult(Activity.RESULT_OK, out);

        finishSession();
    }

    private void finishSession() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
        } catch (Exception ignored) {}
        mediaPlayer = null;

        if (updateSeekBar != null) handler.removeCallbacks(updateSeekBar);
        finish();
    }

    @Override
    protected void onDestroy() {
        finishSession();
        super.onDestroy();
    }
}
