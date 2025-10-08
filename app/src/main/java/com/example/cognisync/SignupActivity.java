package com.example.cognisync;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class SignupActivity extends AppCompatActivity {

    private EditText fullNameInput;
    private EditText ageInput;
    private EditText genderInput;
    private EditText mailInput;
    private EditText passwordInput;
    private AppCompatButton btnSignup;
    private TextView loginText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize views
        initializeViews();

        // Set click listeners
        setClickListeners();

        // Handle back press
        setupBackPressHandler();
    }

    private void initializeViews() {
        fullNameInput = findViewById(R.id.fullNameInput);
        ageInput = findViewById(R.id.ageInput);
        genderInput = findViewById(R.id.genderInput);
        mailInput = findViewById(R.id.mailInput);
        passwordInput = findViewById(R.id.passwordInput);
        btnSignup = findViewById(R.id.btnSignup);
        loginText = findViewById(R.id.loginText);
    }

    private void setClickListeners() {
        // Sign up button click listener
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSignup();
            }
        });

        // Login text click listener - navigates back to login page
        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToLogin();
            }
        });
    }

    private void setupBackPressHandler() {
        // Handle back press to return to login page
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToLogin();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void performSignup() {
        String fullName = fullNameInput.getText().toString().trim();
        String age = ageInput.getText().toString().trim();
        String gender = genderInput.getText().toString().trim();
        String email = mailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate all input fields
        if (TextUtils.isEmpty(fullName)) {
            fullNameInput.setError("Full name is required");
            fullNameInput.requestFocus();
            return;
        }

        if (fullName.length() < 2) {
            fullNameInput.setError("Please enter a valid name");
            fullNameInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(age)) {
            ageInput.setError("Age is required");
            ageInput.requestFocus();
            return;
        }

        int ageValue;
        try {
            ageValue = Integer.parseInt(age);
            if (ageValue < 13 || ageValue > 120) {
                ageInput.setError("Please enter a valid age (13-120)");
                ageInput.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            ageInput.setError("Please enter a valid number");
            ageInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(gender)) {
            genderInput.setError("Gender is required");
            genderInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            mailInput.setError("Email is required");
            mailInput.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mailInput.setError("Please enter a valid email address");
            mailInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            passwordInput.requestFocus();
            return;
        }

        // Show loading state
        btnSignup.setText("Creating Account...");
        btnSignup.setEnabled(false);

        // Simulate signup process
        createAccount(fullName, ageValue, gender, email, password);
    }

    private void createAccount(String fullName, int age, String gender, String email, String password) {
        // Simulate network delay for account creation
        btnSignup.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Reset button state
                btnSignup.setText("Sign up");
                btnSignup.setEnabled(true);

                // Account creation successful
                Toast.makeText(SignupActivity.this,
                        "Account created successfully for " + fullName + "!",
                        Toast.LENGTH_SHORT).show();

                // Navigate to Task1Activity after successful signup
                navigateToTask1Activity();
            }
        }, 2000); // 2 second delay to simulate network request
    }

    private void navigateToTask1Activity() {
        // Navigate to Task1Activity after successful signup
        Intent intent = new Intent(SignupActivity.this, Task1Activity.class);

        // Pass user data to next activity if needed
        intent.putExtra("user_name", fullNameInput.getText().toString().trim());
        intent.putExtra("user_age", ageInput.getText().toString().trim());
        intent.putExtra("user_gender", genderInput.getText().toString().trim());
        intent.putExtra("user_email", mailInput.getText().toString().trim());

        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        // Clear activity stack so user can't go back to signup
        finish();
    }

    private void navigateToLogin() {
        // Navigate back to login page
        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        finish(); // Close signup activity
    }
}
