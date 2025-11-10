package com.example.cognisync;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LanguageActivity extends AppCompatActivity {

    private RadioGroup languageOptions;
    private RadioButton englishOption, hindiOption, tamilOption;
    private Button saveLanguageButton;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String LANGUAGE_KEY = "selectedLanguage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);

        // Initialize views
        languageOptions = findViewById(R.id.languageOptions);
        englishOption = findViewById(R.id.englishOption);
        hindiOption = findViewById(R.id.hindiOption);
        tamilOption = findViewById(R.id.tamilOption);
        saveLanguageButton = findViewById(R.id.saveLanguageButton);

        // SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Load saved language
        String savedLanguage = sharedPreferences.getString(LANGUAGE_KEY, "English");
        if (savedLanguage.equals("English")) {
            englishOption.setChecked(true);
        } else if (savedLanguage.equals("Hindi")) {
            hindiOption.setChecked(true);
        } else if (savedLanguage.equals("Tamil")) {
            tamilOption.setChecked(true);
        }

        // Save button
        saveLanguageButton.setOnClickListener(v -> {
            int selectedId = languageOptions.getCheckedRadioButtonId();
            String selectedLanguage;

            if (selectedId == R.id.englishOption) {
                selectedLanguage = "English";
            } else if (selectedId == R.id.hindiOption) {
                selectedLanguage = "Hindi";
            } else if (selectedId == R.id.tamilOption) {
                selectedLanguage = "Tamil";
            } else {
                Toast.makeText(this, "Please select a language", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save language
            sharedPreferences.edit().putString(LANGUAGE_KEY, selectedLanguage).apply();
            Toast.makeText(this, "Language set to: " + selectedLanguage, Toast.LENGTH_SHORT).show();
            finish(); // go back to ProfileActivity
        });
    }
}
