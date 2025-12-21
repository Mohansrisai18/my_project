package com.example.cognisync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cognisync.del.ApiClient;
import com.example.cognisync.del.ApiService;
import com.example.cognisync.model.ScoreRequest;

import java.util.ArrayList;
import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskAActivity extends AppCompatActivity {

    TextView q1, q2, q3, q4, q5;
    Spinner s1, s2, s3, s4, s5;
    Button btnNext;

    // NOTE: These arrays are kept static as they are large, constant definitions
    public static String[] questions = {
            "I find it difficult to stay focused on the present.",
            "I rush through activities without paying attention.",
            "I do things automatically without awareness.",
            "I get lost in thoughts or daydreaming.",
            "I listen with one ear while doing something else.",
            "I act without paying attention.",
            "I notice I'm not present in the moment.",

            "I feel Interested",
            "I feel Excited",
            "I feel Enthusiastic",
            "I feel Inspired",
            "I feel Active",

            "I feel Distressed",
            "I feel Upset",
            "I feel Nervous",
            "I feel Irritable",
            "I feel Jittery",

            "I found it hard to wind down.",
            "I tended to over-react to situations.",
            "I used a lot of nervous energy.",
            "I found it difficult to relax.",
            "I became easily agitated.",
            "I found it hard to calm down after being upset.",
            "I got irritated more than usual.",
            "I had trouble relaxing.",
            "I felt overwhelmed by everything to do.",

            "I control emotions by changing how I think.",
            "I keep my emotions to myself.",

            "I was aware of my emotions as they arose.",
            "I ignored my emotions."
    };

    public static int[] scaleMin = {
            1,1,1,1,1,1,1,
            1,1,1,1,1,
            1,1,1,1,1,
            0,0,0,0,0,0,0,0,0,
            0,0,
            1,1
    };

    public static int[] scaleMax = {
            6,6,6,6,6,6,6,
            5,5,5,5,5,
            5,5,5,5,5,
            3,3,3,3,3,3,3,3,3,
            6,6,
            5,5
    };

    public static boolean[] reverse = {
            true,true,true,true,true,true,true,
            false,false,false,false,false,
            true,true,true,true,true,
            true,true,true,true,true,true,true,true,true,
            false,true,
            false,true
    };

    // Keep 'selected' static for use within this class methods
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

        generateRandom10();
        loadQuestions();

        btnNext.setOnClickListener(v -> processTaskA());
    }

    private void generateRandom10() {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < questions.length; i++)
            list.add(i);

        Collections.shuffle(list);

        for (int i = 0; i < 10; i++)
            selected[i] = list.get(i);
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

        for (int i = scaleMin[idx]; i <= scaleMax[idx]; i++)
            items.add(String.valueOf(i));

        ArrayAdapter<String> ad =
                new ArrayAdapter<>(this, R.layout.spinner_selected_item, items);

        ad.setDropDownViewResource(R.layout.spinner_dropdown_item);
        sp.setAdapter(ad);
    }

    private void processTaskA() {

        Spinner[] arr = {s1, s2, s3, s4, s5};

        float maas = 0, panas = 0, dass = 0, erq = 0, phlms = 0;
        float maasMax = 0, panasMax = 0, dassMax = 0, erqMax = 0, phlmsMax = 0;

        for (int i = 0; i < 5; i++) {

            int idx = selected[i];
            int pos = arr[i].getSelectedItemPosition();

            if (pos == 0) {
                Toast.makeText(this, "Please answer all questions", Toast.LENGTH_SHORT).show();
                return;
            }

            int min = scaleMin[idx];
            int max = scaleMax[idx];
            int value = min + (pos - 1);

            if (reverse[idx]) {
                value = (max + min) - value;
            }

            if (idx <= 6) { maas += value; maasMax += max; }
            else if (idx <= 16) { panas += value; panasMax += max; }
            else if (idx <= 25) { dass += value; dassMax += max; }
            else if (idx <= 27) { erq += value; erqMax += max; }
            else { phlms += value; phlmsMax += max; }
        }

        sendToBackend(
                (int)(maas / maasMax * 100),
                (int)(panas / panasMax * 100),
                (int)(dass / dassMax * 100),
                (int)(erq / erqMax * 100),
                (int)(phlms / phlmsMax * 100)
        );

        // --- CRITICAL FIX APPLIED HERE ---
        Intent intent = new Intent(this, TaskBActivity.class);
        // Pass the array of question indices to the next activity
        intent.putExtra("SELECTED_QUESTIONS", selected);
        startActivity(intent);
        // --- END CRITICAL FIX ---

        finish();
    }

    private void sendToBackend(int maas, int panas, int dass, int erq, int phlms) {

        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String email = sp.getString("email", "");

        ApiService api = ApiClient.getClient().create(ApiService.class);

        api.saveMaasInitial(new ScoreRequest(email, maas)).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {}
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });

        api.savePanasInitial(new ScoreRequest(email, panas)).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {}
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });

        api.saveDassInitial(new ScoreRequest(email, dass)).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {}
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });

        api.saveErqInitial(new ScoreRequest(email, erq)).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {}
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });

        api.savePhlmsInitial(new ScoreRequest(email, phlms)).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {}
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
    }
}