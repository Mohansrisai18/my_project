package com.example.cognisync;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cognisync.del.ApiClient;
import com.example.cognisync.del.ApiService;
import com.example.cognisync.model.ScoreRequest;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskBActivity extends AppCompatActivity {

    // Views
    private TextView q1, q2, q3, q4, q5;
    private Spinner s1, s2, s3, s4, s5;
    private Button btnFinish;
    private ImageButton btnBack;

    // Question indices passed from TaskA
    private int[] selectedQuestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (getSupportActionBar() != null) getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_b);

        // ---------------- BIND VIEWS ----------------
        btnBack = findViewById(R.id.btnBack);

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

        btnFinish = findViewById(R.id.btnNext);
        btnFinish.setText("FINISH");

        // ---------------- RECEIVE DATA ----------------
        Intent intent = getIntent();
        if (intent == null || !intent.hasExtra("SELECTED_QUESTIONS")) {
            Toast.makeText(this, "Survey data missing", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        selectedQuestions = intent.getIntArrayExtra("SELECTED_QUESTIONS");

        // ---------------- LOAD QUESTIONS ----------------
        loadQuestions();

        // ---------------- ACTIONS ----------------
        btnBack.setOnClickListener(v -> {
            finish();
        });

        btnFinish.setOnClickListener(v -> {
            int[] scores = calculateScores();
            if (scores == null) return;

            sendToBackend(scores);

            startActivity(new Intent(TaskBActivity.this, IntroActivity.class));
            finish();
        });
    }

    // ---------------- LOAD UI ----------------
    private void loadQuestions() {
        load(q1, s1, selectedQuestions[5]);
        load(q2, s2, selectedQuestions[6]);
        load(q3, s3, selectedQuestions[7]);
        load(q4, s4, selectedQuestions[8]);
        load(q5, s5, selectedQuestions[9]);
    }

    private void load(TextView tv, Spinner sp, int idx) {

        tv.setText(TaskAActivity.questions[idx]);

        ArrayList<String> items = new ArrayList<>();
        items.add("Select");

        for (int i = TaskAActivity.scaleMin[idx];
             i <= TaskAActivity.scaleMax[idx]; i++) {
            items.add(String.valueOf(i));
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this,
                        R.layout.spinner_selected_item, items);

        adapter.setDropDownViewResource(
                R.layout.spinner_dropdown_item);

        sp.setAdapter(adapter);
    }

    // ---------------- SCORE CALCULATION ----------------
    private int[] calculateScores() {

        Spinner[] spinners = {s1, s2, s3, s4, s5};

        float maas = 0, panas = 0, dass = 0, erq = 0, phlms = 0;
        float maasMax = 0, panasMax = 0, dassMax = 0, erqMax = 0, phlmsMax = 0;

        for (int i = 0; i < 5; i++) {

            int idx = selectedQuestions[i + 5];
            int pos = spinners[i].getSelectedItemPosition();

            if (pos == 0) {
                Toast.makeText(this,
                        "Please answer all questions",
                        Toast.LENGTH_SHORT).show();
                return null;
            }

            int min = TaskAActivity.scaleMin[idx];
            int max = TaskAActivity.scaleMax[idx];
            int value = min + (pos - 1);

            if (TaskAActivity.reverse[idx]) {
                value = (max + min) - value;
            }

            if (idx <= 6) {
                maas += value; maasMax += max;
            } else if (idx <= 16) {
                panas += value; panasMax += max;
            } else if (idx <= 25) {
                dass += value; dassMax += max;
            } else if (idx <= 27) {
                erq += value; erqMax += max;
            } else {
                phlms += value; phlmsMax += max;
            }
        }

        return new int[]{
                (int)(maas / maasMax * 100),
                (int)(panas / panasMax * 100),
                (int)(dass / dassMax * 100),
                (int)(erq / erqMax * 100),
                (int)(phlms / phlmsMax * 100)
        };
    }

    // ---------------- BACKEND ----------------
    private void sendToBackend(int[] scores) {

        SharedPreferences sp =
                getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        String email = sp.getString("email", "");

        ApiService api =
                ApiClient.getClient().create(ApiService.class);

        api.saveMaasInitial(new ScoreRequest(email, scores[0])).enqueue(empty());
        api.savePanasInitial(new ScoreRequest(email, scores[1])).enqueue(empty());
        api.saveDassInitial(new ScoreRequest(email, scores[2])).enqueue(empty());
        api.saveErqInitial(new ScoreRequest(email, scores[3])).enqueue(empty());
        api.savePhlmsInitial(new ScoreRequest(email, scores[4])).enqueue(empty());
    }

    private Callback<Void> empty() {
        return new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {}
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        };
    }
}
