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

import com.example.cognisync.del.ApiService;
import com.example.cognisync.del.ApiClient;
import com.example.cognisync.model.Patient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {

    private EditText fullNameInput, ageInput, mailInput, passwordInput;
    private Spinner genderSpinner;
    private AppCompatButton btnSignup;
    private TextView loginText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (getSupportActionBar() != null) getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Inputs
        fullNameInput = findViewById(R.id.fullNameInput);
        ageInput = findViewById(R.id.ageInput);
        mailInput = findViewById(R.id.mailInput);
        passwordInput = findViewById(R.id.passwordInput);
        genderSpinner = findViewById(R.id.genderSpinner);
        btnSignup = findViewById(R.id.btnSignup);
        loginText = findViewById(R.id.loginText);

        // Set gender dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_selected_item,
                new String[]{"Male", "Female"}
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

        String fullName = fullNameInput.getText().toString().trim();
        String age = ageInput.getText().toString().trim();
        String email = mailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String gender = genderSpinner.getSelectedItem().toString().toLowerCase();

        if (TextUtils.isEmpty(fullName) ||
                TextUtils.isEmpty(age) ||
                TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(password)) {

            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSignup.setEnabled(false);
        btnSignup.setText("Creating account…");

        Patient patient = new Patient(fullName, Integer.parseInt(age), gender, email, password);
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        apiService.registerPatient(patient).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {

                btnSignup.setEnabled(true);
                btnSignup.setText("Sign Up");

                if (response.isSuccessful()) {

                    // Save user credentials for auto-login
                    SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor ed = sp.edit();
                    ed.putString("username", fullName);
                    ed.putString("email", email);
                    ed.putString("age", age);
                    ed.putString("gender", gender);
                    ed.apply();

                    Toast.makeText(SignupActivity.this, "Account created!", Toast.LENGTH_SHORT).show();

                    // Move to Task1Activity (your assessment screen)
                    navigateToTask1Activity();

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

    private void navigateToTask1Activity() {
        Intent intent = new Intent(SignupActivity.this, Task1Activity.class);
        startActivity(intent);
        finish();
    }

    private void goToLogin() {
        startActivity(new Intent(SignupActivity.this, LoginActivity.class));
        finish();
    }
}
