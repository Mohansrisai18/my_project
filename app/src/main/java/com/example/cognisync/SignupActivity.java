package com.example.cognisync;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

        // Hide action bar
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        /* =====================================================
           ✅ SYSTEM INSETS + KEYBOARD FIX (MISSING EARLIER)
        ===================================================== */
        View container = findViewById(R.id.container);
        if (container != null) {
            int left = container.getPaddingLeft();
            int top = container.getPaddingTop();
            int right = container.getPaddingRight();
            int bottom = container.getPaddingBottom();

            ViewCompat.setOnApplyWindowInsetsListener(container, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());

                int bottomInset = Math.max(systemBars.bottom, ime.bottom);

                v.setPadding(
                        left,
                        top + systemBars.top,
                        right,
                        bottom + bottomInset
                );
                return insets;
            });

            ViewCompat.requestApplyInsets(container);
        }
        /* ===================================================== */

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
                    tv.setTextColor(
                            position == 0
                                    ? Color.parseColor("#888888")
                                    : Color.parseColor("#000000")
                    );
                    tv.setTextSize(13);
                    tv.setTypeface(Typeface.DEFAULT);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 👁 PASSWORD SHOW / HIDE TOGGLE
        passwordInput.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {

                if (passwordInput.getCompoundDrawables()[2] == null) return false;

                int drawableWidth =
                        passwordInput.getCompoundDrawables()[2]
                                .getBounds().width();

                if (event.getX() >=
                        (passwordInput.getWidth()
                                - passwordInput.getPaddingEnd()
                                - drawableWidth)) {

                    if (passwordInput.getInputType() ==
                            (InputType.TYPE_CLASS_TEXT |
                                    InputType.TYPE_TEXT_VARIATION_PASSWORD)) {

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

        // Back navigation handling
        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        goToLogin();
                    }
                }
        );
    }

    /* ================= SIGNUP LOGIC ================= */

    private void performSignup() {

        String userIdRaw = userIdInput.getText().toString().trim();
        String age = ageInput.getText().toString().trim();
        String emailRaw = mailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // normalize email (trim only — case is not critical for validation but we'll use lowercase for storage)
        String email = emailRaw.toLowerCase();

        if (genderSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(userIdRaw) ||
                TextUtils.isEmpty(age) ||
                TextUtils.isEmpty(emailRaw) ||
                TextUtils.isEmpty(password)) {

            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        /* ---------------- EMAIL VALIDATION ---------------- */
        // No spaces
        if (emailRaw.contains(" ")) {
            mailInput.setError("Email must not contain spaces");
            mailInput.requestFocus();
            return;
        }

        // Use Android built-in pattern (robust for real-world addresses)
        if (!Patterns.EMAIL_ADDRESS.matcher(emailRaw).matches()) {
            mailInput.setError("Enter a valid email address");
            mailInput.requestFocus();
            return;
        }

        // No consecutive dots
        if (emailRaw.contains("..")) {
            mailInput.setError("Email format is invalid");
            mailInput.requestFocus();
            return;
        }

        // Domain must have a dot (e.g. example.com or dept.university.ac.in)
        int atIndex = emailRaw.indexOf("@");
        if (atIndex < 0 || atIndex >= emailRaw.length() - 1) {
            mailInput.setError("Email is invalid");
            mailInput.requestFocus();
            return;
        }
        String domain = emailRaw.substring(atIndex + 1);
        if (!domain.contains(".")) {
            mailInput.setError("Email domain is invalid");
            mailInput.requestFocus();
            return;
        }
        /* -------------------------------------------------- */

        // Normalize userId to lowercase for validation & storage
        String userId = userIdRaw.toLowerCase();

        // STRICT User ID validation (5-15 chars, starts with letter, only a-z0-9_, no double underscores, no reserved words)
        String USER_ID_REGEX = "^(?!.*__)(?!(?:admin|support|root|system|null|user)$)[a-z][a-z0-9_]{4,14}$";

        if (!userId.matches(USER_ID_REGEX)) {
            userIdInput.setError(
                    "User ID rules:\n" +
                            "• 5–15 chars\n" +
                            "• start with a letter\n" +
                            "• letters, numbers, underscore only\n" +
                            "• no spaces, no consecutive '_' \n" +
                            "• cannot be admin/support/root/system/null/user"
            );
            userIdInput.requestFocus();
            userIdInput.setSelection(Math.min(userIdInput.length(), userIdInput.getText().length()));
            return;
        }

        // Password validation
        if (!password.matches("^[A-Z](?=.*[0-9])(?=.*[@#$%^&+=!]).{7,}$")) {
            passwordInput.setError(
                    "Password must start with a capital letter, be at least 8 characters, " +
                            "contain a number and a special character"
            );
            passwordInput.requestFocus();
            return;
        }

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

        // use normalized userId (lowercase) when creating the patient and storing locally
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

                    SharedPreferences sp =
                            getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor ed = sp.edit();

                    // store profile fields in lowercase where appropriate
                    ed.putString("user_id", userId);
                    ed.putString("username", userId);
                    ed.putString("email", email);
                    ed.putString("age", String.valueOf(ageValue));
                    ed.putString(
                            "gender",
                            genderSpinner.getSelectedItem().toString().toLowerCase()
                    );

                    // IMPORTANT: mark user as logged in so app restarts preserve session
                    ed.putBoolean("isLoggedIn", true);

                    ed.apply();

                    Toast.makeText(
                            SignupActivity.this,
                            "Account created!",
                            Toast.LENGTH_SHORT
                    ).show();

                    // Keep existing onboarding flow — IntroActivity — but user is already logged in
                    navigateToIntroActivity();

                } else {
                    Toast.makeText(
                            SignupActivity.this,
                            "Signup failed (" + response.code() + ")",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                btnSignup.setEnabled(true);
                btnSignup.setText("Sign Up");
                Toast.makeText(
                        SignupActivity.this,
                        "Error: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void navigateToIntroActivity() {
        startActivity(new Intent(this, IntroActivity.class));
        finish();
    }

    private void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
