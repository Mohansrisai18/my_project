package com.example.cognisync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class UpdateInfoActivity extends AppCompatActivity {

    private EditText editName, editAge, editOldPassword, editNewPassword, editConfirmPassword;
    private Spinner genderSpinner;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String NAME_KEY = "userName";
    private static final String AGE_KEY = "userAge";
    private static final String GENDER_KEY = "userGender";
    private static final String PASSWORD_KEY = "userPassword";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide Action Bar
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        setContentView(R.layout.activity_account_settings);

        // Light Statusbar
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Views
        editName = findViewById(R.id.editName);
        editAge = findViewById(R.id.editAge);
        genderSpinner = findViewById(R.id.genderSpinner);
        editOldPassword = findViewById(R.id.editOldPassword);
        editNewPassword = findViewById(R.id.editNewPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);

        findViewById(R.id.updateInfoButton).setOnClickListener(v -> saveInfo());

        setupGenderSpinner();
        loadData();
    }

    private void setupGenderSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_selected_item,
                new String[]{"Male", "Female", "Other"}
        );
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);
    }

    private void loadData() {
        editName.setText(sharedPreferences.getString(NAME_KEY, ""));
        editAge.setText(sharedPreferences.getString(AGE_KEY, ""));

        String savedGender = sharedPreferences.getString(GENDER_KEY, "");

        if (!savedGender.isEmpty()) {
            savedGender = savedGender.substring(0,1).toUpperCase() + savedGender.substring(1);
            ArrayAdapter adapter = (ArrayAdapter) genderSpinner.getAdapter();
            int pos = adapter.getPosition(savedGender);
            if (pos >= 0) genderSpinner.setSelection(pos);
        }
    }

    private void saveInfo() {

        String name = editName.getText().toString().trim();
        String age = editAge.getText().toString().trim();
        String gender = genderSpinner.getSelectedItem().toString();

        String oldPass = editOldPassword.getText().toString().trim();
        String newPass = editNewPassword.getText().toString().trim();
        String confirmPass = editConfirmPassword.getText().toString().trim();

        // Validation
        if (name.isEmpty()) {
            editName.setError("Enter your name");
            return;
        }

        if (age.isEmpty()) {
            editAge.setError("Enter your age");
            return;
        }

        // Handle password update
        String storedPass = sharedPreferences.getString(PASSWORD_KEY, "");

        if (!oldPass.isEmpty() || !newPass.isEmpty() || !confirmPass.isEmpty()) {

            if (!oldPass.equals(storedPass)) {
                editOldPassword.setError("Incorrect password");
                return;
            }

            if (newPass.length() < 4) {
                editNewPassword.setError("Min 4 characters");
                return;
            }

            if (!newPass.equals(confirmPass)) {
                editConfirmPassword.setError("Passwords do not match");
                return;
            }

            sharedPreferences.edit().putString(PASSWORD_KEY, newPass).apply();
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(NAME_KEY, name);
        editor.putString(AGE_KEY, age);
        editor.putString(GENDER_KEY, gender);
        editor.apply();

        Toast.makeText(this, "Account Updated", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, ProfileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
