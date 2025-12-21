package com.example.cognisync;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton; // Added ImageButton import

import androidx.appcompat.app.AppCompatActivity;

import com.example.cognisync.del.ApiClient;
import com.example.cognisync.del.ApiService;
import com.example.cognisync.model.ScoreRequest;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskBActivity extends AppCompatActivity {

    TextView q1, q2, q3, q4, q5;
    Spinner s1, s2, s3, s4, s5;
    Button btnFinish;
    ImageButton btnBack; // Corrected type to ImageButton

    // NEW: Local array to hold the question indices passed from TaskAActivity
    private int[] selectedQuestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (getSupportActionBar() != null) getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_b);

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
        btnFinish.setText("Finish");

        btnBack = findViewById(R.id.btnBack);

        // --- CRITICAL FIX APPLIED HERE ---
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("SELECTED_QUESTIONS")) {
            selectedQuestions = intent.getIntArrayExtra("SELECTED_QUESTIONS");
        } else {
            // Handle scenario where data is missing (e.g., app launched improperly)
            Toast.makeText(this, "Error: Survey data not found.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, TaskAActivity.class));
            finish();
            return;
        }
        // --- END CRITICAL FIX ---

        loadTaskBQuestions();

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(TaskBActivity.this, TaskAActivity.class));
            finish();
        });

        btnFinish.setOnClickListener(v -> {
            int[] results = calculateScaleScores();
            if (results == null) return;

            sendToBackend(results);

            startActivity(new Intent(TaskBActivity.this, IntroActivity.class));
            finish();
        });
    }

    private void loadTaskBQuestions() {
        // Use the local instance variable 'selectedQuestions'
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

        for (int i = TaskAActivity.scaleMin[idx]; i <= TaskAActivity.scaleMax[idx]; i++)
            items.add(String.valueOf(i));

        ArrayAdapter<String> ad =
                new ArrayAdapter<>(this, R.layout.spinner_selected_item, items);

        ad.setDropDownViewResource(R.layout.spinner_dropdown_item);
        sp.setAdapter(ad);
    }

    private int[] calculateScaleScores() {

        Spinner[] arr = {s1, s2, s3, s4, s5};

        float maas = 0, panas = 0, dass = 0, erq = 0, phlms = 0;
        float maasMax = 0, panasMax = 0, dassMax = 0, erqMax = 0, phlmsMax = 0;

        for (int i = 0; i < 5; i++) {

            // Use the local instance variable 'selectedQuestions'
            int idx = selectedQuestions[i + 5];
            int pos = arr[i].getSelectedItemPosition();

            if (pos == 0) {
                Toast.makeText(this, "Please answer all questions", Toast.LENGTH_SHORT).show();
                return null;
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
            }
            else if (idx <= 16) {
                panas += value;
                panasMax += max;
            }
            else if (idx <= 25) {
                dass += value;
                dassMax += max;
            }
            else if (idx <= 27) {
                erq += value;
                erqMax += max;
            }
            else {
                phlms += value;
                phlmsMax += max;
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

    private void sendToBackend(int[] scores) {
        // ... (API logic is unchanged)
        SharedPreferences sp = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String email = sp.getString("email", "");

        ApiService api = ApiClient.getClient().create(ApiService.class);

        api.saveMaasInitial(new ScoreRequest(email, scores[0])).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {}
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });

        api.savePanasInitial(new ScoreRequest(email, scores[1])).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {}
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });

        api.saveDassInitial(new ScoreRequest(email, scores[2])).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {}
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });

        api.saveErqInitial(new ScoreRequest(email, scores[3])).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {}
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });

        api.savePhlmsInitial(new ScoreRequest(email, scores[4])).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {}
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
    }
}