package com.example.cognisync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private ImageButton backButton;
    private Button signOutButton;
    private LinearLayout feedbackOption, languageOption, accountSettingsOption, resetProgressOption;
    private TextView userName, userInfo, sessionCount, totalMinutes, selectedLanguage;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String LANGUAGE_KEY = "selectedLanguage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize views
        backButton = findViewById(R.id.backButton);
        signOutButton = findViewById(R.id.signOutButton);
        feedbackOption = findViewById(R.id.feedbackOption);
        languageOption = findViewById(R.id.languageOption);
        accountSettingsOption = findViewById(R.id.accountSettingsOption);
        resetProgressOption = findViewById(R.id.resetProgressOption);
        userName = findViewById(R.id.userName);
        userInfo = findViewById(R.id.userInfo);
        sessionCount = findViewById(R.id.sessionCount);
        totalMinutes = findViewById(R.id.totalMinutes);
        selectedLanguage = findViewById(R.id.selectedLanguage);

        // SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Example dynamic info
        userName.setText("Mohan");
        userInfo.setText("Age: 20, Male");
        sessionCount.setText("4");
        totalMinutes.setText("67 Minutes");

        // Load selected language
        String language = sharedPreferences.getString(LANGUAGE_KEY, "English");
        selectedLanguage.setText(language);

        // Back button
        backButton.setOnClickListener(v -> onBackPressed());

        // Feedback option
        feedbackOption.setOnClickListener(v ->
                Toast.makeText(this, "Feedback clicked", Toast.LENGTH_SHORT).show()
        );

        // Language option → navigate to LanguageActivity
        languageOption.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, LanguageActivity.class);
            startActivity(intent);
        });

        // Account settings
        accountSettingsOption.setOnClickListener(v ->
                Toast.makeText(this, "Account settings clicked", Toast.LENGTH_SHORT).show()
        );

        // Reset Progress
        resetProgressOption.setOnClickListener(v ->
                Toast.makeText(this, "Progress reset", Toast.LENGTH_SHORT).show()
        );

        // Sign Out button
        signOutButton.setOnClickListener(v -> {
            Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update selected language when returning from LanguageActivity
        String language = sharedPreferences.getString(LANGUAGE_KEY, "English");
        selectedLanguage.setText(language);
    }
}
