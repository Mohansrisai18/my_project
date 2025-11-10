package com.example.cognisync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Task4Activity extends AppCompatActivity {

    private ImageButton btnBack;
    private Spinner firstSpinner, secondSpinner, thirdSpinner;
    private AppCompatButton btnNext;
    private TextView q1Text, q2Text, q3Text;

    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task4);

        sp = getSharedPreferences("Task4Answers", MODE_PRIVATE);

        // UI Components
        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);
        firstSpinner = findViewById(R.id.firstSpinner);
        secondSpinner = findViewById(R.id.secondSpinner);
        thirdSpinner = findViewById(R.id.thirdSpinner);
        q1Text = findViewById(R.id.q1Text);
        q2Text = findViewById(R.id.q2Text);
        q3Text = findViewById(R.id.q3Text);

        // 🎭 Emotion Regulation Questions (ERQ-10 Inspired)
        String[] emotionRegulationQuestions = {
                "I control my emotions by changing the way I think about the situation I’m in.",
                "When I’m upset, I remind myself that things could be worse.",
                "I keep my emotions to myself.",
                "When I want to feel more positive emotion, I change the way I’m thinking about the situation.",
                "I control my emotions by not expressing them.",
                "When I’m faced with a stressful situation, I make myself think about it in a way that helps me stay calm.",
                "I bottle up my feelings.",
                "I make sure not to express my negative emotions.",
                "I change how I think about things to control how I feel.",
                "I keep my negative emotions to myself.",
                "I try to see things from a different perspective to manage my emotions.",
                "I suppress my emotions when I feel upset.",
                "I think carefully before reacting emotionally.",
                "I avoid showing my emotions to others.",
                "I look for positive sides in difficult situations."
        };

        List<String> list = new ArrayList<>(Arrays.asList(emotionRegulationQuestions));

        // ✅ Shuffle only ONCE
        if (!sp.contains("q1")) {
            Collections.shuffle(list);
            sp.edit()
                    .putString("q1", list.get(0))
                    .putString("q2", list.get(1))
                    .putString("q3", list.get(2))
                    .apply();
        }

        q1Text.setText(sp.getString("q1", ""));
        q2Text.setText(sp.getString("q2", ""));
        q3Text.setText(sp.getString("q3", ""));

        // ✅ Spinner values
        String[] options = {
                "Select your answer",
                "0 — Strongly Disagree",
                "1 — Disagree",
                "2 — Slightly Disagree",
                "3 — Neutral",
                "4 — Slightly Agree",
                "5 — Agree",
                "6 — Strongly Agree"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.spinner_selected_item, options);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        firstSpinner.setAdapter(adapter);
        secondSpinner.setAdapter(adapter);
        thirdSpinner.setAdapter(adapter);

        // ✅ Restore answers
        firstSpinner.setSelection(sp.getInt("ans1_pos", 0));
        secondSpinner.setSelection(sp.getInt("ans2_pos", 0));
        thirdSpinner.setSelection(sp.getInt("ans3_pos", 0));

        btnNext.setOnClickListener(v -> navigateToTask5());
        btnBack.setOnClickListener(v -> navigateToTask3());

        // Physical back press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToTask3();
            }
        });
    }

    private void navigateToTask3() {

        // ✅ Save selections before going back
        SharedPreferences.Editor ed = sp.edit();
        ed.putInt("ans1_pos", firstSpinner.getSelectedItemPosition());
        ed.putInt("ans2_pos", secondSpinner.getSelectedItemPosition());
        ed.putInt("ans3_pos", thirdSpinner.getSelectedItemPosition());
        ed.apply();

        Intent intent = new Intent(this, Task3Activity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        finish();
    }

    private void navigateToTask5() {

        SharedPreferences.Editor ed = sp.edit();
        ed.putInt("ans1_pos", firstSpinner.getSelectedItemPosition());
        ed.putInt("ans2_pos", secondSpinner.getSelectedItemPosition());
        ed.putInt("ans3_pos", thirdSpinner.getSelectedItemPosition());
        ed.apply();

        Intent intent = new Intent(this, Task5Activity.class);
        intent.putExtra("answer1", firstSpinner.getSelectedItem().toString());
        intent.putExtra("answer2", secondSpinner.getSelectedItem().toString());
        intent.putExtra("answer3", thirdSpinner.getSelectedItem().toString());

        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        finish();
    }
}
