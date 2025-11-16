package com.example.cognisync;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class SessionActivity extends AppCompatActivity {

    private VideoView videoView;
    private SeekBar progressBar;
    private ImageButton playPause;
    private Button btnFinish;
    private TextView tvTitle, tvDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        videoView = findViewById(R.id.videoView);
        progressBar = findViewById(R.id.progressBar);
        playPause = findViewById(R.id.playPauseButton);
        btnFinish = findViewById(R.id.btnFinishSave);
        tvTitle = findViewById(R.id.tvVideoTitle);
        tvDesc = findViewById(R.id.tvVideoDesc);

        String videoUri = getIntent().getStringExtra("video_uri");
        String videoTitle = getIntent().getStringExtra("video_title");
        String videoDesc = getIntent().getStringExtra("video_desc");

        tvTitle.setText(videoTitle);
        tvDesc.setText(videoDesc);

        if (videoUri == null || videoUri.isEmpty()) {
            videoView.setVisibility(android.view.View.GONE);
            progressBar.setProgress(0);
            btnFinish.setText("Finish");

            btnFinish.setOnClickListener(v -> {
                Intent out = new Intent();
                out.putExtra("watched_percent", 0f);
                setResult(Activity.RESULT_OK, out);
                finish();
            });

        } else {
            Uri uri = Uri.parse(videoUri);
            videoView.setVideoURI(uri);

            videoView.setOnPreparedListener(mp -> {
                progressBar.setMax(videoView.getDuration());
                videoView.start();
                playPause.setImageResource(android.R.drawable.ic_media_pause);
            });

            playPause.setOnClickListener(v -> {
                if (videoView.isPlaying()) {
                    videoView.pause();
                    playPause.setImageResource(android.R.drawable.ic_media_play);
                } else {
                    videoView.start();
                    playPause.setImageResource(android.R.drawable.ic_media_pause);
                }
            });

            videoView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (videoView.isPlaying()) {
                        progressBar.setProgress(videoView.getCurrentPosition());
                    }
                    videoView.postDelayed(this, 300);
                }
            }, 300);
        }

        btnFinish.setOnClickListener(v -> {
            int pos = videoView.getCurrentPosition();
            int dur = videoView.getDuration();
            float percent = dur > 0 ? (pos * 100f) / dur : 0f;

            Intent out = new Intent();
            out.putExtra("watched_percent", percent);
            setResult(Activity.RESULT_OK, out);
            finish();
        });

        ImageButton back = findViewById(R.id.backButton);
        back.setOnClickListener(v -> finish());
    }
}
