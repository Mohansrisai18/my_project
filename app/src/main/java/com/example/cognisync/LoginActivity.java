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

import com.example.cognisync.del.ApiClient;
import com.example.cognisync.del.ApiService;
import com.example.cognisync.model.LoginRequest;
import com.example.cognisync.model.Patient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private AppCompatButton btnLogin;
    private TextView signupText, forgotPasswordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (getSupportActionBar() != null) getSupportActionBar().hide();
        super.onCreate(savedInstanceState);

        // 🔥 AUTO LOGIN
        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        if (sp.getBoolean("isLoggedIn", false)) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        // UI
        emailInput = findViewById(R.id.mailInput);
        passwordInput = findViewById(R.id.passwordInput);
        btnLogin = findViewById(R.id.btnLogin);
        signupText = findViewById(R.id.signupText);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);

        // LOGIN
        btnLogin.setOnClickListener(v -> performLogin());

        // SIGNUP
        signupText.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            finish();
        });

        // FORGOT PASSWORD
        forgotPasswordText.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class))
        );

        // BACK PRESS → EXIT APP
        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        finishAffinity();
                    }
                });
    }

    // ===============================
    // LOGIN API
    // ===============================
    private void performLogin() {

        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in…");

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        LoginRequest request = new LoginRequest(email, password);

        apiService.loginPatient(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {

                if (response.isSuccessful()) {
                    fetchUserProfile(email);
                } else {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Login");
                    Toast.makeText(LoginActivity.this,
                            "Invalid email or password",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Login");
                Toast.makeText(LoginActivity.this,
                        "Network Error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // ===============================
    // FETCH PROFILE AFTER LOGIN
    // ===============================
    private void fetchUserProfile(String email) {

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        apiService.getPatientProfile(email).enqueue(new Callback<Patient>() {
            @Override
            public void onResponse(Call<Patient> call, Response<Patient> response) {

                btnLogin.setEnabled(true);
                btnLogin.setText("Login");

                if (response.isSuccessful() && response.body() != null) {

                    Patient user = response.body();
                    String userId = user.getUserId();   // ✅ NEW FIELD

                    SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();

                    // ✅ NEW STANDARD KEY
                    editor.putString("user_id", userId);

                    // ✅ BACKWARD COMPATIBILITY
                    editor.putString("username", userId);

                    editor.putString("email", user.getEmail());
                    editor.putString("gender", user.getGender());
                    editor.putString("age", String.valueOf(user.getAge()));
                    editor.putBoolean("isLoggedIn", true);
                    editor.apply();

                    Toast.makeText(LoginActivity.this,
                            "Welcome " + userId,
                            Toast.LENGTH_SHORT).show();

                    Intent i = new Intent(LoginActivity.this, HomeActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    finish();

                } else {
                    Toast.makeText(LoginActivity.this,
                            "Could not load profile data",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Patient> call, Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Login");
                Toast.makeText(LoginActivity.this,
                        "Profile fetch error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
