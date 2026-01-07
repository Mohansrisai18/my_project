package com.example.cognisync;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;

public class TaskAActivity extends AppCompatActivity {

    private TextView q1, q2, q3, q4, q5;
    private Spinner s1, s2, s3, s4, s5;
    private Button btnNext;

    // ---------------- ALL QUESTIONS ----------------
    public static final String[] questions = {
            // MAAS (0–6)
            "I find it difficult to stay focused on the present.",
            "I rush through activities without paying attention.",
            "I do things automatically without awareness.",
            "I get lost in thoughts or daydreaming.",
            "I listen with one ear while doing something else.",
            "I act without paying attention.",
            "I notice I'm not present in the moment.",

            // PANAS POS (7–11)
            "I feel Interested",
            "I feel Excited",
            "I feel Enthusiastic",
            "I feel Inspired",
            "I feel Active",

            // PANAS NEG (12–16)
            "I feel Distressed",
            "I feel Upset",
            "I feel Nervous",
            "I feel Irritable",
            "I feel Jittery",

            // DASS (17–25)
            "I found it hard to wind down.",
            "I tended to over-react to situations.",
            "I used a lot of nervous energy.",
            "I found it difficult to relax.",
            "I became easily agitated.",
            "I found it hard to calm down.",
            "I got irritated easily.",
            "I had trouble relaxing.",
            "I felt overwhelmed.",

            // ERQ (26–27)
            "I control emotions by changing how I think.",
            "I keep my emotions to myself.",

            // PHLMS (28–29)
            "I was aware of my emotions as they arose.",
            "I ignored my emotions."
    };

    // ---------------- SCALE CONFIG ----------------
    public static final int[] scaleMin = {
            1,1,1,1,1,1,1,
            1,1,1,1,1,
            1,1,1,1,1,
            0,0,0,0,0,0,0,0,0,
            0,0,
            1,1
    };

    public static final int[] scaleMax = {
            6,6,6,6,6,6,6,
            5,5,5,5,5,
            5,5,5,5,5,
            3,3,3,3,3,3,3,3,3,
            6,6,
            5,5
    };

    public static final boolean[] reverse = {
            true,true,true,true,true,true,true,
            false,false,false,false,false,
            true,true,true,true,true,
            true,true,true,true,true,true,true,true,true,
            false,true,
            false,true
    };

    // ---------------- QUESTION POOLS ----------------
    // Task A → MAAS + PANAS only
    public static final int[] TASK_A_POOL = {
            0,1,2,3,4,5,6,
            7,8,9,10,11,
            12,13,14,15,16
    };

    // Task B → DASS + ERQ + PHLMS
    public static final int[] TASK_B_POOL = {
            17,18,19,20,21,22,23,24,25,
            26,27,
            28,29
    };

    // Shared array (A uses 0–4, B uses 5–9)
    public static int[] selected = new int[10];

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (getSupportActionBar() != null) getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_a);

        q1 = findViewById(R.id.q1);
        q2 = findViewById(R.id.q2);
        q3 = findViewById(R.id.q3);
        q4 = findViewById(R.id.q4);
        q5 = findViewById(R.id.q5);

        s1 = findViewById(R.id.s1);
        s2 = findViewById(R.id.s2);
        s3 = findViewById(R.id.s3);
        s4 = findViewById(R.id.s4);
        s5 = findViewById(R.id.s5);

        btnNext = findViewById(R.id.btnNext);

        generateTaskAQuestions();
        loadQuestions();

        btnNext.setOnClickListener(v -> {
            if (!validate()) return;

            Intent i = new Intent(this, TaskBActivity.class);
            i.putExtra("SELECTED_QUESTIONS", selected);
            startActivity(i);
            finish();
        });
    }

    private void generateTaskAQuestions() {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i : TASK_A_POOL) list.add(i);
        Collections.shuffle(list);

        for (int i = 0; i < 5; i++) {
            selected[i] = list.get(i);
        }
    }

    private void loadQuestions() {
        load(q1, s1, selected[0]);
        load(q2, s2, selected[1]);
        load(q3, s3, selected[2]);
        load(q4, s4, selected[3]);
        load(q5, s5, selected[4]);
    }

    private void load(TextView tv, Spinner sp, int idx) {
        tv.setText(questions[idx]);

        ArrayList<String> items = new ArrayList<>();
        items.add("Select");
        for (int i = scaleMin[idx]; i <= scaleMax[idx]; i++) {
            items.add(String.valueOf(i));
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, R.layout.spinner_selected_item, items);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        sp.setAdapter(adapter);
    }

    private boolean validate() {
        Spinner[] sp = {s1,s2,s3,s4,s5};
        for (Spinner s : sp) {
            if (s.getSelectedItemPosition() == 0) {
                Toast.makeText(this,"Answer all questions",Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }
}
