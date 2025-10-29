package com.example.cognisync;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AssessmentDetailActivity extends AppCompatActivity {

    private TextView titleText, taskLabel, question1, question2, question3;
    private EditText firstInput, secondInput, thirdInput;
    private ImageButton backButton;
    private Button btnNext;

    private String subtopic;  // e.g., focused_attention, working_memory, etc.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment);

        titleText = findViewById(R.id.titleText);
        taskLabel = findViewById(R.id.taskLabel);
        question1 = findViewById(R.id.question1);
        question2 = findViewById(R.id.question2);
        question3 = findViewById(R.id.question3);
        firstInput = findViewById(R.id.firstInput);
        secondInput = findViewById(R.id.secondInput);
        thirdInput = findViewById(R.id.thirdInput);
        backButton = findViewById(R.id.backButton);
        btnNext = findViewById(R.id.btnNext);

        subtopic = getIntent().getStringExtra("subtopic");
        if (subtopic == null) subtopic = "focused_attention";

        loadQuestionsForSubtopic(subtopic);

        backButton.setOnClickListener(v -> finish());

        btnNext.setOnClickListener(v -> {
            // Collect answers
            String ans1 = firstInput.getText().toString().trim();
            String ans2 = secondInput.getText().toString().trim();
            String ans3 = thirdInput.getText().toString().trim();

            // Validate or save answers as needed (implementation specific)
            if (ans1.isEmpty() || ans2.isEmpty() || ans3.isEmpty()) {
                Toast.makeText(this, "Please answer all questions", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: Save answers or pass to next activity

            // For example, go back or move to next assessment subtopic or dashboard
            finish();
        });
    }

    private void loadQuestionsForSubtopic(String topic) {
        switch (topic) {
            case "focused_attention":
                taskLabel.setText("Task 1");
                question1.setText("What distracts you most during work?");
                question2.setText("How often do you lose focus?");
                question3.setText("Describe a strategy you use to refocus.");
                break;

            case "working_memory":
                taskLabel.setText("Task 2");
                question1.setText("Recall these numbers: 7, 4, 9.");
                question2.setText("What did you see outside your window this morning?");
                question3.setText("Describe something you did just before this test.");
                break;

            case "present_moment":
                taskLabel.setText("Task 3");
                question1.setText("How do you feel right now?");
                question2.setText("What sounds do you hear?");
                question3.setText("Notice your breath and describe it.");
                break;

            case "cognitive_integration":
                taskLabel.setText("Task 4");
                question1.setText("Describe a complex task you completed recently.");
                question2.setText("How do you plan your day?");
                question3.setText("Name three things you did well today.");
                break;

            case "emotional_regulation":
                taskLabel.setText("Task 5");
                question1.setText("How do you handle stress?");
                question2.setText("Recall a recent time you felt calm.");
                question3.setText("Describe something that makes you happy.");
                break;

            default:
                taskLabel.setText("Task");
                question1.setText("Question 1");
                question2.setText("Question 2");
                question3.setText("Question 3");
                break;
        }
    }
}
