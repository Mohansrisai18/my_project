package com.example.cognisync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.cognisync.del.ApiClient;
import com.example.cognisync.del.ApiService;
import com.example.cognisync.model.ScoreRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Task1Activity extends AppCompatActivity {

    private ImageButton btnBack;
    private AppCompatButton btnNext;
    private Spinner s1, s2, s3, s4, s5;
    private TextView q1, q2, q3, q4, q5;
    private SharedPreferences userSp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (getSupportActionBar() != null) getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task1);

        userSp = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);

        s1 = findViewById(R.id.spinner1);
        s2 = findViewById(R.id.spinner2);
        s3 = findViewById(R.id.spinner3);
        s4 = findViewById(R.id.spinner4);
        s5 = findViewById(R.id.spinner5);

        q1 = findViewById(R.id.q1);
        q2 = findViewById(R.id.q2);
        q3 = findViewById(R.id.q3);
        q4 = findViewById(R.id.q4);
        q5 = findViewById(R.id.q5);

        // QUESTIONS
        String[] questions = {
                "I find it difficult to stay focused on what’s happening in the present.",
                "I rush through activities without really paying attention.",
                "I do things automatically without being aware of what I'm doing.",
                "I get lost in thoughts or daydreaming.",
                "I find myself listening with one ear while doing something else.",
                "I find myself doing things without paying attention.",
                "I notice that I am not feeling present in the moment."
        };

        List<String> list = new ArrayList<>(Arrays.asList(questions));
        Collections.shuffle(list);

        // SET SHUFFLED TEXT
        q1.setText(list.get(0));
        q2.setText(list.get(1));
        q3.setText(list.get(2));
        q4.setText(list.get(3));
        q5.setText(list.get(4));

        // SET TEXT COLOR BLACK
        int black = getResources().getColor(android.R.color.black);
        q1.setTextColor(black);
        q2.setTextColor(black);
        q3.setTextColor(black);
        q4.setTextColor(black);
        q5.setTextColor(black);

        // SPINNER OPTIONS
        String[] options = {
                "Select your answer",
                "1 — Almost Always",
                "2 — Very Often",
                "3 — Often",
                "4 — Sometimes",
                "5 — Rarely",
                "6 — Almost Never"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_selected_item, options);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        s1.setAdapter(adapter);
        s2.setAdapter(adapter);
        s3.setAdapter(adapter);
        s4.setAdapter(adapter);
        s5.setAdapter(adapter);

        // BACK BUTTON
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(Task1Activity.this, SignupActivity.class));
            finish();
        });

        // NEXT BUTTON
        btnNext.setOnClickListener(v -> {

            int[] pos = {
                    s1.getSelectedItemPosition(),
                    s2.getSelectedItemPosition(),
                    s3.getSelectedItemPosition(),
                    s4.getSelectedItemPosition(),
                    s5.getSelectedItemPosition()
            };

            for (int p : pos) {
                if (p == 0) {
                    Toast.makeText(this, "Please answer all questions", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // CALCULATE MAAS
            int sumReversed = 0;
            for (int p : pos) {
                sumReversed += (7 - p);
            }

            float avg = sumReversed / 5f;
            int percent = (int) (((avg - 1f) / 5f) * 100f);

            String email = userSp.getString("email", null);
            if (email != null) sendMaasInitial(email, percent);

            startActivity(new Intent(Task1Activity.this, Task2Activity.class));
            finish();
        });

        // SYSTEM BACK
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                startActivity(new Intent(Task1Activity.this, SignupActivity.class));
                finish();
            }
        });
    }

    private void sendMaasInitial(String email, int percent) {
        ApiService api = ApiClient.getClient().create(ApiService.class);
        ScoreRequest req = new ScoreRequest(email, percent);

        api.saveMaasInitial(req).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(Task1Activity.this, "MAAS initial save failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(Task1Activity.this, "MAAS initial save error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
