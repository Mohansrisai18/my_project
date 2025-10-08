package com.example.cognisync;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class Task1Activity extends AppCompatActivity {

    private ImageButton btnBack;
    private EditText firstInput, secondInput, thirdInput;
    private AppCompatButton btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task1);

        btnBack     = findViewById(R.id.btnBack);
        firstInput  = findViewById(R.id.firstInput);
        secondInput = findViewById(R.id.secondInput);
        thirdInput  = findViewById(R.id.thirdInput);
        btnNext     = findViewById(R.id.btnNext);

        btnBack.setOnClickListener(v -> navigateToSignup());
        btnNext.setOnClickListener(v -> navigateToTask2());

        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        navigateToSignup();
                    }
                }
        );
    }

    private void navigateToSignup() {
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        finish();
    }

    private void navigateToTask2() {
        // Collect answers if needed
        String ans1 = firstInput.getText().toString().trim();
        String ans2 = secondInput.getText().toString().trim();
        String ans3 = thirdInput.getText().toString().trim();

        Intent intent = new Intent(this, Task2Activity.class);
        intent.putExtra("answer1", ans1);
        intent.putExtra("answer2", ans2);
        intent.putExtra("answer3", ans3);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        finish();
    }
}
