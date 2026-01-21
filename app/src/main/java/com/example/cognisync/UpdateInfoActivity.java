package com.example.cognisync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cognisync.del.ApiClient;
import com.example.cognisync.del.ApiService;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateInfoActivity extends AppCompatActivity {

    private EditText editName, editAge;
    private Spinner genderSpinner;
    private Button updateBtn;
    private ImageButton backButton;

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_NAME = "userName";
    private static final String KEY_AGE = "age";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_EMAIL = "email";

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // keep action bar hidden like before
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_account_settings);

        // ---------------- SAFE INSETS (status bar + keyboard) ----------------
        View container = findViewById(R.id.container);
        if (container != null) {
            int l = container.getPaddingLeft();
            int t = container.getPaddingTop();
            int r = container.getPaddingRight();
            int b = container.getPaddingBottom();

            ViewCompat.setOnApplyWindowInsetsListener(container, (v, insets) -> {
                Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());

                v.setPadding(
                        l,
                        t + sys.top,
                        r,
                        b + Math.max(sys.bottom, ime.bottom)
                );
                return insets;
            });
            ViewCompat.requestApplyInsets(container);
        }
        // --------------------------------------------------------------------

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        backButton = findViewById(R.id.backButton);
        editName = findViewById(R.id.editName);
        editAge = findViewById(R.id.editAge);
        genderSpinner = findViewById(R.id.genderSpinner);
        updateBtn = findViewById(R.id.updateInfoButton);

        // Ensure back chevron tint is applied programmatically (covers older devices)
        try {
            backButton.setImageTintList(ColorStateList.valueOf(Color.BLACK));
        } catch (Exception ignored) {}

        setupGenderSpinner();
        loadLocalData();

        // style the displayed selected text (ensure selected entry is visible/black)
        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // If the selected view is a TextView (most adapter item layouts are), style it
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(Color.BLACK);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        backButton.setOnClickListener(v -> finish());
        updateBtn.setOnClickListener(v -> updateProfile());
    }

    // ---------------- GENDER SPINNER ----------------
    private void setupGenderSpinner() {
        // Use custom row layouts if you already have them to control colors.
        // Fallback to platform simple layouts (they are safe and readable).
        ArrayAdapter<String> adapter;
        try {
            // If you have custom layouts (spinner_selected_item & spinner_dropdown_item) in res/layout, use them:
            adapter = new ArrayAdapter<>(
                    this,
                    R.layout.spinner_selected_item,
                    new String[]{"Select gender", "Male", "Female", "Other"}
            );
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        } catch (Exception e) {
            // fallback
            adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    new String[]{"Select gender", "Male", "Female", "Other"}
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }

        genderSpinner.setAdapter(adapter);
        genderSpinner.setSelection(0, false);
    }

    // ---------------- LOAD LOCAL DATA ----------------
    private void loadLocalData() {

        String name = sharedPreferences.getString(KEY_USER_ID, "");
        if (name.isEmpty()) {
            name = sharedPreferences.getString(KEY_NAME, "");
        }
        editName.setText(name);

        editAge.setText(sharedPreferences.getString(KEY_AGE, ""));

        String gender = sharedPreferences.getString(KEY_GENDER, "");
        if (gender != null && !gender.isEmpty()) {
            String normalized = gender.substring(0, 1).toUpperCase() + gender.substring(1).toLowerCase();
            ArrayAdapter adapter = (ArrayAdapter) genderSpinner.getAdapter();
            int pos = adapter.getPosition(normalized);
            if (pos >= 0) {
                // Post selection to ensure the Spinner's internal view updates properly
                genderSpinner.post(() -> genderSpinner.setSelection(pos));
            }
        }
    }

    // ---------------- UPDATE PROFILE ----------------
    private void updateProfile() {

        String userId = editName.getText().toString().trim();
        String age = editAge.getText().toString().trim();
        String gender = genderSpinner.getSelectedItem() == null ? "" : genderSpinner.getSelectedItem().toString();
        String email = sharedPreferences.getString(KEY_EMAIL, "");

        if (userId.isEmpty()) {
            editName.setError("Enter your name");
            return;
        }

        if (age.isEmpty()) {
            editAge.setError("Enter your age");
            return;
        }

        if (genderSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show();
            return;
        }

        if (email.isEmpty()) {
            Toast.makeText(this,
                    "Email missing. Please login again.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        updateBtn.setEnabled(false);

        // -------- BACKEND PAYLOAD --------
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("user_id", userId);
        body.put("age", age);
        body.put("gender", gender);

        ApiService api = ApiClient.getClient().create(ApiService.class);

        api.updateUserProfile(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                updateBtn.setEnabled(true);

                if (!response.isSuccessful()) {
                    Toast.makeText(UpdateInfoActivity.this,
                            "Server update failed",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // SAVE LOCALLY ONLY AFTER SERVER SUCCESS
                sharedPreferences.edit()
                        .putString(KEY_USER_ID, userId)
                        .putString(KEY_NAME, userId)
                        .putString(KEY_AGE, age)
                        .putString(KEY_GENDER, gender)
                        .apply();

                Toast.makeText(UpdateInfoActivity.this,
                        "Account updated successfully",
                        Toast.LENGTH_SHORT).show();

                Intent i = new Intent(UpdateInfoActivity.this, ProfileActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
                finish();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                updateBtn.setEnabled(true);
                Toast.makeText(UpdateInfoActivity.this,
                        "Network error. Try again.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
