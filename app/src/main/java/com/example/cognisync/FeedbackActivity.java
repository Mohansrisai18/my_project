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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback); // Make sure this matches your XML filename

        // Initialize UI elements
        feedbackInput = findViewById(R.id.feedbackInput);
        submitFeedbackButton = findViewById(R.id.submitFeedbackButton);

        // Handle submit button click
        submitFeedbackButton.setOnClickListener(v -> {
            String feedback = feedbackInput.getText().toString().trim();

            if (feedback.isEmpty()) {
                Toast.makeText(FeedbackActivity.this, "Please enter your feedback", Toast.LENGTH_SHORT).show();
            } else {
                // Feedback submitted
                Toast.makeText(FeedbackActivity.this, "Thank you for your feedback!", Toast.LENGTH_SHORT).show();

                // Optionally clear the input
                feedbackInput.setText("");

                // Navigate back to ProfileActivity
                Intent intent = new Intent(FeedbackActivity.this, ProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);

                // Finish current activity to remove it from back stack
                finish();
            }
        });
    }
}
