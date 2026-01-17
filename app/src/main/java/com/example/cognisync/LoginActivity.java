package com.example.cognisync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.WindowManager;
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

        // Hide ActionBar
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        super.onCreate(savedInstanceState);

        // Keyboard resize
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        );

        // AUTO LOGIN
        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        if (sp.getBoolean("isLoggedIn", false)) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        // UI binding
        emailInput = findViewById(R.id.mailInput);
        passwordInput = findViewById(R.id.passwordInput);
        btnLogin = findViewById(R.id.btnLogin);
        signupText = findViewById(R.id.signupText);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);

        btnLogin.setText("Login");

        // 👁 Ensure eye icon exists
        passwordInput.setCompoundDrawablesWithIntrinsicBounds(
                0, 0, R.drawable.ic_eye_closed, 0
        );

        // 👁 PASSWORD SHOW / HIDE TOGGLE (CLEAN + SAFE)
        passwordInput.setOnTouchListener((v, event) -> {
            if (event.getAction() != MotionEvent.ACTION_UP) return false;

            if (passwordInput.getCompoundDrawables()[2] == null) return false;

            int drawableWidth =
                    passwordInput.getCompoundDrawables()[2].getBounds().width();

            int touchAreaStart =
                    passwordInput.getWidth()
                            - passwordInput.getPaddingRight()
                            - drawableWidth;

            if (event.getX() >= touchAreaStart) {

                boolean isHidden =
                        (passwordInput.getInputType()
                                & InputType.TYPE_TEXT_VARIATION_PASSWORD)
                                == InputType.TYPE_TEXT_VARIATION_PASSWORD;

                if (isHidden) {
                    passwordInput.setInputType(
                            InputType.TYPE_CLASS_TEXT |
                                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    );
                    passwordInput.setCompoundDrawablesWithIntrinsicBounds(
                            0, 0, R.drawable.ic_eye_open, 0
                    );
                } else {
                    passwordInput.setInputType(
                            InputType.TYPE_CLASS_TEXT |
                                    InputType.TYPE_TEXT_VARIATION_PASSWORD
                    );
                    passwordInput.setCompoundDrawablesWithIntrinsicBounds(
                            0, 0, R.drawable.ic_eye_closed, 0
                    );
                }

                passwordInput.setSelection(passwordInput.getText().length());
                passwordInput.performClick(); // accessibility
                return true;
            }

            return false;
        });

        // LOGIN
        btnLogin.setOnClickListener(v -> performLogin());

        // SIGNUP
        signupText.setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
            finish();
        });

        // FORGOT PASSWORD
        forgotPasswordText.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class))
        );

        // BACK → EXIT APP
        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        finishAffinity();
                    }
                });
    }

    // ================= LOGIN =================
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
                    resetLoginButton();
                    Toast.makeText(LoginActivity.this,
                            "Invalid email or password",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                resetLoginButton();
                Toast.makeText(LoginActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // ================= FETCH PROFILE =================
    private void fetchUserProfile(String email) {

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        apiService.getPatientProfile(email).enqueue(new Callback<Patient>() {
            @Override
            public void onResponse(Call<Patient> call, Response<Patient> response) {

                resetLoginButton();

                if (response.isSuccessful() && response.body() != null) {

                    Patient user = response.body();

                    SharedPreferences.Editor editor =
                            getSharedPreferences("UserPrefs", MODE_PRIVATE).edit();

                    editor.putString("user_id", user.getUserId());
                    editor.putString("username", user.getUserId());
                    editor.putString("email", user.getEmail());
                    editor.putString("gender", user.getGender());
                    editor.putString("age", String.valueOf(user.getAge()));
                    editor.putBoolean("isLoggedIn", true);
                    editor.apply();

                    Toast.makeText(LoginActivity.this,
                            "Welcome " + user.getUserId(),
                            Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    finish();

                } else {
                    Toast.makeText(LoginActivity.this,
                            "Could not load profile data",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Patient> call, Throwable t) {
                resetLoginButton();
                Toast.makeText(LoginActivity.this,
                        "Profile fetch error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void resetLoginButton() {
        btnLogin.setEnabled(true);
        btnLogin.setText("Login");
    }
}
