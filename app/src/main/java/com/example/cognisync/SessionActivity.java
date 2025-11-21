package com.example.cognisync;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SessionActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private SeekBar progressBar;
    private ImageButton playPause;
    private Button btnFinish;
    private TextView tvTitle, tvDesc;

    private Handler handler = new Handler();
    private Runnable updateSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        progressBar = findViewById(R.id.progressBar);
        playPause = findViewById(R.id.playPauseButton);
        btnFinish = findViewById(R.id.btnFinishSave);
        tvTitle = findViewById(R.id.tvAudioTitle);
        tvDesc = findViewById(R.id.tvAudioDesc);

        String audioUrl = getIntent().getStringExtra("audio_url");
        String audioTitle = getIntent().getStringExtra("audio_title");
        String audioDesc = getIntent().getStringExtra("audio_desc");

        tvTitle.setText(audioTitle);
        tvDesc.setText(audioDesc);

        if (audioUrl == null || audioUrl.isEmpty()) {
            btnFinish.setText("Finish");

            btnFinish.setOnClickListener(v -> {
                Intent out = new Intent();
                out.putExtra("listened_percent", 0f);
                setResult(Activity.RESULT_OK, out);
                finish();
            });

        } else {

            Uri uri = Uri.parse(audioUrl);

            mediaPlayer = MediaPlayer.create(this, uri);

            mediaPlayer.setOnPreparedListener(mp -> {
                progressBar.setMax(mediaPlayer.getDuration());
                mediaPlayer.start();
                playPause.setImageResource(android.R.drawable.ic_media_pause);
                startSeekBarUpdate();
            });

            playPause.setOnClickListener(v -> {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    playPause.setImageResource(android.R.drawable.ic_media_play);
                } else {
                    mediaPlayer.start();
                    playPause.setImageResource(android.R.drawable.ic_media_pause);
                }
            });
        }

        // Finish button: calculate listened %
        btnFinish.setOnClickListener(v -> {
            int pos = (mediaPlayer != null) ? mediaPlayer.getCurrentPosition() : 0;
            int dur = (mediaPlayer != null) ? mediaPlayer.getDuration() : 1;

            float percent = dur > 0 ? (pos * 100f) / dur : 0f;

            Intent out = new Intent();
            out.putExtra("listened_percent", percent);
            setResult(Activity.RESULT_OK, out);

            if (mediaPlayer != null) mediaPlayer.release();
            finish();
        });

        ImageButton back = findViewById(R.id.backButton);
        back.setOnClickListener(v -> finish());
    }

    private void startSeekBarUpdate() {
        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    progressBar.setProgress(mediaPlayer.getCurrentPosition());
                }
                handler.postDelayed(this, 300);
            }
        };
        handler.post(updateSeekBar);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacks(updateSeekBar);
    }
}
