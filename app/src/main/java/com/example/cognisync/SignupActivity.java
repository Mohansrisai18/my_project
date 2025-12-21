package com.example.cognisync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.cognisync.del.ApiClient;
import com.example.cognisync.del.ApiService;
import com.example.cognisync.model.Patient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {

    private EditText userIdInput, ageInput, mailInput, passwordInput;
    private Spinner genderSpinner;
    private AppCompatButton btnSignup;
    private TextView loginText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (getSupportActionBar() != null) getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Bind views
        userIdInput = findViewById(R.id.fullNameInput);
        ageInput = findViewById(R.id.ageInput);
        mailInput = findViewById(R.id.mailInput);
        passwordInput = findViewById(R.id.passwordInput);
        genderSpinner = findViewById(R.id.genderSpinner);
        btnSignup = findViewById(R.id.btnSignup);
        loginText = findViewById(R.id.loginText);

        // 🔥 Gender spinner with HINT
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_selected_item,
                new String[]{
                        "Select Gender",   // 👈 HINT
                        "Male",
                        "Female"
                }
        );
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);

        btnSignup.setOnClickListener(v -> performSignup());
        loginText.setOnClickListener(v -> goToLogin());

        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        goToLogin();
                    }
                });
    }

    private void performSignup() {

        String userId = userIdInput.getText().toString().trim();
        String age = ageInput.getText().toString().trim();
        String email = mailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // 🔥 Gender validation
        if (genderSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show();
            return;
        }

        String gender = genderSpinner.getSelectedItem().toString().toLowerCase();

        // Basic validation
        if (TextUtils.isEmpty(userId) ||
                TextUtils.isEmpty(age) ||
                TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(password)) {

            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSignup.setEnabled(false);
        btnSignup.setText("Creating account…");

        Patient patient = new Patient(
                userId,
                Integer.parseInt(age),
                gender,
                email,
                password
        );

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        apiService.registerPatient(patient).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {

                btnSignup.setEnabled(true);
                btnSignup.setText("Sign Up");

                if (response.isSuccessful()) {

                    SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor ed = sp.edit();

                    ed.putString("user_id", userId);
                    ed.putString("email", email);
                    ed.putString("age", age);
                    ed.putString("gender", gender);
                    ed.apply();

                    Toast.makeText(SignupActivity.this,
                            "Account created!",
                            Toast.LENGTH_SHORT).show();

                    navigateToTaskAActivity();

                } else {
                    Toast.makeText(SignupActivity.this,
                            "Signup failed (" + response.code() + ")",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                btnSignup.setEnabled(true);
                btnSignup.setText("Sign Up");
                Toast.makeText(SignupActivity.this,
                        "Error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void navigateToTaskAActivity() {
        startActivity(new Intent(this, TaskAActivity.class));
        finish();
    }

    private void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
