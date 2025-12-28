package com.example.cognisync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

    // SharedPreferences keys
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_USER_ID = "user_id";   // backend field
    private static final String KEY_NAME = "userName";     // legacy/local
    private static final String KEY_AGE = "age";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_EMAIL = "email";

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_account_settings);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            getWindow().getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        editName = findViewById(R.id.editName);
        editAge = findViewById(R.id.editAge);
        genderSpinner = findViewById(R.id.genderSpinner);
        updateBtn = findViewById(R.id.updateInfoButton);

        setupGenderSpinner();
        loadLocalData();

        updateBtn.setOnClickListener(v -> updateProfile());
    }

    // ---------------- GENDER SPINNER ----------------
    private void setupGenderSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_selected_item,
                new String[]{"Male", "Female", "Other"}
        );
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);
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
        if (!gender.isEmpty()) {
            String normalized =
                    gender.substring(0, 1).toUpperCase() + gender.substring(1);
            ArrayAdapter adapter = (ArrayAdapter) genderSpinner.getAdapter();
            int pos = adapter.getPosition(normalized);
            if (pos >= 0) genderSpinner.setSelection(pos);
        }
    }

    // ---------------- BACKEND-FIRST UPDATE ----------------
    private void updateProfile() {

        String userId = editName.getText().toString().trim();
        String age = editAge.getText().toString().trim();
        String gender = genderSpinner.getSelectedItem().toString();
        String email = sharedPreferences.getString(KEY_EMAIL, "");

        if (userId.isEmpty()) {
            editName.setError("Enter your name");
            return;
        }

        if (age.isEmpty()) {
            editAge.setError("Enter your age");
            return;
        }

        if (email.isEmpty()) {
            Toast.makeText(this,
                    "Email missing. Please login again.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        updateBtn.setEnabled(false);

        // ✅ PAYLOAD MUST MATCH DJANGO MODEL
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("user_id", userId);   // 🔥 FIXED
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

                // ✅ SAVE LOCALLY ONLY AFTER BACKEND SUCCESS
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(KEY_USER_ID, userId);
                editor.putString(KEY_NAME, userId);
                editor.putString(KEY_AGE, age);
                editor.putString(KEY_GENDER, gender);
                editor.apply();

                Toast.makeText(UpdateInfoActivity.this,
                        "Account updated successfully",
                        Toast.LENGTH_SHORT).show();

                Intent intent =
                        new Intent(UpdateInfoActivity.this, ProfileActivity.class);
                intent.setFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_SINGLE_TOP
                );
                startActivity(intent);
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
