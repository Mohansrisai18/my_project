package com.example.cognisync;

public class AssessmentDetailActivity extends AppCompatActivity {

    private List<String> questions;
    private int page = 0;
    private String moduleType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment_detail);

        moduleType = getIntent().getStringExtra("module_type");
        fetchQuestionsFromBackend(moduleType);

        Button nextButton = findViewById(R.id.nextButton);
        nextButton.setOnClickListener(v -> onNextClicked());
    }

    private void fetchQuestionsFromBackend(String moduleType) {
        // Implement async API call to fetch questions,
        // then update `questions` list and display first page
    }

    private void displayQuestions() {
        // Display 3 questions based on current page (0 or 1)
    }

    private void onNextClicked() {
        if (page == 0) {
            page++;
            displayQuestions();
        } else {
            // After last page, go back home
            Intent homeIntent = new Intent(this, HomeActivity.class);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
            finish();
        }
    }
}
