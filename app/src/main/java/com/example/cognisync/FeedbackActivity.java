package com.example.cognisync;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class FeedbackActivity extends AppCompatActivity {

    private EditText feedbackInput;
    private Button submitFeedbackButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        feedbackInput = findViewById(R.id.feedbackInput);
        submitFeedbackButton = findViewById(R.id.submitFeedbackButton);

        submitFeedbackButton.setOnClickListener(v -> {
            String feedback = feedbackInput.getText().toString().trim();

            if (feedback.isEmpty()) {
                Toast.makeText(this, "Please enter your feedback", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Thank you for your feedback!", Toast.LENGTH_SHORT).show();
                feedbackInput.setText("");

                // Navigate back to profile
                Intent intent = new Intent(FeedbackActivity.this, ProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });
    }
}
