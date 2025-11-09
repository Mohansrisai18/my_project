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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Task3Activity extends AppCompatActivity {

    ImageButton btnBack;
    Spinner firstSpinner, secondSpinner, thirdSpinner;
    TextView q1Text, q2Text, q3Text;
    AppCompatButton btnNext;

    SharedPreferences sp;

    List<String> stressQuestions = Arrays.asList(
            "I found it hard to wind down.",
            "I felt tense or “on edge.”",
            "I felt nervous energy building up inside me.",
            "I found it difficult to relax.",
            "I became frustrated easily.",
            "I reacted strongly to small problems.",
            "I found it hard to switch my mind away from stressful thoughts.",
            "I stayed stuck on one way of thinking, even when it wasn’t helpful.",
            "I had trouble adapting when plans suddenly changed.",
            "I felt overwhelmed when multiple tasks came up at once.",
            "I felt impatient with others.",
            "It was hard for me to calm down once I got upset.",
            "I became irritated more than usual.",
            "I struggled to think clearly when stressed.",
            "I found it challenging to shift from stress to relaxation."
    );

    String[] stressOptions = {
            "Select your answer",
            "0 - Did not apply to me at all",
            "1 - Applied to me sometimes",
            "2 - Applied to me often",
            "3 - Applied to me most of the time"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task3);

        sp = getSharedPreferences("Task3Answers", MODE_PRIVATE);

        btnBack = findViewById(R.id.btnBack);
        firstSpinner = findViewById(R.id.firstSpinner);
        secondSpinner = findViewById(R.id.secondSpinner);
        thirdSpinner = findViewById(R.id.thirdSpinner);
        q1Text = findViewById(R.id.q1Text);
        q2Text = findViewById(R.id.q2Text);
        q3Text = findViewById(R.id.q3Text);
        btnNext = findViewById(R.id.btnNext);

        // Shuffle only first time
        if (!sp.contains("q1")) {
            Collections.shuffle(stressQuestions);
            sp.edit()
                    .putString("q1", stressQuestions.get(0))
                    .putString("q2", stressQuestions.get(1))
                    .putString("q3", stressQuestions.get(2))
                    .apply();
        }

        q1Text.setText(sp.getString("q1", ""));
        q2Text.setText(sp.getString("q2", ""));
        q3Text.setText(sp.getString("q3", ""));

        // Spinner Adapter with custom UI
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, R.layout.spinner_selected_item, stressOptions);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        firstSpinner.setAdapter(adapter);
        secondSpinner.setAdapter(adapter);
        thirdSpinner.setAdapter(adapter);

        // Restore selection
        firstSpinner.setSelection(sp.getInt("ans1_pos", 0));
        secondSpinner.setSelection(sp.getInt("ans2_pos", 0));
        thirdSpinner.setSelection(sp.getInt("ans3_pos", 0));

        btnBack.setOnClickListener(v -> navigateToTask2());

        // Handle phone back
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToTask2();
            }
        });

        btnNext.setOnClickListener(v -> {
            SharedPreferences.Editor ed = sp.edit();
            ed.putInt("ans1_pos", firstSpinner.getSelectedItemPosition());
            ed.putInt("ans2_pos", secondSpinner.getSelectedItemPosition());
            ed.putInt("ans3_pos", thirdSpinner.getSelectedItemPosition());
            ed.apply();

            Intent intent = new Intent(Task3Activity.this, Task4Activity.class);
            intent.putExtra("stress1", firstSpinner.getSelectedItem().toString());
            intent.putExtra("stress2", secondSpinner.getSelectedItem().toString());
            intent.putExtra("stress3", thirdSpinner.getSelectedItem().toString());
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            finish();
        });
    }

    private void navigateToTask2() {
        Intent intent = new Intent(Task3Activity.this, Task2Activity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        finish();
    }
}
