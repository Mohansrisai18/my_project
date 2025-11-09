package com.example.cognisync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Task2Activity extends AppCompatActivity {

    Spinner firstSpinner, secondSpinner, thirdSpinner;
    TextView q1Text, q2Text, q3Text;
    ImageButton btnBack;
    AppCompatButton btnNext;

    SharedPreferences sp;

    // List of emotions for Task 2
    List<String> emotions = Arrays.asList(
            "Interested", "Excited", "Enthusiastic", "Inspired", "Alert",
            "Determined", "Active", "Proud", "Upset", "Nervous",
            "Irritable", "Anxious", "Distressed", "Jittery", "Sad"
    );

    // Rating scale options
    String[] scaleOptions = {
            "Select your answer",
            "1 - Not at all",
            "2 - A little",
            "3 - Somewhat",
            "4 - Moderately",
            "5 - Quite a bit",
            "6 - Extremely"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task2);

        // Initialize SharedPreferences
        sp = getSharedPreferences("Task2Answers", MODE_PRIVATE);

        // Initialize UI elements
        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);
        firstSpinner = findViewById(R.id.firstSpinner);
        secondSpinner = findViewById(R.id.secondSpinner);
        thirdSpinner = findViewById(R.id.thirdSpinner);
        q1Text = findViewById(R.id.q1Text);
        q2Text = findViewById(R.id.q2Text);
        q3Text = findViewById(R.id.q3Text);

        // ✅ Generate & store questions once
        if (!sp.contains("q2_1")) {
            List<String> shuffled = new ArrayList<>(emotions);
            Collections.shuffle(shuffled);

            sp.edit()
                    .putString("q2_1", "I feel " + shuffled.get(0))
                    .putString("q2_2", "I feel " + shuffled.get(1))
                    .putString("q2_3", "I feel " + shuffled.get(2))
                    .apply();
        }

        // Load stored question text
        q1Text.setText(sp.getString("q2_1", ""));
        q2Text.setText(sp.getString("q2_2", ""));
        q3Text.setText(sp.getString("q2_3", ""));

        // ✅ Set up spinner adapter
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, R.layout.spinner_selected_item, scaleOptions);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        firstSpinner.setAdapter(adapter);
        secondSpinner.setAdapter(adapter);
        thirdSpinner.setAdapter(adapter);

        // ✅ Restore last selected answers
        firstSpinner.setSelection(sp.getInt("ans2_1_pos", 0));
        secondSpinner.setSelection(sp.getInt("ans2_2_pos", 0));
        thirdSpinner.setSelection(sp.getInt("ans2_3_pos", 0));

        // ✅ Next button → Save + go to Task3
        btnNext.setOnClickListener(v -> {
            saveSelections();
            Intent intent = new Intent(Task2Activity.this, Task3Activity.class);
            intent.putExtra("emotion1", firstSpinner.getSelectedItem().toString());
            intent.putExtra("emotion2", secondSpinner.getSelectedItem().toString());
            intent.putExtra("emotion3", thirdSpinner.getSelectedItem().toString());
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            finish();
        });

        // ✅ Back button → Navigate to Task1
        btnBack.setOnClickListener(v -> navigateToTask1());

        // ✅ Handle hardware back button
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToTask1();
            }
        });
    }

    // ✅ Save answers when user navigates away or minimizes the app
    @Override
    protected void onPause() {
        super.onPause();
        saveSelections();
    }

    // ✅ Helper method to save spinner selections
    private void saveSelections() {
        SharedPreferences.Editor ed = sp.edit();
        ed.putInt("ans2_1_pos", firstSpinner.getSelectedItemPosition());
        ed.putInt("ans2_2_pos", secondSpinner.getSelectedItemPosition());
        ed.putInt("ans2_3_pos", thirdSpinner.getSelectedItemPosition());
        ed.apply();
    }

    private void navigateToTask1() {
        saveSelections(); // ✅ Ensure state is saved before going back
        Intent intent = new Intent(this, Task1Activity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        finish();
    }
}
