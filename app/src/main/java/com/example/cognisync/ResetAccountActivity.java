package com.example.cognisync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ResetAccountActivity extends AppCompatActivity {

    private Button resetButton;
    private static final String PREFS_NAME = "UserPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_account);

        resetButton = findViewById(R.id.resetButton);

        resetButton.setOnClickListener(v -> {
            // Clear all SharedPreferences data
            SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            Toast.makeText(ResetAccountActivity.this, "Account has been reset.", Toast.LENGTH_SHORT).show();

            // Navigate to LoginActivity
            Intent intent = new Intent(ResetAccountActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
