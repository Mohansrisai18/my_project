package com.example.cognisync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class UpdateInfoActivity extends AppCompatActivity {

    private EditText editName, editAge;
    private Spinner genderSpinner;
    private Button updateInfoButton;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String NAME_KEY = "userName";
    private static final String AGE_KEY = "userAge";
    private static final String GENDER_KEY = "userGender";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_account_settings);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // Initialize views
        editName = findViewById(R.id.editName);
        editAge = findViewById(R.id.editAge);
        genderSpinner = findViewById(R.id.genderSpinner);
        updateInfoButton = findViewById(R.id.updateInfoButton);

        // SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Load existing info if available
        editName.setText(sharedPreferences.getString(NAME_KEY, ""));
        editAge.setText(sharedPreferences.getString(AGE_KEY, ""));
        String savedGender = sharedPreferences.getString(GENDER_KEY, "");
        if (!savedGender.isEmpty()) {
            if (genderSpinner.getAdapter() instanceof ArrayAdapter) {
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) genderSpinner.getAdapter();
                int spinnerPosition = adapter.getPosition(savedGender);
                if (spinnerPosition >= 0) {
                    genderSpinner.setSelection(spinnerPosition);
                }
            }
        }

        // Update info button click
        updateInfoButton.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String age = editAge.getText().toString().trim();
            String gender = genderSpinner.getSelectedItem().toString();

            // Validate inputs
            if (TextUtils.isEmpty(name)) {
                editName.setError("Please enter your name");
                return;
            }
            if (TextUtils.isEmpty(age)) {
                editAge.setError("Please enter your age");
                return;
            }

            // Save to SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(NAME_KEY, name);
            editor.putString(AGE_KEY, age);
            editor.putString(GENDER_KEY, gender);
            editor.apply();

            Toast.makeText(UpdateInfoActivity.this, "Info updated successfully", Toast.LENGTH_SHORT).show();

            // Navigate back to ProfileActivity
            Intent intent = new Intent(UpdateInfoActivity.this, ProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
