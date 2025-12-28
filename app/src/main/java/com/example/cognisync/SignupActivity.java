package com.example.cognisync;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
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

    @SuppressLint("ClickableViewAccessibility")
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

        // Gender spinner adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_selected_item,
                new String[]{
                        "Select Gender",
                        "Male",
                        "Female"
                }
        );
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);

        // Spinner text style fix
        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (view instanceof TextView) {
                    TextView tv = (TextView) view;
                    if (position == 0) {
                        tv.setTextColor(Color.parseColor("#888888"));
                    } else {
                        tv.setTextColor(Color.parseColor("#000000"));
                    }
                    tv.setTextSize(13);
                    tv.setTypeface(Typeface.DEFAULT);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 👁 PASSWORD SHOW / HIDE TOGGLE (FIXED)
        passwordInput.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {

                int drawableRightIndex = 2;
                if (passwordInput.getCompoundDrawables()[drawableRightIndex] == null) {
                    return false;
                }

                int drawableWidth =
                        passwordInput.getCompoundDrawables()[drawableRightIndex]
                                .getBounds().width();

                if (event.getX() >=
                        (passwordInput.getWidth()
                                - passwordInput.getPaddingEnd()
                                - drawableWidth)) {

                    if (passwordInput.getInputType() ==
                            (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {

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
                    passwordInput.performClick();
                    return true;
                }
            }
            return false;
        });

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

        if (genderSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(userId) ||
                TextUtils.isEmpty(age) ||
                TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(password)) {

            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // User ID validation
        if (!userId.matches("^[A-Za-z][A-Za-z0-9_]*$")) {
            userIdInput.setError(
                    "User ID must start with a letter and contain only letters, numbers, or _"
            );
            userIdInput.requestFocus();
            return;
        }

        // 🔐 PASSWORD VALIDATION (NEW)
        if (!password.matches("^[A-Z](?=.*[0-9])(?=.*[@#$%^&+=!]).{7,}$")) {
            passwordInput.setError(
                    "Password must start with a capital letter, be at least 8 characters, " +
                            "contain a number and a special character"
            );
            passwordInput.requestFocus();
            return;
        }

        // Age validation
        int ageValue;
        try {
            ageValue = Integer.parseInt(age);
        } catch (NumberFormatException e) {
            ageInput.setError("Please enter a valid age");
            ageInput.requestFocus();
            return;
        }

        if (ageValue < 13 || ageValue > 90) {
            ageInput.setError("Age must be between 13 and 90");
            ageInput.requestFocus();
            return;
        }

        btnSignup.setEnabled(false);
        btnSignup.setText("Creating account…");

        Patient patient = new Patient(
                userId,
                ageValue,
                genderSpinner.getSelectedItem().toString().toLowerCase(),
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
                    ed.putString("age", String.valueOf(ageValue));
                    ed.putString("gender",
                            genderSpinner.getSelectedItem().toString().toLowerCase());
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
