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

public class Task5Activity extends AppCompatActivity {

    private ImageButton btnBack;
    private AppCompatButton btnNext;
    private TextView q1, q2, q3, q4, q5;
    private Spinner s1, s2, s3, s4, s5;
    private SharedPreferences sp;
    private SharedPreferences userSp;

    private final List<String> awarenessItems = Arrays.asList(
            "I was aware of my emotions as they arose.",
            "I noticed changes in my breathing.",
            "I paid attention to how my body felt.",
            "I was aware when my mind started wandering.",
            "I noticed when my mood shifted.",
            "I observed my emotions without ignoring them.",
            "I paid attention to sensations in my body.",
            "I noticed how situations affected my feelings.",
            "I recognized when stress was building up.",
            "I was conscious of my thoughts as they came and went."
    );

    private final String[] options = {
            "Select your answer",
            "1 - Almost Never",
            "2 - Rarely",
            "3 - Sometimes",
            "4 - Often",
            "5 - Almost Always"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getSupportActionBar()!=null) getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task5);

        sp = getSharedPreferences("Task5Answers", MODE_PRIVATE);
        userSp = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);

        q1 = findViewById(R.id.q1);
        q2 = findViewById(R.id.q2);
        q3 = findViewById(R.id.q3);
        q4 = findViewById(R.id.q4);
        q5 = findViewById(R.id.q5);

        s1 = findViewById(R.id.spinner1);
        s2 = findViewById(R.id.spinner2);
        s3 = findViewById(R.id.spinner3);
        s4 = findViewById(R.id.spinner4);
        s5 = findViewById(R.id.spinner5);

        if (!sp.contains("t5_q1")) {
            List<String> shuffled = new ArrayList<>(awarenessItems);
            Collections.shuffle(shuffled);
            sp.edit()
                    .putString("t5_q1", shuffled.get(0))
                    .putString("t5_q2", shuffled.get(1))
                    .putString("t5_q3", shuffled.get(2))
                    .putString("t5_q4", shuffled.get(3))
                    .putString("t5_q5", shuffled.get(4))
                    .apply();
        }

        q1.setText(sp.getString("t5_q1", ""));
        q2.setText(sp.getString("t5_q2", ""));
        q3.setText(sp.getString("t5_q3", ""));
        q4.setText(sp.getString("t5_q4", ""));
        q5.setText(sp.getString("t5_q5", ""));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_selected_item, options);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        s1.setAdapter(adapter);
        s2.setAdapter(adapter);
        s3.setAdapter(adapter);
        s4.setAdapter(adapter);
        s5.setAdapter(adapter);

        s1.setSelection(sp.getInt("t5_a1", 0));
        s2.setSelection(sp.getInt("t5_a2", 0));
        s3.setSelection(sp.getInt("t5_a3", 0));
        s4.setSelection(sp.getInt("t5_a4", 0));
        s5.setSelection(sp.getInt("t5_a5", 0));

        btnBack.setOnClickListener(v -> {
            saveSelections();
            startActivity(new Intent(Task5Activity.this, Task4Activity.class));
            finish();
        });

        btnNext.setOnClickListener(v -> {
            if (s1.getSelectedItemPosition()==0 || s2.getSelectedItemPosition()==0 ||
                    s3.getSelectedItemPosition()==0 || s4.getSelectedItemPosition()==0 ||
                    s5.getSelectedItemPosition()==0) {
                Toast.makeText(this, "Please answer all questions", Toast.LENGTH_SHORT).show();
                return;
            }

            saveSelections();

            // spinner pos 1..5 => value 1..5
            int v1 = s1.getSelectedItemPosition();
            int v2 = s2.getSelectedItemPosition();
            int v3 = s3.getSelectedItemPosition();
            int v4 = s4.getSelectedItemPosition();
            int v5 = s5.getSelectedItemPosition();

            int sum = v1 + v2 + v3 + v4 + v5; // 5..25
            float avg = sum / 5f; // 1..5
            int percent = (int) ((avg / 5f) * 100f);

            String email = userSp.getString("email", null);
            if (email != null) sendPhlmsInitial(email, percent);

            // after finishing initial tasks start Intro1Activity (your flow)
            startActivity(new Intent(Task5Activity.this, Intro1Activity.class));
            finish();
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                saveSelections();
                startActivity(new Intent(Task5Activity.this, Task4Activity.class));
                finish();
            }
        });
    }

    private void saveSelections() {
        sp.edit()
                .putInt("t5_a1", s1.getSelectedItemPosition())
                .putInt("t5_a2", s2.getSelectedItemPosition())
                .putInt("t5_a3", s3.getSelectedItemPosition())
                .putInt("t5_a4", s4.getSelectedItemPosition())
                .putInt("t5_a5", s5.getSelectedItemPosition())
                .apply();
    }

    private void sendPhlmsInitial(String email, int percent) {
        ApiService api = ApiClient.getClient().create(ApiService.class);
        ScoreRequest req = new ScoreRequest(email, percent);
        api.savePhlmsInitial(req).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(Task5Activity.this, "PHLMS initial save failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(Task5Activity.this, "PHLMS initial save error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
