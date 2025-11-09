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

public class Task5Activity extends AppCompatActivity {

    ImageButton btnBack;
    Spinner firstSpinner, secondSpinner, thirdSpinner;
    TextView q1Text, q2Text, q3Text;
    AppCompatButton btnNext;

    SharedPreferences sp;

    List<String> mindfulnessQuestions = Arrays.asList(
            "I was aware of my emotions as they arose.",
            "I noticed when my mood changed.",
            "I paid attention to how my body felt.",
            "I was aware of my thoughts as they came and went.",
            "I noticed changes in my breathing.",
            "I recognized when I started feeling stressed.",
            "I was aware of physical tension in my body.",
            "I noticed how my body reacted to situations.",
            "I paid attention to shifts in my energy level.",
            "I noticed when I was beginning to feel irritated.",
            "I was aware when a thought affected my mood.",
            "I observed my emotions without ignoring them.",
            "I was aware of body sensations.",
            "I noticed when my attention shifted from the present moment.",
            "I recognized how different situations influenced my emotions."
    );

    String[] options = {
            "Select your answer",
            "1 - Almost Never",
            "2 - Rarely",
            "3 - Sometimes",
            "4 - Often",
            "5 - Almost Always"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task5);

        sp = getSharedPreferences("Task5Answers", MODE_PRIVATE);

        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);
        firstSpinner = findViewById(R.id.firstSpinner);
        secondSpinner = findViewById(R.id.secondSpinner);
        thirdSpinner = findViewById(R.id.thirdSpinner);
        q1Text = findViewById(R.id.q1Text);
        q2Text = findViewById(R.id.q2Text);
        q3Text = findViewById(R.id.q3Text);

        List<String> list = new ArrayList<>(mindfulnessQuestions);

        // ✅ Shuffle only once
        if (!sp.contains("q1")) {
            Collections.shuffle(list);
            sp.edit()
                    .putString("q1", list.get(0))
                    .putString("q2", list.get(1))
                    .putString("q3", list.get(2))
                    .apply();
        }

        // ✅ Show same questions again
        q1Text.setText(sp.getString("q1", ""));
        q2Text.setText(sp.getString("q2", ""));
        q3Text.setText(sp.getString("q3", ""));

        // ✅ Spinner setup
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.spinner_selected_item, options);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        firstSpinner.setAdapter(adapter);
        secondSpinner.setAdapter(adapter);
        thirdSpinner.setAdapter(adapter);

        // ✅ Restore previously selected answers
        firstSpinner.setSelection(sp.getInt("ans1_pos", 0));
        secondSpinner.setSelection(sp.getInt("ans2_pos", 0));
        thirdSpinner.setSelection(sp.getInt("ans3_pos", 0));

        btnBack.setOnClickListener(v -> navigateToTask4());

        btnNext.setOnClickListener(v -> {
            SharedPreferences.Editor ed = sp.edit();
            ed.putInt("ans1_pos", firstSpinner.getSelectedItemPosition());
            ed.putInt("ans2_pos", secondSpinner.getSelectedItemPosition());
            ed.putInt("ans3_pos", thirdSpinner.getSelectedItemPosition());
            ed.apply();

            Intent intent = new Intent(Task5Activity.this, Intro1Activity.class);
            intent.putExtra("mind1", firstSpinner.getSelectedItem().toString());
            intent.putExtra("mind2", secondSpinner.getSelectedItem().toString());
            intent.putExtra("mind3", thirdSpinner.getSelectedItem().toString());
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            finish();
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToTask4();
            }
        });
    }

    private void navigateToTask4() {
        // ✅ Save selections before going back
        SharedPreferences.Editor ed = sp.edit();
        ed.putInt("ans1_pos", firstSpinner.getSelectedItemPosition());
        ed.putInt("ans2_pos", secondSpinner.getSelectedItemPosition());
        ed.putInt("ans3_pos", thirdSpinner.getSelectedItemPosition());
        ed.apply();

        Intent intent = new Intent(Task5Activity.this, Task4Activity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        finish();
    }
}
