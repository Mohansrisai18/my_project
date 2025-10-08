package com.example.cognisync;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class Task2Activity extends AppCompatActivity {

    private ImageButton btnBack;
    private EditText firstInput, secondInput, thirdInput;
    private AppCompatButton btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task2);

        // Initialize views
        btnBack      = findViewById(R.id.btnBack);
        firstInput   = findViewById(R.id.firstInput);
        secondInput  = findViewById(R.id.secondInput);
        thirdInput   = findViewById(R.id.thirdInput);
        btnNext      = findViewById(R.id.btnNext);

        // Back button click listener
        btnBack.setOnClickListener(v -> navigateToTask1());

        // Next button click listener
        btnNext.setOnClickListener(v -> navigateToTask3());

        // Handle system back press
        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        navigateToTask1();
                    }
                }
        );
    }

    private void navigateToTask1() {
        Intent intent = new Intent(Task2Activity.this, Task1Activity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        finish();
    }

    private void navigateToTask3() {
        // Collect ratings
        String rating1 = firstInput.getText().toString().trim();
        String rating2 = secondInput.getText().toString().trim();
        String rating3 = thirdInput.getText().toString().trim();

        Intent intent = new Intent(Task2Activity.this, Task3Activity.class);
        intent.putExtra("rating1", rating1);
        intent.putExtra("rating2", rating2);
        intent.putExtra("rating3", rating3);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        finish();
    }
}
