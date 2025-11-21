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

public class Task3Activity extends AppCompatActivity {

    private ImageButton btnBack;
    private AppCompatButton btnNext;
    private TextView q1, q2, q3, q4, q5;
    private Spinner s1, s2, s3, s4, s5;

    private SharedPreferences userSp;

    private final List<String> stressItems = Arrays.asList(
            "I found it hard to wind down.",
            "I tended to over-react to situations.",
            "I felt that I was using a lot of nervous energy.",
            "I found it difficult to relax.",
            "I became easily agitated.",
            "I found it hard to calm down after being upset.",
            "I got irritated more than usual.",
            "I had trouble relaxing.",
            "I felt overwhelmed by everything I had to do."
    );

    private final String[] stressOptions = {
            "Select your answer",
            "0 - Did not apply to me at all",
            "1 - Applied to me sometimes",
            "2 - Applied to me often",
            "3 - Applied to me most of the time"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (getSupportActionBar() != null) getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task3);

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

        // SHUFFLE QUESTIONS fresh
        List<String> shuffled = new ArrayList<>(stressItems);
        Collections.shuffle(shuffled);

        q1.setText(shuffled.get(0));
        q2.setText(shuffled.get(1));
        q3.setText(shuffled.get(2));
        q4.setText(shuffled.get(3));
        q5.setText(shuffled.get(4));

        // Force black color
        int black = getResources().getColor(android.R.color.black);
        q1.setTextColor(black);
        q2.setTextColor(black);
        q3.setTextColor(black);
        q4.setTextColor(black);
        q5.setTextColor(black);

        // Adapter for spinners
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_selected_item, stressOptions);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        s1.setAdapter(adapter);
        s2.setAdapter(adapter);
        s3.setAdapter(adapter);
        s4.setAdapter(adapter);
        s5.setAdapter(adapter);

        // Back button
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(Task3Activity.this, Task2Activity.class));
            finish();
        });

        // Next button
        btnNext.setOnClickListener(v -> {

            if (s1.getSelectedItemPosition()==0 || s2.getSelectedItemPosition()==0 ||
                    s3.getSelectedItemPosition()==0 || s4.getSelectedItemPosition()==0 ||
                    s5.getSelectedItemPosition()==0) {
                Toast.makeText(this, "Please answer all questions", Toast.LENGTH_SHORT).show();
                return;
            }

            // Convert spinner pos -> values 0..3
            int v1 = s1.getSelectedItemPosition() - 1;
            int v2 = s2.getSelectedItemPosition() - 1;
            int v3 = s3.getSelectedItemPosition() - 1;
            int v4 = s4.getSelectedItemPosition() - 1;
            int v5 = s5.getSelectedItemPosition() - 1;

            int raw = v1 + v2 + v3 + v4 + v5; // 0..15
            int scaled = raw * 2;            // 0..30
            int percent = (int)((scaled / 42f) * 100f);

            String email = userSp.getString("email", null);
            if (email != null) sendDassInitial(email, percent);

            startActivity(new Intent(Task3Activity.this, Task4Activity.class));
            finish();
        });

        // System back override
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startActivity(new Intent(Task3Activity.this, Task2Activity.class));
                finish();
            }
        });
    }

    private void sendDassInitial(String email, int percent) {
        ApiService api = ApiClient.getClient().create(ApiService.class);
        ScoreRequest req = new ScoreRequest(email, percent);

        api.saveDassInitial(req).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(Task3Activity.this, "DASS initial save failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(Task3Activity.this, "DASS initial save error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
