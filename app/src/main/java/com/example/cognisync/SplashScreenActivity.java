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
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(this::checkLoginStatus, 1200);
    }

    private void checkLoginStatus() {

        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        boolean isLoggedIn = sp.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            // ✅ Old user – go to Home
            startActivity(new Intent(this, HomeActivity.class));

        } else {
            // ❌ First time user – go to Login
            startActivity(new Intent(this, LoginActivity.class));
        }

        finish();
    }
}
