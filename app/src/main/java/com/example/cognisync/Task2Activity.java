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

public class Task2Activity extends AppCompatActivity {

    ImageButton btnBack;
    AppCompatButton btnNext;
    Spinner s1, s2, s3, s4, s5;
    TextView q1, q2, q3, q4, q5;
    SharedPreferences sp;
    SharedPreferences userSp;

    List<String> panasItems = Arrays.asList(
            "Interested", "Excited", "Enthusiastic", "Inspired", "Active",
            "Distressed", "Upset", "Nervous", "Irritable", "Jittery"
    );

    String[] ratingScale = {
            "Select your answer",
            "1 - Very Slightly",
            "2 - A Little",
            "3 - Moderately",
            "4 - Quite a Bit",
            "5 - Extremely"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (getSupportActionBar()!=null) getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task2);

        sp = getSharedPreferences("Task2Answers", MODE_PRIVATE);
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

        // SHUFFLING QUESTIONS
        if (!sp.contains("t2_q1")) {
            List<String> shuffled = new ArrayList<>(panasItems);
            Collections.shuffle(shuffled);
            sp.edit()
                    .putString("t2_q1", "I feel " + shuffled.get(0))
                    .putString("t2_q2", "I feel " + shuffled.get(1))
                    .putString("t2_q3", "I feel " + shuffled.get(2))
                    .putString("t2_q4", "I feel " + shuffled.get(3))
                    .putString("t2_q5", "I feel " + shuffled.get(4))
                    .apply();
        }

        // SETTING TEXT
        q1.setText(sp.getString("t2_q1", ""));
        q2.setText(sp.getString("t2_q2", ""));
        q3.setText(sp.getString("t2_q3", ""));
        q4.setText(sp.getString("t2_q4", ""));
        q5.setText(sp.getString("t2_q5", ""));

        // 🔥 FORCE ALL QUESTION TEXTS TO BLACK
        int black = getResources().getColor(android.R.color.black);
        q1.setTextColor(black);
        q2.setTextColor(black);
        q3.setTextColor(black);
        q4.setTextColor(black);
        q5.setTextColor(black);

        // ADAPTER
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_selected_item, ratingScale);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        s1.setAdapter(adapter);
        s2.setAdapter(adapter);
        s3.setAdapter(adapter);
        s4.setAdapter(adapter);
        s5.setAdapter(adapter);

        // RESTORE UI STATE
        s1.setSelection(sp.getInt("t2_a1", 0));
        s2.setSelection(sp.getInt("t2_a2", 0));
        s3.setSelection(sp.getInt("t2_a3", 0));
        s4.setSelection(sp.getInt("t2_a4", 0));
        s5.setSelection(sp.getInt("t2_a5", 0));

        // BACK BUTTON
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(Task2Activity.this, Task1Activity.class));
            finish();
        });

        // NEXT BUTTON
        btnNext.setOnClickListener(v -> {

            // SAVE SPINNER STATES
            sp.edit()
                    .putInt("t2_a1", s1.getSelectedItemPosition())
                    .putInt("t2_a2", s2.getSelectedItemPosition())
                    .putInt("t2_a3", s3.getSelectedItemPosition())
                    .putInt("t2_a4", s4.getSelectedItemPosition())
                    .putInt("t2_a5", s5.getSelectedItemPosition())
                    .apply();

            // VALIDATION
            if (s1.getSelectedItemPosition()==0 || s2.getSelectedItemPosition()==0 ||
                    s3.getSelectedItemPosition()==0 || s4.getSelectedItemPosition()==0 ||
                    s5.getSelectedItemPosition()==0) {
                Toast.makeText(this, "Please answer all questions", Toast.LENGTH_SHORT).show();
                return;
            }

            // VALUES 1..5
            int v1 = s1.getSelectedItemPosition();
            int v2 = s2.getSelectedItemPosition();
            int v3 = s3.getSelectedItemPosition();
            int v4 = s4.getSelectedItemPosition();
            int v5 = s5.getSelectedItemPosition();

            // PA/NA CALCULATION
            int PA = v1 + v2 + v3; // 3–15
            int NA = v4 + v5;      // 2–10

            int paPercent = (int)(((PA - 3f) / 12f) * 100f);
            int naPercent = (int)(((NA - 2f) / 8f) * 100f);

            int panasFinal = (paPercent + naPercent) / 2;

            String email = userSp.getString("email", null);
            if (email != null) sendPanasInitial(email, panasFinal);

            startActivity(new Intent(Task2Activity.this, Task3Activity.class));
            finish();
        });

        // SYSTEM BACK OVERRIDE
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                startActivity(new Intent(Task2Activity.this, Task1Activity.class));
                finish();
            }
        });
    }

    private void sendPanasInitial(String email, int percent) {
        ApiService api = ApiClient.getClient().create(ApiService.class);
        ScoreRequest req = new ScoreRequest(email, percent);

        api.savePanasInitial(req).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(Task2Activity.this, "PANAS initial save failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(Task2Activity.this, "PANAS initial save error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
