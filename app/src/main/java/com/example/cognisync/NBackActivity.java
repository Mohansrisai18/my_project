package com.example.cognisync;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.*;

public class NBackActivity extends AppCompatActivity {

    private TextView tvLetter, tvScore;
    private Button btnMatch;

    private final String[] letters = {"A","B","C","D","E","F","G","H"};
    private final List<String> shown = new ArrayList<>();
    private int correct = 0, total = 0;

    private Handler handler = new Handler();
    private Random random = new Random();
    private boolean canRespond = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nback);

        tvLetter = findViewById(R.id.tvLetter);
        tvScore = findViewById(R.id.tvScore);
        btnMatch = findViewById(R.id.btnMatch);

        btnMatch.setOnClickListener(v -> {
            if (canRespond && shown.size() > 2) {
                String current = shown.get(shown.size() - 1);
                String twoBack = shown.get(shown.size() - 3);
                total++;
                if (current.equals(twoBack)) correct++;
            }
        });

        startSequence();
    }

    private void startSequence() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (shown.size() >= 20) {
                    showResult();
                    return;
                }
                String letter = letters[random.nextInt(letters.length)];
                shown.add(letter);
                tvLetter.setText(letter);
                canRespond = true;
                handler.postDelayed(this, 1500);
            }
        }, 1000);
    }

    private void showResult() {
        canRespond = false;
        double accuracy = (total == 0) ? 0 : (correct * 100.0 / total);
        tvScore.setText(String.format("Accuracy: %.1f%%", accuracy));
        // TODO: Save to dashboard variable "nback_accuracy"
    }
}
