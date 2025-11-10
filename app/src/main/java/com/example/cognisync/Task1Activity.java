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

public class Task1Activity extends AppCompatActivity {

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
        setContentView(R.layout.activity_task1);

        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);
        firstSpinner = findViewById(R.id.firstSpinner);
        secondSpinner = findViewById(R.id.secondSpinner);
        thirdSpinner = findViewById(R.id.thirdSpinner);
        q1Text = findViewById(R.id.q1Text);
        q2Text = findViewById(R.id.q2Text);
        q3Text = findViewById(R.id.q3Text);

        sp = getSharedPreferences("Task1Answers", MODE_PRIVATE);

        String[] mindfulnessQuestions = {
                "I find my mind wandering when I try to focus.",
                "I do things on autopilot without being aware.",
                "I struggle to stay focused on one task.",
                "I notice when my attention drifts off.",
                "I am fully present during daily tasks.",
                "I realize I'm thinking about the past or future instead of now.",
                "I forget what I’m doing because my mind is elsewhere.",
                "I am aware of how my body feels in the moment.",
                "I catch myself operating without thinking.",
                "I am able to redirect my attention when distracted.",
                "I get lost in thoughts and miss what’s happening.",
                "I am attentive to the current task.",
                "I react before thinking things through.",
                "I notice small details in my environment.",
                "I feel like I am functioning on auto-pilot."
        };

        List<String> list = new ArrayList<>(Arrays.asList(mindfulnessQuestions));
        Collections.shuffle(list);

        if (!sp.contains("q1")) {
            sp.edit()
                    .putString("q1", list.get(0))
                    .putString("q2", list.get(1))
                    .putString("q3", list.get(2))
                    .apply();
        }

        q1Text.setText(sp.getString("q1", ""));
        q2Text.setText(sp.getString("q2", ""));
        q3Text.setText(sp.getString("q3", ""));

        String[] options = {
                "Select your answer",
                "1 — Almost Always",
                "2 — Very Often",
                "3 — Often",
                "4 — Sometimes",
                "5 — Rarely",
                "6 — Almost Never"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.spinner_selected_item, options);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        firstSpinner.setAdapter(adapter);
        secondSpinner.setAdapter(adapter);
        thirdSpinner.setAdapter(adapter);

        firstSpinner.setSelection(sp.getInt("ans1_pos", 0));
        secondSpinner.setSelection(sp.getInt("ans2_pos", 0));
        thirdSpinner.setSelection(sp.getInt("ans3_pos", 0));

        btnNext.setOnClickListener(v -> navigateToTask2());
        btnBack.setOnClickListener(v -> navigateToSignup());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToSignup();
            }
        });
    }

    private void navigateToSignup() {
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        finish();
    }

    private void navigateToTask2() {

        SharedPreferences.Editor ed = sp.edit();
        ed.putInt("ans1_pos", firstSpinner.getSelectedItemPosition());
        ed.putInt("ans2_pos", secondSpinner.getSelectedItemPosition());
        ed.putInt("ans3_pos", thirdSpinner.getSelectedItemPosition());
        ed.apply();

        Intent intent = new Intent(this, Task2Activity.class);

        intent.putExtra("answer1", firstSpinner.getSelectedItem().toString());
        intent.putExtra("answer2", secondSpinner.getSelectedItem().toString());
        intent.putExtra("answer3", thirdSpinner.getSelectedItem().toString());

        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        finish();
    }
}
