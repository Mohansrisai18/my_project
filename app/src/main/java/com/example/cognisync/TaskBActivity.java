package com.example.cognisync;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cognisync.del.ApiClient;
import com.example.cognisync.del.ApiService;
import com.example.cognisync.model.MLPredictRequest;
import com.example.cognisync.model.MLPredictResponse;
import com.example.cognisync.util.TimetableStore;
import com.google.gson.Gson;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskBActivity extends AppCompatActivity {

    private TextView q1, q2, q3, q4, q5;
    private Spinner s1, s2, s3, s4, s5;
    private Button btnFinish;

    private int[] selected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (getSupportActionBar() != null) getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_b);

        // -------- UI BINDING --------
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

        selected = getIntent().getIntArrayExtra("SELECTED_QUESTIONS");
        if (selected == null) {
            Toast.makeText(this, "Survey error", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        loadQuestions();

        btnFinish.setOnClickListener(v -> submit());
    }

    // --------------------------------------------------
    // Load questions
    // --------------------------------------------------
    private void loadQuestions() {
        load(q1, s1, selected[5]);
        load(q2, s2, selected[6]);
        load(q3, s3, selected[7]);
        load(q4, s4, selected[8]);
        load(q5, s5, selected[9]);
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
                new ArrayAdapter<>(this, R.layout.spinner_selected_item, items);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        sp.setAdapter(adapter);
    }

    // --------------------------------------------------
    // Submit answers
    // --------------------------------------------------
    private void submit() {

        Spinner[] spinners = {s1, s2, s3, s4, s5};

        float maas = 0, panasPos = 0, panasNeg = 0, dass = 0, erq = 0, phlms = 0;
        float maasMax = 0, panasPosMax = 0, panasNegMax = 0,
                dassMax = 0, erqMax = 0, phlmsMax = 0;

        for (int i = 0; i < 5; i++) {

            int idx = selected[i + 5];
            int pos = spinners[i].getSelectedItemPosition();

            if (pos == 0) {
                Toast.makeText(this, "Answer all questions", Toast.LENGTH_SHORT).show();
                return;
            }

            int min = TaskAActivity.scaleMin[idx];
            int max = TaskAActivity.scaleMax[idx];
            int value = min + (pos - 1);

            if (TaskAActivity.reverse[idx]) {
                value = (max + min) - value;
            }

            if (idx <= 6) {
                maas += value;
                maasMax += max;
            } else if (idx <= 11) {
                panasPos += value;
                panasPosMax += max;
            } else if (idx <= 16) {
                panasNeg += value;
                panasNegMax += max;
            } else if (idx <= 25) {
                dass += value;
                dassMax += max;
            } else if (idx <= 27) {
                erq += value;
                erqMax += max;
            } else {
                phlms += value;
                phlmsMax += max;
            }
        }

        // -------- CREATE REQUEST --------
        MLPredictRequest request = new MLPredictRequest(
                (int) (maas / maasMax * 100),
                (int) (panasPos / panasPosMax * 100),
                (int) (panasNeg / panasNegMax * 100),
                (int) (dass / dassMax * 100),
                (int) (erq / erqMax * 100),
                (int) (phlms / phlmsMax * 100)
        );

        ApiService api = ApiClient.getClient().create(ApiService.class);

        // -------- API CALL --------
        api.predictMentalState(request).enqueue(new Callback<MLPredictResponse>() {

            @Override
            public void onResponse(
                    Call<MLPredictResponse> call,
                    Response<MLPredictResponse> response
            ) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(
                            TaskBActivity.this,
                            "Prediction failed",
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                // 🔴 CLEAR OLD TIMETABLE
                TimetableStore.clear(TaskBActivity.this);

                // SAVE NEW TIMETABLE
                String json = new Gson().toJson(response.body());
                TimetableStore.save(TaskBActivity.this, json);

                // OPEN TIMETABLE
                Intent i = new Intent(
                        TaskBActivity.this,
                        TimetableActivity.class
                );
                startActivity(i);
                finish();
            }

            @Override
            public void onFailure(Call<MLPredictResponse> call, Throwable t) {
                Toast.makeText(
                        TaskBActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}
