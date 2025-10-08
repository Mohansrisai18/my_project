package com.example.cognisync;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class LoginActivity extends AppCompatActivity {

    private EditText mailInput;
    private EditText passwordInput;
    private AppCompatButton btnLogin;
    private TextView signupText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        initializeViews();

        // Set click listeners
        setClickListeners();

        // Handle back press with new method
        setupBackPressHandler();
    }

    private void initializeViews() {
        mailInput = findViewById(R.id.mailInput);
        passwordInput = findViewById(R.id.passwordInput);
        btnLogin = findViewById(R.id.btnLogin);
        signupText = findViewById(R.id.signupText);
    }

    private void setClickListeners() {
        // Login button click listener
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });

        // Signup text click listener
        signupText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToSignup();
            }
        });
    }

    private void setupBackPressHandler() {
        // New way to handle back press (replaces deprecated onBackPressed)
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Navigate back to MainActivity
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void performLogin() {
        String email = mailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate input fields
        if (TextUtils.isEmpty(email)) {
            mailInput.setError("Email is required");
            mailInput.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mailInput.setError("Please enter a valid email");
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
        btnLogin.setText("Logging in...");
        btnLogin.setEnabled(false);

        // Simulate login process
        authenticateUser(email, password);
    }

    private void authenticateUser(String email, String password) {
        // Simulate network delay
        btnLogin.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Reset button state
                btnLogin.setText("Login");
                btnLogin.setEnabled(true);

                // For demo purposes
                if (isValidLogin(email, password)) {
                    // Login successful - navigate to home page
                    Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                    navigateToHomePage();
                } else {
                    // Login failed
                    Toast.makeText(LoginActivity.this, "Invalid credentials. Please try again.", Toast.LENGTH_SHORT).show();
                    clearFields();
                }
            }
        }, 2000);
    }

    private boolean isValidLogin(String email, String password) {
        return email.contains("@") && password.length() >= 6;
    }

    private void navigateToHomePage() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void navigateToSignup() {
        Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void clearFields() {
        mailInput.setText("");
        passwordInput.setText("");
        mailInput.requestFocus();
    }
}
