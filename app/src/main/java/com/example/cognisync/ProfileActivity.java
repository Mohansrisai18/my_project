package com.example.cognisync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
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
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

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

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Load user info
        String name = sharedPreferences.getString("username", "User");
        String age = sharedPreferences.getString("age", "--");
        String gender = sharedPreferences.getString("gender", "--");
        userName.setText(name);
        userInfo.setText("Age: " + age + ", " + gender);

        // Optional static placeholders
        sessionCount.setText("4");
        totalMinutes.setText("67 Minutes");

        // Load selected language
        String language = sharedPreferences.getString(LANGUAGE_KEY, "English");
        selectedLanguage.setText(language);

        // Back button
        backButton.setOnClickListener(v -> onBackPressed());

        // Feedback
        feedbackOption.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, FeedbackActivity.class));
        });

        // Language
        languageOption.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, LanguageActivity.class));
        });

        // Account Settings
        accountSettingsOption.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, UpdateInfoActivity.class));
        });

        // Reset Progress
        resetProgressOption.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, ResetAccountActivity.class));
        });

        // Sign Out
        signOutButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isLoggedIn", false);  // 🔥 IMPORTANT (logout)
            editor.clear();
            editor.apply();

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
        String language = sharedPreferences.getString(LANGUAGE_KEY, "English");
        selectedLanguage.setText(language);
    }
}
