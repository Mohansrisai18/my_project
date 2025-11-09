package com.example.cognisync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.cognisync.del.ApiService;
import com.example.cognisync.del.ApiClient;
import com.example.cognisync.model.Patient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {

    private EditText fullNameInput, ageInput, genderInput, mailInput, passwordInput;
    private AppCompatButton btnSignup;
    private TextView loginText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        fullNameInput = findViewById(R.id.fullNameInput);
        ageInput = findViewById(R.id.ageInput);
        genderInput = findViewById(R.id.genderInput);
        mailInput = findViewById(R.id.mailInput);
        passwordInput = findViewById(R.id.passwordInput);
        btnSignup = findViewById(R.id.btnSignup);
        loginText = findViewById(R.id.loginText);

        btnSignup.setOnClickListener(v -> performSignup());
        loginText.setOnClickListener(v -> navigateToLogin());

        // Handle back press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToLogin();
            }
        });
    }

    private void performSignup() {
        String fullName = fullNameInput.getText().toString().trim();
        String age = ageInput.getText().toString().trim();
        String gender = genderInput.getText().toString().trim();
        String email = mailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(age) || TextUtils.isEmpty(gender)
                || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSignup.setEnabled(false);
        btnSignup.setText("Creating Account...");

        Patient patient = new Patient(fullName, Integer.parseInt(age), gender, email, password);
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<Void> call = apiService.registerPatient(patient);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                btnSignup.setEnabled(true);
                btnSignup.setText("Sign Up");

                if (response.isSuccessful()) {
                    Toast.makeText(SignupActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();

                    // ✅ Save user info in SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("username", fullName);
                    editor.putString("email", email);
                    editor.apply();

                    // ✅ Navigate to Task1Activity instead of Login
                    navigateToTask1Activity();
                } else {
                    Toast.makeText(SignupActivity.this, "Signup failed! (" + response.code() + ")", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                btnSignup.setEnabled(true);
                btnSignup.setText("Sign Up");
                Toast.makeText(SignupActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // ✅ Redirect to Task1Activity after signup
    private void navigateToTask1Activity() {
        Intent intent = new Intent(SignupActivity.this, Task1Activity.class);
        intent.putExtra("user_name", fullNameInput.getText().toString().trim());
        intent.putExtra("user_age", ageInput.getText().toString().trim());
        intent.putExtra("user_gender", genderInput.getText().toString().trim());
        intent.putExtra("user_email", mailInput.getText().toString().trim());
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        finish();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        finish();
    }
}
