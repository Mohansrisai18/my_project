package com.example.cognisync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);   // you will create this XML

        new Handler().postDelayed(() -> checkLoginStatus(), 1200);
    }

    private void checkLoginStatus() {

        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String email = sp.getString("email", null);

        if (email != null && !email.isEmpty()) {
            // ✅ User already logged in → go Home
            Intent i = new Intent(SplashScreenActivity.this, HomeActivity.class);
            startActivity(i);
        } else {
            // ❌ No login → go Login screen
            Intent i = new Intent(SplashScreenActivity.this, LoginActivity.class);
            startActivity(i);
        }

        finish();
    }
}
